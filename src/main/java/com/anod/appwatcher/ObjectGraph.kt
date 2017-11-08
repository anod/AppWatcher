package com.anod.appwatcher

import android.content.Context
import android.net.ConnectivityManager
import android.util.LruCache
import com.android.volley.RequestQueue
import com.android.volley.toolbox.NoCache
import com.anod.appwatcher.backup.gdrive.UploadServiceContentObserver
import com.anod.appwatcher.market.DeviceId
import com.anod.appwatcher.market.Network
import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.utils.PicassoAppIcon
import com.firebase.jobdispatcher.FirebaseJobDispatcher
import com.firebase.jobdispatcher.GooglePlayDriver
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * @author alex
 * *
 * @date 2015-02-22
 */
class ObjectGraph internal constructor(private val app: AppWatcherApplication) {

    val prefs = Preferences(app)
    val uploadServiceContentObserver: UploadServiceContentObserver by lazy {UploadServiceContentObserver(app, app.contentResolver) }
    val deviceId: String by lazy { DeviceId(this.app, prefs).load() }
    val requestQueue: RequestQueue by lazy {
       val requestQueue = RequestQueue(NoCache(), Network(), 2)
        requestQueue.start()
        requestQueue
    }
    val iconLoader: PicassoAppIcon by lazy { PicassoAppIcon(this.app) }
    val fireBase: FirebaseAnalytics by lazy { FirebaseAnalytics.getInstance(this.app) }
    val connectivityManager: ConnectivityManager
        get() = app.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val jobDispatcher: FirebaseJobDispatcher by lazy { FirebaseJobDispatcher(GooglePlayDriver(app)) }
    val memoryCache: LruCache<String, Any?> by lazy {
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024)
        // Use 1/8th of the available memory for this memory cache.
        val cacheSize = maxMemory / 8
        LruCache<String, Any?>(cacheSize.toInt())
    }
}
