package com.anod.appwatcher.diagnostics

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.anod.appwatcher.accounts.AuthAccount
import com.anod.appwatcher.preferences.Preferences
import info.anodsplace.notification.NotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
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
class DiagnosticPreferencesTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
    private val hashes = DiagnosticAccounts.hash("diagnostic-account")
    private fun preferences() = Preferences(context, NotificationManager.NoOp(), scope, DiagnosticAllowlist(setOf(hashes)))
    private fun account(name: String) = AuthAccount(name, "account-type", "", "", "")

    @After
    fun tearDown() {
        scope.cancel()
    }

    @Test
    fun consentIsOffByDefaultAndPersistsAcrossPreferencesInstances() {
        val prefs = preferences()
        prefs.account = account("diagnostic-account")
        assertTrue(prefs.isDiagnosticsAllowed)
        assertFalse(prefs.isDiagnosticsEnabled)
        prefs.isDiagnosticsEnabled = true
        assertTrue(preferences().isDiagnosticsEnabled)
        assertEquals(hashes, context.getSharedPreferences(Preferences.DEVICE_PREFS_NAME, 0).getString("diagnostic-consent", null))
        assertFalse(context.getSharedPreferences("WatcherPrefs", 0).contains("diagnostic-consent"))
        prefs.isDiagnosticsEnabled = false
        assertFalse(preferences().isDiagnosticsEnabled)
    }

    @Test
    fun accountSwitchClearsConsentAndBackgroundSaveBindsConsentToSelectedAccount() = runBlocking {
        val prefs = preferences()
        prefs.account = account("diagnostic-account")
        prefs.isDiagnosticsEnabled = true
        prefs.account = account("not-allowed")
        assertFalse(prefs.isDiagnosticsAllowed)
        assertFalse(prefs.isDiagnosticsEnabled)
        assertFalse(preferences().isDiagnosticsEnabled)
        prefs.account = account("diagnostic-account")
        assertFalse(prefs.isDiagnosticsEnabled)
        assertFalse(preferences().isDiagnosticsEnabled)
        prefs.isDiagnosticsEnabled = true
        assertTrue(prefs.saveAccount(account("not-allowed"), null, null, null))
        assertFalse(prefs.isDiagnosticsEnabled)
        assertFalse(preferences().isDiagnosticsEnabled)
    }

    @Test
    fun sameAccountCredentialRefreshPreservesConsent() = runBlocking {
        val prefs = preferences()
        prefs.account = account("diagnostic-account")
        prefs.isDiagnosticsEnabled = true
        prefs.account = account("diagnostic-account").copy(gfsId = "refreshed")
        assertTrue(prefs.isDiagnosticsEnabled)
        assertTrue(prefs.saveAccount(account("diagnostic-account"), null, null, null))
        assertTrue(preferences().isDiagnosticsEnabled)
    }

    @Test
    fun normalizationEmptyAllowlistAndLiteralInjectionValues() {
        val normalized = preferences()
        normalized.account = account("  DIAGNOSTIC-ACCOUNT ")
        assertTrue(normalized.isDiagnosticsAllowed)
        normalized.account = null
        assertFalse(normalized.isDiagnosticsAllowed)
        val digest = DiagnosticAccounts.hash("quotes\"\\\n,=injection")
        assertTrue(digest.matches(Regex("[0-9a-f]{64}")))
        val prefs = Preferences(context, NotificationManager.NoOp(), scope, DiagnosticAllowlist(emptySet()))
        prefs.account = account("diagnostic-account")
        assertFalse(prefs.isDiagnosticsAllowed)
        prefs.isDiagnosticsEnabled = true
        assertFalse(prefs.isDiagnosticsEnabled)
        prefs.account = null
        prefs.isDiagnosticsEnabled = true
        assertFalse(prefs.isDiagnosticsEnabled)
    }
}