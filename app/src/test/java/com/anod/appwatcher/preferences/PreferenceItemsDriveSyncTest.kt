package com.anod.appwatcher.preferences

import android.app.Application
import android.content.Context
import android.text.format.DateUtils
import androidx.test.core.app.ApplicationProvider
import com.anod.appwatcher.R
import com.anod.appwatcher.diagnostics.DiagnosticAllowlist
import com.anod.appwatcher.diagnostics.DiagnosticStorageState
import info.anodsplace.compose.ActionablePreferenceItem
import info.anodsplace.compose.CheckablePreferenceItem
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
@Config(application = Application::class, sdk = [35], shadows = [ShadowSupportedPlayServices::class])
class PreferenceItemsDriveSyncTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
    private val prefs = Preferences(context, TestNotificationManager(), scope, DiagnosticAllowlist(emptySet()))
    private val syncedPrefix = context.getString(R.string.pref_descr_drive_sync_now, "")

    @After
    fun tearDown() {
        scope.cancel()
    }

    private fun item(key: String, inProgress: Boolean = false) =
        preferenceItems(prefs, inProgress, GooglePlayServices(context), context, DiagnosticStorageState())
            .filterIsInstance<ActionablePreferenceItem>()
            .single { it.key == key }

    @Test
    fun driveSyncSwitchIsAvailableAndFollowsThePreference() {
        val off = item("drive_sync")
        assertTrue(off.enabled)
        assertFalse((off as CheckablePreferenceItem).checked)
        assertEquals(R.string.pref_descr_drive_sync_enabled, off.summaryRes)
        assertEquals("", off.summary)

        prefs.isDriveSyncEnabled = true

        assertTrue((item("drive_sync") as CheckablePreferenceItem).checked)
    }

    @Test
    fun syncNowIsAvailableOnlyWhileDriveSyncIsEnabledAndIdle() {
        assertFalse(item("drive-sync-now").enabled)

        prefs.isDriveSyncEnabled = true

        assertTrue(item("drive-sync-now").enabled)
        assertTrue(item("drive-sync-now").clickable)
        assertFalse(item("drive-sync-now", inProgress = true).enabled)
    }

    @Test
    fun syncNowSaysTheListWasNeverSyncedUntilItIs() {
        assertEquals(context.getString(R.string.pref_descr_drive_sync_now, context.getString(R.string.never)), item("drive-sync-now").summary)
    }

    @Test
    fun syncNowShowsWhenTheListWasLastSynced() {
        prefs.lastDriveSyncTime = System.currentTimeMillis() - 3 * DateUtils.HOUR_IN_MILLIS

        val summary = item("drive-sync-now").summary

        assertTrue(summary, summary.startsWith(syncedPrefix))
        assertFalse(summary, summary.contains(context.getString(R.string.never)))
    }
}