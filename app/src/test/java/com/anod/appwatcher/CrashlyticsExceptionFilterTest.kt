package com.anod.appwatcher

import com.anod.appwatcher.backup.gdrive.GDriveSync
import com.anod.appwatcher.database.entities.Schedule
import com.anod.appwatcher.sync.SyncFailureException
import com.anod.appwatcher.sync.SyncFailureStage
import com.google.protobuf.InvalidProtocolBufferException
import finsky.api.DfeParseError
import finsky.api.DfeServerError
import java.io.EOFException
import java.io.IOException
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.security.cert.CertificateException
import java.time.Instant
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLPeerUnverifiedException
import javax.net.ssl.SSLProtocolException
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
    fun `reports explicit sync diagnostics with a transient label for known network errors`() {
        val schedule = Schedule(isManual = false).apply { id = 42L }
        val error = SyncFailureException(
            schedule = schedule.finish(Schedule.STATUS_FAILED),
            stage = SyncFailureStage.PLAY_STORE_UPDATE_CHECK,
            error = IOException("NetworkError")
        )

        assertFalse(CrashlyticsExceptionFilter.shouldIgnore(error) { true })
        assertTrue(error.message!!.contains("failureKind=expected-transient"))
    }

    @Test
    fun `labels sanitized DNS diagnostics with a nested system cause as transient`() {
        val error = SyncFailureException(
            schedule = Schedule(isManual = false).finish(Schedule.STATUS_FAILED),
            stage = SyncFailureStage.PLAY_STORE_UPDATE_CHECK,
            error = UnknownHostException("private host").apply {
                initCause(RuntimeException("resolver failure"))
            }
        )

        assertEquals(null, error.cause)
        assertFalse(CrashlyticsExceptionFilter.shouldIgnore(error) { false })
        assertTrue(error.message!!.contains("failureKind=expected-transient"))
    }

    @Test
    fun `labels sanitized diagnostics for wrapped socket timeout as transient`() {
        val error = SyncFailureException(
            schedule = Schedule(isManual = false).finish(Schedule.STATUS_FAILED),
            stage = SyncFailureStage.SESSION_INITIALIZATION,
            error = IOException("request failed", SocketTimeoutException("timeout"))
        )

        assertFalse(CrashlyticsExceptionFilter.shouldIgnore(error) { false })
        assertTrue(error.message!!.contains("failureKind=expected-transient"))
    }

    @Test
    fun `reports and labels expected transport failures for manual and scheduled sync`() {
        val failures = listOf(
            ConnectException("refused"),
            SocketException("reset"),
            SocketTimeoutException("timeout"),
            InterruptedIOException("timeout"),
            EOFException("connection closed"),
            SSLHandshakeException("connection closed during handshake")
        )
        for (manual in listOf(false, true)) {
            for (failure in failures) {
                val error = syncFailure(failure, manual)

                assertFalse(failure.javaClass.name, CrashlyticsExceptionFilter.shouldIgnore(error) { true })
                assertTrue(error.message!!.contains("failureKind=expected-transient"))
                assertEquals(null, error.cause)
            }
        }
    }

    @Test
    fun `reports and labels rate limiting and server errors after chunk retries`() {
        for (statusCode in listOf(429, 500, 502, 503, 504, 599)) {
            val error = syncFailure(DfeServerError("server failure", statusCode, DfeServerError("display error", null, null)))

            assertFalse("HTTP $statusCode", CrashlyticsExceptionFilter.shouldIgnore(error) { true })
            assertTrue(error.message!!.contains("failureKind=expected-transient"))
        }
    }

    @Test
    fun `reports actionable sync errors even if the generic network callback would ignore them`() {
        val certificateFailure = SSLHandshakeException("certificate rejected").apply {
            initCause(CertificateException("untrusted"))
        }
        val failures = listOf(
            IllegalStateException("failed"),
            IOException("unexpected IO failure"),
            DfeParseError("invalid protobuf", EOFException("truncated")),
            DfeServerError("unclassified server error", null, null),
            DfeServerError("invalid request", 400, SocketTimeoutException("timeout")),
            DfeServerError("unauthorized", 401, null),
            DfeServerError("forbidden", 403, null),
            DfeServerError("missing", 404, null),
            certificateFailure,
            SSLPeerUnverifiedException("hostname mismatch")
        )
        for (failure in failures) {
            val error = syncFailure(failure)
            assertFalse(failure.javaClass.name, CrashlyticsExceptionFilter.shouldIgnore(error) { true })
            assertTrue(error.message!!.contains("failureKind=unexpected"))
        }
    }

    @Test
    fun `reports persistent TLS protocol failure during handshake`() {
        val error = SSLHandshakeException("handshake failed").apply {
            initCause(SSLProtocolException("unsupported protocol"))
        }

        val diagnostic = syncFailure(error)
        assertFalse(CrashlyticsExceptionFilter.shouldIgnore(diagnostic) { true })
        assertTrue(diagnostic.message!!.contains("failureKind=unexpected"))
    }

    @Test
    fun `reports protobuf errors and distinguishes malformed responses from transport interruption`() {
        val malformed = InvalidProtocolBufferException("truncated response")
        val disconnected = InvalidProtocolBufferException(SocketException("connection reset"))

        val malformedDiagnostic = syncFailure(malformed)
        val disconnectedDiagnostic = syncFailure(disconnected)
        assertFalse(CrashlyticsExceptionFilter.shouldIgnore(malformedDiagnostic) { true })
        assertTrue(malformedDiagnostic.message!!.contains("failureKind=unexpected"))
        assertFalse(CrashlyticsExceptionFilter.shouldIgnore(disconnectedDiagnostic) { true })
        assertTrue(disconnectedDiagnostic.message!!.contains("failureKind=expected-transient"))
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
        assertFalse(CrashlyticsExceptionFilter.shouldIgnore(syncFailure(first)) { false })
    }

    private fun syncFailure(error: Throwable, manual: Boolean = false) = SyncFailureException(
        schedule = Schedule(isManual = manual).finish(Schedule.STATUS_FAILED),
        stage = SyncFailureStage.PLAY_STORE_UPDATE_CHECK,
        error = error
    )
}