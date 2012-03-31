package com.anod.appwatcher;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {
	private static final String DEVICE_ID = "device_id";
	private static final String DEVICE_ID_MESSAGE = "device_id_message";
	
	private static final String PREFS_NAME = "WatcherPrefs";
	
	private SharedPreferences mSettings;
	
	public Preferences(Context context) {
		mSettings = context.getSharedPreferences(PREFS_NAME, 0);		
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
	
	public void saveDeviceId(String deviceId) {
		SharedPreferences.Editor editor = mSettings.edit();
		if (deviceId == null || deviceId.length() == 0 ) {
			editor.remove(DEVICE_ID);
		} else {
			editor.putString(DEVICE_ID, deviceId);
		}
		editor.commit();		
	}	
}
