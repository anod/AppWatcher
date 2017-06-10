package com.anod.appwatcher

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.support.v7.app.AppCompatDelegate
import android.view.ViewConfiguration
import com.anod.appwatcher.utils.AppDetailsUploadDate
import com.anod.appwatcher.utils.MetricsManagerEvent
import com.google.firebase.crash.FirebaseCrash
import info.anodsplace.android.log.AppLog

class AppWatcherApplication : Application(), AppLog.Listener {
    var objectGraph: ObjectGraph? = null
        private set

    override fun onCreate() {
        super.onCreate()

        try {
            val config = ViewConfiguration.get(this)
            val menuKeyField = ViewConfiguration::class.java.getDeclaredField("sHasPermanentMenuKey")
            if (menuKeyField != null) {
                menuKeyField.isAccessible = true
                menuKeyField.setBoolean(config, false)
            }
        } catch (ex: Exception) {
            // Ignore
        }

        AppLog.LOGGER = FirebaseLogger()
        AppLog.setDebug(true, "AppWatcher")
        AppLog.instance().setListener(this)
        //VolleyLog.setTag("AppWatcher");

        objectGraph = ObjectGraph(this)
        val prefs = Preferences(this)
        AppCompatDelegate.setDefaultNightMode(prefs.nightMode)
    }

    val isNightTheme: Boolean
        get() {
            val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            when (currentNightMode) {
                Configuration.UI_MODE_NIGHT_YES -> return true
                Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_UNDEFINED -> return false
            }
            return false
        }

    override fun onLogException(tr: Throwable) {
        FirebaseCrash.report(tr)
        if (tr is AppDetailsUploadDate.ExtractDateError) {
            val error = tr
            MetricsManagerEvent.track(this, "error_extract_date",
                    "LOCALE", error.locale,
                    "DEFAULT_LOCALE", error.defaultlocale,
                    "ACTUAL", error.actual,
                    "EXPECTED", error.expected,
                    "EXPECTED_FORMAT", error.expectedFormat,
                    "CUSTOM", if (error.isCustomParser) "YES" else "NO")
        }
    }

    private inner class FirebaseLogger : AppLog.Logger.Android() {
        override fun println(priority: Int, tag: String?, msg: String?) {
            super.println(priority, tag, msg)
            FirebaseCrash.logcat(priority, tag, msg)
        }
    }

    companion object {
        operator fun get(context: Context): AppWatcherApplication {
            return context.applicationContext as AppWatcherApplication
        }

        fun provide(context: Context): ObjectGraph {
            return (context.applicationContext as AppWatcherApplication).objectGraph!!
        }
    }
}
