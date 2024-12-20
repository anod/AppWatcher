package com.anod.appwatcher.sync

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.os.RemoteException
import android.provider.BaseColumns
import android.text.format.DateUtils
import androidx.core.content.contentValuesOf
import androidx.work.Data
import com.anod.appwatcher.accounts.AuthAccountInitializer
import com.anod.appwatcher.accounts.AuthTokenStartIntent
import com.anod.appwatcher.backup.gdrive.GDriveSilentSignIn
import com.anod.appwatcher.backup.gdrive.GDriveSync
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.ChangelogTable
import com.anod.appwatcher.database.Cleanup
import com.anod.appwatcher.database.DbContentProvider
import com.anod.appwatcher.database.SchedulesTable
import com.anod.appwatcher.database.contentValues
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.AppChange
import com.anod.appwatcher.database.entities.AppListItem
import com.anod.appwatcher.database.entities.Schedule
import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.utils.compareLettersAndDigits
import com.anod.appwatcher.utils.date.UploadDateParserCache
import com.anod.appwatcher.utils.extractUploadDate
import finsky.api.BulkDocId
import finsky.api.DfeApi
import finsky.api.Document
import finsky.api.filterDocuments
import info.anodsplace.applog.AppLog
import info.anodsplace.framework.content.InstalledApps
import info.anodsplace.framework.net.NetworkConnectivity
import info.anodsplace.playstore.AppDetailsFilter
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.Koin
import org.koin.core.parameter.parametersOf

/**
 *  @author alex
 *  @date 6/3/2017
 */

