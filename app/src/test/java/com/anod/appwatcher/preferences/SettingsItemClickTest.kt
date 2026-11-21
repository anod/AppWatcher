package com.anod.appwatcher.preferences

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.anod.appwatcher.diagnostics.DiagnosticAllowlist
import com.anod.appwatcher.model.Filters
import info.anodsplace.compose.PreferenceItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [35])
class SettingsItemClickTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
    private val prefs = Preferences(context, TestNotificationManager(), scope, DiagnosticAllowlist(emptySet()))
    private val events = mutableListOf<SettingsViewEvent>()

    @After
    fun tearDown() {
        scope.cancel()
    }

    private fun click(item: PreferenceItem) = onSettingsItemClick(prefs, item) { events += it }

    private fun toggle(key: String, checked: Boolean) = click(PreferenceItem.Switch(checked = checked, key = key))

    private fun pick(key: String, value: String) = click(PreferenceItem.Pick(value = value, key = key))

    private inline fun <reified T : SettingsViewEvent> onlyEvent(): T {
        val event = events.single()
        assertTrue("Unexpected event ${event::class.simpleName}", event is T)
        return event as T
    }

    @Test
    fun frequencyChoiceKeepsTheStoredNetworkAndChargingOptions() {
        listOf(true to false, false to true).forEach { (wifiOnly, requiresCharging) ->
            events.clear()
            prefs.isWifiOnly = wifiOnly
            prefs.isRequiresCharging = requiresCharging

            pick("update_frequency", "10800")

            val event = onlyEvent<SettingsViewEvent.ChangeUpdatePolicy>()
            assertEquals(10800, event.frequency)
            assertEquals(wifiOnly, event.isWifiOnly)
            assertEquals(requiresCharging, event.isRequiresCharging)
        }
        assertEquals(7200, prefs.updatesFrequency)
    }

    @Test
    fun wifiOnlyToggleKeepsTheStoredFrequencyAndChargingOption() {
        prefs.updatesFrequency = 3600
        prefs.isRequiresCharging = true

        listOf(true, false).forEach { checked ->
            events.clear()

            toggle("wifi_only", checked)

            val event = onlyEvent<SettingsViewEvent.ChangeUpdatePolicy>()
            assertEquals(3600, event.frequency)
            assertEquals(checked, event.isWifiOnly)
            assertTrue(event.isRequiresCharging)
        }
        assertFalse(prefs.isWifiOnly)
    }

    @Test
    fun chargingToggleKeepsTheStoredFrequencyAndWifiOption() {
        prefs.updatesFrequency = 21600
        prefs.isWifiOnly = true

        listOf(true, false).forEach { checked ->
            events.clear()

            toggle("requires-charging", checked)

            val event = onlyEvent<SettingsViewEvent.ChangeUpdatePolicy>()
            assertEquals(21600, event.frequency)
            assertTrue(event.isWifiOnly)
            assertEquals(checked, event.isRequiresCharging)
        }
        assertFalse(prefs.isRequiresCharging)
    }

    @Test
    fun notificationSwitchesAreWrittenStraightToPreferences() {
        val readers = mapOf<String, (Preferences) -> Boolean>(
            "notify_installed_uptodate" to { it.isNotifyInstalledUpToDate },
            "notify-installed" to { it.isNotifyInstalled },
            "notify-no-changes" to { it.isNotifyNoChanges }
        )

        readers.forEach { (key, read) ->
            toggle(key, false)
            assertFalse("$key off", read(prefs))
            assertTrue("$key leaves the others on", readers.filterKeys { it != key }.values.all { it(prefs) })

            toggle(key, true)
            assertTrue("$key on", read(prefs))
        }
        assertTrue(events.isEmpty())
    }

    @Test
    fun defaultFilterChoiceIsWrittenStraightToPreferences() {
        listOf(Filters.UPDATABLE, Filters.UNINSTALLED, Filters.INSTALLED, Filters.ALL).forEach { filterId ->
            pick("default-filter", filterId.toString())

            assertEquals(filterId, prefs.defaultMainFilterId)
        }
        assertTrue(events.isEmpty())
    }

    @Test
    fun crashReportsSwitchRequestsTheChange() {
        listOf(false, true).forEach { checked ->
            events.clear()

            toggle("crash-reports", checked)

            assertEquals(checked, onlyEvent<SettingsViewEvent.UpdateCrashReports>().checked)
        }
        assertTrue(prefs.collectCrashReports)
    }

    @Test
    fun diagnosticsSwitchRequestsTheChange() {
        toggle("diagnostics-enabled", true)
        toggle("diagnostics-enabled", false)

        assertEquals(
            listOf(SettingsViewEvent.DiagnosticToggle(true), SettingsViewEvent.DiagnosticToggle(false)),
            events
        )
    }

    @Test
    fun driveSyncSwitchRequestsTheChange() {
        listOf(true, false).forEach { checked ->
            events.clear()

            toggle("drive_sync", checked)

            assertEquals(checked, onlyEvent<SettingsViewEvent.GDriveSyncToggle>().checked)
        }
        assertFalse(prefs.isDriveSyncEnabled)
    }

    @Test
    fun driveSyncNowRequestsASync() {
        click(PreferenceItem.Text(key = "drive-sync-now"))

        assertSame(SettingsViewEvent.GDriveSyncNow, events.single())
    }

    @Test
    fun themeChoiceRequestsTheSelectedThemeIndex() {
        (0 until 5).forEach { index ->
            events.clear()

            pick("theme", index.toString())

            assertEquals(index, onlyEvent<SettingsViewEvent.UpdateTheme>().newTheme)
        }
        assertEquals(0, prefs.themeIndex)
    }

    private class ListPreference(val key: String, val default: Boolean, val read: (Preferences) -> Boolean)

    private val listPreferences = listOf(
        ListPreference("show-recent", true) { it.showRecent },
        ListPreference("show-on-device", false) { it.showOnDevice },
        ListPreference("show-recently-updated", true) { it.showRecentlyDiscovered },
        ListPreference("pull-to-refresh", true) { it.enablePullToRefresh }
    )

    @Test
    fun listSwitchesRequestAnUpdateThatOnlyChangesTheirOwnPreference() {
        listPreferences.forEach { preference ->
            events.clear()

            toggle(preference.key, !preference.default)

            val event = onlyEvent<SettingsViewEvent.UpdateListPreference>()
            assertEquals("${preference.key} requested value", !preference.default, event.checked)
            assertEquals("${preference.key} before the update runs", preference.default, preference.read(prefs))

            event.update(event.checked)

            listPreferences.forEach { other ->
                val expected = if (other === preference) !other.default else other.default
                assertEquals("${other.key} after changing ${preference.key}", expected, other.read(prefs))
            }
            event.update(preference.default)
        }
    }

    @Test
    fun debugActionsRequestTheirOperation() {
        click(PreferenceItem.Text(key = "test-notification"))
        click(PreferenceItem.Text(key = "db-cleanup"))

        assertEquals(listOf<SettingsViewEvent>(SettingsViewEvent.TestNotification, SettingsViewEvent.DbCleanup), events)
    }

    @Test
    fun itemsWithoutAHandlerAreIgnored() {
        click(PreferenceItem.Text(key = "about"))
        click(PreferenceItem.Text(key = "unknown"))
        click(PreferenceItem.Switch(checked = true, key = "unknown"))

        assertTrue(events.isEmpty())
        assertEquals(7200, prefs.updatesFrequency)
        assertTrue(prefs.isNotifyInstalled)
    }
}