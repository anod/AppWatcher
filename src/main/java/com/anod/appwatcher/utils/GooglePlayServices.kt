package com.anod.appwatcher.utils

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import com.anod.appwatcher.R
import com.anod.appwatcher.ui.ActivityListener
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import info.anodsplace.android.log.AppLog
import java.util.concurrent.CountDownLatch

/**
 * @author alex
 * *
 * @date 7/30/14.
 */
abstract class GooglePlayServices : GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ActivityListener.ResultListener {
    private val activity: Activity?
    protected val mContext: Context
    protected var mGoogleApiClient: GoogleApiClient? = null
    private var mOnConnectAction: Int = 0
    private var mResolutionIntent: PendingIntent? = null

    class ResolutionException internal constructor(message: String, val resolution: PendingIntent) : Exception(message)

    constructor(activity: Activity) {
        mContext = activity.applicationContext
        this.activity = activity
    }

    constructor(context: Context) {
        mContext = context.applicationContext
        activity = null
    }

    fun connect() {
        if (!isConnected) {
            connectWithAction(ACTION_CONNECT)
        } else {
            onConnectAction(ACTION_CONNECT)
        }
    }

    @Throws(Exception::class)
    fun connectLocked() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = createGoogleApiClientBuilder().build()
        }
        mResolutionIntent = null
        val latch = CountDownLatch(1)
        mGoogleApiClient!!.registerConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {
            override fun onConnectionSuspended(cause: Int) {}

            override fun onConnected(arg0: Bundle?) {
                latch.countDown()
            }
        })

        mGoogleApiClient!!.registerConnectionFailedListener { result ->
            mResolutionIntent = result.resolution
            latch.countDown()
            AppLog.e(result.toString())
        }
        mGoogleApiClient!!.connect()
        latch.await()
        if (!mGoogleApiClient!!.isConnected) {
            val resolution = if (mResolutionIntent == null) "none" else mResolutionIntent!!.toString()
            throw ResolutionException("Cannot connect to Google Play Services. See log. Resolution: " + resolution, mResolutionIntent!!)
        }
    }

    fun disconnect() {
        mGoogleApiClient?.disconnect()
    }

    val isSupported: Boolean
        get() = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(mContext) == ConnectionResult.SUCCESS

    override fun onConnected(bundle: Bundle?) {
        onConnectAction(mOnConnectAction)
    }

    override fun onConnectionSuspended(i: Int) {

    }


    override fun onConnectionFailed(result: ConnectionResult) {
        AppLog.e("GoogleApiClient connection failed: " + result.toString())

        if (!result.hasResolution()) {
            // show the localized error dialog.
            onConnectionError()
            GoogleApiAvailability.getInstance().getErrorDialog(activity, result.errorCode, 0).show()
            return
        }
        try {
            result.startResolutionForResult(activity, REQUEST_CODE_RESOLUTION)
        } catch (e: IntentSender.SendIntentException) {
            AppLog.e(e)
            onConnectionError()
        }

    }


    val isConnected: Boolean
        get() = mGoogleApiClient != null && mGoogleApiClient!!.isConnected

    /**
     * @return
     */
    val playServiceStatusText: String
        get() {
            val errorCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(mContext)
            if (errorCode == ConnectionResult.SERVICE_MISSING) {
                return mContext.getString(R.string.gms_service_missing)
            }
            if (errorCode == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED) {
                return mContext.getString(R.string.gms_service_update_required)
            }
            if (errorCode == ConnectionResult.SERVICE_DISABLED) {
                return mContext.getString(R.string.gms_service_disabled)
            }
            if (errorCode == ConnectionResult.SERVICE_INVALID) {
                return mContext.getString(R.string.gms_service_invalid)
            }
            return GoogleApiAvailability.getInstance().getErrorString(errorCode)
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == REQUEST_CODE_RESOLUTION && resultCode == Activity.RESULT_OK) {
            connectWithAction(mOnConnectAction)
        }
    }


    protected fun connectWithAction(action: Int) {
        mOnConnectAction = action
        if (mGoogleApiClient == null) {

            mGoogleApiClient = createGoogleApiClientBuilder()
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build()
        }
        mGoogleApiClient!!.connect()
    }

    protected abstract fun onConnectAction(action: Int)
    protected abstract fun createGoogleApiClientBuilder(): GoogleApiClient.Builder
    protected abstract fun onConnectionError()

    companion object {

        internal val REQUEST_CODE_RESOLUTION = 123

        private val ACTION_CONNECT = 1
    }


}
