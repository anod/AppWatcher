package com.anod.appwatcher;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {
	private static final String VIEWED = "viewed";
	private static final String FIRT_LAUNCH = "firt_launch";
	private static final String LAST_UPDATE_TIME = "last_update_time";
	private static final String WIFI_ONLY = "wifi_only";
	private static final String DEVICE_ID = "device_id";
	private static final String DEVICE_ID_MESSAGE = "device_id_message";
	
	private static final String PREFS_NAME = "WatcherPrefs";
	
	private SharedPreferences mSettings;
	
	public Preferences(Context context) {
		mSettings = context.getSharedPreferences(PREFS_NAME, 0);		
	}

	public boolean checkFirstLaunch() {
		boolean value = mSettings.getBoolean(FIRT_LAUNCH, true);
		if (value) {
			SharedPreferences.Editor editor = mSettings.edit();
			editor.putBoolean(FIRT_LAUNCH, false);
			editor.commit();	
		}
		return value;
	}
	
	public long getLastUpdateTime() {
		return mSettings.getLong(LAST_UPDATE_TIME, -1);
	}
	
	public boolean isDeviceIdMessageEnabled() {
		return mSettings.getBoolean(DEVICE_ID_MESSAGE, true);
	}
	
	public void disableDeviceIdMessage() {
		SharedPreferences.Editor editor = mSettings.edit();
		editor.putBoolean(DEVICE_ID_MESSAGE, false);
		editor.commit();		
	}
	
	public String getDeviceId() {
		return mSettings.getString(DEVICE_ID, null);
	}
	
	public boolean isWifiOnly() {
		return mSettings.getBoolean(WIFI_ONLY, false);
	}
	
	public void saveDeviceId(String deviceId) {
		SharedPreferences.Editor editor = mSettings.edit();
		if (deviceId == null || deviceId.length() == 0 ) {
			editor.remove(DEVICE_ID);
		} else {
			editor.putString(DEVICE_ID, deviceId);
		}
		editor.commit();		
	}
	
	public void saveWifiOnly(boolean useWifiOnly) {
		SharedPreferences.Editor editor = mSettings.edit();
		editor.putBoolean(WIFI_ONLY, useWifiOnly);
		editor.commit();
	}

	public void updateLastTime(long time) {
		SharedPreferences.Editor editor = mSettings.edit();
		editor.putLong(LAST_UPDATE_TIME, time);
		editor.commit();
	}

	public void markViewed(boolean viewed) {
		SharedPreferences.Editor editor = mSettings.edit();
		editor.putBoolean(VIEWED, viewed);
		editor.commit();
	}

	public boolean isLastUpdatesViewed() {
		return mSettings.getBoolean(VIEWED, true);
	}
}