class UpdateCheck(
    private val context: info.anodsplace.context.ApplicationContext,
    private val packageManager: PackageManager,
    private val notificationManager: info.anodsplace.notification.NotificationManager,
    private val database: AppsDatabase,
    private val preferences: Preferences,
    private val networkConnection: NetworkConnectivity,
    private val authAccount: AuthAccountInitializer,
    private val uploadDateParserCache: UploadDateParserCache,
    private val koin: Koin
) {

    class SyncResult(
        val success: Boolean,
        val updates: List<UpdatedApp>,
        val checked: Int,
        val unavailable: Int
    )

    companion object {
        private const val ONE_SEC_IN_MILLIS = 1000
        private const val BULK_SIZE = 20
        internal const val EXTRAS_MANUAL = "manual"

        const val SYNC_STOP = "com.anod.appwatcher.sync.start"
        const val SYNC_PROGRESS = "com.anod.appwatcher.sync.progress"
        const val EXTRA_UPDATES_COUNT = "extra_updates_count"
    }

    private val installedAppsProvider = InstalledApps.PackageManager(packageManager)

    suspend fun perform(extras: Data): Int = withContext(Dispatchers.Default) {
        val manualSync = extras.getBoolean(EXTRAS_MANUAL, false)
        AppLog.i("Perform ${if (manualSync) "manual" else "scheduled"} sync", "UpdateCheck")
        val schedule = Schedule(manualSync)

        val account = preferences.account
        if (account == null) {
            AppLog.w("No active account, skipping sync...", "UpdateCheck")
            SchedulesTable.Queries.save(schedule.finish(Schedule.STATUS_FAILED_NO_ACCOUNT), database)
            return@withContext -1
        }

        // Skip any check if sync requested from application
        if (!manualSync) {
            if (preferences.isWifiOnly && !networkConnection.isWifiEnabled) {
                AppLog.i("Wifi not enabled, skipping update check....", "UpdateCheck")
                SchedulesTable.Queries.save(schedule.finish(Schedule.STATUS_SKIPPED_NO_WIFI), database)
                return@withContext -1
            }
            val updateTime = preferences.lastUpdateTime
            if (updateTime != (-1).toLong() && System.currentTimeMillis() - updateTime < ONE_SEC_IN_MILLIS) {
                SchedulesTable.Queries.save(schedule.finish(Schedule.STATUS_SKIPPED_MIN_TIME), database)
                AppLog.i("Last update less than second, skipping...", "UpdateCheck")
                return@withContext -1
            }
        }

        AppLog.i("Perform synchronization", "UpdateCheck")

        val refreshed = refreshAuthToken()
        if (!refreshed) {
            SchedulesTable.Queries.save(schedule.finish(Schedule.STATUS_FAILED_NO_TOKEN), database)
            AppLog.e("Cannot receive access token")
            return@withContext -1
        }

        // Broadcast progress intent
        val startIntent = Intent(SYNC_PROGRESS).apply {
            `package` = context.actual.packageName
        }
        context.sendBroadcast(startIntent)

        val lastUpdatesViewed = preferences.isLastUpdatesViewed
        AppLog.d("Last update viewed: $lastUpdatesViewed")
        SchedulesTable.Queries.save(schedule, database)

        val syncResult = try {
            doSync(lastUpdatesViewed)
        } catch (e: Exception) {
            AppLog.e("Error during synchronization ${e.message}", e)
            SyncResult(false, listOf(), 0, 0)
        }

        if (syncResult.success) {
            SchedulesTable.Queries.save(schedule.finish(Schedule.STATUS_SUCCESS, syncResult.checked, syncResult.updates.size, syncResult.unavailable), database)
        } else {
            SchedulesTable.Queries.save(schedule.finish(Schedule.STATUS_FAILED), database)
        }

        val updatedApps: List<UpdatedApp> = syncResult.updates
        if (updatedApps.isNotEmpty() && updatedApps.first().uploadTime == 0.toLong()) {
            val uploadDate = updatedApps.first().uploadDate
            val locale = Locale.getDefault()
            AppLog.e("Cannot parse date '$uploadDate' for locale '$locale'")
        }

        val now = System.currentTimeMillis()
        preferences.lastUpdateTime = now

        if (!manualSync
            && updatedApps.isNotEmpty()
            && (updatedApps.firstOrNull { it.isNewUpdate } != null)
            && lastUpdatesViewed) {
            preferences.isLastUpdatesViewed = false
        }

        notifyIfNeeded(manualSync, updatedApps, schedule)

        if (!manualSync) {
            if (preferences.isDriveSyncEnabled) {
                AppLog.d("DriveSyncEnabled = true")
                performGDriveSync(preferences, now)
            } else {
                AppLog.d("DriveSyncEnabled = false, skipping...")
            }

            Cleanup(preferences, database).performIfNeeded(now)
        }

        AppLog.d("Finish::perform()")
        return@withContext updatedApps.size
    }

    @Throws(RemoteException::class)
    private suspend fun doSync(lastUpdatesViewed: Boolean): SyncResult {
        val sortId = preferences.sortIndex
        val apps = AppListTable.Queries.loadAppList(false, sortId, database.apps())
        if (apps.isEmpty) {
            apps.close()
            AppLog.i("Sync finished: no apps", "UpdateCheck")
            return SyncResult(true, listOf(), 0, 0)
        }

        val updatedApps = mutableListOf<UpdatedApp>()
        var unavailable = 0
        apps.chunked(BULK_SIZE) { list ->
            list.associateBy { it.app.packageName }
        }.forEach { localApps ->
            val docIds = localApps.map { BulkDocId(it.key, it.value.app.versionNumber) }
            val dfeApi = koin.get<DfeApi>()
            AppLog.d("Sending chunk... $docIds")
            val documents = try {
                dfeApi.details(docIds, includeDetails = true).filterDocuments(AppDetailsFilter.hasAppDetails)
            } catch (e: Exception) {
                AppLog.e("Fetching of bulk updates failed ${e.message ?: ""}", "UpdateCheck")
                emptyList()
            }
            unavailable += (docIds.size - documents.size)
            AppLog.i("Sent ${docIds.size}, received ${documents.size}", "UpdateCheck")
            updateApps(documents, localApps, updatedApps, lastUpdatesViewed, context.contentResolver, database)
        }

        apps.close()
        AppLog.i("Sync finished for ${apps.count} apps", "UpdateCheck")
        return SyncResult(true, updatedApps, apps.count, unavailable)
    }

    private suspend fun updateApps(
        documents: List<Document>,
        localApps: Map<String, AppListItem>,
        updatedApps: MutableList<UpdatedApp>,
        lastUpdatesViewed: Boolean,
        contentResolver: ContentResolver,
        db: AppsDatabase
    ) {
        val fetched = mutableMapOf<String, Boolean>()
        val batch = mutableListOf<ContentValues>()
        val changelog = mutableListOf<ContentValues>()
        for (marketApp in documents) {
            val docId = marketApp.docId
            localApps[docId]?.let { localItem ->
                fetched[docId] = true
                val (values, updatedApp) = updateApp(marketApp, localItem, lastUpdatesViewed)
                if (values.size() > 0) {
                    batch.add(values)
                }
                val isNewVersion = marketApp.appDetails.versionCode > localItem.app.versionNumber
                val recentChanges = marketApp.appDetails.recentChangesHtml?.trim() ?: ""
                val noNewDetails = if (isNewVersion) {
                    recentChanges.compareLettersAndDigits(localItem.changeDetails)
                } else {
                    localItem.noNewDetails
                }
                changelog.add(AppChange(
                    docId,
                    marketApp.appDetails.versionCode,
                    marketApp.appDetails.versionString,
                    recentChanges,
                    marketApp.appDetails.uploadDate,
                    noNewDetails).contentValues)
                if (updatedApp != null) {
                    updatedApps.add(updatedApp.copy(noNewDetails = noNewDetails))
                }
            }
        }

        if (batch.isNotEmpty()) {
            db.applyBatchUpdates(contentResolver, batch) {
                val rowId = it.getAsString(BaseColumns._ID)
                DbContentProvider.appsUri.buildUpon().appendPath(rowId).build()
            }
        }

        if (changelog.isNotEmpty()) {
            db.applyBatchInsert(contentResolver, changelog) {
                DbContentProvider.changelogUri
                    .buildUpon()
                    .appendPath("apps")
                    .appendPath(it.getAsString(ChangelogTable.Columns.APP_ID))
                    .appendPath("v")
                    .appendPath(it.getAsString(ChangelogTable.Columns.VERSION_CODE))
                    .build()
            }
        }

        // Reset not fetched app statuses
        if (lastUpdatesViewed && fetched.size < localApps.size) {
            val statusBatch = mutableListOf<ContentValues>()
            localApps.values.forEach {
                val app = it.app
                if (fetched[app.appId] == null) {
                    if (app.status == App.STATUS_UPDATED) {
                        AppLog.d("Set not fetched app as viewed")
                        statusBatch.add(contentValuesOf(
                            BaseColumns._ID to app.rowId,
                            AppListTable.Columns.STATUS to App.STATUS_NORMAL
                        ))
                    }
                }
            }
            if (statusBatch.isNotEmpty()) {
                db.applyBatchUpdates(contentResolver, statusBatch) {
                    val rowId = it.getAsString(BaseColumns._ID)
                    DbContentProvider.appsUri.buildUpon().appendPath(rowId).build()
                }
            }
        }
    }

    private fun updateApp(marketDoc: Document, localItem: AppListItem, lastUpdatesViewed: Boolean): Pair<ContentValues, UpdatedApp?> {
        val appDetails = marketDoc.appDetails
        val localApp = localItem.app

        if (appDetails.versionCode > localApp.versionNumber) {
            AppLog.d("New version found [" + appDetails.versionCode + "]")
            val uploadTime = marketDoc.extractUploadDate(uploadDateParserCache)
            val newApp = App(
                rowId = localApp.rowId,
                status = App.STATUS_UPDATED,
                doc = marketDoc,
                uploadTime = uploadTime,
                syncTime = System.currentTimeMillis(),
            )
            val installedInfo = installedAppsProvider.packageInfo(appDetails.packageName)
            val recentChanges = appDetails.recentChangesHtml ?: ""
            return Pair(newApp.contentValues, UpdatedApp(newApp, recentChanges, installedInfo.versionCode, true))
        }

        val values = ContentValues()
        var updatedApp: UpdatedApp? = null
        // Mark updated app as normal
        if (localApp.status == App.STATUS_UPDATED && lastUpdatesViewed) {
            AppLog.d("Set ${localApp.appId} update as viewed")
            values.put(AppListTable.Columns.STATUS, App.STATUS_NORMAL)
        } else if (localApp.status == App.STATUS_UPDATED) {
            // Application was previously updated
            val installedInfo = installedAppsProvider.packageInfo(appDetails.packageName)
            val recentChanges = appDetails.recentChangesHtml ?: ""
            updatedApp = UpdatedApp(localApp, recentChanges, installedInfo.versionCode, false)
        }
        // Refresh app info with latest fetched
        updateLocalApp(marketDoc, localApp, values)
        return Pair(values, updatedApp)
    }

    private suspend fun notifyIfNeeded(manualSync: Boolean, updatedApps: List<UpdatedApp>, schedule: Schedule) {
        val sn = SyncNotification(context, notificationManager)
        if (manualSync) {
            if (updatedApps.isEmpty()) {
                AppLog.i("No new updates", "UpdateCheck")
            } else {
                AppLog.i("Updates: [${updatedApps.joinToString(",") { "${it.title} (${it.versionNumber})" }}]", "UpdateCheck")
            }
            sn.cancel()
        } else if (updatedApps.isNotEmpty()) {
            val filter = SyncNotification.Filter(preferences)
            val filteredApps = if (filter.hasFilters) {
                filter.apply(updatedApps)
            } else {
                updatedApps
            }
            AppLog.i("Notifying about: [${filteredApps.joinToString(",") { "${it.title} (${it.versionNumber})" }}]", "UpdateCheck")
            database.schedules().updateNotified(schedule.id, filteredApps.size)
            if (filteredApps.isNotEmpty()) {
                sn.show(filteredApps)
            }
        } else {
            AppLog.i("No new updates", "UpdateCheck")
        }
    }

    private suspend fun performGDriveSync(pref: Preferences, now: Long) {
        val driveSyncTime = pref.lastDriveSyncTime
        if (driveSyncTime == (-1).toLong() || now > DateUtils.DAY_IN_MILLIS + driveSyncTime) {
            AppLog.d("DriveSync perform sync")
            val signIn = GDriveSilentSignIn(context)

            try {
                AppLog.i("Perform Google Drive sync", "UpdateCheck")
                val googleAccount = signIn.signInLocked()
                val worker = koin.get<GDriveSync> { parametersOf(googleAccount) }
                worker.doSync()
                pref.lastDriveSyncTime = System.currentTimeMillis()
            } catch (e: GDriveSync.SyncError) {
                if (e.error != null) {
                    AppLog.e("Perform Google Drive sync exception: Requires interactive sign in: '${e.message}'", "UpdateCheck")
                } else {
                    AppLog.e("Perform Google Drive sync exception: ${e.message}", "UpdateCheck", e)
                }
            }
        } else {
            AppLog.d("Google Drive sync is fresh")
        }
    }

    private suspend fun refreshAuthToken(): Boolean {
        return try {
            authAccount.refresh()
            true
        } catch (e: AuthTokenStartIntent) {
            AppLog.e("AuthToken: require interactive sing in")
            false
        } catch (e: Throwable) {
            AppLog.e("AuthTokenBlocking request exception: " + e.message, e)
            false
        }
    }

    private fun updateLocalApp(marketApp: Document, localApp: App, values: ContentValues) {
        val uploadTime = marketApp.extractUploadDate(uploadDateParserCache)
        values.put(BaseColumns._ID, localApp.rowId)
        values.put(AppListTable.Columns.UPLOAD_TIMESTAMP, uploadTime)
        values.put(AppListTable.Columns.UPLOAD_DATE, marketApp.appDetails.uploadDate)
        values.put(AppListTable.Columns.VERSION_NAME, marketApp.appDetails.versionString)
        values.put(AppListTable.Columns.VERSION_NUMBER, marketApp.appDetails.versionCode)

        if (marketApp.appDetails.appType != localApp.appType) {
            values.put(AppListTable.Columns.APP_TYPE, marketApp.appDetails.appType)
        }

        val offer = marketApp.offer
        if (offer.currencyCode != localApp.price.cur) {
            values.put(AppListTable.Columns.PRICE_CURRENCY, offer.currencyCode)
        }
        if (offer.formattedAmount != localApp.price.text) {
            values.put(AppListTable.Columns.PRICE_TEXT, offer.formattedAmount)
        }
        if (localApp.price.micros != offer.micros.toInt()) {
            values.put(AppListTable.Columns.PRICE_MICROS, offer.micros)
        }
        if (!marketApp.iconUrl.isNullOrEmpty()) {
            values.put(AppListTable.Columns.ICON_URL, marketApp.iconUrl)
        }
    }
}