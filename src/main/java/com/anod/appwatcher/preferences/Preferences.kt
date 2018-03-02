package com.anod.appwatcher.preferences

import android.accounts.Account
import android.content.Context
import android.content.SharedPreferences
import android.support.v7.app.AppCompatDelegate
import com.anod.appwatcher.model.Filters
import info.anodsplace.playstore.DeviceIdStorage

class Preferences(context: Context) : DeviceIdStorage {

    private val preferences: SharedPreferences

    init {
        preferences = context.getSharedPreferences(PREFS_NAME, 0)
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

    var lastUpdateTime: Long
        get() = preferences.getLong(LAST_UPDATE_TIME, -1)
        set(value) = preferences.edit().putLong(LAST_UPDATE_TIME, value).apply()

    override var deviceId: String
        get() = preferences.getString(DEVICE_ID, "")
        set(value) {
            val editor = preferences.edit()
            if (value.isEmpty()) {
                editor.remove(DEVICE_ID)
            } else {
                editor.putString(DEVICE_ID, value)
            }
            editor.apply()
        }

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

    val useAutoSync: Boolean
        get() = this.updatesFrequency > 0

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

    var showRecent: Boolean
        get() = preferences.getBoolean("show-recent", false)
        set(value) = preferences.edit().putBoolean("show-recent", value).apply()

    var showOnDevice: Boolean
        get() = preferences.getBoolean("show-on-device", false)
        set(value) = preferences.edit().putBoolean("show-on-device", value).apply()

    var nightMode: Int
        get() = preferences.getInt(NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_NO)
        set(nightMode) = preferences.edit().putInt(NIGHT_MODE, nightMode).apply()

    var theme: Int
        get() = preferences.getInt(THEME, THEME_DEFAULT)
        set(theme) = preferences.edit().putInt(THEME, theme).apply()

    var defaultMainFilterId: Int
        get() = preferences.getInt(FILTER_ID, Filters.TAB_ALL)
        set(filterId) = preferences.edit().putInt(FILTER_ID, filterId).apply()

    companion object {
        private const val VIEWED = "viewed"
        private const val LAST_UPDATE_TIME = "last_update_time"
        private const val WIFI_ONLY = "wifi_only"
        private const val DEVICE_ID = "device_id"
        private const val ACCOUNT_NAME = "account_name"
        private const val ACCOUNT_TYPE = "account_type"
        private const val SORT_INDEX = "sort_index"
        private const val FILTER_ID = "default_main_filter_id"

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
    }

}
