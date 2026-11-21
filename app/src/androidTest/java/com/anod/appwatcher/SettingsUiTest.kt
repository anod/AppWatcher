package com.anod.appwatcher

import android.app.Notification
import android.app.NotificationManager
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.anod.appwatcher.diagnostics.DiagnosticStorageState
import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.preferences.preferenceItems
import com.anod.appwatcher.sync.SyncNotification
import info.anodsplace.compose.ActionablePreferenceItem
import info.anodsplace.compose.PreferenceItem
import info.anodsplace.playservices.GooglePlayServices
import java.io.File
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.koin.java.KoinJavaComponent

/**
 * Drives the settings screen of the installed app on a device and checks what each setting does
 * beyond the screen: the preference written to disk, the update check handed to WorkManager and the
 * notification the debug row posts.
 *
 * The screen is reached through the real drawer. The update check is scheduled on an in-memory
 * WorkManager ([IsolatedWorkManagerRule]) and every preference is restored afterwards
 * ([StableStartupPreferencesRule]), so a run leaves a developer's device as it found it. The theme is
 * not chosen here because doing so restarts the process; [ThemeRenderingUiTest] covers it.
 * The Drive, backup, crash report and database rows are not clicked, as they would reach accounts,
 * documents or real data.
 */
@RunWith(AndroidJUnit4::class)
class SettingsUiTest {
    private class SwitchSetting(val key: String, val titleRes: Int, val read: (Preferences) -> Boolean)

    private val switches = listOf(
        SwitchSetting("notify_installed_uptodate", R.string.uptodate_title, Preferences::isNotifyInstalledUpToDate),
        SwitchSetting("notify-installed", R.string.pref_notify_installed, Preferences::isNotifyInstalled),
        SwitchSetting("notify-no-changes", R.string.pref_notify_no_changes, Preferences::isNotifyNoChanges),
        SwitchSetting("show-recent", R.string.pref_show_recent_title, Preferences::showRecent),
        SwitchSetting("show-on-device", R.string.pref_show_ondevice_title, Preferences::showOnDevice),
        SwitchSetting("show-recently-updated", R.string.pref_show_recently_discovered_title, Preferences::showRecentlyDiscovered),
        SwitchSetting("pull-to-refresh", R.string.pref_pull_to_refresh, Preferences::enablePullToRefresh)
    )

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val prefs by lazy { KoinJavaComponent.getKoin().get<Preferences>() }
    private val compose = createAndroidComposeRule<AppWatcherActivity>()
    private val support = AppWatcherUiTestSupport(compose)
    private val robot = SettingsScreenRobot(compose, support)

    @get:Rule
    val rules: RuleChain = RuleChain
        .outerRule(IsolatedWorkManagerRule())
        .around(StableStartupPreferencesRule(mapOf("wifi_only" to false, "requires-charging" to false)))
        .around(compose)

    @Before
    fun openSettings() {
        support.awaitAppReady()
        robot.open()
    }

    @Test
    fun everySettingIsShownWithItsTitleAndSummary() {
        val items = settingsItems()
        assertTrue("Expected the whole settings list, found ${items.size} rows", items.size > 20)

        items.forEach { item ->
            val isCategory = item is PreferenceItem.Category
            val title = titleOf(item)
            robot.scrollTo(title, ignoreCase = isCategory)
            compose.onNode(hasText(title, ignoreCase = isCategory)).assertIsDisplayed()
            val summary = summaryOf(item)
            if (summary.isNotEmpty() && item.key !in SUMMARIES_THAT_CHANGE) {
                compose.onNode(hasText(title) and hasText(summary)).assertIsDisplayed()
            }
        }
    }

    @Test
    fun everySwitchStoresItsChoiceAndShowsItAgain() {
        switches.forEach { setting ->
            val title = context.getString(setting.titleRes)
            val initial = setting.read(prefs)
            robot.assertSwitch(title, initial)

            robot.switchOf(title).performClick()
            awaitStored(setting, !initial)
            robot.assertSwitch(title, !initial)

            robot.switchOf(title).performClick()
            awaitStored(setting, initial)
            robot.assertSwitch(title, initial)
        }
    }

