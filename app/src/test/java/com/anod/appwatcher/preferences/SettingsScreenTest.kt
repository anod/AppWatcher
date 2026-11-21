package com.anod.appwatcher.preferences

import android.app.Application
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.anod.appwatcher.BuildConfig
import com.anod.appwatcher.R
import com.anod.appwatcher.model.Filters
import info.anodsplace.compose.ActionablePreferenceItem
import info.anodsplace.compose.PreferenceItem
import kotlin.reflect.KMutableProperty1
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.robolectric.annotation.Config

@Config(application = Application::class, sdk = [35], qualifiers = "w411dp-h891dp")
class SettingsScreenTest : SettingsScreenTestBase() {
    private class SwitchSetting(val key: String, val default: Boolean, val property: KMutableProperty1<Preferences, Boolean>)

    private val notificationSwitches = listOf(
        SwitchSetting("notify_installed_uptodate", true, Preferences::isNotifyInstalledUpToDate),
        SwitchSetting("notify-installed", true, Preferences::isNotifyInstalled),
        SwitchSetting("notify-no-changes", true, Preferences::isNotifyNoChanges)
    )
    private val listSwitches = listOf(
        SwitchSetting("show-recent", true, Preferences::showRecent),
        SwitchSetting("show-on-device", false, Preferences::showOnDevice),
        SwitchSetting("show-recently-updated", true, Preferences::showRecentlyDiscovered),
        SwitchSetting("pull-to-refresh", true, Preferences::enablePullToRefresh)
    )
    private val networkSwitches = listOf(
        SwitchSetting("wifi_only", false, Preferences::isWifiOnly),
        SwitchSetting("requires-charging", false, Preferences::isRequiresCharging)
    )
    private val allSwitches = notificationSwitches + listSwitches + networkSwitches +
        SwitchSetting("crash-reports", true, Preferences::collectCrashReports)

    private fun SwitchSetting.stored() = property.get(prefs)

    private fun List<SwitchSetting>.storedWithOnly(flipped: SwitchSetting) =
        map { if (it === flipped) !it.default else it.default }

    @Test
    fun everySettingIsShownWithItsTitleAndSummary() {
        render()

        items.filterIsInstance<ActionablePreferenceItem>().forEach { item ->
            val isCategory = item is PreferenceItem.Category
            val title = titleOf(item)
            scrollTo(title, ignoreCase = isCategory)
            compose.onNode(hasText(title, ignoreCase = isCategory)).assertIsDisplayed()
            val summary = summaryOf(item)
            if (summary.isNotEmpty()) {
                compose.onNode(hasText(title) and hasText(summary)).assertIsDisplayed()
            }
        }
    }

    @Test
    fun rowsAreEnabledOnlyWhenTheirItemsAre() {
        render()
        assertRowsFollowTheirItems()

        notifications.areNotificationsEnabled = false
        render()
        assertRowsFollowTheirItems()
    }

    private fun assertRowsFollowTheirItems() {
        val rows = items.filter { it is PreferenceItem.Text || it is PreferenceItem.Switch || it is PreferenceItem.Pick }
            .filterIsInstance<ActionablePreferenceItem>()
        assertTrue("Expected some disabled rows", rows.any { !it.enabled })
        rows.forEach { item ->
            val row = row(item.key)
            if (item.enabled) row.assertIsEnabled() else row.assertIsNotEnabled()
        }
    }

    @Test
    fun titleBarNamesTheScreenAndBackArrowLeavesIt() {
        render()

        compose.onNodeWithText(context.getString(R.string.navdrawer_item_settings)).assertIsDisplayed()
        compose.onNodeWithContentDescription(context.getString(R.string.back)).performClick()

        assertEquals(listOf<SettingsViewEvent>(SettingsViewEvent.NavigateBack), events)
    }

    @Test
    fun progressIndicatorIsShownOnlyWhileTheViewModelIsBusy() {
        val indicator = hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)
        render()
        compose.onAllNodes(indicator).assertCountEquals(0)

        render(inProgress = true)
        compose.onNode(indicator).assertIsDisplayed()

