package com.anod.appwatcher;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatDelegate;
import android.view.ViewConfiguration;

import com.android.volley.VolleyLog;
import com.anod.appwatcher.utils.AppDetailsUploadDate;
import com.anod.appwatcher.utils.MetricsManagerEvent;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;
import com.squareup.leakcanary.LeakCanary;

import java.lang.reflect.Field;
import java.util.Locale;

import info.anodsplace.android.log.AppLog;

public class AppWatcherApplication extends Application implements AppLog.Listener {
    private ObjectGraph mObjectGraph;

    @Override
    public void onCreate() {
        super.onCreate();
        LeakCanary.install(this);

        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ex) {
            // Ignore
        }

        AppLog.LOGGER = new FirebaseLogger();
        AppLog.setDebug(true, "AppWatcher");
        AppLog.instance().setListener(this);
        //VolleyLog.setTag("AppWatcher");

        mObjectGraph = new ObjectGraph(this);
        Preferences prefs = new Preferences(this);
        AppCompatDelegate.setDefaultNightMode(prefs.getNightMode());
    }

    public ObjectGraph getObjectGraph() {
        return mObjectGraph;
    }

    public static AppWatcherApplication get(Context context) {
        return (AppWatcherApplication) context.getApplicationContext();
    }

    public static ObjectGraph provide(Context context) {
        return ((AppWatcherApplication) context.getApplicationContext()).getObjectGraph();
    }

    public boolean isNightTheme() {
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (currentNightMode) {
            case Configuration.UI_MODE_NIGHT_YES:
                return true;
            case Configuration.UI_MODE_NIGHT_NO:
            case Configuration.UI_MODE_NIGHT_UNDEFINED:
                return false;
        }
        return false;
    }

    @Override
    public void onLogException(Throwable tr) {
        FirebaseCrash.report(tr);
        if (tr instanceof AppDetailsUploadDate.ExtractDateError) {
            AppDetailsUploadDate.ExtractDateError error = (AppDetailsUploadDate.ExtractDateError) tr;
            MetricsManagerEvent.track(this, "error_extract_date",
                    "LOCALE", error.locale,
                    "DEFAULT_LOCALE", error.defaultlocale,
                    "ACTUAL", error.actual,
                    "EXPECTED", error.expected,
                    "EXPECTED_FORMAT", error.expectedFormat,
                    "CUSTOM", error.isCustomParser? "YES" : "NO");
        }
    }

    private class FirebaseLogger extends AppLog.Logger.Android {
        @Override
        public void println(int priority, String tag, String msg) {
            super.println(priority, tag, msg);
            FirebaseCrash.logcat(priority, tag, msg);
        }
    }
}
