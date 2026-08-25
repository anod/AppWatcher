package com.anod.appwatcher

import com.anod.appwatcher.backup.gdrive.GDriveSync
import com.anod.appwatcher.database.entities.Schedule
import com.anod.appwatcher.sync.SyncFailureException
import com.anod.appwatcher.sync.SyncFailureStage
import finsky.api.DfeServerError
import java.io.IOException
import java.time.Instant
import kotlinx.coroutines.CancellationException
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
    fun `reports explicit sync diagnostics for known network errors`() {
        val schedule = Schedule(isManual = false).apply { id = 42L }
        val error = SyncFailureException(
            schedule = schedule.finish(Schedule.STATUS_FAILED),
            stage = SyncFailureStage.PLAY_STORE_UPDATE_CHECK,
            error = IOException("NetworkError")
        )

        assertEquals(false, CrashlyticsExceptionFilter.shouldIgnore(error) { true })
    }

    @Test
    fun `sync diagnostics include the run stage timing status and cause chain`() {
        val networkError = IOException("request for alex@example.com used token=private")
        val cause = DfeServerError("service unavailable for com.private.app", statusCode = 503, cause = networkError)
        val schedule = Schedule(
            id = 42L,
            start = Instant.parse("2026-08-24T07:21:30Z").toEpochMilli(),
            finish = Instant.parse("2026-08-24T07:21:36Z").toEpochMilli(),
            reason = Schedule.REASON_SCHEDULE,
            result = Schedule.STATUS_FAILED,
            checked = 0,
            found = 0,
            unavailable = 0,
            notified = 0
        )

        val failure = SyncFailureException(
            schedule = schedule,
            stage = SyncFailureStage.PLAY_STORE_UPDATE_CHECK,
            error = cause
        )

        assertEquals(null, failure.cause)
        assertArrayEquals(cause.stackTrace, failure.stackTrace)
        assertTrue(failure.message!!.contains("scheduled sync #42 failed during play-store-update-check"))
        assertTrue(failure.message!!.contains("started=2026-08-24T07:21:30Z"))
        assertTrue(failure.message!!.contains("durationMs=6000"))
        assertTrue(failure.message!!.contains("finsky.api.DfeServerError [statusCode=503]"))
        assertTrue(failure.message!!.contains("java.io.IOException"))
        assertFalse(failure.message!!.contains("alex@example.com"))
        assertFalse(failure.message!!.contains("token=private"))
        assertFalse(failure.message!!.contains("com.private.app"))
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