        render()
        compose.onAllNodes(indicator).assertCountEquals(0)
    }

    @Test
    fun switchesStartAtTheirDefaults() {
        render()

        allSwitches.forEach { assertSwitch(it.key, it.default) }
    }

    @Test
    fun switchesShowTheStoredValues() {
        allSwitches.forEach { it.property.set(prefs, !it.default) }
        render()

        allSwitches.forEach { assertSwitch(it.key, !it.default) }
    }

    @Test
    fun notificationSwitchesSaveTheChoiceThemselves() {
        render()

        notificationSwitches.forEach { setting ->
            row(setting.key).performClick()
            assertEquals(setting.key, notificationSwitches.storedWithOnly(setting), notificationSwitches.map { it.stored() })

            switchOf(setting.key).performClick()
            assertEquals(setting.key, notificationSwitches.map { it.default }, notificationSwitches.map { it.stored() })
        }
        assertTrue(events.isEmpty())
    }

    @Test
    fun listSwitchesLeaveSavingTheChoiceToTheViewModel() {
        render()

        listSwitches.forEach { setting ->
            events.clear()
            row(setting.key).performClick()

            val event = onlyEvent<SettingsViewEvent.UpdateListPreference>()
            assertEquals(setting.key, !setting.default, event.checked)
            assertEquals(setting.key, listSwitches.map { it.default }, listSwitches.map { it.stored() })

            event.update(event.checked)
            assertEquals(setting.key, listSwitches.storedWithOnly(setting), listSwitches.map { it.stored() })
            setting.property.set(prefs, setting.default)
        }
    }

    @Test
    fun crashReportsSwitchAsksTheViewModelToApplyTheChoice() {
        render()
        row("crash-reports").performClick()
        assertFalse(onlyEvent<SettingsViewEvent.UpdateCrashReports>().checked)

        events.clear()
        prefs.collectCrashReports = false
        render()
        row("crash-reports").performClick()
        assertTrue(onlyEvent<SettingsViewEvent.UpdateCrashReports>().checked)
    }

    @Test
    fun networkSwitchesKeepTheOtherUpdateOptions() {
        prefs.updatesFrequency = 21600
        prefs.isRequiresCharging = true
        render()

        row("wifi_only").performClick()

        val wifiOnly = onlyEvent<SettingsViewEvent.ChangeUpdatePolicy>()
        assertEquals(21600, wifiOnly.frequency)
        assertTrue(wifiOnly.isWifiOnly)
        assertTrue(wifiOnly.isRequiresCharging)

        events.clear()
        prefs.isRequiresCharging = false
        render()

        switchOf("requires-charging").performClick()

        val charging = onlyEvent<SettingsViewEvent.ChangeUpdatePolicy>()
        assertEquals(21600, charging.frequency)
        assertFalse(charging.isWifiOnly)
        assertTrue(charging.isRequiresCharging)
    }

    @Test
    fun networkSwitchesAreLockedWhileUpdatesAreManual() {
        prefs.updatesFrequency = 0
        render()

        assertNetworkSwitchesLocked()
    }

    @Test
    fun networkSwitchesAreLockedWithoutNotificationPermission() {
        notifications.areNotificationsEnabled = false
        render()

        assertNetworkSwitchesLocked()
    }

    private fun assertNetworkSwitchesLocked() {
        networkSwitches.forEach { setting ->
            row(setting.key).assertIsNotEnabled().performClick()
            switchOf(setting.key).assertIsNotEnabled().performClick()
        }
        assertTrue(events.isEmpty())
    }

    @Test
    fun updateFrequencyShowsTheStoredChoiceAndRequestsEachOption() {
        val labels = context.resources.getStringArray(R.array.updates_frequency)
        val values = context.resources.getStringArray(R.array.updates_frequency_values).map { it.toInt() }
        prefs.updatesFrequency = 10800
        render()
        scrollToItem("update_frequency")

        assertSelectedChip(labels, values.indexOf(10800))
        labels.forEachIndexed { index, label ->
            events.clear()
            chip(label).performClick()
            assertEquals(label, values[index], onlyEvent<SettingsViewEvent.ChangeUpdatePolicy>().frequency)
        }
    }

    @Test
    fun updateFrequencyIsLockedToManualUntilNotificationsAreAllowed() {
        val labels = context.resources.getStringArray(R.array.updates_frequency)
        notifications.areNotificationsEnabled = false
        prefs.updatesFrequency = 3600
        render()
        scrollToItem("update_frequency")

        labels.forEach { chip(it).assertIsNotEnabled().performClick() }
        assertSelectedChip(labels, 0)
        assertTrue(events.isEmpty())

        compose.onNodeWithText(context.getString(R.string.notifications_not_enabled)).assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.allow)).performClick()

        assertEquals(listOf<SettingsViewEvent>(SettingsViewEvent.NotificationPermissionRequest), events)
    }

    @Test
    fun notificationBannerIsHiddenWhenNotificationsAreAllowed() {
        render()
        scrollToItem("update_frequency")

        compose.onAllNodesWithText(context.getString(R.string.notifications_not_enabled)).assertCountEquals(0)
        compose.onAllNodesWithText(context.getString(R.string.allow)).assertCountEquals(0)
    }

    @Test
    fun themeShowsTheStoredChoiceAndRequestsEachOption() {
        val labels = context.resources.getStringArray(R.array.themes)
        prefs.themeIndex = 3
        render()
        scrollToItem("theme")

        assertSelectedChip(labels, 3)
        labels.forEachIndexed { index, label ->
            events.clear()
            chip(label).performClick()
            assertEquals(label, index, onlyEvent<SettingsViewEvent.UpdateTheme>().newTheme)
        }
        assertEquals("The view model applies the theme", 3, prefs.themeIndex)
    }

    @Test
    fun defaultListShowsTheStoredChoice() {
        val labels = context.resources.getStringArray(R.array.filter_titles)
        render()
        scrollToItem("default-filter")
        assertSelectedChip(labels, Filters.ALL)

        prefs.defaultMainFilterId = Filters.UPDATABLE
        render()
        scrollToItem("default-filter")
        assertSelectedChip(labels, Filters.UPDATABLE)
    }

    @Test
    fun defaultListSavesTheChoiceItself() {
        val labels = context.resources.getStringArray(R.array.filter_titles)
        render()
        scrollToItem("default-filter")

        listOf(Filters.UPDATABLE, Filters.UNINSTALLED, Filters.INSTALLED, Filters.ALL).forEach { filter ->
            chip(labels[filter]).performClick()
            assertEquals(labels[filter], filter, prefs.defaultMainFilterId)
        }
        assertTrue(events.isEmpty())
    }

    @Test
    fun rowsOpenTheirScreens() {
        render()

        listOf<Pair<String, SettingsViewEvent>>(
            "licenses" to SettingsViewEvent.OssLicenses,
            "user-log" to SettingsViewEvent.OpenUserLog,
            "refresh-history" to SettingsViewEvent.OpenRefreshHistory
        ).forEach { (key, event) ->
            events.clear()
            row(key).performClick()
            assertEquals(key, listOf(event), events)
        }
    }

    @Test
    fun debugRowsRequestTheirTools() {
        assumeTrue(BuildConfig.DEBUG)
        render()

        row("test-notification").performClick()
        assertEquals(listOf<SettingsViewEvent>(SettingsViewEvent.TestNotification), events)

        events.clear()
        row("db-cleanup").performClick()
        assertEquals(listOf<SettingsViewEvent>(SettingsViewEvent.DbCleanup), events)
    }

    @Test
    fun informationRowsDoNothingWhenTapped() {
        render()

        listOf("about", "update_frequency", "theme", "default-filter", "icon-style").forEach { row(it).performClick() }

        assertTrue(events.toString(), events.isEmpty())
    }

    @Test
    fun driveSyncIsUnavailableWithoutPlayServices() {
        prefs.isDriveSyncEnabled = true
        render()

        val reason = summaryOf(item("drive_sync"))
        assertTrue("Expected the reason to be shown", reason.isNotEmpty())
        row("drive_sync").assertIsNotEnabled().assert(hasText(reason)).performClick()
        switchOf("drive_sync").assertIsOff().assertIsNotEnabled()
        row("drive-sync-now").assertIsNotEnabled().performClick()

        assertTrue(events.isEmpty())
    }
}