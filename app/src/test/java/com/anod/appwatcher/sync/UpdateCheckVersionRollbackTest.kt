package com.anod.appwatcher.sync

import android.content.ContentValues
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.Price
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
    fun chunkFailurePreventsApplyingEarlierFetches() = runBlocking {
        val applied = mutableListOf<Int>()
        var failedChunkAttempts = 0

        try {
            val fetched = fetchAllChunks(
                chunks = listOf(1, 2, 3),
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

    private fun app(versionNumber: Int) = App(
        rowId = 1,
        appId = "com.example.app",
        packageName = "com.example.app",
        versionNumber = versionNumber,
        versionName = versionNumber.toString(),
        title = "Example",
        creator = "Example",
        iconUrl = "",
        status = App.STATUS_UPDATED,
        uploadDate = "",
        price = Price("", "", 0),
        detailsUrl = null,
        uploadTime = 0,
        appType = "",
        syncTime = System.currentTimeMillis()
    )
}