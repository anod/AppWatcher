package com.anod.appwatcher

import android.app.Application
import android.content.res.Configuration
import android.support.v7.app.AppCompatDelegate
import android.view.ViewConfiguration
import com.anod.appwatcher.utils.AppDetailsUploadDate
import com.anod.appwatcher.utils.MetricsManagerEvent
import com.google.firebase.crash.FirebaseCrash
import info.anodsplace.android.log.AppLog
import com.android.volley.NetworkError
import com.android.volley.NoConnectionError
import com.android.volley.TimeoutError
import com.android.volley.VolleyError
import com.anod.appwatcher.sync.SyncNotification
import java.io.IOException
import java.net.SocketException


class AppWatcherApplication : Application(), AppLog.Listener {
    lateinit var objectGraph: ObjectGraph

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
        if (objectGraph.prefs.isDriveSyncEnabled) {
            objectGraph.uploadServiceContentObserver
        }
        AppCompatDelegate.setDefaultNightMode(objectGraph.prefs.nightMode)
        SyncNotification(this).createChannel()
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

    val isNetworkAvailable: Boolean
        get() = objectGraph.connectivityManager.activeNetworkInfo?.isConnectedOrConnecting ?:false

    override fun onLogException(tr: Throwable) {

        if (isNetworkError(tr) && !isNetworkAvailable) {
            // Ignore
            return
        }

        MetricsManagerEvent.track(this, "log_exception",
            "CLASS_NAME", "${tr::class.java}",
            "MESSAGE", tr.message ?: "empty",
            "NETWORK_AVAILABLE", isNetworkAvailable.toString()
        )

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

    private fun isNetworkError(tr: Throwable): Boolean {
        return tr is NetworkError
                || (tr is IOException && tr.message?.contains("NetworkError") == true)
                || tr is VolleyError
                || tr is TimeoutError
                || tr is SocketException
                || tr is NoConnectionError
    }

    private inner class FirebaseLogger : AppLog.Logger.Android() {
        override fun println(priority: Int, tag: String, msg: String) {
            super.println(priority, tag, msg)
            FirebaseCrash.logcat(priority, tag, msg)
        }
    }

}
