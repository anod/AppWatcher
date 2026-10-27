package com.anod.appwatcher.diagnostics

import android.app.Application
import android.app.Notification
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.anod.appwatcher.accounts.AuthAccount
import com.anod.appwatcher.accounts.PlaySessionCoordinator
import com.anod.appwatcher.createAppModule
import com.anod.appwatcher.preferences.Preferences
import info.anodsplace.notification.NotificationManager
import java.io.File
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.dsl.koinApplication
import org.koin.dsl.module
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
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var selected: String? = "diagnostic-account"
    private var persisted: String? = null
    private val consent = DiagnosticConsent(
        { selected }, DiagnosticAccounts.hash("diagnostic-account"), { persisted }, { persisted = it }
    )

    @After
    fun tearDown() {
        scope.cancel()
        root.deleteRecursively()
    }

    @Test
    fun disabledRecordsAreIgnoredAndRetainedFilesCanBeExportedAndDeleted() = runBlocking {
        val now = System.currentTimeMillis()
        val store = DiagnosticFileStore(File(context.noBackupFilesDir, "diagnostics"))
        store.append(DiagnosticRecord(DiagnosticEvent.WORK_START).encode(now, UUID.randomUUID()), now)
        val recorder = DiagnosticRecorder(context, consent, scope)
        recorder.refresh()
        val originalBytes = recorder.state.value.bytes
        recorder.record(DiagnosticRecord(DiagnosticEvent.WORK_END))
        recorder.refresh()
        assertEquals(originalBytes, recorder.state.value.bytes)

        selected = "not-allowed"
        val snapshot = recorder.snapshot()
        // AndroidX FileProvider's slash-based canonical-path check cannot run on Windows.
        // Exercise the real snapshot and intent separately; manifest restricts the Android URI root.
        val chooser = diagnosticShareIntent(Uri.parse("content://${context.packageName}.diagnostics/diagnostic_snapshots/${snapshot.name}"))
        val send = chooser.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)!!
        assertEquals(Intent.ACTION_SEND, send.action)
        assertTrue(send.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
        assertFalse(send.hasExtra(Intent.EXTRA_TEXT))
        val uri = send.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)!!
        assertEquals("content", uri.scheme)
        assertTrue(uri.path!!.startsWith("/diagnostic_snapshots/"))
        assertEquals(uri, send.clipData!!.getItemAt(0).uri)
        assertTrue(File(context.cacheDir, "diagnostic-exports").listFiles()!!.single().readText().contains("WORK_START"))
        recorder.delete()
        assertEquals(0L, recorder.state.value.bytes)
        assertTrue(File(context.cacheDir, "diagnostic-exports").listFiles()!!.isEmpty())
    }

    @Test
    fun boundedQueueReportsDropsWithoutRecursiveLogging() {
        scope.cancel()
        consent.setEnabled(true)
        val recorder = DiagnosticRecorder(context, consent, scope)
        repeat(140) { recorder.record(DiagnosticRecord(DiagnosticEvent.STAGE)) }
        assertEquals(12L, recorder.state.value.dropped)
    }

    @Test
    fun enablingCapturesStartupMetadataAndDisablingImmediatelyStopsWrites() = runBlocking {
        val recorder = DiagnosticRecorder(context, consent, scope)
        recorder.setEnabled(true)
        recorder.refresh()
        val startup = recorder.snapshot().readText()
        assertTrue(startup.contains("\"event\":\"PROCESS_START\""))
        assertTrue(startup.contains("\"code\":35"))
        assertTrue(startup.contains("\"event\":\"POWER_STATE\""))
        assertFalse(startup.contains("diagnostic-account"))
        recorder.setEnabled(false)
        val bytes = recorder.state.value.bytes
        repeat(5) { recorder.record(DiagnosticRecord(DiagnosticEvent.WORK_START)) }
        recorder.refresh()
        assertEquals(bytes, recorder.state.value.bytes)
    }

    @Test
    fun productionDependencyGraphSharesThePreferencesGate() {
        val container = koinApplication {
            modules(module {
                single<Context> { context }
                single<Application> { app as Application }
            }, createAppModule())
        }
        try {
            val recorder = container.koin.get<DiagnosticRecorder>()
            assertTrue(recorder.consent === container.koin.get<Preferences>().diagnosticConsent)
            container.koin.get<PlaySessionCoordinator>()
        } finally {
            container.koin.get<CoroutineScope>().cancel()
            container.close()
        }
    }

    @Test
    fun preferencesAccountSetterAndBackgroundSaveRevokeConsentPersistently() = runBlocking {
        val manager = object : NotificationManager {
            override val areNotificationsEnabled = true
            override fun notify(notificationId: Int, notification: Notification) = Unit
            override fun cancel(notificationId: Int) = Unit
        }
        fun preferences() = Preferences(context, manager, scope, DiagnosticAccounts.hash("diagnostic-account"))
        val prefs = preferences()
        prefs.account = AuthAccount("diagnostic-account", "account-type", "", "", "")
        assertFalse(prefs.diagnosticConsent.enabled)
        prefs.diagnosticConsent.setEnabled(true)
        assertTrue(preferences().diagnosticConsent.enabled)
        prefs.account = AuthAccount("not-allowed", "account-type", "", "", "")
        assertFalse(prefs.diagnosticConsent.enabled)
        assertFalse(preferences().diagnosticConsent.enabled)
        prefs.account = AuthAccount("diagnostic-account", "account-type", "", "", "")
        assertFalse(prefs.diagnosticConsent.enabled)
        prefs.diagnosticConsent.setEnabled(true)
        assertTrue(prefs.saveAccount(AuthAccount("not-allowed", "account-type", "", "", ""), null, null, null))
        assertFalse(preferences().diagnosticConsent.enabled)
    }

    @Test
    fun storageFailureIsVisibleAndDeleteFailureIsReported() = runBlocking {
        val location = File(context.noBackupFilesDir, "diagnostics")
        location.writeText("not a directory")
        val recorder = DiagnosticRecorder(context, consent, scope)
        val result = runCatching { recorder.refresh() }
        assertTrue(result.isFailure)
        assertTrue(recorder.state.value.error)
        assertEquals(1L, recorder.state.value.writeFailures)
        assertTrue(runCatching { recorder.delete() }.isFailure)
        assertEquals(2L, recorder.state.value.writeFailures)
    }

    @Test
    fun exportFailureIsVisibleWithoutDiscardingRetainedLogs() = runBlocking {
        val now = System.currentTimeMillis()
        DiagnosticFileStore(File(context.noBackupFilesDir, "diagnostics"))
            .append(DiagnosticRecord(DiagnosticEvent.WORK_START).encode(now, UUID.randomUUID()), now)
        val recorder = DiagnosticRecorder(context, consent, scope)
        recorder.refresh()
        val bytes = recorder.state.value.bytes
        File(context.cacheDir, "diagnostic-exports").writeText("not a directory")
        assertTrue(runCatching { recorder.snapshot() }.isFailure)
        assertTrue(recorder.state.value.error)
        assertEquals(1L, recorder.state.value.writeFailures)
        assertEquals(bytes, recorder.state.value.bytes)
    }

    @Test
    fun cachedExportsAreBoundedAndExpireWithoutEnablingRecording() = runBlocking {
        val now = System.currentTimeMillis()
        DiagnosticFileStore(File(context.noBackupFilesDir, "diagnostics"))
            .append(DiagnosticRecord(DiagnosticEvent.WORK_START).encode(now, UUID.randomUUID()), now)
        val recorder = DiagnosticRecorder(context, consent, scope)
        repeat(5) { recorder.snapshot() }
        val directory = File(context.cacheDir, "diagnostic-exports")
        assertEquals(3, directory.listFiles()!!.size)
        directory.listFiles()!!.forEach { assertTrue(it.setLastModified(now - 25L * 60 * 60 * 1000)) }
        recorder.refresh()
        assertTrue(directory.listFiles()!!.isEmpty())
        assertTrue(recorder.state.value.bytes > 0)
        assertFalse(consent.enabled)
        val largeExport = ByteArray(6 * 1024 * 1024)
        File(directory, "old-a.jsonl").writeBytes(largeExport)
        File(directory, "old-b.jsonl").writeBytes(largeExport)
        val latest = recorder.snapshot()
        assertTrue(latest.isFile)
        assertTrue(directory.listFiles()!!.sumOf { it.length() } <= DiagnosticFileStore.MAX_BYTES)
    }

    @Test
    fun diagnosticFailureNeverStrandsSessionLockOrReplacesActionOutcome() = runBlocking {
        val failingConsent = DiagnosticConsent({ error("diagnostic gate unavailable") }, "", { null }, { })
        val recorder = DiagnosticRecorder(context, failingConsent, scope)
        val coordinator = PlaySessionCoordinator(recorder)

        assertEquals(7, coordinator.withSession(owner = "first") { 7 })
        val failure = runCatching { coordinator.withSession(owner = "failing") { throw IllegalArgumentException("action") } }
        assertTrue(failure.exceptionOrNull() is IllegalArgumentException)
        assertEquals("holder=none", coordinator.describeHolder())
        assertEquals(9, coordinator.withUserInitiatedSession { 9 })
        assertTrue(recorder.state.value.error)
    }

    @Test
    fun cancelledMetadataFutureDoesNotStopTheRecorder() = runBlocking {
        val recorder = DiagnosticRecorder(context, consent, scope, workInfos = { throw CancellationException("work future cancelled") })
        recorder.setEnabled(true)
        recorder.refresh()
        val snapshot = recorder.snapshot().readText()
        assertTrue(snapshot, snapshot.contains("\"event\":\"SNAPSHOT_UNAVAILABLE\",\"code\":4"))
        assertTrue(snapshot, snapshot.contains("\"event\":\"SNAPSHOT_UNAVAILABLE\",\"code\":5"))
        recorder.refresh()
    }

    @Test
    fun cancelledCommandFailsOnlyThatCommand() = runBlocking {
        val recorder = DiagnosticRecorder(context, consent, scope)
        val failure = runCatching { recorder.operation { throw CancellationException("not the writer") } }
        assertTrue(failure.exceptionOrNull() is IOException)
        assertTrue(recorder.state.value.error)
        recorder.refresh()
    }

    @Test
    fun writerScopeCancellationCompletesRunningAndQueuedCallers() = runBlocking {
        val writerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val recorder = DiagnosticRecorder(context, consent, writerScope)
        val started = CompletableDeferred<Unit>()
        val running = async { runCatching { recorder.operation { started.complete(Unit); awaitCancellation() } } }
        started.await()
        val queued = async { runCatching { recorder.refresh() } }
        writerScope.cancel()
        withTimeout(5000) {
            assertTrue(running.await().exceptionOrNull() is IOException)
            assertTrue(queued.await().exceptionOrNull() is IOException)
            assertTrue(runCatching { recorder.refresh() }.exceptionOrNull() is IOException)
        }
    }}