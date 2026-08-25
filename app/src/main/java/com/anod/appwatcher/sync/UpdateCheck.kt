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
import java.time.Instant
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

    class SyncResult(
        val success: Boolean,
        val updates: List<UpdatedApp>,
        val checked: Int,
        val unavailable: Int,
        val failure: Throwable? = null
    )

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
            AppLog.i("Play Store update check started (syncId=${schedule.id})", "UpdateCheck")

            try {
                doSync(lastUpdatesViewed)
            } catch (e: AuthTokenStartIntent) {
                throw e
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                SyncResult(false, listOf(), 0, 0, e)
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
            return@withContext finishFailedSync(
                schedule = schedule,
                status = Schedule.STATUS_FAILED,
                manualSync = manualSync,
                stage = SyncFailureStage.SESSION_INITIALIZATION,
                error = e
            )
        }

        if (syncResult.success) {
            SchedulesTable.Queries.save(schedule.finish(Schedule.STATUS_SUCCESS, syncResult.checked, syncResult.updates.size, syncResult.unavailable), database)
        } else {
            return@withContext finishFailedSync(
                schedule = schedule,
                status = Schedule.STATUS_FAILED,
                manualSync = manualSync,
                stage = SyncFailureStage.PLAY_STORE_UPDATE_CHECK,
                error = checkNotNull(syncResult.failure)
            )
        }

        val updatedApps: List<UpdatedApp> = syncResult.updates
        if (updatedApps.isNotEmpty() && updatedApps.first().uploadTime == 0.toLong()) {
            AppLog.e("Cannot parse update upload date")
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
            AppLog.d("Sending chunk of ${docIds.size} apps")
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
        // The update-check response (?au=1) only signals availability, so fetch full documents for
        // apps that moved to a new version.
        val releaseDetails = fetchReleaseDetails(selectReleaseDetailsApps(fetchedChunks))
        // Applied chunk by chunk so only one chunk worth of prepared updates is held at a time.
        // Each chunk commits its own transaction, so a failure part way through leaves earlier
        // chunks applied. That is safe: a row and its changelog still commit together, app rows
        // carry no cross-row invariant, and the next sync re-fetches and reconciles whatever the
        // failed run did not reach.
        val updatedApps = mutableListOf<UpdatedApp>()
        for ((localApps, documents) in fetchedChunks) {
            val pendingUpdates = prepareAppUpdates(documents, localApps, lastUpdatesViewed, releaseDetails)
            updatedApps.addAll(applyAppUpdates(pendingUpdates, database))
        }

        // Missing from the response, so they never reach `updatedApps`.
        clearUnavailableUpdates(selectStaleUpdatedApps(fetchedChunks))

        val checked = localAppChunks.sumOf { it.size }
        AppLog.i("Sync finished for $checked apps", "UpdateCheck")
        return SyncResult(true, updatedApps, checked, unavailable)
    }

    /**
     * Full documents for apps that moved to a new version, from the plain (non `?au=1`) endpoint,
     * chunked at [BULK_SIZE].
     *
     * Best-effort: a failing chunk is skipped and apps Play does not return are simply absent, so
     * they keep their sparse document and cached values.
     *
     * The bulk request carries only doc ids and `includeDetails`, so the response cannot be narrowed
     * to the changelog: `includeDetails = false` would drop `recentChangesHtml` itself.
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
                    "Cannot fetch release details for ${chunk.size} apps: ${e.syncFailureDescription()}",
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
     * Drop the stale updated flag for watched apps the update-check response did not return.
     *
     * A bulk entry carries no doc id or error, so an omission cannot be attributed. Clearing resets
     * only the badge and touches no stored data, but the app is not highlighted again for that
     * same version.
     */
    private suspend fun clearUnavailableUpdates(staleApps: List<AppListItem>) {
        if (staleApps.isEmpty()) {
            return
        }
        AppLog.i(
            "Clearing update status for ${staleApps.size} apps missing from the update check",
            "UpdateCheck"
        )
        for (item in staleApps) {
            database.apps().clearUpdateStatus(rowId = item.app.rowId)
        }
    }

    private suspend fun finishFailedSync(
        schedule: Schedule,
        status: Int,
        manualSync: Boolean,
        stage: SyncFailureStage? = null,
        error: Throwable? = null
    ): Int {
        val failedSchedule = schedule.finish(status)
        SchedulesTable.Queries.save(failedSchedule, database)
        if (stage != null && error != null) {
            val failure = SyncFailureException(failedSchedule, stage, error)
            AppLog.e(checkNotNull(failure.message), "UpdateCheck", failure)
        }
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
                // Play advancing between the two requests would file one version's details under
                // another.
                val releaseApp = releaseDetails[docId]
                    ?.takeIf { it.appDetails.versionCode == marketApp.appDetails.versionCode }
                    ?: marketApp
                val result = updateApp(marketApp, releaseApp, localItem, lastUpdatesViewed)
                val isNewVersion = marketApp.appDetails.versionCode > localItem.app.versionNumber
                val recentChanges = releaseApp.appDetails.recentChangesHtml?.trim().orEmpty()
                val noNewDetails = if (isNewVersion) {
                    recentChanges.compareLettersAndDigits(localItem.changeDetails)
                } else {
                    localItem.noNewDetails
                }
                // Rows are keyed by (app_id, code) and inserted with CONFLICT_REPLACE, so a write
                // without a description would blank real notes, version name and upload date.
                val persistChangelog = result.persistChangelog && recentChanges.isNotBlank()
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
                "Skipped ${skippedRowIds.size} app rows changed during synchronization",
                "UpdateCheck"
            )
        }
        return pendingUpdates
            .filter { it.rowId in appliedRowIds }
            .mapNotNull { it.updatedApp }
    }

    /**
     * [marketDoc] (`?au=1`) stays the authority on availability and on which version to store;
     * [releaseDoc] carries that version's data - the full document when fetched, else [marketDoc].
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
                "Suppressing unavailable update version ${appDetails.versionCode}, restriction " +
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
                "Play Store version rolled back: ${localApp.versionNumber} -> ${appDetails.versionCode}",
                "UpdateCheck"
            )
            updateLocalApp(releaseDoc, localApp, values, uploadDateParserCache)
            return AppUpdateResult(
                values = values,
                updatedApp = null,
                persistChangelog = true
            )
        }

        if (appDetails.versionCode > localApp.versionNumber) {
            AppLog.i(
                "Play update candidate: installed ${installedInfo.versionCode}, cached ${localApp.versionNumber}, " +
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
            AppLog.d("Set update as viewed")
            values.put(AppListTable.Columns.STATUS, App.STATUS_NORMAL)
        } else if (localApp.status == App.STATUS_UPDATED) {
            // Application was previously updated
            val recentChanges = releaseDoc.appDetails.recentChangesHtml ?: ""
            updatedApp = UpdatedApp(localApp, recentChanges, installedInfo.versionCode, false)
        }
        // Refresh app info with latest fetched
        updateLocalApp(releaseDoc, localApp, values, uploadDateParserCache)
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
                AppLog.i("Updates found: ${updatedApps.size}", "UpdateCheck")
            }
            sn.cancel()
        } else if (updatedApps.isNotEmpty()) {
            val filter = SyncNotification.Filter(preferences)
            val filteredApps = if (filter.hasFilters) {
                filter.apply(updatedApps)
            } else {
                updatedApps
            }
            AppLog.i("Notifying about ${filteredApps.size} updates", "UpdateCheck")
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
}

/**
 * Copy the fetched release onto the watched row.
 *
 * The `?au=1` response omits optional metadata for unchanged documents, so a blank field means
 * "not reported", not "cleared", and only reported fields are written. Same-version and rollback
 * rows are built here rather than through
 * [com.anod.appwatcher.database.entities.preserveCachedMetadata].
 */
