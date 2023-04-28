package com.anod.appwatcher.preferences

import android.accounts.Account
import android.annotation.SuppressLint
import android.app.UiModeManager
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.anod.appwatcher.model.Filters
import info.anodsplace.framework.app.NotificationManager
import info.anodsplace.graphics.AdaptiveIcon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class Preferences(context: Context, private val notificationManager: NotificationManager, private val appScope: CoroutineScope) : SharedPreferences.OnSharedPreferenceChangeListener {
    private val _changes = MutableSharedFlow<String>()
    private val preferences = context.getSharedPreferences(PREFS_NAME, 0)

    val changes: Flow<String> = _changes

    init {
        preferences.registerOnSharedPreferenceChangeListener(this)
    }

    var account: Account?
        get() {
            val name = preferences.getString(ACCOUNT_NAME, null) ?: return null
            val type = preferences.getString(ACCOUNT_TYPE, null)
            return Account(name, type)
        }
        set(value) {
            val editor = preferences.edit()
            if (value == null) {
                editor.remove(ACCOUNT_NAME)
                editor.remove(ACCOUNT_TYPE)
            } else {
                editor.putString(ACCOUNT_NAME, value.name)
                editor.putString(ACCOUNT_TYPE, value.type)
            }
            editor.apply()
        }

    var notificationDisabledToastCount: Int
        get() = preferences.getInt("noti_disabled_toasts", -0)
        set(value) = preferences.edit().putInt("noti_disabled_toasts", value).apply()

    var lastUpdateTime: Long
        get() = preferences.getLong(LAST_UPDATE_TIME, -1)
        set(value) = preferences.edit().putLong(LAST_UPDATE_TIME, value).apply()

    var isWifiOnly: Boolean
        get() = preferences.getBoolean(WIFI_ONLY, false)
        set(value) = preferences.edit().putBoolean(WIFI_ONLY, value).apply()

    var isLastUpdatesViewed: Boolean
        get() = preferences.getBoolean(VIEWED, true)
        set(value) = preferences.edit().putBoolean(VIEWED, value).apply()

    var isDriveSyncEnabled: Boolean
        get() = preferences.getBoolean(DRIVE_SYNC, false)
        set(value) = preferences.edit().putBoolean(DRIVE_SYNC, value).apply()

    var lastDriveSyncTime: Long
        get() = preferences.getLong(DRIVE_SYNC_TIME, -1)
        set(value) = preferences.edit().putLong(DRIVE_SYNC_TIME, value).apply()

    var lastCleanupTime: Long
        get() = preferences.getLong("cleanup-time", -1)
        set(value) = preferences.edit().putLong("cleanup-time", value).apply()

    val areNotificationsEnabled: Boolean
        get() = notificationManager.areNotificationsEnabled

    val useAutoSync: Boolean
        get() = this.updatesFrequency > 0 && notificationManager.areNotificationsEnabled

    var updatesFrequency: Int
        get() {
            val freq = preferences.getInt(updateFrequency, -1)
            if (freq == -1) {
                // 2 hrs fallback
                return if (preferences.getBoolean(AUTOSYNC, true)) 7200 else 0
            }
            return freq
        }
        set(value) {
            preferences.edit().putInt(updateFrequency, value).apply()
        }

    var isRequiresCharging: Boolean
        get() = preferences.getBoolean(REQUIRES_CHARGING, false)
        set(requiresCharging) = preferences.edit().putBoolean(REQUIRES_CHARGING, requiresCharging).apply()

    var sortIndex: Int
        get() = preferences.getInt(SORT_INDEX, 0)
        set(index) = preferences.edit().putInt(SORT_INDEX, index).apply()

    var versionCode: Int
        get() = preferences.getInt(VERSION_CODE, 0)
        set(value) = preferences.edit().putInt(VERSION_CODE, value).apply()

    var isNotifyInstalledUpToDate: Boolean
        get() = preferences.getBoolean(NOTIFY_INSTALLED_UPTODATE, true)
        set(notify) = preferences.edit().putBoolean(NOTIFY_INSTALLED_UPTODATE, notify).apply()

    var isNotifyInstalled: Boolean
        get() = preferences.getBoolean("notify-installed", true)
        set(notify) = preferences.edit().putBoolean("notify-installed", notify).apply()

    var isNotifyNoChanges: Boolean
        get() = preferences.getBoolean("notify-no-changes", true)
        set(notify) = preferences.edit().putBoolean("notify-no-changes", notify).apply()

    var showRecent: Boolean
        get() = preferences.getBoolean("show-recent", true)
        set(value) = preferences.edit().putBoolean("show-recent", value).apply()

    var showOnDevice: Boolean
        get() = preferences.getBoolean("show-on-device", false)
        set(value) = preferences.edit().putBoolean("show-on-device", value).apply()

    var showRecentlyDiscovered: Boolean
        get() = preferences.getBoolean("show-recently-updated", true)
        set(value) = preferences.edit().putBoolean("show-recently-updated", value).apply()

    var uiMode: Int
        get() = preferences.getInt(NIGHT_MODE, UiModeManager.MODE_NIGHT_AUTO)
        set(nightMode) = preferences.edit().putInt(NIGHT_MODE, nightMode).apply()

    val appCompatNightMode: Int
        get() = uiModeMap[uiMode] ?: AppCompatDelegate.MODE_NIGHT_NO

    var theme: Int
        get() = preferences.getInt(THEME, THEME_DEFAULT)
        set(theme) = preferences.edit().putInt(THEME, theme).apply()

    var themeIndex: Int
        get() = themesCombined.indexOf("$uiMode-$theme")
        set(value) {
            uiMode = uiModes[value]
            theme = themeIds[value]
        }

    var defaultMainFilterId: Int
        get() = preferences.getInt(FILTER_ID, Filters.ALL)
        set(filterId) = preferences.edit().putInt(FILTER_ID, filterId).apply()

    var enablePullToRefresh: Boolean
        get() = preferences.getBoolean("pull-to-refresh", true)
        set(value) = preferences.edit().putBoolean("pull-to-refresh", value).apply()

    var collectCrashReports: Boolean
        get() = preferences.getBoolean("crash-reports", true)
        @SuppressLint("ApplySharedPref")
        set(value) {
            preferences.edit().putBoolean("crash-reports", value).commit()
        }

    var iconShape: String
        get() = preferences.getString("adaptive-icon-shape", defaultSystemMask)!!
        set(value) = preferences.edit().putString("adaptive-icon-shape", value).apply()

    val defaultSystemMask: String by lazy {
        AdaptiveIcon.getSystemDefaultMask()
    }

    companion object {
        const val recentDays: Long = 2

        private const val VIEWED = "viewed"
        const val LAST_UPDATE_TIME = "last_update_time"
        private const val WIFI_ONLY = "wifi_only"
        private const val DEVICE_ID = "device_id"
        private const val ACCOUNT_NAME = "account_name"
        private const val ACCOUNT_TYPE = "account_type"
        private const val SORT_INDEX = "sort_index"
        private const val FILTER_ID = "default_main_filter_id"

        const val SORT_NAME_ASC = 0
        const val SORT_NAME_DESC = 1
        const val SORT_DATE_ASC = 2
        const val SORT_DATE_DESC = 3

        private const val PREFS_NAME = "WatcherPrefs"
        private const val DRIVE_SYNC = "drive_sync"
        private const val DRIVE_SYNC_TIME = "drive_sync_time"
        private const val AUTOSYNC = "autosync"
        private const val REQUIRES_CHARGING = "requires-charging"
        private const val updateFrequency = "update_frequency"

        const val VERSION_CODE = "version_code"
        const val NOTIFY_INSTALLED_UPTODATE = "notify_installed_uptodate"
        const val NIGHT_MODE = "night-mode"
        const val THEME = "theme"

        const val THEME_DEFAULT = 0
        const val THEME_BLACK = 1

        private val themesCombined = arrayOf(
                "${UiModeManager.MODE_NIGHT_AUTO}-$THEME_DEFAULT",
                "${UiModeManager.MODE_NIGHT_AUTO}-$THEME_BLACK",
                "${UiModeManager.MODE_NIGHT_NO}-$THEME_DEFAULT",
                "${UiModeManager.MODE_NIGHT_YES}-$THEME_DEFAULT",
                "${UiModeManager.MODE_NIGHT_YES}-$THEME_BLACK",
        )

        private val uiModes = arrayOf(
                UiModeManager.MODE_NIGHT_AUTO,
                UiModeManager.MODE_NIGHT_AUTO,
                UiModeManager.MODE_NIGHT_NO,
                UiModeManager.MODE_NIGHT_YES,
                UiModeManager.MODE_NIGHT_YES,
        )

        private val uiModeMap = mapOf(
                UiModeManager.MODE_NIGHT_AUTO to AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
                UiModeManager.MODE_NIGHT_NO to AppCompatDelegate.MODE_NIGHT_NO,
                UiModeManager.MODE_NIGHT_YES to AppCompatDelegate.MODE_NIGHT_YES
        )

        private val themeIds = arrayOf(
                THEME_DEFAULT,
                THEME_BLACK,
                THEME_DEFAULT,
                THEME_DEFAULT,
                THEME_BLACK,
        )
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key != null) {
            appScope.launch {
                _changes.emit(key)
            }
        }
    }

}