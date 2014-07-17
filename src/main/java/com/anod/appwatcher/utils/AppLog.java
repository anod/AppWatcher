package com.anod.appwatcher.utils;

import android.content.IntentSender;
import android.text.format.Time;
import android.util.Log;

import com.anod.appwatcher.BuildConfig;

public class AppLog {
	private final static String TAG = "AppWatcher";
	
	public static void d(String msg) {
		if (BuildConfig.DEBUG) Log.d(TAG, format(msg));
	}
	
	public static void v(String msg) {
		Log.v(TAG, format(msg));
	}
	
	public static void e(String msg) {
		Log.e(TAG, format(msg));
	}

	public static void e(String msg, Throwable tr) {
		Log.e(TAG, format(msg), tr);
	}

    public static void ex(Throwable e) { Log.e(TAG, e.getMessage(), e); }

    public static String format(String msg) {
    	long unixTime = System.currentTimeMillis();
        Time time = new Time();
        time.set(unixTime);

        return String.format("[%s] %s ", time.format("%d-%m-%Y %H:%M:%S"), msg);
    }

}
