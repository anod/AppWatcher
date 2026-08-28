package com.anod.appwatcher.sync

import android.content.ContentValues
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.AppListItem
import com.anod.appwatcher.database.entities.Price
import com.anod.appwatcher.utils.date.UploadDateParserCache
import finsky.api.Document
import finsky.protos.AppDetails
import finsky.protos.Availability
import finsky.protos.DocDetails
import finsky.protos.DocV2
import info.anodsplace.framework.content.InstalledApps
import java.io.IOException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UpdateCheckVersionRollbackTest {

    @Test
    fun lowerMarketVersionClearsUpdateAndRecentState() {
        val values = ContentValues()

        val rolledBack = reconcileVersionRollback(
            marketVersionCode = 99,
            localApp = app(versionNumber = 100),
            values = values
        )

        assertTrue(rolledBack)
        assertEquals(App.STATUS_NORMAL, values.getAsInteger(AppListTable.Columns.STATUS))
        assertEquals(0L, values.getAsLong(AppListTable.Columns.SYNC_TIMESTAMP))
    }

    @Test
    fun equalOrHigherMarketVersionKeepsCurrentState() {
        val equalValues = ContentValues()
        val higherValues = ContentValues()

        assertFalse(reconcileVersionRollback(100, app(versionNumber = 100), equalValues))
        assertFalse(reconcileVersionRollback(101, app(versionNumber = 100), higherValues))
        assertEquals(0, equalValues.size())
        assertEquals(0, higherValues.size())
    }

    @Test
    fun newRemoteVersionAtInstalledVersionDoesNotMarkUpdate() {
        assertEquals(
            AppUpdateDecision.REFRESH_INSTALLED_CURRENT,
            selectAppUpdateDecision(
                remoteVersion = 101,
                cachedVersion = 100,
                installedVersion = 101,
                status = App.STATUS_NORMAL,
                lastUpdatesViewed = false
            )
        )
    }

    @Test
    fun cachedRemoteNewerThanInstalledRestoresDeviceUpdate() {
        assertEquals(
            AppUpdateDecision.RESTORE_DEVICE_UPDATE,
            selectAppUpdateDecision(
                remoteVersion = 101,
                cachedVersion = 101,
                installedVersion = 100,
                status = App.STATUS_NORMAL,
                lastUpdatesViewed = true
            )
        )
    }

    @Test
    fun deviceUpdateRemainsVisibleAfterUpdatesAreViewed() {
        assertEquals(
            AppUpdateDecision.KEEP_DEVICE_UPDATE,
            selectAppUpdateDecision(
                remoteVersion = 101,
                cachedVersion = 101,
                installedVersion = 100,
                status = App.STATUS_UPDATED,
                lastUpdatesViewed = true
            )
        )
    }

    @Test
    fun installedUpdateStatusClearsAfterDeviceCatchesUp() {
        assertEquals(
            AppUpdateDecision.CLEAR_INSTALLED_UPDATE,
            selectAppUpdateDecision(
                remoteVersion = 101,
                cachedVersion = 101,
                installedVersion = 101,
                status = App.STATUS_UPDATED,
                lastUpdatesViewed = false
            )
        )
    }

    @Test
    fun uninstalledWatchedUpdateClearsAfterBeingViewed() {
        assertEquals(
            AppUpdateDecision.CLEAR_VIEWED_UPDATE,
            selectAppUpdateDecision(
                remoteVersion = 101,
                cachedVersion = 101,
                installedVersion = 0,
                status = App.STATUS_UPDATED,
                lastUpdatesViewed = true
            )
        )
    }

    @Test
    fun uninstalledWatchedUpdateRemainsUntilViewed() {
        assertEquals(
            AppUpdateDecision.KEEP_UPDATED,
            selectAppUpdateDecision(
                remoteVersion = 101,
                cachedVersion = 101,
                installedVersion = 0,
                status = App.STATUS_UPDATED,
                lastUpdatesViewed = false
            )
        )
    }

    @Test
    fun installedCurrentAppRemainsCurrent() {
        assertEquals(
            AppUpdateDecision.CURRENT,
            selectAppUpdateDecision(
                remoteVersion = 101,
                cachedVersion = 101,
                installedVersion = 101,
                status = App.STATUS_NORMAL,
                lastUpdatesViewed = true
            )
        )
    }

    @Test
    fun genuinelyNewAvailableVersionMarksUpdate() {
        assertEquals(
            AppUpdateDecision.MARK_UPDATED,
            selectAppUpdateDecision(
                remoteVersion = 101,
                cachedVersion = 100,
                installedVersion = 100,
                status = App.STATUS_NORMAL,
                lastUpdatesViewed = false
            )
        )
    }

    @Test
    fun newRemoteVersionMarksUninstalledWatchedApp() {
        assertEquals(
            AppUpdateDecision.MARK_UPDATED,
            selectAppUpdateDecision(
                remoteVersion = 101,
                cachedVersion = 100,
                installedVersion = 0,
                status = App.STATUS_NORMAL,
                lastUpdatesViewed = false
            )
        )
    }

    @Test
    fun invalidMarkedUpdateStillProducesDiagnosticSignal() {
        assertEquals(
            SyncDecisionSignal.INVALID_MARKED_NOT_NEWER_THAN_INSTALLED,
            detectSyncDecisionSignal(
                decision = AppUpdateDecision.MARK_UPDATED,
                installedVersion = 101,
                remoteVersion = 101
            )
        )
    }

    @Test
    fun restoredAndClearedDeviceUpdatesProduceNeutralSignals() {
        assertEquals(
            SyncDecisionSignal.DEVICE_UPDATE_RESTORED,
            detectSyncDecisionSignal(
                decision = AppUpdateDecision.RESTORE_DEVICE_UPDATE,
                installedVersion = 100,
                remoteVersion = 101
            )
        )
        assertEquals(
            SyncDecisionSignal.INSTALLED_UPDATE_CLEARED,
            detectSyncDecisionSignal(
                decision = AppUpdateDecision.CLEAR_INSTALLED_UPDATE,
                installedVersion = 101,
                remoteVersion = 101
            )
        )
    }

    @Test
    fun unavailableMarketVersionFallsBackToInstalledVersion() {
        val values = ContentValues()

        val reconciled = reconcileUnavailableUpdate(
            marketDoc = document(versionCode = 101, restriction = 9),
            localApp = app(versionNumber = 101),
            installedInfo = InstalledApps.Info(versionCode = 100, versionName = "100"),
            values = values
        )

        assertEquals(UnavailableUpdateAction.ROLL_BACK, reconciled)
        assertEquals(App.STATUS_NORMAL, values.getAsInteger(AppListTable.Columns.STATUS))
        assertEquals(100, values.getAsInteger(AppListTable.Columns.VERSION_NUMBER))
        assertEquals("100", values.getAsString(AppListTable.Columns.VERSION_NAME))
        assertEquals(0L, values.getAsLong(AppListTable.Columns.UPLOAD_TIMESTAMP))
        assertEquals("", values.getAsString(AppListTable.Columns.UPLOAD_DATE))
        assertEquals(0L, values.getAsLong(AppListTable.Columns.SYNC_TIMESTAMP))
    }

    @Test
    fun availableMarketVersionIsNotReconciled() {
        val values = ContentValues()

        val reconciled = reconcileUnavailableUpdate(
            marketDoc = document(versionCode = 101, restriction = 1),
            localApp = app(versionNumber = 100),
            installedInfo = InstalledApps.Info(versionCode = 100, versionName = "100"),
            values = values
        )

        assertEquals(UnavailableUpdateAction.NONE, reconciled)
        assertEquals(0, values.size())
    }

    @Test
    fun alreadyReconciledUnavailableVersionIsSuppressedWithoutAnotherWrite() {
        val values = ContentValues()

        val reconciled = reconcileUnavailableUpdate(
            marketDoc = document(versionCode = 101, restriction = 9),
            localApp = app(
                versionNumber = 100,
                status = App.STATUS_NORMAL,
                syncTime = 0
            ),
            installedInfo = InstalledApps.Info(versionCode = 100, versionName = "100"),
            values = values
        )

        assertEquals(UnavailableUpdateAction.SUPPRESS, reconciled)
        assertEquals(0, values.size())
    }

    @Test
    fun alreadyReconciledUnavailableVersionClearsStaleUpdateState() {
        val values = ContentValues()

        val reconciled = reconcileUnavailableUpdate(
            marketDoc = document(versionCode = 101, restriction = 9),
            localApp = app(
                versionNumber = 100,
                status = App.STATUS_UPDATED,
                syncTime = System.currentTimeMillis()
            ),
            installedInfo = InstalledApps.Info(versionCode = 100, versionName = "100"),
            values = values
        )

        assertEquals(UnavailableUpdateAction.SUPPRESS, reconciled)
        assertEquals(App.STATUS_NORMAL, values.getAsInteger(AppListTable.Columns.STATUS))
        assertEquals(0L, values.getAsLong(AppListTable.Columns.SYNC_TIMESTAMP))
    }

    @Test
    fun unavailableNewerVersionPreservesDifferentCachedUpdate() {
        val values = ContentValues()

        val reconciled = reconcileUnavailableUpdate(
            marketDoc = document(versionCode = 102, restriction = 9),
            localApp = app(versionNumber = 101),
            installedInfo = InstalledApps.Info(versionCode = 100, versionName = "100"),
            values = values
        )

        assertEquals(UnavailableUpdateAction.SUPPRESS, reconciled)
        assertEquals(0, values.size())
    }

    @Test
    fun chunkFailurePreventsApplyingEarlierFetches() = runBlocking {
        val applied = mutableListOf<Int>()
        var failedChunkAttempts = 0

        try {
            val fetched = fetchAllChunks(
                chunks = listOf(1, 2, 3),
                maxAttempts = UpdateCheck.MAX_CHUNK_ATTEMPTS,
                initialRetryDelayMillis = 0
            ) { chunk ->
                if (chunk == 2) {
                    failedChunkAttempts++
                    throw IOException("failed chunk")
                }
                "result-$chunk"
            }
            fetched.forEach { (chunk, _) -> applied.add(chunk) }
            throw AssertionError("Expected chunk failure")
        } catch (_: IOException) {
        }

        assertEquals(emptyList<Int>(), applied)
        assertEquals(3, failedChunkAttempts)
    }

    @Test
    fun transientChunkFailureIsRetriedBeforeApplying() = runBlocking {
        var attempts = 0

        val fetched = fetchAllChunks(
            chunks = listOf(1),
            maxAttempts = UpdateCheck.MAX_CHUNK_ATTEMPTS,
            initialRetryDelayMillis = 0
        ) {
            attempts++
            if (attempts < 3) {
                throw IOException("temporary failure")
            }
            "result"
        }

        assertEquals(listOf(1 to "result"), fetched)
        assertEquals(3, attempts)
    }

    @Test
    fun missingBulkDocumentsAreDistinguishedWithoutSelectingRowsForMutation() {
        val classification = classifyBulkDocuments(
            requestedDocIds = setOf("missing.updated", "missing.normal", "returned.updated"),
            responseDocuments = listOf(document(docId = "returned.updated", versionCode = 2, restriction = 1))
        )

        assertEquals(listOf("returned.updated"), classification.documents.map { it.docId })
        assertEquals(setOf("missing.updated", "missing.normal"), classification.missingDocIds)
        assertTrue(classification.withoutDetailsDocIds.isEmpty())
    }

    @Test
    fun returnedDocumentWithoutAppDetailsIsNotReportedAsMissing() {
        val classification = classifyBulkDocuments(
            requestedDocIds = setOf("without.details", "returned"),
            responseDocuments = listOf(
                documentWithoutDetails("without.details"),
                document(docId = "returned", versionCode = 2, restriction = 1)
            )
        )

        assertEquals(listOf("returned"), classification.documents.map { it.docId })
        assertEquals(setOf("without.details"), classification.withoutDetailsDocIds)
        assertTrue(classification.missingDocIds.isEmpty())
    }

    @Test
    fun fullyUsableBulkResponseHasNoMissingOrDetailLessDocuments() {
        val classification = classifyBulkDocuments(
            requestedDocIds = setOf("com.example.app"),
            responseDocuments = listOf(document(docId = "com.example.app", versionCode = 2, restriction = 1))
        )

        assertEquals(1, classification.documents.size)
        assertTrue(classification.withoutDetailsDocIds.isEmpty())
        assertTrue(classification.missingDocIds.isEmpty())
    }

    @Test
    fun newVersionIsSelectedForReleaseDetails() {
        val localApps = mapOf(
            "com.example.app" to listItem(packageName = "com.example.app", status = App.STATUS_NORMAL)
        )

        val missing = selectReleaseDetailsApps(
            fetchedChunks = listOf(
                localApps to listOf(document(docId = "com.example.app", versionCode = 2, restriction = 1))
            )
        )

        assertEquals(listOf("com.example.app"), missing.map { it.packageName })
    }

    @Test
    fun newVersionIsSelectedEvenWhenUpdateCheckCarriesChanges() {
        val localApps = mapOf(
            "com.example.app" to listItem(packageName = "com.example.app", status = App.STATUS_NORMAL)
        )

        // Recent changes alone don't make the sparse document complete: icon, version name,
        // upload date and price still have to come from the full release document.
        val missing = selectReleaseDetailsApps(
            fetchedChunks = listOf(
                localApps to listOf(
                    document(docId = "com.example.app", versionCode = 2, restriction = 1, recentChanges = "What's new")
                )
            )
        )

        assertEquals(listOf("com.example.app"), missing.map { it.packageName })
    }

    @Test
    fun sameVersionIsNotRefetched() {
        val localApps = mapOf(
            "com.example.app" to listItem(
                packageName = "com.example.app",
                status = App.STATUS_UPDATED,
                changeDetails = "Cached notes"
            )
        )

        val missing = selectReleaseDetailsApps(
            fetchedChunks = listOf(
                localApps to listOf(document(docId = "com.example.app", versionCode = 1, restriction = 1))
            )
        )

        assertTrue(missing.isEmpty())
    }

    @Test
    fun updatesScatteredAcrossChunksArePackedIntoFullBulkRequests() {
        // One update per update-check chunk: re-splitting the collected ids is what turns 45
        // scattered updates into 3 dense requests instead of 45 sparse ones.
        val fetchedChunks = (1..45).map { index ->
            mapOf(
                "updated.$index" to listItem(packageName = "updated.$index", status = App.STATUS_NORMAL),
                "same.$index" to listItem(packageName = "same.$index", status = App.STATUS_NORMAL)
            ) to listOf(
                document(docId = "updated.$index", versionCode = 2, restriction = 1),
                document(docId = "same.$index", versionCode = 1, restriction = 1)
            )
        }

        val missing = selectReleaseDetailsApps(fetchedChunks)

        assertEquals(45, missing.size)
        assertEquals(3, missing.chunked(20).size)
        assertTrue(missing.none { it.packageName.startsWith("same.") })
    }

    @Test
    fun rolledBackVersionIsNotRefetched() {
        val localApps = mapOf(
            "com.example.app" to listItem(
                packageName = "com.example.app",
                status = App.STATUS_UPDATED,
                changeDetails = "Notes for the newer version"
            )
        )

        val missing = selectReleaseDetailsApps(
            fetchedChunks = listOf(
                localApps to listOf(document(docId = "com.example.app", versionCode = 0, restriction = 1))
            )
        )

        assertTrue(missing.isEmpty())
    }

    @Test
    fun sparseResponseDoesNotBlankStoredMetadata() {
        val values = ContentValues()

        updateLocalApp(
            releaseDoc = releaseDocument(versionCode = 1, versionString = null, uploadDate = null),
            localApp = app(versionNumber = 1),
            values = values,
            uploadDateParserCache = UploadDateParserCache()
        )

        assertEquals(1, values.getAsInteger(AppListTable.Columns.VERSION_NUMBER))
        assertFalse(values.containsKey(AppListTable.Columns.VERSION_NAME))
        assertFalse(values.containsKey(AppListTable.Columns.UPLOAD_DATE))
        assertFalse(values.containsKey(AppListTable.Columns.UPLOAD_TIMESTAMP))
        assertFalse(values.containsKey(AppListTable.Columns.APP_TYPE))
        assertFalse(values.containsKey(AppListTable.Columns.PRICE_CURRENCY))
        assertFalse(values.containsKey(AppListTable.Columns.PRICE_TEXT))
        assertFalse(values.containsKey(AppListTable.Columns.PRICE_MICROS))
    }

    @Test
    fun populatedResponseUpdatesMetadata() {
        val values = ContentValues()

        updateLocalApp(
            releaseDoc = releaseDocument(
                versionCode = 2,
                versionString = "2.0",
                uploadDate = "Feb 2, 2026"
            ),
            localApp = app(versionNumber = 1),
            values = values,
            uploadDateParserCache = UploadDateParserCache()
        )

        assertEquals(2, values.getAsInteger(AppListTable.Columns.VERSION_NUMBER))
        assertEquals("2.0", values.getAsString(AppListTable.Columns.VERSION_NAME))
        assertEquals("Feb 2, 2026", values.getAsString(AppListTable.Columns.UPLOAD_DATE))
    }

    private fun releaseDocument(
        versionCode: Int,
        versionString: String?,
        uploadDate: String?
    ): Document = Document(
        DocV2.newBuilder()
            .setDocid("com.example.app")
            .setDetails(
                DocDetails.newBuilder()
                    .setAppDetails(
                        AppDetails.newBuilder()
                            .setPackageName("com.example.app")
                            .setVersionCode(versionCode)
                            .also { builder ->
                                if (versionString != null) {
                                    builder.setVersionString(versionString)
                                }
                                if (uploadDate != null) {
                                    builder.setUploadDate(uploadDate)
                                }
                            }
                    )
            )
            .build()
    )

    private fun listItem(packageName: String, status: Int) = listItem(packageName, status, changeDetails = null)

    private fun listItem(packageName: String, status: Int, changeDetails: String?) = AppListItem(
        app = app(versionNumber = 1, status = status).copy(
            appId = packageName,
            packageName = packageName
        ),
        changeDetails = changeDetails,
        noNewDetails = false,
        recentFlag = false
    )

    private fun app(
        versionNumber: Int,
        status: Int = App.STATUS_UPDATED,
        syncTime: Long = System.currentTimeMillis()
    ) = App(
        rowId = 1,
        appId = "com.example.app",
        packageName = "com.example.app",
        versionNumber = versionNumber,
        versionName = versionNumber.toString(),
        title = "Example",
        creator = "Example",
        iconUrl = "",
        status = status,
        uploadDate = "Jan 1, 2026",
        price = Price("", "", 0),
        detailsUrl = null,
        uploadTime = 1,
        appType = "",
        syncTime = syncTime
    )

    private fun document(versionCode: Int, restriction: Int): Document = document(
        docId = "com.example.app",
        versionCode = versionCode,
        restriction = restriction
    )

    private fun document(docId: String, versionCode: Int, restriction: Int): Document = document(
        docId = docId,
        versionCode = versionCode,
        restriction = restriction,
        recentChanges = null
    )

    private fun documentWithoutDetails(docId: String): Document = Document(
        DocV2.newBuilder()
            .setDocid(docId)
            .build()
    )

    private fun document(
        docId: String,
        versionCode: Int,
        restriction: Int,
        recentChanges: String?
    ): Document = Document(
        DocV2.newBuilder()
            .setDocid(docId)
            .setAvailability(
                Availability.newBuilder()
                    .setRestriction(restriction)
            )
            .setDetails(
                DocDetails.newBuilder()
                    .setAppDetails(
                        AppDetails.newBuilder()
                            .setPackageName("com.example.app")
                            .setVersionCode(versionCode)
                            .also { builder ->
                                if (recentChanges != null) {
                                    builder.setRecentChangesHtml(recentChanges)
                                }
                            }
                    )
            )
            .build()
    )
}