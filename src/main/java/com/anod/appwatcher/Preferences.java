package com.anod.appwatcher;

import android.accounts.Account;
import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {
    private static final String VIEWED = "viewed";
    private static final String FIRST_LAUNCH = "firt_launch";
    private static final String LAST_UPDATE_TIME = "last_update_time";
    private static final String WIFI_ONLY = "wifi_only";
    private static final String DEVICE_ID = "device_id";
    private static final String DEVICE_ID_MESSAGE = "device_id_message";
    private static final String ACCOUNT_NAME = "account_name";
    private static final String ACCOUNT_TYPE = "account_type";


    private static final String PREFS_NAME = "WatcherPrefs";
    private static final String DRIVE_SYNC = "drive_sync";
    private static final String DRIVE_SYNC_TIME = "drive_sync_time";
    private static final String AUTOSYNC = "autosync";
    private static final String REQUIRES_CHARGING = "requires-charging";

    private SharedPreferences mSettings;

    public Preferences(Context context) {
        mSettings = context.getSharedPreferences(PREFS_NAME, 0);
    }

    public Account getAccount() {
        String name = mSettings.getString(ACCOUNT_NAME, null);
        if (name == null) {
            return null;
        }
        String type = mSettings.getString(ACCOUNT_TYPE, null);
        return new Account(name, type);
    }

    public boolean checkFirstLaunch() {
        boolean value = mSettings.getBoolean(FIRST_LAUNCH, true);
        if (value) {
            saveBoolean(FIRST_LAUNCH, false);
        }
        return value;
    }

    public long getLastUpdateTime() {
        return mSettings.getLong(LAST_UPDATE_TIME, -1);
    }


    public String getDeviceId() {
        return mSettings.getString(DEVICE_ID, null);
    }

    public boolean isWifiOnly() {
        return mSettings.getBoolean(WIFI_ONLY, false);
    }

    public void saveDeviceId(String deviceId) {
        SharedPreferences.Editor editor = mSettings.edit();
        if (deviceId == null || deviceId.length() == 0) {
            editor.remove(DEVICE_ID);
        } else {
            editor.putString(DEVICE_ID, deviceId);
        }
        editor.apply();
    }

    public void saveWifiOnly(boolean useWifiOnly) {
        saveBoolean(WIFI_ONLY, useWifiOnly);
    }


    public void updateLastTime(long time) {
        saveLong(LAST_UPDATE_TIME, time);
    }

    public void markViewed(boolean viewed) {
        saveBoolean(VIEWED, viewed);
    }

    public void updateAccount(Account account) {
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString(ACCOUNT_NAME, account.name);
        editor.putString(ACCOUNT_TYPE, account.type);
        editor.apply();
    }

    public boolean isLastUpdatesViewed() {
        return mSettings.getBoolean(VIEWED, true);
    }

    public boolean isDriveSyncEnabled() {
        return mSettings.getBoolean(DRIVE_SYNC, false);
    }

    public void saveDriveSyncEnabled(boolean enabled) {
        saveBoolean(DRIVE_SYNC, enabled);
    }

    public long getLastDriveSyncTime() {
        return mSettings.getLong(DRIVE_SYNC_TIME, -1);
    }

    public void saveDriveSyncTime(long time) {
        saveLong(DRIVE_SYNC_TIME, time);
    }

    private void saveBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    private void saveLong(String key, long value) {
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putLong(key, value);
        editor.apply();
    }


    public boolean useAutoSync() {
        return mSettings.getBoolean(AUTOSYNC, true);
    }

    public void setUseAutoSync(boolean useAutoSync) {
        mSettings.edit().putBoolean(AUTOSYNC, useAutoSync).apply();
    }

    public void setRequiresCharging(boolean requiresCharging) {
        mSettings.edit().putBoolean(REQUIRES_CHARGING, requiresCharging).apply();
    }

    public boolean isRequiresCharging() {
        return mSettings.getBoolean(REQUIRES_CHARGING, false);
    }
}