internal fun updateLocalApp(
    releaseDoc: Document,
    localApp: App,
    values: ContentValues,
    uploadDateParserCache: UploadDateParserCache
) {
    values.put(BaseColumns._ID, localApp.rowId)
    values.put(AppListTable.Columns.VERSION_NUMBER, releaseDoc.appDetails.versionCode)

    val uploadDate = releaseDoc.appDetails.uploadDate
    if (!uploadDate.isNullOrBlank()) {
        values.put(AppListTable.Columns.UPLOAD_TIMESTAMP, releaseDoc.extractUploadDate(uploadDateParserCache))
        values.put(AppListTable.Columns.UPLOAD_DATE, uploadDate)
    }

    val versionString = releaseDoc.appDetails.versionString
    if (!versionString.isNullOrBlank()) {
        values.put(AppListTable.Columns.VERSION_NAME, versionString)
    }

    val appType = releaseDoc.appDetails.appType
    if (!appType.isNullOrBlank() && appType != localApp.appType) {
        values.put(AppListTable.Columns.APP_TYPE, appType)
    }

    // Reported as a whole, so an entirely empty offer means no price was returned.
    val offer = releaseDoc.offer
    val hasOffer = !offer.formattedAmount.isNullOrBlank() ||
        !offer.currencyCode.isNullOrBlank() ||
        offer.micros > 0
    if (hasOffer) {
        if (offer.currencyCode != localApp.price.cur) {
            values.put(AppListTable.Columns.PRICE_CURRENCY, offer.currencyCode)
        }
        if (offer.formattedAmount != localApp.price.text) {
            values.put(AppListTable.Columns.PRICE_TEXT, offer.formattedAmount)
        }
        if (localApp.price.micros != offer.micros.toInt()) {
            values.put(AppListTable.Columns.PRICE_MICROS, offer.micros)
        }
    }

    if (!releaseDoc.iconUrl.isNullOrEmpty()) {
        values.put(AppListTable.Columns.ICON_URL, releaseDoc.iconUrl)
    }
}

