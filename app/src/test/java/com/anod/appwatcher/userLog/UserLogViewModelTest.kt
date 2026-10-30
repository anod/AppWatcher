package com.anod.appwatcher.userLog

import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.os.Looper
import android.util.Log
import androidx.lifecycle.ViewModelStore
import androidx.test.core.app.ApplicationProvider
import com.anod.appwatcher.R
import com.anod.appwatcher.accounts.AuthAccount
import com.anod.appwatcher.diagnostics.DiagnosticAccounts
import com.anod.appwatcher.diagnostics.DiagnosticAllowlist
import com.anod.appwatcher.diagnostics.DiagnosticRecorder
import com.anod.appwatcher.preferences.Preferences
import info.anodsplace.applog.AppLog
import info.anodsplace.framework.content.ScreenCommonAction
import info.anodsplace.notification.NotificationManager
import java.io.File
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [35])
@LooperMode(LooperMode.Mode.PAUSED)
class UserLogViewModelTest {
    private val root = File("build/user-log-tests/${UUID.randomUUID()}").absoluteFile
    private val app = ApplicationProvider.getApplicationContext<Context>()
    private val context = object : ContextWrapper(app) {
        override fun getNoBackupFilesDir(): File = File(root, "no-backup").apply { mkdirs() }
        override fun getCacheDir(): File = File(root, "cache").apply { mkdirs() }
    }
    private val job = SupervisorJob()
    private val scope = CoroutineScope(job + Dispatchers.IO)
    private val original = AppLog.logger
    private val prefs = Preferences(
        context,
        NotificationManager.NoOp(),
        scope,
        DiagnosticAllowlist(setOf(DiagnosticAccounts.hash("diagnostic-account")))
    ).apply {
        account = AuthAccount("diagnostic-account", "account-type", "", "", "")
        AppLog.logger = object : AppLog.Logger {
            override fun println(priority: Int, tag: String, msg: String) { }
        }
    }
    private val viewModelStore = ViewModelStore()
    private lateinit var recorder: DiagnosticRecorder

    @Before
    fun setUp() {
        recorder = DiagnosticRecorder(context, prefs, scope, workInfos = { emptyList() })
        startKoin {
            modules(module {
                single<Context> { context }
                single { prefs }
                single { recorder }
            })
        }
    }

    @After
    fun tearDown() {
        viewModelStore.clear()
        stopKoin()
        runBlocking { job.cancelAndJoin() }
        AppLog.logger = original
        root.deleteRecursively()
    }

    @Test
    fun enabledRecordingShowsStoredDiagnosticsInsteadOfLogcat() = runBlocking {
        assertTrue(recorder.setEnabled(true))
        AppLog.i("viewer entry")

        val viewModel = UserLogViewModel().also { viewModelStore.put("log", it) }
        val messages = withTimeout(5_000) {
            while (viewModel.viewStates.value.messages.isEmpty()) {
                Shadows.shadowOf(Looper.getMainLooper()).idle()
                delay(10)
            }
            viewModel.viewStates.value.messages
        }

        assertTrue(messages.any { it.level == Log.INFO && it.message.contains("viewer entry") })
    }

    @Test
    fun storageFailureShowsTheErrorToastAndNoMessages() = runBlocking {
        File(context.noBackupFilesDir, "diagnostics").writeText("not a directory")
        assertFalse(recorder.setEnabled(true))

        val viewModel = UserLogViewModel().also { viewModelStore.put("log", it) }
        val action = withTimeout(5_000) {
            val received = async { viewModel.viewActions.first() }
            while (!received.isCompleted) {
                Shadows.shadowOf(Looper.getMainLooper()).idle()
                delay(10)
            }
            received.await()
        }

        assertEquals(R.string.diagnostics_error, (action as ScreenCommonAction.ShowToast).resId)
        assertTrue(viewModel.viewStates.value.messages.isEmpty())
    }
}