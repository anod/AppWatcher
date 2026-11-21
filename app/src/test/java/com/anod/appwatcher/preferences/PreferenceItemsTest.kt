package com.anod.appwatcher.preferences

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.anod.appwatcher.BuildConfig
import com.anod.appwatcher.R
import com.anod.appwatcher.accounts.AuthAccount
import com.anod.appwatcher.diagnostics.DiagnosticAccounts
import com.anod.appwatcher.diagnostics.DiagnosticAllowlist
import com.anod.appwatcher.diagnostics.DiagnosticStorageState
import com.anod.appwatcher.model.Filters
import info.anodsplace.compose.ActionablePreferenceItem
import info.anodsplace.compose.CheckablePreferenceItem
import info.anodsplace.compose.PreferenceItem
import info.anodsplace.compose.SingleValuePreferenceItem
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
class PreferenceItemsTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
    private val notifications = TestNotificationManager()
    private val prefs = Preferences(
        context, notifications, scope,
        DiagnosticAllowlist(setOf(DiagnosticAccounts.hash("diagnostic-account")))
    )

    @After
    fun tearDown() {
        scope.cancel()
    }

    private fun items() = preferenceItems(prefs, false, GooglePlayServices(context), context, DiagnosticStorageState())

    private fun actionable(key: String) = items().filterIsInstance<ActionablePreferenceItem>().single { it.key == key }

    private fun checked(key: String) = (actionable(key) as CheckablePreferenceItem).checked

    private fun value(key: String) = (actionable(key) as SingleValuePreferenceItem).value

    private inline fun <reified T : PreferenceItem> typed(key: String): T =
        items().filterIsInstance<T>().single { (it as ActionablePreferenceItem).key == key }

    private fun describe(item: PreferenceItem) = when (item) {
        is PreferenceItem.Category -> "Category:${context.resources.getResourceEntryName(item.titleRes)}"
        else -> "${item::class.simpleName}:${(item as ActionablePreferenceItem).key}"
    }

    private val debugItems = if (BuildConfig.DEBUG) listOf("Text:test-notification", "Text:db-cleanup") else emptyList()

    @Test
    fun listsEverySettingUnderItsCategoryWithTheControlItsHandlerExpects() {
        assertEquals(
            listOf(
                "Category:category_updates",
                "Pick:update_frequency",
                "Switch:wifi_only",
                "Switch:requires-charging",
                "Text:refresh-history",
                "Category:settings_notifications",
                "Switch:notify_installed_uptodate",
                "Switch:notify-installed",
                "Switch:notify-no-changes",
                "Category:pref_header_drive_sync",
                "Switch:drive_sync",
                "Text:drive-sync-now",
                "Category:pref_header_backup",
                "Text:export",
                "Text:import",
                "Category:pref_header_interface",
                "Pick:theme",
                "Switch:show-recent",
                "Switch:show-on-device",
                "Switch:show-recently-updated",
                "Pick:default-filter",
                "Switch:pull-to-refresh",
                "Placeholder:icon-style",
                "Category:pref_privacy",
                "Switch:crash-reports",
                "Category:pref_header_about",
                "Text:about",
                "Text:licenses",
                "Text:user-log"
            ) + debugItems,
            items().map(::describe)
        )
    }

    @Test
    fun diagnosticsSectionIsAddedAfterAboutWhenTheAccountIsAllowed() {
        prefs.account = AuthAccount("diagnostic-account", "account-type", "", "", "")

        assertEquals(
            listOf("Text:user-log", "Category:diagnostics_title", "Switch:diagnostics-enabled") + debugItems,
            items().map(::describe).takeLast(3 + debugItems.size)
        )
    }

    @Test
    fun everySettingHasATitle() {
        items().filterIsInstance<ActionablePreferenceItem>().forEach { item ->
            assertTrue("${item.key} has no title", item.titleRes != 0 || item.title.isNotEmpty())
        }
    }

    @Test
    fun actionRowsAreEnabled() {
        listOf("refresh-history", "export", "import", "about", "licenses", "user-log").forEach { key ->
            assertTrue("$key is disabled", actionable(key).enabled)
            assertTrue("$key is not clickable", actionable(key).clickable)
        }
    }

    @Test
    fun switchesShowTheStoredValues() {
        class SwitchCase(val key: String, val default: Boolean, val write: (Preferences, Boolean) -> Unit)

        listOf(
            SwitchCase("wifi_only", false) { p, v -> p.isWifiOnly = v },
            SwitchCase("requires-charging", false) { p, v -> p.isRequiresCharging = v },
            SwitchCase("notify_installed_uptodate", true) { p, v -> p.isNotifyInstalledUpToDate = v },
            SwitchCase("notify-installed", true) { p, v -> p.isNotifyInstalled = v },
            SwitchCase("notify-no-changes", true) { p, v -> p.isNotifyNoChanges = v },
            SwitchCase("show-recent", true) { p, v -> p.showRecent = v },
            SwitchCase("show-on-device", false) { p, v -> p.showOnDevice = v },
            SwitchCase("show-recently-updated", true) { p, v -> p.showRecentlyDiscovered = v },
            SwitchCase("pull-to-refresh", true) { p, v -> p.enablePullToRefresh = v },
            SwitchCase("crash-reports", true) { p, v -> p.collectCrashReports = v }
        ).forEach { case ->
            assertEquals("${case.key} default", case.default, checked(case.key))

            case.write(prefs, !case.default)
            assertEquals("${case.key} changed", !case.default, checked(case.key))

            case.write(prefs, case.default)
            assertEquals("${case.key} restored", case.default, checked(case.key))
        }
    }

    @Test
    fun frequencyPickOffersEverySchedule() {
        val pick = typed<PreferenceItem.Pick>("update_frequency")
        val schedules = context.resources.getStringArray(pick.entryValuesRes).toList()

        assertEquals(listOf("0", "3600", "7200", "10800", "21600", "43200"), schedules)
        assertEquals(schedules.size, context.resources.getStringArray(pick.entriesRes).size)
        schedules.forEach { seconds ->
            prefs.updatesFrequency = seconds.toInt()

            assertEquals(seconds, value("update_frequency"))
        }
    }

    @Test
    fun frequencyIsLockedToManualWhileNotificationsAreDisabled() {
        prefs.updatesFrequency = 3600
        notifications.areNotificationsEnabled = false

        assertFalse(actionable("update_frequency").enabled)
        assertEquals("0", value("update_frequency"))
        assertFalse(actionable("wifi_only").enabled)
        assertFalse(actionable("requires-charging").enabled)
    }

    @Test
    fun networkAndChargingOptionsAreDisabledWhileUpdatesAreManual() {
        prefs.updatesFrequency = 0

        assertTrue(actionable("update_frequency").enabled)
        assertEquals("0", value("update_frequency"))
        assertFalse(actionable("wifi_only").enabled)
        assertFalse(actionable("requires-charging").enabled)
    }

    @Test
    fun networkAndChargingOptionsAreEnabledForScheduledUpdates() {
        prefs.updatesFrequency = 3600

        assertTrue(actionable("update_frequency").enabled)
        assertEquals("3600", value("update_frequency"))
        assertTrue(actionable("wifi_only").enabled)
        assertTrue(actionable("requires-charging").enabled)
    }

    @Test
    fun networkAndChargingOptionsKeepTheirValueWhileDisabled() {
        prefs.isWifiOnly = true
        prefs.isRequiresCharging = true
        prefs.updatesFrequency = 0

        assertTrue(checked("wifi_only"))
        assertTrue(checked("requires-charging"))
    }

    @Test
    fun defaultFilterPickMapsEachEntryToItsFilterId() {
        val pick = typed<PreferenceItem.Pick>("default-filter")

        assertEquals(0, pick.entryValuesRes)
        assertEquals(
            listOf(R.string.tab_all, R.string.tab_installed, R.string.tab_not_installed, R.string.tab_updatable).map(context::getString),
            context.resources.getStringArray(pick.entriesRes).toList()
        )
        listOf(Filters.ALL, Filters.INSTALLED, Filters.UNINSTALLED, Filters.UPDATABLE).forEachIndexed { index, filterId ->
            prefs.defaultMainFilterId = filterId

            assertEquals(index, filterId)
            assertEquals(filterId.toString(), value("default-filter"))
        }
    }

    @Test
    fun themePickSelectsTheStoredThemeIndex() {
        val pick = typed<PreferenceItem.Pick>("theme")

        assertEquals(0, pick.entryValuesRes)
        assertEquals(5, context.resources.getStringArray(pick.entriesRes).size)
        (0 until 5).forEach { index ->
            prefs.themeIndex = index

            assertEquals(index.toString(), value("theme"))
        }
    }

    @Test
    fun aboutShowsTheAppVersion() {
        assertEquals("${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})", actionable("about").summary)
    }

    @Test
    fun driveSyncIsDisabledAndExplainsWhyWithoutPlayServices() {
        prefs.isDriveSyncEnabled = true

        val driveSync = actionable("drive_sync")
        assertFalse(driveSync.enabled)
        assertFalse((driveSync as CheckablePreferenceItem).checked)
        assertEquals(0, driveSync.summaryRes)
        assertTrue(driveSync.summary.isNotEmpty())
        assertEquals(GooglePlayServices(context).availabilityMessage, driveSync.summary)

        val syncNow = actionable("drive-sync-now")
        assertFalse(syncNow.enabled)
        assertEquals("", syncNow.summary)
    }
}