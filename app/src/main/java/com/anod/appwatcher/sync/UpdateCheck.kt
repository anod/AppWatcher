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
import com.anod.appwatcher.accounts.PlaySessionCoordinator
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
import com.anod.appwatcher.database.entities.preserveCachedMetadata
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
    private val playSessionCoordinator: PlaySessionCoordinator,
    private val koin: Koin
) {

    class SyncResult(val success: Boolean, val updates: List<UpdatedApp>, val checked: Int, val unavailable: Int)

    private data class PendingAppUpdate(
        val rowId: Long,
        val expectedAppId: String,
        val expectedPackageName: String,
        val values: ContentValues,
        val changelog: ContentValues?,
        val updatedApp: UpdatedApp?
    )

    private data class AppUpdateResult(
        val values: ContentValues,
        val updatedApp: UpdatedApp?,
        val persistChangelog: Boolean
    )

    companion object {
        private const val ONE_SEC_IN_MILLIS = 1000
        private const val BULK_SIZE = 20
        internal const val MAX_CHUNK_ATTEMPTS = 3
        private const val CHUNK_RETRY_DELAY_MILLIS = 250L
        internal const val EXTRAS_MANUAL = "manual"

        const val SYNC_STOP = "com.anod.appwatcher.sync.start"
        const val SYNC_PROGRESS = "com.anod.appwatcher.sync.progress"
        const val EXTRA_UPDATES_COUNT = "extra_updates_count"
    }

    private val installedAppsProvider = InstalledApps.PackageManager(packageManager)

    suspend fun perform(extras: Data): Int = playSessionCoordinator.withSession {
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
            authAccount.refreshInSession()
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
        } catch (e: AuthTokenStartIntent) {
            AppLog.e("AuthToken: require interactive sign in")
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
            val documents = dfeApi.details(
                docIds,
                includeDetails = true,
                forUpdateCheck = true
            )
                .filterDocuments(AppDetailsFilter.hasAppDetails)
            unavailable += (docIds.size - documents.size)
            val availabilitySummary = documents
                .groupingBy { it.availabilityRestriction?.toString() ?: "absent" }
                .eachCount()
                .toSortedMap()
                .entries
                .joinToString { "${it.key}=${it.value}" }
            AppLog.i(
                "Sent ${docIds.size}, received ${documents.size}, availability {$availabilitySummary}",
                "UpdateCheck"
            )
            documents
        }
        // The update-check response (?au=1) only carries the availability signal: it routinely
        // omits recent changes, icon, version name, upload date and price. Fetch the full
        // documents for apps that moved to a new version so the stored data describes the new
        // release rather than the sparse placeholder.
        val releaseDetails = fetchReleaseDetails(selectReleaseDetailsApps(fetchedChunks))
        val pendingUpdates = fetchedChunks.flatMap { (localApps, documents) ->
            prepareAppUpdates(documents, localApps, lastUpdatesViewed, releaseDetails)
        }
        val updatedApps = applyAppUpdates(pendingUpdates, database)

        // Absent from the bulk response, so these never reach `updatedApps`; clearing them only
        // repairs rows the bulk data can no longer speak for.
        clearUnavailableUpdates(selectStaleUpdatedApps(fetchedChunks))

        val checked = localAppChunks.sumOf { it.size }
        AppLog.i("Sync finished for $checked apps", "UpdateCheck")
        return SyncResult(true, updatedApps, checked, unavailable)
    }

    /**
     * Full documents for apps whose release changed, from the plain (non update-check) bulk-details
     * endpoint. Unlike the `?au=1` variant this returns the complete document, so the changelog,
     * icon, version name, upload date and price describe the release actually being stored.
     * Requests are chunked at [BULK_SIZE] like the main update check, so a large watchlist costs a
     * bounded number of extra batched calls rather than one call per app.
     *
     * Best-effort by design: a failing chunk is skipped rather than failing the sync, and apps Play
     * does not return are simply absent from the result. Bulk entries carry no doc id or error, so
     * an omission cannot be attributed anyway - those apps keep their sparse document and cached
     * values.
     */
    private suspend fun fetchReleaseDetails(docIds: List<BulkDocId>): Map<String, Document> {
        if (docIds.isEmpty()) {
            return emptyMap()
        }
        val dfeApi = koin.get<DfeApi>()
        val details = mutableMapOf<String, Document>()
        for (chunk in docIds.chunked(BULK_SIZE)) {
            val documents = try {
                dfeApi.details(
                    chunk,
                    includeDetails = true,
                    forUpdateCheck = false
                ).filterDocuments(AppDetailsFilter.hasAppDetails)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                AppLog.w(
                    "Cannot fetch release details for ${chunk.size} apps: ${e.message}",
                    "UpdateCheck"
                )
                continue
            }
            for (document in documents) {
                details[document.docId] = document
            }
        }
        AppLog.i(
            "Fetched release details for ${details.size} of ${docIds.size} apps",
            "UpdateCheck"
        )
        return details
    }

    /**
     * Drop the stale updated flag for watched apps the bulk update-check response did not return.
     *
     * Play omits documents it no longer serves, and a bulk entry carries no doc id or error, so an
     * omission cannot be attributed to a delisting, a region restriction or a server hiccup. The
     * reason is not needed either: an app the update check can no longer speak for must not keep
     * advertising an update, and a later response that does return it will mark it again.
     */
    private suspend fun clearUnavailableUpdates(staleApps: List<AppListItem>) {
        if (staleApps.isEmpty()) {
            return
        }
        AppLog.i(
            "Clearing update status for ${staleApps.size} apps missing from the update check: " +
                staleApps.joinToString { it.app.packageName },
            "UpdateCheck"
        )
        for (item in staleApps) {
            database.apps().clearUpdateStatus(rowId = item.app.rowId)
        }
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
        lastUpdatesViewed: Boolean,
        releaseDetails: Map<String, Document>
    ): List<PendingAppUpdate> {
        val pendingUpdates = mutableListOf<PendingAppUpdate>()
        for (marketApp in documents) {
            val docId = marketApp.docId
            localApps[docId]?.let { localItem ->
                // Only the version the update check decided on may supply the release data;
                // otherwise Play advancing between the two requests would file one version's
                // details under another.
                val releaseApp = releaseDetails[docId]
                    ?.takeIf { it.appDetails.versionCode == marketApp.appDetails.versionCode }
                    ?: marketApp
                val result = updateApp(marketApp, releaseApp, localItem, lastUpdatesViewed)
                val isNewVersion = marketApp.appDetails.versionCode > localItem.app.versionNumber
                val recentChanges = resolveRecentChanges(
                    responseChanges = releaseApp.appDetails.recentChangesHtml,
                    cachedChanges = localItem.changeDetails,
                    isSameVersion = marketApp.appDetails.versionCode == localItem.app.versionNumber
                )
                val noNewDetails = if (isNewVersion) {
                    recentChanges.compareLettersAndDigits(localItem.changeDetails)
                } else {
                    localItem.noNewDetails
                }
                // A blank changelog is only worth storing for a version that has no row yet.
                // Writing it for an existing version would replace real notes with nothing, since
                // rows are keyed by (app_id, code) and inserted with CONFLICT_REPLACE.
                val persistChangelog = result.persistChangelog && (recentChanges.isNotBlank() || isNewVersion)
                if (result.values.size() > 0) {
                    pendingUpdates.add(
                        PendingAppUpdate(
                            rowId = localItem.app.rowId.toLong(),
                            expectedAppId = localItem.app.appId,
                            expectedPackageName = localItem.app.packageName,
                            values = result.values,
                            changelog = if (persistChangelog) {
                                AppChange(
                                    docId,
                                    releaseApp.appDetails.versionCode,
                                    releaseApp.appDetails.versionString,
                                    recentChanges,
                                    releaseApp.appDetails.uploadDate,
                                    noNewDetails
                                ).contentValues
                            } else {
                                null
                            },
                            updatedApp = result.updatedApp?.copy(
                                recentChanges = recentChanges,
                                noNewDetails = noNewDetails
                            )
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

        val updates = pendingUpdates.map { update ->
            AppSyncUpdate(
                rowId = update.rowId,
                expectedAppId = update.expectedAppId,
                expectedPackageName = update.expectedPackageName,
                values = update.values,
                changelogValues = update.changelog
            )
        }
        val appliedRowIds = AppListTable.Queries.applySyncUpdates(
            updates,
            db
        )
        val skippedRowIds = updates
            .map { it.rowId }
            .filterNot { it in appliedRowIds }
        if (skippedRowIds.isNotEmpty()) {
            AppLog.w(
                "Skipped app rows changed during synchronization: ${skippedRowIds.joinToString()}",
                "UpdateCheck"
            )
        }
        return pendingUpdates
            .filter { it.rowId in appliedRowIds }
            .mapNotNull { it.updatedApp }
    }

    /**
     * [marketDoc] is the update-check (`?au=1`) document and stays the authority on availability
     * and on which version this sync decided to store. [releaseDoc] carries the data written for
     * that version; it is the full document when one could be fetched and [marketDoc] otherwise.
     */
    private fun updateApp(
        marketDoc: Document,
        releaseDoc: Document,
        localItem: AppListItem,
        lastUpdatesViewed: Boolean
    ): AppUpdateResult {
        val appDetails = marketDoc.appDetails
        val localApp = localItem.app

        val values = ContentValues()
        val installedInfo = installedAppsProvider.packageInfo(appDetails.packageName)
        val unavailableAction = reconcileUnavailableUpdate(marketDoc, localApp, installedInfo, values)
        if (unavailableAction != UnavailableUpdateAction.NONE) {
            val reconciliation = if (unavailableAction == UnavailableUpdateAction.ROLL_BACK) {
                ", reconciled cached version ${localApp.versionNumber} " +
                    "to installed version ${installedInfo.versionCode}"
            } else {
                ""
            }
            AppLog.i(
                "Suppressing unavailable update ${appDetails.packageName} " +
                    "version ${appDetails.versionCode}, restriction " +
                    "${marketDoc.availabilityRestriction}$reconciliation",
                "UpdateCheck"
            )
            return AppUpdateResult(
                values = values,
                updatedApp = null,
                persistChangelog = false
            )
        }
        if (reconcileVersionRollback(appDetails.versionCode, localApp, values)) {
            AppLog.w(
                "Play Store version rolled back for ${localApp.packageName}: " +
                    "${localApp.versionNumber} -> ${appDetails.versionCode}",
                "UpdateCheck"
            )
            updateLocalApp(releaseDoc, localApp, values)
            return AppUpdateResult(
                values = values,
                updatedApp = null,
                persistChangelog = true
            )
        }

        if (appDetails.versionCode > localApp.versionNumber) {
            AppLog.i(
                "Play update candidate ${appDetails.packageName}: installed " +
                    "${installedInfo.versionCode}, cached ${localApp.versionNumber}, " +
                    "remote ${appDetails.versionCode}, restriction " +
                    (marketDoc.availabilityRestriction?.toString() ?: "absent"),
                "UpdateCheck"
            )
            val uploadTime = releaseDoc.extractUploadDate(uploadDateParserCache)
            val newApp = releaseDoc.toApp(
                rowId = localApp.rowId,
                status = App.STATUS_UPDATED,
                uploadTime = uploadTime,
                syncTime = System.currentTimeMillis(),
            ).preserveCachedMetadata(localApp)
            val recentChanges = releaseDoc.appDetails.recentChangesHtml ?: ""
            return AppUpdateResult(
                values = newApp.contentValues,
                updatedApp = UpdatedApp(newApp, recentChanges, installedInfo.versionCode, true),
                persistChangelog = true
            )
        }

        var updatedApp: UpdatedApp? = null
        // Mark updated app as normal
        if (localApp.status == App.STATUS_UPDATED && lastUpdatesViewed) {
            AppLog.d("Set ${localApp.appId} update as viewed")
            values.put(AppListTable.Columns.STATUS, App.STATUS_NORMAL)
        } else if (localApp.status == App.STATUS_UPDATED) {
            // Application was previously updated
            val recentChanges = releaseDoc.appDetails.recentChangesHtml ?: ""
            updatedApp = UpdatedApp(localApp, recentChanges, installedInfo.versionCode, false)
        }
        // Refresh app info with latest fetched
        updateLocalApp(releaseDoc, localApp, values)
        return AppUpdateResult(
            values = values,
            updatedApp = updatedApp,
            persistChangelog = true
        )
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

/**
 * Apps whose update-check document reports a new version, as doc ids for a follow-up bulk request.
 * Play's update-purpose response (`?au=1`) only signals availability: it routinely omits recent
 * changes, icon, version name, upload date and price, so it cannot describe the new release.
 *
 * Collected across all update-check chunks so the caller can re-split them into dense bulk
 * requests: the updated apps are usually scattered a few per chunk, and re-packing them costs one
 * request per [BULK_SIZE] updates rather than one per originating chunk.
 */
internal fun selectReleaseDetailsApps(
    fetchedChunks: List<Pair<Map<String, AppListItem>, List<Document>>>
): List<BulkDocId> = fetchedChunks
    .asSequence()
    .flatMap { (localApps, documents) -> documents.asSequence().map { localApps[it.docId] to it } }
    .filter { (localItem, document) ->
        localItem != null && document.appDetails.versionCode > localItem.app.versionNumber
    }
    .map { (_, document) -> BulkDocId(document.docId, document.appDetails.versionCode) }
    .distinctBy { it.packageName }
    .toList()

/**
 * Recent changes to persist for an app. [responseChanges] comes from the best document available
 * for this version - the full release document when one was fetched - and the cached changelog is
 * only a fallback so a blank response never wipes a previously stored description.
 *
 * The cached text is only reused when [isSameVersion], because `AppListItem.changeDetails` is
 * joined on the *cached* version code while the row written here is keyed by the *remote* version.
 * Reusing it across versions would file one version's notes under another - both for a new version
 * (whose notes are not written yet) and for a rollback (whose older row would be overwritten with
 * the newer version's notes).
 */
internal fun resolveRecentChanges(
    responseChanges: String?,
    cachedChanges: String?,
    isSameVersion: Boolean
): String {
    val fresh = responseChanges?.trim().orEmpty()
    if (fresh.isNotBlank()) {
        return fresh
    }
    return if (isSameVersion) cachedChanges?.trim().orEmpty() else ""
}

/**
 * Watched apps still flagged as [App.STATUS_UPDATED] locally that the bulk update-check response
 * did not return. Play omits documents it no longer serves, so these are the only rows whose stale
 * updated flag the bulk data can never clear on its own.
 */
internal fun selectStaleUpdatedApps(
    fetchedChunks: List<Pair<Map<String, AppListItem>, List<Document>>>
): List<AppListItem> = fetchedChunks
    .asSequence()
    .flatMap { (localApps, documents) ->
        val returnedDocIds = documents.mapTo(mutableSetOf()) { it.docId }
        localApps
            .asSequence()
            .filter { it.key !in returnedDocIds && it.value.app.status == App.STATUS_UPDATED }
            .map { it.value }
    }
    .toList()

internal fun reconcileVersionRollback(marketVersionCode: Int, localApp: App, values: ContentValues): Boolean {
    if (marketVersionCode >= localApp.versionNumber) {
        return false
    }
    values.put(AppListTable.Columns.STATUS, App.STATUS_NORMAL)
    values.put(AppListTable.Columns.SYNC_TIMESTAMP, 0L)
    return true
}

internal enum class UnavailableUpdateAction {
    NONE,
    SUPPRESS,
    ROLL_BACK
}

internal fun reconcileUnavailableUpdate(
    marketDoc: Document,
    localApp: App,
    installedInfo: InstalledApps.Info,
    values: ContentValues
): UnavailableUpdateAction {
    if (
        !marketDoc.isUnavailableForUpdate ||
        !installedInfo.isInstalled ||
        !installedInfo.isUpdatable(marketDoc.appDetails.versionCode)
    ) {
        return UnavailableUpdateAction.NONE
    }
    if (
        localApp.versionNumber == marketDoc.appDetails.versionCode &&
        localApp.versionNumber > installedInfo.versionCode
    ) {
        values.put(AppListTable.Columns.STATUS, App.STATUS_NORMAL)
        values.put(AppListTable.Columns.VERSION_NUMBER, installedInfo.versionCode)
        values.put(AppListTable.Columns.VERSION_NAME, installedInfo.versionName)
        values.put(AppListTable.Columns.UPLOAD_TIMESTAMP, 0L)
        values.put(AppListTable.Columns.UPLOAD_DATE, "")
        values.put(AppListTable.Columns.SYNC_TIMESTAMP, 0L)
        return UnavailableUpdateAction.ROLL_BACK
    }
    if (
        localApp.versionNumber <= installedInfo.versionCode &&
        (localApp.status != App.STATUS_NORMAL || localApp.syncTime != 0L)
    ) {
        values.put(AppListTable.Columns.STATUS, App.STATUS_NORMAL)
        values.put(AppListTable.Columns.SYNC_TIMESTAMP, 0L)
    }
    return UnavailableUpdateAction.SUPPRESS
}

internal suspend fun <T, R> fetchAllChunks(
    chunks: List<T>,
    maxAttempts: Int,
    initialRetryDelayMillis: Long,
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