    @Test
    fun updateFrequencyAndItsConstraintsRescheduleTheUpdateCheck() {
        assumeTrue("Automatic checks need notifications to be allowed", prefs.areNotificationsEnabled)
        val title = context.getString(R.string.pref_title_updates_frequency)
        val labels = context.resources.getStringArray(R.array.updates_frequency).toList()
        val seconds = context.resources.getStringArray(R.array.updates_frequency_values).map { it.toInt() }
        val wifiOnly = context.getString(R.string.menu_wifi_only)
        val requiresCharging = context.getString(R.string.menu_requires_charging)
        assertEquals(labels.size, seconds.size)

        robot.assertSelectedChip(title, labels, selectedIndex = 0)
        robot.assertRowEnabled(wifiOnly, enabled = false)
        robot.assertRowEnabled(requiresCharging, enabled = false)
        assertNull(currentSyncSchedule(context))

        for (index in 1 until labels.size) {
            robot.select(title, labels[index])
            awaitSchedule(SyncSchedule(seconds[index].toLong(), wifiOnly = false, requiresCharging = false))
            awaitStored("update_frequency", seconds[index].toString())
            robot.assertSelectedChip(title, labels, index)
            robot.assertRowEnabled(wifiOnly, enabled = true)
            robot.assertRowEnabled(requiresCharging, enabled = true)
        }

        val period = seconds.last().toLong()
        robot.switchOf(wifiOnly).performClick()
        awaitSchedule(SyncSchedule(period, wifiOnly = true, requiresCharging = false))
        awaitStored("wifi_only", "true")
        robot.assertSwitch(wifiOnly, on = true)

        robot.switchOf(requiresCharging).performClick()
        awaitSchedule(SyncSchedule(period, wifiOnly = true, requiresCharging = true))
        awaitStored("requires-charging", "true")
        robot.assertSwitch(requiresCharging, on = true)

        robot.switchOf(wifiOnly).performClick()
        awaitSchedule(SyncSchedule(period, wifiOnly = false, requiresCharging = true))
        awaitStored("wifi_only", "false")
        robot.assertSwitch(wifiOnly, on = false)

        robot.switchOf(requiresCharging).performClick()
        awaitSchedule(SyncSchedule(period, wifiOnly = false, requiresCharging = false))
        awaitStored("requires-charging", "false")
        robot.assertSwitch(requiresCharging, on = false)

        robot.select(title, labels.first())
        awaitSchedule(null)
        awaitStored("update_frequency", "0")
        robot.assertSelectedChip(title, labels, selectedIndex = 0)
        robot.assertRowEnabled(wifiOnly, enabled = false)
        robot.assertRowEnabled(requiresCharging, enabled = false)
    }

    @Test
    fun defaultListChoiceIsStoredAndShownAgain() {
        val title = context.getString(R.string.pref_default_filter)
        val labels = context.resources.getStringArray(R.array.filter_titles).toList()
        val original = prefs.defaultMainFilterId
        robot.assertSelectedChip(title, labels, original)

        (labels.indices.filter { it != original } + original).forEach { index ->
            robot.select(title, labels[index])
            robot.await("the default list to be stored as ${labels[index]}") {
                prefs.defaultMainFilterId == index && storedValue(Preferences.DEFAULT_MAIN_FILTER_ID) == index.toString()
            }
            robot.assertSelectedChip(title, labels, index)
        }
    }

    @Test
    fun storedChoicesAreShownAgainWhenTheScreenIsReopened() {
        val onDevice = context.getString(R.string.pref_show_ondevice_title)
        val pullToRefresh = context.getString(R.string.pref_pull_to_refresh)
        val listTitle = context.getString(R.string.pref_default_filter)
        val listLabels = context.resources.getStringArray(R.array.filter_titles).toList()
        val showOnDevice = !prefs.showOnDevice
        val enablePullToRefresh = !prefs.enablePullToRefresh
        val defaultList = (prefs.defaultMainFilterId + 1) % listLabels.size

        robot.switchOf(onDevice).performClick()
        robot.switchOf(pullToRefresh).performClick()
        robot.select(listTitle, listLabels[defaultList])
        robot.await("the three choices to be stored") {
            prefs.showOnDevice == showOnDevice && prefs.enablePullToRefresh == enablePullToRefresh && prefs.defaultMainFilterId == defaultList
        }

        support.returnToWatchList()
        robot.open()

        robot.assertSwitch(onDevice, showOnDevice)
        robot.assertSwitch(pullToRefresh, enablePullToRefresh)
        robot.assertSelectedChip(listTitle, listLabels, defaultList)
    }

