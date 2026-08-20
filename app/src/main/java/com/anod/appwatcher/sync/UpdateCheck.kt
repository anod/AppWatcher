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
import finsky.api.isItemNotFoundError
import finsky.api.toDocument
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

        /**
         * Upper bound on the extra non-bulk details requests issued per sync to confirm that an
         * app is delisted. Keeps a large watchlist from turning one sync into hundreds of calls.
         */
        internal const val MAX_UNAVAILABLE_CONFIRMATIONS = 10

        /**
         * Upper bound on the extra full-details requests issued per sync to recover changelog text
         * missing from the sparse update-check response.
         */
        internal const val MAX_CHANGELOG_RECOVERIES = 30

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
        // The update-check response (?au=1) is sparse and frequently omits recent changes, so
        // fetch the full documents for the affected apps before persisting any changelog.
        val recentChanges = fetchMissingRecentChanges(selectMissingChangelogApps(fetchedChunks))
        val pendingUpdates = fetchedChunks.flatMap { (localApps, documents) ->
            prepareAppUpdates(documents, localApps, lastUpdatesViewed, recentChanges)
        }
        val updatedApps = applyAppUpdates(pendingUpdates, database)

        // Candidates are absent from the bulk response, so they never reach `updatedApps`;
        // clearing them only repairs rows the bulk data can no longer speak for.
        clearUnavailableUpdates(selectStaleUpdatedApps(fetchedChunks))

        val checked = localAppChunks.sumOf { it.size }
        AppLog.i("Sync finished for $checked apps", "UpdateCheck")
        return SyncResult(true, updatedApps, checked, unavailable)
    }

    /**
     * Doc ids for apps whose bulk update-check document arrived without recent changes.
     */
    private fun selectMissingChangelogApps(
        fetchedChunks: List<Pair<Map<String, AppListItem>, List<Document>>>
    ): List<String> = selectMissingChangelogApps(fetchedChunks, MAX_CHANGELOG_RECOVERIES)

    /**
     * Fetch the full (non update-check) details document for each app whose bulk response lacks
     * recent changes, and return the recovered changelog HTML keyed by doc id. Failures are
     * tolerated: a missing entry simply leaves the cached changelog untouched.
     */
    private suspend fun fetchMissingRecentChanges(docIds: List<String>): Map<String, String> {
        if (docIds.isEmpty()) {
            return emptyMap()
        }
        val dfeApi = koin.get<DfeApi>()
        val recovered = mutableMapOf<String, String>()
        for (docId in docIds) {
            try {
                val document = dfeApi.details(App.createDetailsUrl(docId)).toDocument()
                val changes = document?.appDetails?.recentChangesHtml?.trim() ?: ""
                if (changes.isNotBlank()) {
                    recovered[docId] = changes
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                AppLog.w(
                    "Cannot fetch recent changes for $docId: ${e.message}",
                    "UpdateCheck"
                )
            }
        }
        return recovered
    }

    /**
     * Watched apps that are still flagged as updated locally but were not returned by the bulk
     * update-check response. Play omits documents it no longer serves, so these are candidates for
     * a delisted app whose cached [App.STATUS_UPDATED] flag can never be cleared by the bulk data
     * alone.
     */
    private fun selectStaleUpdatedApps(
        fetchedChunks: List<Pair<Map<String, AppListItem>, List<Document>>>
    ): List<AppListItem> = selectStaleUpdatedApps(fetchedChunks, MAX_UNAVAILABLE_CONFIRMATIONS)

    /**
     * Confirm each candidate against the non-bulk details endpoint, which answers with Play's
     * definitive item-not-found error for delisted apps, and drop the stale updated flag for the
     * confirmed ones. Any other failure (network, auth, throttling) leaves the app untouched so a
     * transient error never hides a legitimate update.
     */
    private suspend fun clearUnavailableUpdates(candidates: List<AppListItem>) {
        if (candidates.isEmpty()) {
            return
        }
        val dfeApi = koin.get<DfeApi>()
        for (item in candidates) {
            val app = item.app
            // Always derive the URL from the package name: a stale cached `detailsUrl` answers with
            // a bare 404, which is deliberately not treated as item-not-found, so a genuinely
            // delisted app would never get its stale flag cleared.
            val detailsUrl = App.createDetailsUrl(app.packageName)
            val unavailable = try {
                dfeApi.details(detailsUrl)
                false
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (!e.isItemNotFoundError) {
                    AppLog.w(
                        "Cannot confirm availability of ${app.packageName}: ${e.message}",
                        "UpdateCheck"
                    )
                    false
                } else {
                    true
                }
            }
            if (!unavailable) {
                continue
            }
            AppLog.i(
                "Clearing update status for unavailable ${app.packageName}",
                "UpdateCheck"
            )
            database.apps().clearUpdateStatus(rowId = app.rowId)
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
        recoveredChanges: Map<String, String>
    ): List<PendingAppUpdate> {
        val pendingUpdates = mutableListOf<PendingAppUpdate>()
        for (marketApp in documents) {
            val docId = marketApp.docId
            localApps[docId]?.let { localItem ->
                val result = updateApp(marketApp, localItem, lastUpdatesViewed)
                val isNewVersion = marketApp.appDetails.versionCode > localItem.app.versionNumber
                val recentChanges = resolveRecentChanges(
                    responseChanges = marketApp.appDetails.recentChangesHtml,
                    recoveredChanges = recoveredChanges[docId],
                    cachedChanges = localItem.changeDetails,
                    isNewVersion = isNewVersion
                )
                val noNewDetails = if (isNewVersion) {
                    recentChanges.compareLettersAndDigits(localItem.changeDetails)
                } else {
                    localItem.noNewDetails
                }
                if (result.values.size() > 0) {
                    pendingUpdates.add(
                        PendingAppUpdate(
                            rowId = localItem.app.rowId.toLong(),
                            expectedAppId = localItem.app.appId,
                            expectedPackageName = localItem.app.packageName,
                            values = result.values,
                            changelog = if (result.persistChangelog) {
                                AppChange(
                                    docId,
                                    marketApp.appDetails.versionCode,
                                    marketApp.appDetails.versionString,
                                    recentChanges,
                                    marketApp.appDetails.uploadDate,
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

    private fun updateApp(
        marketDoc: Document,
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
            updateLocalApp(marketDoc, localApp, values)
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
            val uploadTime = marketDoc.extractUploadDate(uploadDateParserCache)
            val newApp = marketDoc.toApp(
                rowId = localApp.rowId,
                status = App.STATUS_UPDATED,
                uploadTime = uploadTime,
                syncTime = System.currentTimeMillis(),
            ).preserveCachedMetadata(localApp)
            val recentChanges = appDetails.recentChangesHtml ?: ""
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
            val recentChanges = appDetails.recentChangesHtml ?: ""
            updatedApp = UpdatedApp(localApp, recentChanges, installedInfo.versionCode, false)
        }
        // Refresh app info with latest fetched
        updateLocalApp(marketDoc, localApp, values)
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
 * Doc ids returned by the update-check bulk response without any recent changes text. Play's
 * update-purpose response (`?au=1`) is sparse and routinely omits `recentChangesHtml`, so these
 * apps need a full details request before their changelog can be persisted. Only apps whose
 * changelog would actually be written are selected: an app on a new version always needs the text,
 * and an app on the cached version only needs it when nothing is cached yet.
 */
internal fun selectMissingChangelogApps(
    fetchedChunks: List<Pair<Map<String, AppListItem>, List<Document>>>,
    limit: Int
): List<String> = fetchedChunks
    .asSequence()
    .flatMap { (localApps, documents) -> documents.asSequence().map { localApps[it.docId] to it } }
    .filter { (localItem, document) ->
        localItem != null &&
            document.appDetails.recentChangesHtml.isNullOrBlank() &&
            (document.appDetails.versionCode > localItem.app.versionNumber || localItem.changeDetails.isNullOrBlank())
    }
    .map { (_, document) -> document.docId }
    .distinct()
    .take(limit)
    .toList()

/**
 * Recent changes to persist for an app. The sparse update-check response wins only when it
 * actually carries text; otherwise the recovered full-details text is used, and finally the
 * cached changelog is kept so a blank response never wipes a previously stored description. A new
 * version with no text anywhere is stored as blank, since the cached text belongs to the old
 * version.
 */
internal fun resolveRecentChanges(
    responseChanges: String?,
    recoveredChanges: String?,
    cachedChanges: String?,
    isNewVersion: Boolean
): String {
    val fresh = responseChanges?.trim().orEmpty().ifBlank { recoveredChanges?.trim().orEmpty() }
    if (fresh.isNotBlank()) {
        return fresh
    }
    return if (isNewVersion) "" else cachedChanges?.trim().orEmpty()
}

/**
 * Watched apps still flagged as [App.STATUS_UPDATED] locally that the bulk update-check response
 * did not return. Play omits documents it no longer serves, so these are the only rows whose stale
 * updated flag the bulk data can never clear on its own. Capped by [limit] to bound the number of
 * follow-up confirmation requests.
 */
internal fun selectStaleUpdatedApps(
    fetchedChunks: List<Pair<Map<String, AppListItem>, List<Document>>>,
    limit: Int
): List<AppListItem> = fetchedChunks
    .asSequence()
    .flatMap { (localApps, documents) ->
        val returnedDocIds = documents.mapTo(mutableSetOf()) { it.docId }
        localApps
            .asSequence()
            .filter { it.key !in returnedDocIds && it.value.app.status == App.STATUS_UPDATED }
            .map { it.value }
    }
    .take(limit)
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