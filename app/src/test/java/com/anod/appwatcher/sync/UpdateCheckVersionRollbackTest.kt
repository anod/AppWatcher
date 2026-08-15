package com.anod.appwatcher.sync

import android.content.ContentValues
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.Price
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

    private fun document(versionCode: Int, restriction: Int): Document = Document(
        DocV2.newBuilder()
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
                    )
            )
            .build()
    )
}