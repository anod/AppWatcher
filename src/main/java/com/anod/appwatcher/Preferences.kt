package com.anod.appwatcher

import android.accounts.Account
import android.content.Context
import android.content.SharedPreferences
import android.support.v7.app.AppCompatDelegate

class Preferences(context: Context) {

    private val mSettings: SharedPreferences

    init {
        mSettings = context.getSharedPreferences(PREFS_NAME, 0)
    }

    val account: Account?
        get() {
            val name = mSettings.getString(ACCOUNT_NAME, null) ?: return null
            val type = mSettings.getString(ACCOUNT_TYPE, null)
            return Account(name, type)
        }

    fun checkFirstLaunch(): Boolean {
        val value = mSettings.getBoolean(FIRST_LAUNCH, true)
        if (value) {
            saveBoolean(FIRST_LAUNCH, false)
        }
        return value
    }

    val lastUpdateTime: Long
        get() = mSettings.getLong(LAST_UPDATE_TIME, -1)

    val deviceId: String
        get() = mSettings.getString(DEVICE_ID, "")

    val isWifiOnly: Boolean
        get() = mSettings.getBoolean(WIFI_ONLY, false)

    fun saveDeviceId(deviceId: String?) {
        val editor = mSettings.edit()
        if (deviceId == null || deviceId.isEmpty()) {
            editor.remove(DEVICE_ID)
        } else {
            editor.putString(DEVICE_ID, deviceId)
        }
        editor.apply()
    }

    internal fun saveWifiOnly(useWifiOnly: Boolean) {
        saveBoolean(WIFI_ONLY, useWifiOnly)
    }

    fun updateLastTime(time: Long) {
        saveLong(LAST_UPDATE_TIME, time)
    }

    fun markViewed(viewed: Boolean) {
        saveBoolean(VIEWED, viewed)
    }

    fun updateAccount(account: Account) {
        val editor = mSettings.edit()
        editor.putString(ACCOUNT_NAME, account.name)
        editor.putString(ACCOUNT_TYPE, account.type)
        editor.apply()
    }

    val isLastUpdatesViewed: Boolean
        get() = mSettings.getBoolean(VIEWED, true)

    val isDriveSyncEnabled: Boolean
        get() = mSettings.getBoolean(DRIVE_SYNC, false)

    internal fun saveDriveSyncEnabled(enabled: Boolean) {
        saveBoolean(DRIVE_SYNC, enabled)
    }

    val lastDriveSyncTime: Long
        get() = mSettings.getLong(DRIVE_SYNC_TIME, -1)

    fun saveDriveSyncTime(time: Long) {
        saveLong(DRIVE_SYNC_TIME, time)
    }

    private fun saveBoolean(key: String, value: Boolean) {
        val editor = mSettings.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    private fun saveLong(key: String, value: Long) {
        val editor = mSettings.edit()
        editor.putLong(key, value)
        editor.apply()
    }


    fun useAutoSync(): Boolean {
        return mSettings.getBoolean(AUTOSYNC, true)
    }

    internal fun setUseAutoSync(useAutoSync: Boolean) {
        mSettings.edit().putBoolean(AUTOSYNC, useAutoSync).apply()
    }

    var isRequiresCharging: Boolean
        get() = mSettings.getBoolean(REQUIRES_CHARGING, false)
        internal set(requiresCharging) = mSettings.edit().putBoolean(REQUIRES_CHARGING, requiresCharging).apply()

    var sortIndex: Int
        get() = mSettings.getInt(SORT_INDEX, 0)
        set(index) = mSettings.edit().putInt(SORT_INDEX, index).apply()

    val versionCode: Int
        get() = mSettings.getInt(VERSION_CODE, 0)

    fun saveVersionCode(code: Int) {
        mSettings.edit().putInt(VERSION_CODE, code).apply()
    }

    var isNotifyInstalledUpToDate: Boolean
        get() = mSettings.getBoolean(NOTIFY_INSTALLED_UPTODATE, true)
        set(notify) = mSettings.edit().putBoolean(NOTIFY_INSTALLED_UPTODATE, notify).apply()

    var nightMode: Int
        get() = mSettings.getInt(NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_NO)
        set(nightMode) = mSettings.edit().putInt(NIGHT_MODE, nightMode).apply()

    companion object {
        private val VIEWED = "viewed"
        private val FIRST_LAUNCH = "firt_launch"
        private val LAST_UPDATE_TIME = "last_update_time"
        private val WIFI_ONLY = "wifi_only"
        private val DEVICE_ID = "device_id"
        private val DEVICE_ID_MESSAGE = "device_id_message"
        private val ACCOUNT_NAME = "account_name"
        private val ACCOUNT_TYPE = "account_type"
        private val SORT_INDEX = "sort_index"
        private val EXTRACT_DATE_LOCALES = "date_locales"

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
