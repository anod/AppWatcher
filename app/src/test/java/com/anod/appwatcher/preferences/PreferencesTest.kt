package com.anod.appwatcher.preferences

import android.app.Application
import android.app.UiModeManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.anod.appwatcher.R
import com.anod.appwatcher.diagnostics.DiagnosticAllowlist
import com.anod.appwatcher.model.Filters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
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
class PreferencesTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
    private val notifications = TestNotificationManager()
    private val prefs = newPreferences()
    private val stored get() = context.getSharedPreferences("WatcherPrefs", Context.MODE_PRIVATE)

    @After
    fun tearDown() {
        scope.cancel()
    }

    private fun newPreferences() = Preferences(context, notifications, scope, DiagnosticAllowlist(emptySet()))

    private class Setting<T : Any>(
        val key: String,
        val default: T,
        val changed: T,
        private val read: (Preferences) -> T,
        private val write: (Preferences, T) -> Unit
    ) {
        fun current(prefs: Preferences): Any = read(prefs)
        fun change(prefs: Preferences) = write(prefs, changed)
        fun restoreDefault(prefs: Preferences) = write(prefs, default)
    }

    private val updateSettings = listOf(
        Setting("update_frequency", 7200, 3600, { it.updatesFrequency }, { p, v -> p.updatesFrequency = v }),
        Setting("wifi_only", false, true, { it.isWifiOnly }, { p, v -> p.isWifiOnly = v }),
        Setting("requires-charging", false, true, { it.isRequiresCharging }, { p, v -> p.isRequiresCharging = v })
    )

    private val notificationSettings = listOf(
        Setting("notify_installed_uptodate", true, false, { it.isNotifyInstalledUpToDate }, { p, v -> p.isNotifyInstalledUpToDate = v }),
        Setting("notify-installed", true, false, { it.isNotifyInstalled }, { p, v -> p.isNotifyInstalled = v }),
        Setting("notify-no-changes", true, false, { it.isNotifyNoChanges }, { p, v -> p.isNotifyNoChanges = v }),
        Setting("noti_disabled_toasts", 0, 2, { it.notificationDisabledToastCount }, { p, v -> p.notificationDisabledToastCount = v })
    )

    private val driveSettings = listOf(
        Setting("drive_sync", false, true, { it.isDriveSyncEnabled }, { p, v -> p.isDriveSyncEnabled = v }),
        Setting("drive_sync_time", -1L, 1_700_000_000_000L, { it.lastDriveSyncTime }, { p, v -> p.lastDriveSyncTime = v })
    )

    private val interfaceSettings = listOf(
        Setting("show-recent", true, false, { it.showRecent }, { p, v -> p.showRecent = v }),
        Setting("show-on-device", false, true, { it.showOnDevice }, { p, v -> p.showOnDevice = v }),
        Setting("show-recently-updated", true, false, { it.showRecentlyDiscovered }, { p, v -> p.showRecentlyDiscovered = v }),
        Setting("pull-to-refresh", true, false, { it.enablePullToRefresh }, { p, v -> p.enablePullToRefresh = v }),
        Setting("default_main_filter_id", Filters.ALL, Filters.UPDATABLE, { it.defaultMainFilterId }, { p, v -> p.defaultMainFilterId = v })
    )

    private val privacySettings = listOf(
        Setting("crash-reports", true, false, { it.collectCrashReports }, { p, v -> p.collectCrashReports = v })
    )

    private val allSettings = updateSettings + notificationSettings + driveSettings + interfaceSettings + privacySettings

    private fun assertPersisted(settings: List<Setting<*>>) = settings.forEach { setting ->
        val key = setting.key
        assertEquals("$key default", setting.default, setting.current(prefs))
        assertFalse("$key must not be stored before it is changed", stored.contains(key))

        setting.change(prefs)

        assertEquals("$key changed", setting.changed, setting.current(prefs))
        assertEquals("$key stored value", setting.changed, stored.all[key])
        assertEquals("$key read by a new instance", setting.changed, setting.current(newPreferences()))

        setting.restoreDefault(prefs)

        assertEquals("$key restored", setting.default, setting.current(prefs))
    }

    @Test
    fun updateSettingsPersistUnderTheirOwnKeys() = assertPersisted(updateSettings)

    @Test
    fun notificationSettingsPersistUnderTheirOwnKeys() = assertPersisted(notificationSettings)

    @Test
    fun driveSyncSettingsPersistUnderTheirOwnKeys() = assertPersisted(driveSettings)

    @Test
    fun interfaceSettingsPersistUnderTheirOwnKeys() = assertPersisted(interfaceSettings)

    @Test
    fun crashReportsSettingPersistsUnderItsOwnKey() = assertPersisted(privacySettings)

    @Test
    fun changingOneSettingNeverAffectsTheOthers() {
        assertEquals(allSettings.size, allSettings.map { it.key }.toSet().size)

        allSettings.forEach { changed ->
            stored.edit().clear().commit()

            changed.change(prefs)

            allSettings.filter { it !== changed }.forEach { other ->
                assertEquals("${other.key} after changing ${changed.key}", other.default, other.current(prefs))
            }
        }
    }

    @Test
    fun updateFrequencyFallsBackToTheLegacyAutoSyncFlagWhenNeverChosen() {
        assertEquals(7200, prefs.updatesFrequency)

        stored.edit().putBoolean("autosync", false).commit()
        assertEquals(0, prefs.updatesFrequency)

        stored.edit().putBoolean("autosync", true).commit()
        assertEquals(7200, prefs.updatesFrequency)
    }

    @Test
    fun explicitUpdateFrequencyWinsOverTheLegacyAutoSyncFlag() {
        stored.edit().putBoolean("autosync", false).commit()
        prefs.updatesFrequency = 3600
        assertEquals(3600, prefs.updatesFrequency)

        stored.edit().putBoolean("autosync", true).commit()
        prefs.updatesFrequency = 0
        assertEquals(0, prefs.updatesFrequency)
    }

    @Test
    fun everyOfferedUpdateFrequencyRoundTrips() {
        context.resources.getStringArray(R.array.updates_frequency_values).map { it.toInt() }.forEach { seconds ->
            prefs.updatesFrequency = seconds

            assertEquals(seconds, prefs.updatesFrequency)
            assertEquals(seconds, newPreferences().updatesFrequency)
        }
    }

    @Test
    fun autoSyncNeedsBothAFrequencyAndEnabledNotifications() {
        prefs.updatesFrequency = 3600
        notifications.areNotificationsEnabled = true
        assertTrue(prefs.useAutoSync)
        assertTrue(prefs.areNotificationsEnabled)

        notifications.areNotificationsEnabled = false
        assertFalse(prefs.useAutoSync)
        assertFalse(prefs.areNotificationsEnabled)

        notifications.areNotificationsEnabled = true
        prefs.updatesFrequency = 0
        assertFalse(prefs.useAutoSync)
    }

    @Test
    fun iconShapeDefaultsToTheSystemMask() {
        assertEquals(prefs.defaultSystemMask, prefs.iconShape)
        assertFalse(stored.contains("adaptive-icon-shape"))
    }

    @Test
    fun everyOfferedIconShapePersistsIncludingNone() {
        context.resources.getStringArray(R.array.adaptive_icon_style_paths_values).forEach { path ->
            prefs.iconShape = path

            assertEquals(path, prefs.iconShape)
            assertEquals(path, stored.getString("adaptive-icon-shape", null))
            assertEquals(path, newPreferences().iconShape)
        }
    }

    private class ThemeChoice(
        val index: Int,
        val nightMode: Int,
        val theme: Int,
        val mode: Int,
        val isBlack: Boolean,
        val isDark: Boolean,
        val isSystem: Boolean
    )

    // Same order as R.array.themes: Follow system (Dark), Follow system (Black), Light, Dark, Black
    private val themeChoices = listOf(
        ThemeChoice(0, UiModeManager.MODE_NIGHT_AUTO, 0, Preferences.THEME_MODE_SYSTEM, isBlack = false, isDark = false, isSystem = true),
        ThemeChoice(1, UiModeManager.MODE_NIGHT_AUTO, 1, Preferences.THEME_MODE_SYSTEM, isBlack = true, isDark = false, isSystem = true),
        ThemeChoice(2, UiModeManager.MODE_NIGHT_NO, 0, Preferences.THEME_MODE_LIGHT, isBlack = false, isDark = false, isSystem = false),
        ThemeChoice(3, UiModeManager.MODE_NIGHT_YES, 0, Preferences.THEME_MODE_DARK, isBlack = false, isDark = true, isSystem = false),
        ThemeChoice(4, UiModeManager.MODE_NIGHT_YES, 1, Preferences.THEME_MODE_DARK, isBlack = true, isDark = true, isSystem = false)
    )

    @Test
    fun themeChoicesCoverEveryEntryOfTheThemePicker() {
        assertEquals(themeChoices.size, context.resources.getStringArray(R.array.themes).size)
    }

    @Test
    fun themeFollowsTheSystemByDefault() {
        assertEquals(0, prefs.themeIndex)
        assertTrue(prefs.selectedTheme.isSystem)
        assertFalse(prefs.selectedTheme.isDark)
        assertFalse(prefs.selectedTheme.isBlack)
    }

    @Test
    fun themeChoiceIsStoredAsNightModeAndThemeId() {
        themeChoices.forEach { choice ->
            prefs.themeIndex = choice.index

            assertEquals("night mode of ${choice.index}", choice.nightMode, stored.getInt("night-mode", -99))
            assertEquals("theme id of ${choice.index}", choice.theme, stored.getInt("theme", -99))
            assertEquals("index of ${choice.index}", choice.index, prefs.themeIndex)
            assertEquals("index of ${choice.index} in a new instance", choice.index, newPreferences().themeIndex)
        }
    }

    @Test
    fun selectedThemeDescribesTheChosenTheme() {
        themeChoices.forEach { choice ->
            prefs.themeIndex = choice.index

            val selected = prefs.selectedTheme
            assertEquals("id of ${choice.index}", choice.theme, selected.id)
            assertEquals("mode of ${choice.index}", choice.mode, selected.mode)
            assertEquals("black of ${choice.index}", choice.isBlack, selected.isBlack)
            assertEquals("dark of ${choice.index}", choice.isDark, selected.isDark)
            assertEquals("system of ${choice.index}", choice.isSystem, selected.isSystem)
        }
    }

    @Test
    fun everyThemeChoiceResolvesToADifferentSelectedTheme() {
        val selected = themeChoices.map { choice ->
            prefs.themeIndex = choice.index
            prefs.selectedTheme
        }

        assertEquals(themeChoices.size, selected.toSet().size)
    }

    @Test
    fun unsupportedNightModeAndThemeCombinationHasNoThemeIndex() {
        stored.edit().putInt("night-mode", UiModeManager.MODE_NIGHT_NO).putInt("theme", 1).commit()
        assertEquals(-1, prefs.themeIndex)

        stored.edit().putInt("night-mode", UiModeManager.MODE_NIGHT_AUTO).putInt("theme", 2).commit()
        assertEquals(-1, prefs.themeIndex)
    }

    @Test
    fun unknownNightModeIsTreatedAsLight() {
        stored.edit().putInt("night-mode", 7).commit()

        assertEquals(Preferences.THEME_MODE_LIGHT, prefs.selectedTheme.mode)
    }

    @Test
    fun selectedThemeFlowFollowsThemeChangesFromAnySource() {
        themeChoices.forEach { choice ->
            prefs.themeIndex = choice.index

            assertEquals("theme ${choice.index}", prefs.selectedTheme, prefs.selectedThemeFlow.value)
        }

        stored.edit().putInt("night-mode", UiModeManager.MODE_NIGHT_NO).putInt("theme", 0).commit()
        assertEquals(SelectedTheme(0, Preferences.THEME_MODE_LIGHT), prefs.selectedThemeFlow.value)

        newPreferences().themeIndex = 3
        assertEquals(SelectedTheme(0, Preferences.THEME_MODE_DARK), prefs.selectedThemeFlow.value)
    }

    @Test
    fun changesFlowEmitsTheKeyOfEveryWrittenSetting() {
        val keys = mutableListOf<String>()
        scope.launch { prefs.changes.collect { keys += it } }

        prefs.showRecent = false
        prefs.updatesFrequency = 3600
        prefs.themeIndex = 4
        prefs.isNotifyInstalled = false

        assertEquals(listOf("show-recent", "update_frequency"), keys.take(2))
        assertEquals("theme is written in one edit", setOf("night-mode", "theme"), keys.subList(2, 4).toSet())
        assertEquals(listOf("notify-installed"), keys.drop(4))
    }
}