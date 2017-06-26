package com.anod.appwatcher

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

    private var _uploadServiceContentObserver: UploadServiceContentObserver? = null
    val uploadServiceContentObserver: UploadServiceContentObserver
        get() {
            if (_uploadServiceContentObserver == null) {
                _uploadServiceContentObserver = UploadServiceContentObserver(app, app.contentResolver)
            }
            return _uploadServiceContentObserver!!
        }

    private var _accountManager: AccountManager? = null
    val accountManager: AccountManager
        get() {
            if (_accountManager == null) {
                _accountManager = AccountManager(this.app)
            }
            return _accountManager!!
        }

    private var _deviceId: String? = null
    val deviceId: String
        get() {
            if (_deviceId == null) {
                val prefs = Preferences(this.app)
                _deviceId = DeviceIdHelper.getDeviceId(this.app, prefs)
            }
            return _deviceId!!
        }

    private var _requestQueue: RequestQueue? = null
    val requestQueue: RequestQueue
        get() {
            if (_requestQueue == null) {
                _requestQueue = RequestQueue(NoCache(), Network(), 2)
                _requestQueue!!.start()
            }
            return _requestQueue!!
        }

    private var _iconLoader: AppIconLoader? = null
    val  iconLoader: AppIconLoader
        get() {
            if (_iconLoader == null) {
                _iconLoader = AppIconLoader(this.app)
            }
            return _iconLoader!!
        }

    val gcmNetworkManager: GcmNetworkManager
        get() {
            return GcmNetworkManager.getInstance(this.app)
        }

    private var _fireBaseAnalytics: FirebaseAnalytics? = null
    val fireBase: FirebaseAnalytics
        get() {
            if (_fireBaseAnalytics == null) {
                _fireBaseAnalytics = FirebaseAnalytics.getInstance(this.app)
            }
            return _fireBaseAnalytics!!
        }
}
