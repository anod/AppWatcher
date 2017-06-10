package com.anod.appwatcher

import com.android.volley.RequestQueue
import com.android.volley.toolbox.NoCache
import com.anod.appwatcher.accounts.AccountManager
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
    private var mRequestQueue: RequestQueue? = null
    private var mDeviceId: String? = null
    private var mAccountManager: AccountManager? = null
    private var mIconLoader: AppIconLoader? = null
    private var mFirebaseAnalytics: FirebaseAnalytics? = null

    fun accountManager(): AccountManager {
        if (mAccountManager == null) {
            mAccountManager = AccountManager(this.app)
        }
        return mAccountManager!!
    }

    fun deviceId(): String {
        if (mDeviceId == null) {
            val prefs = Preferences(this.app)
            mDeviceId = DeviceIdHelper.getDeviceId(this.app, prefs)
        }
        return mDeviceId!!
    }

    fun requestQueue(): RequestQueue {
        if (mRequestQueue == null) {
            mRequestQueue = RequestQueue(NoCache(), Network(), 2)
            mRequestQueue!!.start()
        }
        return mRequestQueue!!
    }

    fun iconLoader(): AppIconLoader {
        if (mIconLoader == null) {
            mIconLoader = AppIconLoader(this.app)
        }
        return mIconLoader!!
    }

    fun gcmNetworkManager(): GcmNetworkManager {
        return GcmNetworkManager.getInstance(this.app)
    }

    fun firebase(): FirebaseAnalytics {
        if (mFirebaseAnalytics == null) {
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(this.app)
        }
        return mFirebaseAnalytics!!
    }
}
