package com.anod.appwatcher.utils;

import android.util.Log;

import com.android.volley.VolleyError;
import com.anod.appwatcher.BuildConfig;
import com.crashlytics.android.Crashlytics;

import java.util.IllegalFormatException;
import java.util.Locale;

public class AppLog {
	private final static String TAG = "AppWatcher";
	
	public static void d(String msg) {
		if (BuildConfig.DEBUG) Log.d(TAG, format(msg));
	}

    public static void d(final String msg, final Object... params) {
        if (BuildConfig.DEBUG) Log.d(TAG, format(msg, params));
    }

	public static void v(String msg) {
		Log.v(TAG, format(msg));
	}
	
	public static void e(String msg) {
        Log.e(TAG, format(msg));
	}

	public static void e(String msg, Throwable tr) {
        Log.e(TAG, format(msg), tr);
        Crashlytics.logException(tr);
	}

    public static void e(String msg, final Object... params) {
        Log.e(TAG, format(msg, params));
    }

    public static void e(VolleyError error) {
         e("Volley: "+error.getClass().getSimpleName()+": "+error.getMessage(),error.getCause());
    }

    public static void ex(Throwable e) { Log.e(TAG, e.getMessage(), e); }

    public static void w(String msg) {
        Log.v(TAG, format(msg));
    }


    public static void v(String msg, final Object... params) {
        Log.v(TAG, format(msg, params));
    }

    /*
    public static String format(String msg) {
    	long unixTime = System.currentTimeMillis();
        Time time = new Time();
        time.set(unixTime);

        return String.format("[%s] %s ", time.format("%d-%m-%Y %H:%M:%S"), msg);
    }
*/

    private static String format(final String msg, final Object... array) {
        String formatted;
        if (array == null) {
            formatted = msg;
        } else {
            try {
                formatted = String.format(Locale.US, msg, array);
            }
            catch (IllegalFormatException ex) {
                e("IllegalFormatException: formatString='%s' numArgs=%d", msg, array.length);
                formatted = msg + " (An error occurred while formatting the message.)";
            }
        }
        final StackTraceElement[] stackTrace = new Throwable().fillInStackTrace().getStackTrace();
        String string = "<unknown>";
        for (int i = 2; i < stackTrace.length; ++i) {
            final String className = stackTrace[i].getClassName();
            if (!className.equals(AppLog.class.getName())) {
                final String substring = className.substring(1 + className.lastIndexOf(46));
                string = substring.substring(1 + substring.lastIndexOf(36)) + "." + stackTrace[i].getMethodName();
                break;
            }
        }
        return String.format(Locale.US, "[%d] %s: %s", Thread.currentThread().getId(), string, formatted);
    }


}
