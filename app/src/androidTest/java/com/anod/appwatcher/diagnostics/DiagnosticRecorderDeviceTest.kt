package com.anod.appwatcher.diagnostics

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.anod.appwatcher.accounts.AuthAccount
import com.anod.appwatcher.preferences.Preferences
import info.anodsplace.applog.AppLog
import info.anodsplace.notification.NotificationManager
import java.io.File
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@RunWith(AndroidJUnit4::class)
class DiagnosticRecorderDeviceTest {
    @Test
    fun recordsLogsAndSharesFileProviderSnapshotOnDevice() = runBlocking {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val id = UUID.randomUUID().toString()
        val root = File(appContext.cacheDir, "diagnostic-device-test-$id")
        val context = IsolatedContext(appContext, "diagnostic-device-test-$id", File(root, "no-backup"))
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val originalLogger = AppLog.logger
        val account = AuthAccount("device-test-$id@example.invalid", "test", "", "", "")
        val prefs = Preferences(
            context,
            NotificationManager.NoOp(),
            scope,
            DiagnosticAllowlist(setOf(DiagnosticAccounts.hash(account.name)))
        ).apply {
            this.account = account
        }
        val recorder = DiagnosticRecorder(context, prefs, scope, WorkInfoSourceHolder().source)

        try {
            assertTrue(recorder.setEnabled(true))
            AppLog.i("physical-device diagnostic marker $id")
            assertTrue(recorder.captureProcessState())

            val recent = recorder.recentLines(100)
            assertNotNull(recent)
            assertTrue(recent.toString(), recent!!.any { it.contains("physical-device diagnostic marker $id") })

            val chooser = recorder.shareIntent()
            assertNotNull(chooser)
            @Suppress("DEPRECATION")
            val sendIntent = chooser!!.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)!!
            assertEquals(Intent.ACTION_SEND, sendIntent.action)
            assertEquals("text/plain", sendIntent.type)
            assertTrue(sendIntent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
            @Suppress("DEPRECATION")
            val uri = sendIntent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)!!
            val contents = context.contentResolver.openInputStream(uri)!!.bufferedReader().use { it.readText() }
            assertTrue(contents, contents.contains("physical-device diagnostic marker $id"))
            assertTrue(contents, contents.contains("Diagnostics started versionCode="))
            assertTrue(contents, contents.contains("Work AppRefresh"))

            assertTrue(recorder.setEnabled(false))
            assertEquals(0L, recorder.state.value.bytes)
            assertEquals(emptyList<String>(), recorder.recentLines(100))
            assertFalse(File(context.cacheDir, "diagnostic-exports/${uri.lastPathSegment}").exists())
        } finally {
            recorder.setEnabled(false)
            AppLog.logger = originalLogger
            scope.cancel()
            root.deleteRecursively()
        }
    }

    private class IsolatedContext(
        base: Context,
        private val preferencePrefix: String,
        private val isolatedNoBackupFilesDir: File
    ) : ContextWrapper(base) {
        override fun getSharedPreferences(name: String, mode: Int) =
            super.getSharedPreferences("$preferencePrefix-$name", mode)

        override fun getNoBackupFilesDir(): File = isolatedNoBackupFilesDir.apply { mkdirs() }
    }

    private class WorkInfoSourceHolder : KoinComponent {
        val source: WorkInfoSource by inject()
    }
}