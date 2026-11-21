package com.anod.appwatcher.preferences

import android.app.Application
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import com.anod.appwatcher.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.robolectric.annotation.Config

@Config(application = Application::class, sdk = [35], qualifiers = "w411dp-h891dp", shadows = [ShadowSupportedPlayServices::class])
class SettingsScreenDriveSyncTest : SettingsScreenTestBase() {
    @Test
    fun driveSyncIsOfferedAndStartsOff() {
        render()

        row("drive_sync").assertIsEnabled().assert(hasText(context.getString(R.string.pref_descr_drive_sync_enabled)))
        assertSwitch("drive_sync", false)
    }

    @Test
    fun turningDriveSyncOnAsksTheViewModelToEnableIt() {
        render()

        row("drive_sync").performClick()

        assertTrue(onlyEvent<SettingsViewEvent.GDriveSyncToggle>().checked)
        assertFalse("The view model applies the choice", prefs.isDriveSyncEnabled)
    }

    @Test
    fun turningDriveSyncOffAsksTheViewModelToDisableIt() {
        prefs.isDriveSyncEnabled = true
        render()
        assertSwitch("drive_sync", true)

        switchOf("drive_sync").performClick()

        assertFalse(onlyEvent<SettingsViewEvent.GDriveSyncToggle>().checked)
        assertTrue("The view model applies the choice", prefs.isDriveSyncEnabled)
    }

    @Test
    fun syncNowIsLockedUntilDriveSyncIsEnabled() {
        render()

        row("drive-sync-now").assertIsNotEnabled().performClick()

        assertTrue(events.isEmpty())
    }

    @Test
    fun syncNowStartsASyncOnceDriveSyncIsEnabled() {
        prefs.isDriveSyncEnabled = true
        render()

        row("drive-sync-now").assertIsEnabled().performClick()

        assertEquals(listOf<SettingsViewEvent>(SettingsViewEvent.GDriveSyncNow), events)
    }

    @Test
    fun syncNowIsLockedWhileASyncIsRunning() {
        prefs.isDriveSyncEnabled = true
        render(inProgress = true)

        row("drive-sync-now").assertIsNotEnabled().performClick()

        assertTrue(events.isEmpty())
    }

    @Test
    fun syncNowShowsWhenTheListWasLastSynced() {
        prefs.isDriveSyncEnabled = true
        prefs.lastDriveSyncTime = System.currentTimeMillis()
        render()

        val summary = summaryOf(item("drive-sync-now"))

        assertFalse(summary, summary.contains(context.getString(R.string.never)))
        row("drive-sync-now").assert(hasText(summary))
    }
}