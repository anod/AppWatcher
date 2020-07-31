package com.anod.appwatcher

import android.app.Activity
import android.app.Application
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.util.LruCache
import androidx.appcompat.app.AppCompatDelegate
import androidx.work.Configuration
import com.android.volley.NetworkError
import com.android.volley.NoConnectionError
import com.android.volley.TimeoutError
import com.android.volley.VolleyError
import com.anod.appwatcher.sync.SyncNotification
import com.google.firebase.crashlytics.FirebaseCrashlytics
import info.anodsplace.framework.AppLog
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.framework.app.ApplicationInstance
import info.anodsplace.framework.app.CustomThemeActivity
import info.anodsplace.framework.app.WindowCustomTheme
import java.io.File
import java.io.IOException

class AppWatcherApplication : Application(), AppLog.Listener, ApplicationInstance, Configuration.Provider {

    override val notificationManager: NotificationManager
        get() = appComponent.notificationManager
    override val memoryCache: LruCache<String, Any?>
        get() = appComponent.memoryCache
    override val nightMode: Int
        get() = appComponent.prefs.nightMode

    val appComponent: AppComponent by lazy {
        AppComponent(this)
    }

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
                    .detectActivityLeaks()
                    .detectAll()
                    .detectNonSdkApiUsage()
                    .penaltyLog()
                    .build())
        }

        AppLog.setDebug(BuildConfig.DEBUG, "AppWatcher")

        if (appComponent.prefs.isDriveSyncEnabled) {
            appComponent.database.invalidationTracker.addObserver(appComponent.uploadServiceContentObserver)
        }

        if (appComponent.prefs.collectCrashReports) {
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
            AppLog.logger = FirebaseLogger()
            AppLog.instance.listener = this
        }

        AppCompatDelegate.setDefaultNightMode(appComponent.prefs.nightMode)
        SyncNotification(ApplicationContext(this)).createChannels()
        registerActivityLifecycleCallbacks(LifecycleCallbacks())

        deleteUserLog()
    }

    private fun deleteUserLog() {
        val userLog = File(filesDir, "user-log")
        if (userLog.exists()) {
            userLog.delete()
        }
    }

    override fun onLogException(tr: Throwable) {

        if (isNetworkError(tr) && !appComponent.networkConnection.isNetworkAvailable) {
            // Ignore
            return
        }

        if (appComponent.prefs.collectCrashReports) {
            FirebaseCrashlytics.getInstance().recordException(tr)
        }
    }

    private fun isNetworkError(tr: Throwable): Boolean {
        return tr is NetworkError
                || (tr is IOException && tr.message?.contains("NetworkError") == true)
                || tr is VolleyError
                || tr is TimeoutError
                || tr is NoConnectionError
                || appComponent.networkConnection.isNetworkException(tr)
    }

    private inner class FirebaseLogger : AppLog.Logger {
        override fun println(priority: Int, tag: String, msg: String) {
            FirebaseCrashlytics.getInstance().log("$priority/$tag: $msg")
        }
    }

    override fun getWorkManagerConfiguration() = Configuration.Builder().apply {
        setMinimumLoggingLevel(android.util.Log.DEBUG)
    }.build()
}

class LifecycleCallbacks : Application.ActivityLifecycleCallbacks {
    override fun onActivityPaused(activity: Activity?) {}
    override fun onActivityResumed(activity: Activity?) {}
    override fun onActivityStarted(activity: Activity?) {}
    override fun onActivityDestroyed(activity: Activity?) {}
    override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {}
    override fun onActivityStopped(activity: Activity?) {}

    override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
        if (activity == null) return

        if (activity is CustomThemeActivity) {
            val themeRes = activity.themeRes
            if (themeRes > 0) {
                if (activity.themeColors.available) {
                    WindowCustomTheme.apply(activity.themeColors, activity.window, activity)
                }
                activity.setTheme(themeRes)
            }
        }
    }
}
