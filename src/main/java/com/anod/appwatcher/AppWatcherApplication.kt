package com.anod.appwatcher

import android.app.Application
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.support.v7.app.AppCompatDelegate
import android.util.LruCache
import android.view.ViewConfiguration
import com.google.firebase.crash.FirebaseCrash
import info.anodsplace.android.log.AppLog
import com.android.volley.NetworkError
import com.android.volley.NoConnectionError
import com.android.volley.TimeoutError
import com.android.volley.VolleyError
import com.anod.appwatcher.sync.SyncNotification
import info.anodsplace.appwatcher.framework.ApplicationContext
import info.anodsplace.appwatcher.framework.ApplicationInstance
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

        AppLog.LOGGER = FirebaseLogger()
        AppLog.setDebug(true, "AppWatcher")
        AppLog.instance().setListener(this)
        //VolleyLog.setTag("AppWatcher");

        objectGraph = ObjectGraph(this)
        if (objectGraph.prefs.isDriveSyncEnabled) {
            objectGraph.uploadServiceContentObserver
        }
        AppCompatDelegate.setDefaultNightMode(objectGraph.prefs.nightMode)
        SyncNotification(ApplicationContext(this)).createChannel()
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
        get() = objectGraph.connectivityManager.activeNetworkInfo?.isConnectedOrConnecting == true

    val isWifiEnabled: Boolean
        get() = objectGraph.connectivityManager.activeNetworkInfo?.type == ConnectivityManager.TYPE_WIFI

    override fun onLogException(tr: Throwable) {

        if (isNetworkError(tr) && !isNetworkAvailable) {
            // Ignore
            return
        }

        FirebaseCrash.report(tr)
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

    private inner class FirebaseLogger : AppLog.Logger.Android() {
        override fun println(priority: Int, tag: String, msg: String) {
            super.println(priority, tag, msg)
            FirebaseCrash.logcat(priority, tag, msg)
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
