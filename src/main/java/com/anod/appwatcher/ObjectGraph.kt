package com.anod.appwatcher

import android.content.Context
import android.net.ConnectivityManager
import com.android.volley.RequestQueue
import com.android.volley.toolbox.NoCache
import com.anod.appwatcher.accounts.AccountManager
import com.anod.appwatcher.backup.gdrive.UploadServiceContentObserver
import com.anod.appwatcher.market.DeviceIdHelper
import com.anod.appwatcher.market.Network
import com.anod.appwatcher.utils.AppIconLoader
import com.google.android.gms.gcm.GcmNetworkManager
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * @author alex
 * *
 * @date 2015-02-22
 */
class ObjectGraph internal constructor(private val app: AppWatcherApplication) {

    val prefs = Preferences(app)
    val uploadServiceContentObserver: UploadServiceContentObserver by lazy {UploadServiceContentObserver(app, app.contentResolver) }
    val accountManager: AccountManager by lazy { AccountManager(this.app, prefs) }
    val deviceId: String by lazy { DeviceIdHelper.getDeviceId(this.app, prefs) }
    val requestQueue: RequestQueue by lazy {
       val _requestQueue = RequestQueue(NoCache(), Network(), 2)
        _requestQueue.start()
        _requestQueue
    }
    val iconLoader: AppIconLoader by lazy { AppIconLoader(this.app) }
    val gcmNetworkManager: GcmNetworkManager
        get() {
            return GcmNetworkManager.getInstance(this.app)
        }
    val fireBase: FirebaseAnalytics by lazy { FirebaseAnalytics.getInstance(this.app) }
    val connectivityManager: ConnectivityManager
        get() = app.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
}
