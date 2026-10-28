package com.anod.appwatcher.preferences

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.anod.appwatcher.R
import com.anod.appwatcher.accounts.AuthAccount
import com.anod.appwatcher.diagnostics.DiagnosticAccounts
import com.anod.appwatcher.diagnostics.DiagnosticAllowlist
import com.anod.appwatcher.diagnostics.DiagnosticStorageState
import info.anodsplace.compose.ActionablePreferenceItem
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
    fun detectedDiagnosticAccountShowsSectionWithoutDescription() {
        prefs.account = account("diagnostic-account")

        val items = diagnosticItems()

        assertEquals(
            listOf(
                R.string.diagnostics_title,
                R.string.diagnostics_record,
                R.string.diagnostics_retained,
                R.string.diagnostics_share,
                R.string.diagnostics_delete
            ),
            items.map { it.titleRes }
        )
        val record = items.single { it.key == "diagnostics-enabled" }
        assertTrue(record.enabled)
        assertEquals(0, record.summaryRes)
        assertEquals("", record.summary)
        assertFalse(items.single { it.key == "diagnostics-share" }.enabled)
    }

    @Test
    fun retainedLogsEnableShareAndStorageErrorIsListed() {
        prefs.account = account("diagnostic-account")

        val items = diagnosticItems(retainedWithError)

        assertTrue(items.single { it.key == "diagnostics-share" }.enabled)
        assertTrue(items.any { it.key == "diagnostics-error" })
    }
}