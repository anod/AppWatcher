package com.anod.appwatcher.diagnostics

import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.anod.appwatcher.accounts.AuthAccount
import com.anod.appwatcher.preferences.Preferences
import info.anodsplace.applog.AppLog
import info.anodsplace.notification.NotificationManager
import java.io.File
import java.io.IOException
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [35])
class DiagnosticRecorderTest {
    private val root = File("build/diagnostic-recorder-tests/${UUID.randomUUID()}").absoluteFile
    private val app = ApplicationProvider.getApplicationContext<Context>()
    private val context = object : ContextWrapper(app) {
        override fun getNoBackupFilesDir(): File = File(root, "no-backup").apply { mkdirs() }
        override fun getCacheDir(): File = File(root, "cache").apply { mkdirs() }
    }
    private val job = SupervisorJob()
    private val scope = CoroutineScope(job + Dispatchers.IO)
    private val original = AppLog.logger
    private val delegate = object : AppLog.Logger {
        override fun println(priority: Int, tag: String, msg: String) { }
    }
    private val prefs = Preferences(context, NotificationManager.NoOp(), scope,
        DiagnosticAllowlist(setOf(DiagnosticAccounts.hash("diagnostic-account"), DiagnosticAccounts.hash("other-account")))).apply {
        account = account("diagnostic-account")
        AppLog.logger = delegate
    }
    private fun account(name: String) = AuthAccount(name, "account-type", "", "", "")
    private fun recorder() = DiagnosticRecorder(context, prefs, scope, workInfos = { emptyList() })

    @After
    fun tearDown() = runBlocking {
        job.cancelAndJoin()
        AppLog.logger = original
        root.deleteRecursively()
        Unit
    }

    @Test
    fun disabledAndNotAllowedNeverInstallTeeOrWriteFiles() = runBlocking {
        val recorder = recorder()
        assertSame(delegate, AppLog.logger)
        AppLog.i("disabled")
        prefs.account = account("not-allowed")
        assertTrue(recorder.setEnabled(true))
        assertSame(delegate, AppLog.logger)
        AppLog.i("not allowed")
        recorder.refresh()
        assertEquals(0L, recorder.state.value.bytes)
        assertFalse(File(context.noBackupFilesDir, "diagnostics").exists())
    }

    @Test
    fun enabledPersistsAppLogAndStartupMetadataAndDisablingUninstallsTee() = runBlocking {
        val recorder = recorder()
        assertTrue(recorder.setEnabled(true))
        assertTrue(AppLog.logger is DiagnosticLogger)
        recorder.captureProcessState()
        AppLog.i("saved application line")
        val text = recorder.snapshot().readText()
        assertTrue(text, text.contains("saved application line"))
        assertTrue(text, text.contains("Diagnostics started versionCode="))
        assertTrue(text, text.contains("sdk=35"))
        assertTrue(text, text.contains("Standby bucket="))
        assertTrue(text, text.contains("Work AppRefresh"))
        assertTrue(recorder.setEnabled(false))
        assertSame(delegate, AppLog.logger)
        val bytes = recorder.state.value.bytes
        AppLog.i("not saved")
        recorder.refresh()
        assertEquals(bytes, recorder.state.value.bytes)
    }

    @Test
    fun enabledAtProcessStartWrapsCurrentLoggerAndCapturesMetadataOnce() = runBlocking {
        prefs.isDiagnosticsEnabled = true
        val captured = CompletableDeferred<Unit>()
        val queries = AtomicInteger()
        val recorder = DiagnosticRecorder(context, prefs, scope, workInfos = {
            if (queries.incrementAndGet() == 2) captured.complete(Unit)
            emptyList()
        })
        assertSame(delegate, (AppLog.logger as DiagnosticLogger).delegate)
        withTimeout(5000) { captured.await() }
        val text = recorder.snapshot().readText()
        assertEquals(1, text.lineSequence().count { it.contains("Diagnostics started versionCode=") })
        assertEquals(2, queries.get())
    }

    @Test
    fun boundedQueueNeverBlocksOrThrowsAndReportsDrops() = runBlocking {
        val recorder = recorder()
        recorder.setEnabled(true)
        val started = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        val blocker = async { recorder.operation { started.complete(Unit); release.await() } }
        started.await()
        repeat(1000) { AppLog.i("queued $it") }
        release.complete(Unit)
        blocker.await()
        recorder.refresh()
        assertTrue(recorder.state.value.dropped >= 872)
    }

    @Test
    fun failedDelegateAndSinkNeverThrowFromPrintln() {
        val logger = DiagnosticLogger(object : AppLog.Logger {
            override fun println(priority: Int, tag: String, msg: String) { error("delegate") }
        }) { _, _, _ -> error("sink") }
        logger.println(4, "tag", "message")
    }

    @Test
    fun disablingAndReenablingDiscardsQueuedLinesImmediately() = runBlocking {
        val recorder = recorder()
        recorder.setEnabled(true)
        val started = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        val blocker = async { recorder.operation { started.complete(Unit); release.await() } }
        started.await()
        AppLog.i("discard on disable")
        val disable = async(start = CoroutineStart.UNDISPATCHED) { recorder.setEnabled(false) }
        assertSame(delegate, AppLog.logger)
        val enable = async(start = CoroutineStart.UNDISPATCHED) { recorder.setEnabled(true) }
        release.complete(Unit)
        blocker.await()
        assertTrue(disable.await())
        assertTrue(enable.await())
        assertFalse(recorder.snapshot().readText().contains("discard on disable"))
    }

