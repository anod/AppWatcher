package com.anod.appwatcher

import android.app.Activity
import android.app.Application
import android.app.NotificationManager
import android.os.Bundle
import android.support.v7.app.AppCompatDelegate
import android.util.LruCache
import android.view.ViewConfiguration
import com.android.volley.NetworkError
import com.android.volley.NoConnectionError
import com.android.volley.TimeoutError
import com.android.volley.VolleyError
import com.anod.appwatcher.sync.SyncNotification
import com.crashlytics.android.Crashlytics
import info.anodsplace.framework.AppLog
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.framework.app.ApplicationInstance
import info.anodsplace.framework.app.CustomThemeActivity
import io.fabric.sdk.android.Fabric
import java.io.IOException



class AppWatcherApplication : Application(), AppLog.Listener, ApplicationInstance {

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

        tryEnableMenuOnDeviceWithHardwareMenuButton()

        AppLog.logger = FirebaseLogger()
        AppLog.setDebug(true, "AppWatcher")
        AppLog.instance.listener = this

        if (appComponent.prefs.isDriveSyncEnabled) {
            appComponent.uploadServiceContentObserver
        }

        Fabric.with(this, Crashlytics())

        AppCompatDelegate.setDefaultNightMode(appComponent.prefs.nightMode)
        SyncNotification(ApplicationContext(this)).createChannels()
        registerActivityLifecycleCallbacks(LifecycleCallbacks(this))
    }

    override fun onLogException(tr: Throwable) {

        if (isNetworkError(tr) && !appComponent.networkConnection.isNetworkAvailable) {
            // Ignore
            return
        }

        Crashlytics.logException(tr)
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
            Crashlytics.log(priority, tag, msg)
        }
    }

    private fun tryEnableMenuOnDeviceWithHardwareMenuButton() {
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
    }

}

class LifecycleCallbacks(private val app: AppWatcherApplication) : Application.ActivityLifecycleCallbacks {
    override fun onActivityPaused(activity: Activity?) {
    }

    override fun onActivityResumed(activity: Activity?) {
    }

    override fun onActivityStarted(activity: Activity?) {
    }

    override fun onActivityDestroyed(activity: Activity?) {
    }

    override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
    }

    override fun onActivityStopped(activity: Activity?) {
    }

    override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
        if (activity == null) return

        if (activity is CustomThemeActivity) {
            val themeRes = activity.themeRes
            if (themeRes > 0) {
                activity.setTheme(themeRes)
            }
        }
    }


}
