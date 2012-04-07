package com.anod.appwatcher.utils;

import android.util.Log;

import com.anod.appwatcher.BuildConfig;

public class AppLog {
	private final static String TAG = "AppWatcher";
	
	public static void d(String msg) {
		if (BuildConfig.DEBUG) Log.d(TAG, msg);
	}
	
	public static void v(String msg) {
		Log.v(TAG, msg);
	}
	
	public static void e(String msg) {
		Log.e(TAG, msg);
	}	
}