    @Test
    fun accountSwitchDiscardsQueuedLinesAndRemovesTee() = runBlocking {
        val recorder = recorder()
        recorder.setEnabled(true)
        val started = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        val blocker = async { recorder.operation { started.complete(Unit); release.await() } }
        started.await()
        AppLog.i("old account line")
        prefs.account = account("other-account")
        withTimeout(5000) { while (AppLog.logger is DiagnosticLogger) delay(1) }
        assertFalse(prefs.isDiagnosticsEnabled)
        prefs.isDiagnosticsEnabled = true
        release.complete(Unit)
        blocker.await()
        assertFalse(recorder.snapshot().readText().contains("old account line"))
    }

    @Test
    fun deleteDiscardsQueuedLinesWithoutResurrectionAndNewLinesStillWrite() = runBlocking {
        val recorder = recorder()
        recorder.setEnabled(true)
        val started = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        val blocker = async { recorder.operation { started.complete(Unit); release.await() } }
        started.await()
        AppLog.i("discard on delete")
        val delete = async(start = CoroutineStart.UNDISPATCHED) { recorder.delete() }
        release.complete(Unit)
        blocker.await()
        assertTrue(delete.await())
        assertFalse(recorder.snapshot().readText().contains("discard on delete"))
        AppLog.i("after delete")
        assertTrue(recorder.snapshot().readText().contains("after delete"))
    }

    @Test
    fun shareUsesStableSnapshotWithReadPermissionAndDeleteClearsExports() = runBlocking {
        val recorder = recorder()
        recorder.setEnabled(true)
        AppLog.i("share this")
        val snapshot = recorder.snapshot()
        // AndroidX FileProvider's slash-based canonical-path check cannot run on Windows.
        // Exercise the real snapshot and intent separately; the manifest restricts the URI root.
        val uri = Uri.parse("content://${context.packageName}.diagnostics/diagnostic_snapshots/${snapshot.name}")
        val chooser = diagnosticShareIntent(uri)
        val send = chooser.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)!!
        assertEquals(Intent.ACTION_SEND, send.action)
        assertEquals("text/plain", send.type)
        assertTrue(send.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
        assertFalse(send.hasExtra(Intent.EXTRA_TEXT))
        assertEquals(uri, send.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java))
        assertEquals(uri, send.clipData!!.getItemAt(0).uri)
        val text = snapshot.readText()
        assertTrue(text.contains("share this"))
        AppLog.i("later line")
        recorder.refresh()
        assertEquals(text, snapshot.readText())
        assertTrue(recorder.delete())
        assertFalse(snapshot.exists())
    }

    @Test
    fun lineLengthIsBoundedAndExportsExpireAndAreCapped() = runBlocking {
        val recorder = recorder()
        recorder.setEnabled(true)
        AppLog.i("x".repeat(100_000))
        val snapshot = recorder.snapshot()
        val text = snapshot.readText()
        assertTrue(text.contains("x".repeat(16_000)))
        assertFalse(text.contains("x".repeat(16_400)))
        repeat(5) { recorder.snapshot() }
        val directory = snapshot.parentFile!!
        assertEquals(3, directory.listFiles()!!.size)
        directory.listFiles()!!.forEach { assertTrue(it.setLastModified(System.currentTimeMillis() - 25L * 60 * 60 * 1000)) }
        recorder.refresh()
        assertTrue(directory.listFiles()!!.isEmpty())
        File(directory, "old-a.log").writeBytes(ByteArray(6 * 1024 * 1024))
        File(directory, "old-b.log").writeBytes(ByteArray(6 * 1024 * 1024))
        recorder.snapshot()
        assertTrue(directory.listFiles()!!.sumOf { it.length() } <= DiagnosticFileStore.MAX_BYTES)
    }

    @Test
    fun storageFailureIsVisibleWithoutThrowingIntoLoggerOrOperations() = runBlocking {
        File(context.noBackupFilesDir, "diagnostics").writeText("not a directory")
        val recorder = recorder()
        assertFalse(recorder.setEnabled(true))
        AppLog.i("failed storage")
        assertTrue(runCatching { recorder.refresh() }.isFailure)
        assertTrue(recorder.state.value.error)
        assertFalse(recorder.delete())
        assertNull(recorder.shareIntent())
    }

    @Test
    fun cancelledMetadataFutureAndCommandDoNotStopWriter() = runBlocking {
        val recorder = DiagnosticRecorder(context, prefs, scope, workInfos = { throw CancellationException("cancelled future") })
        assertTrue(recorder.setEnabled(true))
        assertTrue(recorder.captureProcessState())
        assertTrue(runCatching { recorder.operation { throw CancellationException("command") } }.exceptionOrNull() is IOException)
        AppLog.i("still alive")
        assertTrue(recorder.snapshot().readText().contains("still alive"))
    }

    @Test
    fun writerCancellationCompletesRunningAndQueuedCallers() = runBlocking {
        val recorder = recorder()
        val started = CompletableDeferred<Unit>()
        val running = async { runCatching { recorder.operation { started.complete(Unit); awaitCancellation() } } }
        started.await()
        val queued = async { runCatching { recorder.refresh() } }
        job.cancelAndJoin()
        withTimeout(5000) {
            assertTrue(running.await().exceptionOrNull() is IOException)
            assertTrue(queued.await().exceptionOrNull() is IOException)
            assertTrue(runCatching { recorder.refresh() }.exceptionOrNull() is IOException)
        }
    }
}