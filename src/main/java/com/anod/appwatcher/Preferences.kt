package com.anod.appwatcher

import android.accounts.Account
import android.content.Context
import android.content.SharedPreferences
import android.support.v7.app.AppCompatDelegate

class Preferences(context: Context) {

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

    var deviceId: String
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

    var useAutoSync: Boolean
        get() = preferences.getBoolean(AUTOSYNC, true)
        set(value) = preferences.edit().putBoolean(AUTOSYNC, value).apply()

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

    var nightMode: Int
        get() = preferences.getInt(NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_NO)
        set(nightMode) = preferences.edit().putInt(NIGHT_MODE, nightMode).apply()

    companion object {
        private val VIEWED = "viewed"
        private val LAST_UPDATE_TIME = "last_update_time"
        private val WIFI_ONLY = "wifi_only"
        private val DEVICE_ID = "device_id"
        private val ACCOUNT_NAME = "account_name"
        private val ACCOUNT_TYPE = "account_type"
        private val SORT_INDEX = "sort_index"

        internal val SORT_NAME_ASC = 0
        val SORT_NAME_DESC = 1
        val SORT_DATE_ASC = 2
        val SORT_DATE_DESC = 3

        private val PREFS_NAME = "WatcherPrefs"
        private val DRIVE_SYNC = "drive_sync"
        private val DRIVE_SYNC_TIME = "drive_sync_time"
        private val AUTOSYNC = "autosync"
        private val REQUIRES_CHARGING = "requires-charging"
        val VERSION_CODE = "version_code"
        val NOTIFY_INSTALLED_UPTODATE = "notify_installed_uptodate"
        val NIGHT_MODE = "night-mode"
    }
}
