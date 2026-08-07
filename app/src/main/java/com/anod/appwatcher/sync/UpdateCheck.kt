package com.anod.appwatcher.sync

import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.os.RemoteException
import android.provider.BaseColumns
import android.text.format.DateUtils
import androidx.work.Data
import com.anod.appwatcher.accounts.AuthAccountInitializer
import com.anod.appwatcher.accounts.AuthTokenStartIntent
import com.anod.appwatcher.accounts.AuthTokenUnavailableException
import com.anod.appwatcher.backup.gdrive.GDriveSilentSignIn
import com.anod.appwatcher.backup.gdrive.GDriveSync
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.AppSyncUpdate
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.Cleanup
import com.anod.appwatcher.database.SchedulesTable
import com.anod.appwatcher.database.contentValues
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.AppChange
import com.anod.appwatcher.database.entities.AppListItem
import com.anod.appwatcher.database.entities.Schedule
import com.anod.appwatcher.database.entities.toApp
import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.utils.compareLettersAndDigits
import com.anod.appwatcher.utils.date.UploadDateParserCache
import com.anod.appwatcher.utils.extractUploadDate
import finsky.api.BulkDocId
import finsky.api.DfeApi
import finsky.api.DfeServerError
import finsky.api.Document
import finsky.api.filterDocuments
import info.anodsplace.applog.AppLog
import info.anodsplace.framework.content.InstalledApps
import info.anodsplace.framework.net.NetworkConnectivity
import info.anodsplace.playstore.AppDetailsFilter
import java.io.IOException
import java.util.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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

    class SyncResult(val success: Boolean, val updates: List<UpdatedApp>, val checked: Int, val unavailable: Int)

    private data class PendingAppUpdate(
        val rowId: Long,
        val expectedAppId: String,
        val expectedPackageName: String,
        val values: ContentValues,
        val changelog: ContentValues,
        val updatedApp: UpdatedApp?
    )

    companion object {
        private const val ONE_SEC_IN_MILLIS = 1000
        private const val BULK_SIZE = 20
        private const val MAX_CHUNK_ATTEMPTS = 3
        private const val CHUNK_RETRY_DELAY_MILLIS = 250L
        internal const val EXTRAS_MANUAL = "manual"
        private val syncMutex = Mutex()

        const val SYNC_STOP = "com.anod.appwatcher.sync.start"
        const val SYNC_PROGRESS = "com.anod.appwatcher.sync.progress"
        const val EXTRA_UPDATES_COUNT = "extra_updates_count"
    }

    private val installedAppsProvider = InstalledApps.PackageManager(packageManager)

    suspend fun perform(extras: Data): Int = syncMutex.withLock {
        performSerialized(extras)
    }

    private suspend fun performSerialized(extras: Data): Int = withContext(Dispatchers.Default) {
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

        val lastUpdatesViewed = preferences.isLastUpdatesViewed
        val syncResult = try {
            authAccount.withRefreshedAccount {
                val startIntent = Intent(SYNC_PROGRESS).apply {
                    `package` = context.actual.packageName
                }
                context.sendBroadcast(startIntent)

                AppLog.d("Last update viewed: $lastUpdatesViewed")
                SchedulesTable.Queries.save(schedule, database)

                try {
                    doSync(lastUpdatesViewed)
                } catch (e: AuthTokenStartIntent) {
                    throw e
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    AppLog.e("Error during synchronization ${e.message}", e)
                    SyncResult(false, listOf(), 0, 0)
                }
            }
        } catch (e: AuthTokenStartIntent) {
            AppLog.e("AuthToken: require interactive sing in")
            return@withContext finishFailedSync(schedule, Schedule.STATUS_FAILED_NO_TOKEN, manualSync)
        } catch (e: AuthTokenUnavailableException) {
            AppLog.e("Cannot receive access token", e)
            return@withContext finishFailedSync(schedule, Schedule.STATUS_FAILED_NO_TOKEN, manualSync)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLog.e("Play Store session refresh or sync setup failed: ${e.message}", e)
            return@withContext finishFailedSync(schedule, Schedule.STATUS_FAILED, manualSync)
        }

        if (syncResult.success) {
            SchedulesTable.Queries.save(schedule.finish(Schedule.STATUS_SUCCESS, syncResult.checked, syncResult.updates.size, syncResult.unavailable), database)
        } else {
            return@withContext finishFailedSync(schedule, Schedule.STATUS_FAILED, manualSync)
        }

        val updatedApps: List<UpdatedApp> = syncResult.updates
        if (updatedApps.isNotEmpty() && updatedApps.first().uploadTime == 0.toLong()) {
            val uploadDate = updatedApps.first().uploadDate
            val locale = Locale.getDefault()
            AppLog.e("Cannot parse date '$uploadDate' for locale '$locale'")
        }

        val now = System.currentTimeMillis()
        preferences.lastUpdateTime = now

        if (!manualSync &&
            updatedApps.isNotEmpty() &&
            (updatedApps.firstOrNull { it.isNewUpdate } != null) &&
            lastUpdatesViewed) {
            preferences.isLastUpdatesViewed = false
        }

        notifyIfNeeded(manualSync, updatedApps, schedule)

        performMaintenance(manualSync, now)

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

        val localAppChunks = try {
            apps.chunked(BULK_SIZE) { list ->
                list.associateBy { it.app.packageName }
            }
        } finally {
            apps.close()
        }
        var unavailable = 0
        val fetchedChunks = fetchAllChunks(
            chunks = localAppChunks,
            maxAttempts = MAX_CHUNK_ATTEMPTS,
            initialRetryDelayMillis = CHUNK_RETRY_DELAY_MILLIS
        ) { localApps ->
            val docIds = localApps.map { BulkDocId(it.key, it.value.app.versionNumber) }
            val dfeApi = koin.get<DfeApi>()
            AppLog.d("Sending chunk... $docIds")
            val documents = dfeApi.details(docIds, includeDetails = true)
                .filterDocuments(AppDetailsFilter.hasAppDetails)
            unavailable += (docIds.size - documents.size)
            AppLog.i("Sent ${docIds.size}, received ${documents.size}", "UpdateCheck")
            documents
        }
        val pendingUpdates = fetchedChunks.flatMap { (localApps, documents) ->
            prepareAppUpdates(documents, localApps, lastUpdatesViewed)
        }
        val updatedApps = applyAppUpdates(pendingUpdates, database)

        val checked = localAppChunks.sumOf { it.size }
        AppLog.i("Sync finished for $checked apps", "UpdateCheck")
        return SyncResult(true, updatedApps, checked, unavailable)
    }

    private suspend fun finishFailedSync(
        schedule: Schedule,
        status: Int,
        manualSync: Boolean
    ): Int {
        SchedulesTable.Queries.save(schedule.finish(status), database)
        performMaintenance(manualSync, System.currentTimeMillis())
        return -1
    }

    private suspend fun performMaintenance(manualSync: Boolean, now: Long) {
        if (manualSync) {
            return
        }
        if (preferences.isDriveSyncEnabled) {
            AppLog.d("DriveSyncEnabled = true")
            performGDriveSync(preferences, now)
        } else {
            AppLog.d("DriveSyncEnabled = false, skipping...")
        }
        Cleanup(preferences, database).performIfNeeded(now)
    }

    private fun prepareAppUpdates(
        documents: List<Document>,
        localApps: Map<String, AppListItem>,
        lastUpdatesViewed: Boolean
    ): List<PendingAppUpdate> {
        val pendingUpdates = mutableListOf<PendingAppUpdate>()
        for (marketApp in documents) {
            val docId = marketApp.docId
            localApps[docId]?.let { localItem ->
                val (values, updatedApp) = updateApp(marketApp, localItem, lastUpdatesViewed)
                val isNewVersion = marketApp.appDetails.versionCode > localItem.app.versionNumber
                val recentChanges = marketApp.appDetails.recentChangesHtml?.trim() ?: ""
                val noNewDetails = if (isNewVersion) {
                    recentChanges.compareLettersAndDigits(localItem.changeDetails)
                } else {
                    localItem.noNewDetails
                }
                if (values.size() > 0) {
                    pendingUpdates.add(
                        PendingAppUpdate(
                            rowId = localItem.app.rowId.toLong(),
                            expectedAppId = localItem.app.appId,
                            expectedPackageName = localItem.app.packageName,
                            values = values,
                            changelog = AppChange(
                                docId,
                                marketApp.appDetails.versionCode,
                                marketApp.appDetails.versionString,
                                recentChanges,
                                marketApp.appDetails.uploadDate,
                                noNewDetails
                            ).contentValues,
                            updatedApp = updatedApp?.copy(noNewDetails = noNewDetails)
                        )
                    )
                }
            }
        }
        return pendingUpdates
    }

    private suspend fun applyAppUpdates(
        pendingUpdates: List<PendingAppUpdate>,
        db: AppsDatabase
    ): List<UpdatedApp> {
        if (pendingUpdates.isEmpty()) {
            return emptyList()
        }

        db.applyAppSyncUpdates(
            pendingUpdates.map { update ->
                AppSyncUpdate(
                    rowId = update.rowId,
                    expectedAppId = update.expectedAppId,
                    expectedPackageName = update.expectedPackageName,
                    values = update.values,
                    changelogValues = update.changelog
                )
            }
        )
        return pendingUpdates.mapNotNull { it.updatedApp }
    }

    private fun updateApp(marketDoc: Document, localItem: AppListItem, lastUpdatesViewed: Boolean): Pair<ContentValues, UpdatedApp?> {
        val appDetails = marketDoc.appDetails
        val localApp = localItem.app

        val values = ContentValues()
        if (reconcileVersionRollback(appDetails.versionCode, localApp, values)) {
            AppLog.w(
                "Play Store version rolled back for ${localApp.packageName}: " +
                    "${localApp.versionNumber} -> ${appDetails.versionCode}",
                "UpdateCheck"
            )
            updateLocalApp(marketDoc, localApp, values)
            return Pair(values, null)
        }

        if (appDetails.versionCode > localApp.versionNumber) {
            AppLog.d("New version found [" + appDetails.versionCode + "]")
            val uploadTime = marketDoc.extractUploadDate(uploadDateParserCache)
            val newApp = marketDoc.toApp(
                rowId = localApp.rowId,
                status = App.STATUS_UPDATED,
                uploadTime = uploadTime,
                syncTime = System.currentTimeMillis(),
            )
            val installedInfo = installedAppsProvider.packageInfo(appDetails.packageName)
            val recentChanges = appDetails.recentChangesHtml ?: ""
            return Pair(newApp.contentValues, UpdatedApp(newApp, recentChanges, installedInfo.versionCode, true))
        }

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

internal fun reconcileVersionRollback(marketVersionCode: Int, localApp: App, values: ContentValues): Boolean {
    if (marketVersionCode >= localApp.versionNumber) {
        return false
    }
    values.put(AppListTable.Columns.STATUS, App.STATUS_NORMAL)
    values.put(AppListTable.Columns.SYNC_TIMESTAMP, 0L)
    return true
}

internal suspend fun <T, R> fetchAllChunks(
    chunks: List<T>,
    maxAttempts: Int = 3,
    initialRetryDelayMillis: Long = 250L,
    fetch: suspend (T) -> R
): List<Pair<T, R>> {
    require(maxAttempts > 0)
    require(initialRetryDelayMillis >= 0)
    val fetched = ArrayList<Pair<T, R>>(chunks.size)
    for (chunk in chunks) {
        var attempt = 1
        while (true) {
            try {
                fetched.add(Pair(chunk, fetch(chunk)))
                break
            } catch (error: Exception) {
                if (attempt >= maxAttempts || !isTransientChunkFailure(error)) {
                    throw error
                }
                AppLog.w(
                    "Retrying Play Store chunk after attempt $attempt: ${error.message}",
                    "UpdateCheck"
                )
                delay(initialRetryDelayMillis * attempt)
                attempt++
            }
        }
    }
    return fetched
}

private fun isTransientChunkFailure(error: Exception): Boolean =
    error is IOException ||
        (error is DfeServerError &&
            (error.statusCode == 429 || error.statusCode?.let { it in 500..599 } == true))