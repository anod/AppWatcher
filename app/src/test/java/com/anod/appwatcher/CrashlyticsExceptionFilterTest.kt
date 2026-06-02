package com.anod.appwatcher

import com.anod.appwatcher.backup.gdrive.GDriveSync
import java.io.IOException
import kotlinx.coroutines.CancellationException
import org.junit.Assert.assertEquals
import org.junit.Test

class CrashlyticsExceptionFilterTest {

    @Test
    fun `ignores sync errors caused by cancellation`() {
        val error = GDriveSync.SyncError(null, CancellationException("cancelled"))

        assertEquals(true, CrashlyticsExceptionFilter.shouldIgnore(error) { false })
    }

    @Test
    fun `ignores sync errors caused by known network errors`() {
        val error = GDriveSync.SyncError(null, IOException("NetworkError"))

        assertEquals(true, CrashlyticsExceptionFilter.shouldIgnore(error) { false })
    }

    @Test
    fun `reports sync errors caused by real failures`() {
        val error = GDriveSync.SyncError(null, IllegalStateException("failed"))

        assertEquals(false, CrashlyticsExceptionFilter.shouldIgnore(error) { false })
    }

    @Test
    fun `root cause walk is bounded for cyclic causes`() {
        val first = RuntimeException("first")
        val second = RuntimeException("second")
        first.initCause(second)
        second.initCause(first)

        assertEquals(false, CrashlyticsExceptionFilter.shouldIgnore(first) { false })
    }
}