/**
 * Apps whose update-check document reports a new version, as doc ids for a follow-up bulk request.
 *
 * Collected across all update-check chunks so the caller can re-split them into dense requests:
 * updates are usually scattered a few per chunk.
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
 * Watched apps still flagged as [App.STATUS_UPDATED] locally that the update-check response did
 * not return, so nothing else can clear their stale flag.
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
                    "Retrying Play Store chunk after attempt $attempt: ${error.syncFailureDescription()}",
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

private fun Throwable.syncFailureDescription(): String {
    val details = when (this) {
        is DfeServerError -> " [statusCode=${statusCode ?: "none"}]"
        else -> ""
    }
    return "${javaClass.name}$details"
}

internal enum class SyncFailureStage(val value: String) {
    SESSION_INITIALIZATION("session-initialization"),
    PLAY_STORE_UPDATE_CHECK("play-store-update-check")
}

internal class SyncFailureException(
    schedule: Schedule,
    stage: SyncFailureStage,
    error: Throwable
) : Exception(createMessage(schedule, stage, error)) {

    init {
        stackTrace = error.stackTrace
    }

    companion object {
        private const val MAX_CAUSE_DEPTH = 8

        private fun createMessage(
            schedule: Schedule,
            stage: SyncFailureStage,
            error: Throwable
        ): String {
            val syncType = if (schedule.reason == Schedule.REASON_MANUAL) "manual" else "scheduled"
            return "$syncType sync #${schedule.id} failed during ${stage.value}; " +
                "started=${Instant.ofEpochMilli(schedule.start)}; " +
                "durationMs=${schedule.finish - schedule.start}; " +
                "causeChain=${error.describeChain()}"
        }

        private fun Throwable.describeChain(): String {
            val causes = mutableListOf<Throwable>()
            val seen = mutableSetOf<Throwable>()
            var current: Throwable? = this
            while (current != null && causes.size < MAX_CAUSE_DEPTH && seen.add(current)) {
                causes.add(current)
                current = current.cause
            }
            return causes.joinToString(" <- ", transform = Throwable::syncFailureDescription)
        }
    }
}