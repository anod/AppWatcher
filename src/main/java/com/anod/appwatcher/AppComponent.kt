package com.anod.appwatcher

import android.app.NotificationManager
import android.content.Context
import android.telephony.TelephonyManager
import android.util.LruCache
import com.android.volley.RequestQueue
import com.android.volley.toolbox.NoCache
import com.anod.appwatcher.backup.gdrive.UploadServiceContentObserver
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.utils.PicassoAppIcon
import com.squareup.picasso.Picasso
import info.anodsplace.framework.net.NetworkConnectivity
import info.anodsplace.playstore.DeviceId
import info.anodsplace.playstore.DeviceInfoProvider
import info.anodsplace.playstore.Network
import info.anodsplace.playstore.OnlineNetwork

/**
 * @author alex
 * *
 * @date 2015-02-22
 */
class AppComponent internal constructor(private val app: AppWatcherApplication): DeviceInfoProvider {

    override val deviceId: String by lazy { DeviceId(this.app, prefs).load() }
    override val simOperator: String
        get() = telephonyManager.simOperator

    val prefs = Preferences(app)

    val uploadServiceContentObserver: UploadServiceContentObserver by lazy { UploadServiceContentObserver(app) }

    val requestQueue: RequestQueue by lazy {
       val requestQueue = RequestQueue(NoCache(), OnlineNetwork(networkConnection, Network()), 2)
        requestQueue.start()
        requestQueue
    }
    val iconLoader: PicassoAppIcon by lazy { PicassoAppIcon(this.app) }
    val networkConnection: NetworkConnectivity by lazy {
        NetworkConnectivity(app)
    }
    val memoryCache: LruCache<String, Any?> by lazy {
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024)
        // Use 1/8th of the available memory for this memory cache.
        val cacheSize = maxMemory / 8
        LruCache<String, Any?>(cacheSize.toInt())
    }
    val notificationManager: NotificationManager
        get() = app.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val telephonyManager: TelephonyManager
        get() = app.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    val picasso: Picasso by lazy { Picasso.Builder(app).build(); }

    val deviceInfo: DeviceInfoProvider
        get() = this

    val database: AppsDatabase by lazy { AppsDatabase.instance(app) }
}
