package com.anod.appwatcher

import android.app.Activity
import android.app.Application
import android.app.NotificationManager
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.v7.app.AppCompatDelegate
import android.util.LruCache
import android.view.ViewConfiguration
import info.anodsplace.framework.AppLog
import com.android.volley.NetworkError
import com.android.volley.NoConnectionError
import com.android.volley.TimeoutError
import com.android.volley.VolleyError
import com.anod.appwatcher.sync.SyncNotification
import com.crashlytics.android.Crashlytics
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.framework.app.ApplicationInstance
import info.anodsplace.framework.app.CustomThemeActivity
import java.io.IOException
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLPeerUnverifiedException


class AppWatcherApplication : Application(), AppLog.Listener, ApplicationInstance {

    override val notificationManager: NotificationManager
        get() = objectGraph.notificationManager
    override val memoryCache: LruCache<String, Any?>
        get() = objectGraph.memoryCache
    override val nightMode: Int
        get() = objectGraph.prefs.nightMode

    lateinit var objectGraph: ObjectGraph

    override fun onCreate() {
        super.onCreate()

        tryEnableMenuOnDeviceWithHardwareMenuButton()

        AppLog.logger = FirebaseLogger()
        AppLog.setDebug(true, "AppWatcher")
        AppLog.instance.listener = this

        objectGraph = ObjectGraph(this)
        if (objectGraph.prefs.isDriveSyncEnabled) {
            objectGraph.uploadServiceContentObserver
        }
        AppCompatDelegate.setDefaultNightMode(objectGraph.prefs.nightMode)
        SyncNotification(ApplicationContext(this)).createChannel()
        registerActivityLifecycleCallbacks(LifecycleCallbacks(this))
    }

    override fun onLogException(tr: Throwable) {

        if (isNetworkError(tr) && !objectGraph.networkConnection.isNetworkAvailable) {
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
            || tr is SocketException
            || tr is NoConnectionError
            || tr is UnknownHostException
            || tr is SSLHandshakeException
            || tr is SSLPeerUnverifiedException
            || tr is ConnectException
            || tr is SocketTimeoutException
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
