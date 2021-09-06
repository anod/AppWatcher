package com.anod.appwatcher

import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.TelephonyManager
import android.util.LruCache
import com.android.volley.RequestQueue
import com.android.volley.toolbox.NoCache
import com.anod.appwatcher.backup.gdrive.UploadServiceContentObserver
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.utils.PicassoAppIcon
import com.anod.appwatcher.watchlist.RecentlyInstalledPackages
import info.anodsplace.framework.net.NetworkConnectivity
import info.anodsplace.framework.util.createLruCache
import info.anodsplace.playstore.DeviceId
import info.anodsplace.playstore.DeviceInfoProvider
import info.anodsplace.playstore.Network
import info.anodsplace.playstore.OnlineNetwork
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull

/**
 * @author alex
 * *
 * @date 2015-02-22
 */
class AppComponent internal constructor(private val app: AppWatcherApplication) : DeviceInfoProvider {

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
        createLruCache()
    }
    val notificationManager: NotificationManager
        get() = app.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val telephonyManager: TelephonyManager
        get() = app.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    val deviceInfo: DeviceInfoProvider
        get() = this

    val packageManager: PackageManager
        get() = app.packageManager

    val database: AppsDatabase by lazy { AppsDatabase.instance(app) }

    val appScope: CoroutineScope by lazy { CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate) }

    val packageChangedReceiver = MutableSharedFlow<String?>()
    val packageChanged: Flow<String> = packageChangedReceiver.filterNotNull().distinctUntilChanged()
    val recentlyInstalledPackages: RecentlyInstalledPackages by lazy { RecentlyInstalledPackages(packageManager, database) }
}