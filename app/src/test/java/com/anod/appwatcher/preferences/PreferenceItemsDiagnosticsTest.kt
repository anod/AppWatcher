package com.anod.appwatcher.preferences

import android.app.Application
import android.content.Context
import android.text.format.Formatter
import androidx.test.core.app.ApplicationProvider
import com.anod.appwatcher.R
import com.anod.appwatcher.accounts.AuthAccount
import com.anod.appwatcher.diagnostics.DiagnosticAccounts
import com.anod.appwatcher.diagnostics.DiagnosticAllowlist
import com.anod.appwatcher.diagnostics.DiagnosticStorageState
import info.anodsplace.compose.ActionablePreferenceItem
import info.anodsplace.compose.CheckablePreferenceItem
import info.anodsplace.notification.NotificationManager
import info.anodsplace.playservices.GooglePlayServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [35])
class PreferenceItemsDiagnosticsTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
    private val prefs = Preferences(
        context, NotificationManager.NoOp(), scope,
        DiagnosticAllowlist(setOf(DiagnosticAccounts.hash("diagnostic-account")))
    )
    private val retainedWithError = DiagnosticStorageState(bytes = 2048, oldest = 1_000, error = true)

    @After
    fun tearDown() {
        scope.cancel()
    }

    private fun account(name: String) = AuthAccount(name, "account-type", "", "", "")

    private fun diagnosticItems(storage: DiagnosticStorageState = DiagnosticStorageState()): List<ActionablePreferenceItem> =
        preferenceItems(prefs, inProgress = false, GooglePlayServices(context), context, storage)
            .filterIsInstance<ActionablePreferenceItem>()
            .filter { it.key.startsWith("diagnostics-") || it.titleRes == R.string.diagnostics_title }

    private fun recordSwitch(storage: DiagnosticStorageState): ActionablePreferenceItem =
        diagnosticItems(storage).single { it.key == "diagnostics-enabled" }

    private fun enableRecording() {
        prefs.account = account("diagnostic-account")
        prefs.isDiagnosticsEnabled = true
    }

    private fun stats(oldest: String, bytes: Long, dropped: Long, writeFailures: Long) = context.getString(
        R.string.diagnostics_retained_summary, oldest, Formatter.formatFileSize(context, bytes), dropped, writeFailures
    )

    @Test
    fun sectionIsHiddenUntilDiagnosticAccountIsDetectedEvenWithRetainedLogsOrErrors() {
        assertTrue(diagnosticItems(retainedWithError).isEmpty())

        prefs.account = account("other-account")
        assertTrue(diagnosticItems(retainedWithError).isEmpty())

        prefs.account = account("diagnostic-account")
        assertFalse(diagnosticItems(retainedWithError).isEmpty())

        prefs.account = null
        assertTrue(diagnosticItems(retainedWithError).isEmpty())
    }

    @Test
    fun detectedDiagnosticAccountShowsOnlyTheRecordSwitchWithoutSummaryWhenNothingIsStored() {
        prefs.account = account("diagnostic-account")

        val items = diagnosticItems()

        assertEquals(listOf(R.string.diagnostics_title, R.string.diagnostics_record), items.map { it.titleRes })
        val record = items.single { it.key == "diagnostics-enabled" }
        assertTrue(record.enabled)
        assertEquals(0, record.summaryRes)
        assertEquals("", record.summary)
    }

    @Test
    fun enabledRecordingShowsStorageStatsInTheSwitchSummary() {
        enableRecording()

        val record = recordSwitch(DiagnosticStorageState(bytes = 2048, oldest = 1_000, dropped = 3, writeFailures = 2))

        assertTrue((record as CheckablePreferenceItem).checked)
        assertEquals(stats("1970-01-01T00:00:01Z", 2048, 3, 2), record.summary)
    }

    @Test
    fun enabledRecordingWithoutLogsShowsNeverAsOldest() {
        enableRecording()

        assertEquals(stats(context.getString(R.string.never), 0, 0, 0), recordSwitch(DiagnosticStorageState()).summary)
    }

    @Test
    fun storageErrorIsAppendedToTheSummary() {
        enableRecording()

        assertEquals(
            stats("1970-01-01T00:00:01Z", 2048, 0, 0) + "\n" + context.getString(R.string.diagnostics_error),
            recordSwitch(retainedWithError).summary
        )
    }

    @Test
    fun retainedLogsStayVisibleWhileRecordingIsOff() {
        prefs.account = account("diagnostic-account")

        val record = recordSwitch(DiagnosticStorageState(bytes = 2048, oldest = 1_000))

        assertFalse((record as CheckablePreferenceItem).checked)
        assertEquals(stats("1970-01-01T00:00:01Z", 2048, 0, 0), record.summary)
    }
}