    @Test
    fun logAndRefreshHistoryRowsOpenTheirScreensAndBackReturnsToSettings() {
        val settingsTitle = context.getString(R.string.navdrawer_item_settings)

        listOf(R.string.user_log, R.string.refresh_history).forEach { titleRes ->
            val title = context.getString(titleRes)
            robot.scrollTo(title)
            compose.onNode(hasText(title)).performClick()
            robot.await("the \"$title\" screen to replace the settings") {
                !support.hasVisibleText(settingsTitle) && support.hasVisibleText(title)
            }

            compose.onAllNodesWithContentDescription(support.text(R.string.back))[0].performClick()
            robot.await("the settings to be shown again") { support.hasVisibleText(settingsTitle) }
        }
    }

    @Test
    fun testNotificationRowPostsASampleUpdateNotification() {
        assumeTrue("Only the debug build has the row", BuildConfig.DEBUG)
        assumeTrue("Notifications must be allowed to be posted", prefs.areNotificationsEnabled)
        val row = titleOf(settingsItems().single { it.key == "test-notification" })
        val sampleTitle = context.getString(R.string.notification_one_updated, "Test")
        val manager = context.getSystemService(NotificationManager::class.java)
        // The sample reuses the id of the update notification, so a real one has to be put back.
        val replaced = manager.activeNotifications
            .firstOrNull { it.id == SyncNotification.SYNC_NOTIFICATION_ID && it.tag == null }
            ?.notification
            ?.takeUnless { it.titleText() == sampleTitle }

        try {
            robot.scrollTo(row)
            compose.onNode(hasText(row)).performClick()
            robot.await("the sample notification to be posted") { postedSample(manager, sampleTitle) != null }

            val sample = postedSample(manager, sampleTitle)!!
            assertEquals(SyncNotification.UPDATES_CHANNEL_ID, sample.channelId)
            assertEquals("Test notification", sample.extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim())
        } finally {
            if (replaced != null) {
                replaced.flags = replaced.flags or Notification.FLAG_ONLY_ALERT_ONCE
                manager.notify(SyncNotification.SYNC_NOTIFICATION_ID, replaced)
            } else {
                manager.cancel(SyncNotification.SYNC_NOTIFICATION_ID)
            }
        }
    }

    private fun settingsItems(): List<ActionablePreferenceItem> =
        preferenceItems(prefs, false, GooglePlayServices(context), context, DiagnosticStorageState())
            .filterIsInstance<ActionablePreferenceItem>()

    private fun titleOf(item: ActionablePreferenceItem) = if (item.titleRes != 0) context.getString(item.titleRes) else item.title

    private fun summaryOf(item: ActionablePreferenceItem) = if (item.summaryRes != 0) context.getString(item.summaryRes) else item.summary

    private fun Notification.titleText() = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()

    private fun postedSample(manager: NotificationManager, title: String): Notification? = manager.activeNotifications
        .firstOrNull { it.id == SyncNotification.SYNC_NOTIFICATION_ID && it.notification.titleText() == title }
        ?.notification

    private fun awaitSchedule(expected: SyncSchedule?) {
        try {
            robot.await("the update check to be ${expected ?: "cancelled"}") { currentSyncSchedule(context) == expected }
        } catch (e: AssertionError) {
            throw AssertionError("${e.message}, last seen ${currentSyncSchedule(context)}", e)
        }
    }

    private fun awaitStored(setting: SwitchSetting, expected: Boolean) {
        robot.await("\"${setting.key}\" to be stored as $expected") {
            setting.read(prefs) == expected && storedValue(setting.key) == expected.toString()
        }
    }

    private fun awaitStored(key: String, expected: String) {
        robot.await("\"$key\" to be stored as $expected") { storedValue(key) == expected }
    }

    /** Reads the preferences file itself, because `apply()` answers from memory before anything is written. */
    private fun storedValue(key: String): String? {
        val xml = try {
            File(context.dataDir, "shared_prefs/WatcherPrefs.xml").readText()
        } catch (_: IOException) {
            return null
        }
        return Regex("name=\"${Regex.escape(key)}\" value=\"([^\"]*)\"").find(xml)?.groupValues?.get(1)
    }

    private companion object {
        /** A relative time and a storage figure, which differ between building the rows and reading them. */
        val SUMMARIES_THAT_CHANGE = setOf("drive-sync-now", "diagnostics-enabled")
    }
}