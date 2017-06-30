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
class GooglePlayServices(context: Context, private val listener: Listener) : GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ActivityListener.ResultListener {
    private val activity: Activity? = if (context is Activity) context else null
    private val context: Context = context.applicationContext
    val googleApiClient: GoogleApiClient by lazy {
        listener.createGoogleApiClientBuilder()
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .build()
    }
    private var onConnectAction: Int = 0
    private var resolutionIntent: PendingIntent? = null


    interface Listener {
        fun onConnectAction(action: Int)
        fun createGoogleApiClientBuilder(): GoogleApiClient.Builder
        fun onConnectionError()
        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    }

    class ResolutionException internal constructor(message: String, val resolution: PendingIntent) : Exception(message)

    val isConnected: Boolean
        get() = googleApiClient.isConnected

    val isSupported: Boolean
        get() = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS

    fun connect() {
        if (!isConnected) {
            connectWithAction(ACTION_CONNECT)
        } else {
            listener.onConnectAction(ACTION_CONNECT)
        }
    }

    @Throws(Exception::class)
    fun connectLocked() {
        resolutionIntent = null
        val latch = CountDownLatch(1)
        googleApiClient.registerConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {
            override fun onConnectionSuspended(cause: Int) {}

            override fun onConnected(arg0: Bundle?) {
                latch.countDown()
            }
        })

        googleApiClient.registerConnectionFailedListener { result ->
            resolutionIntent = result.resolution
            latch.countDown()
            AppLog.e(result.toString())
        }
        googleApiClient.connect()
        latch.await()
        if (!googleApiClient.isConnected) {
            val resolution = if (resolutionIntent == null) "none" else resolutionIntent!!.toString()
            throw ResolutionException("Cannot connect to Google Play Services. See log. Resolution: " + resolution, resolutionIntent!!)
        }
    }

    fun disconnect() {
        googleApiClient.disconnect()
    }

    override fun onConnected(bundle: Bundle?) {
        listener.onConnectAction(onConnectAction)
    }

    override fun onConnectionSuspended(i: Int) {

    }

    override fun onConnectionFailed(result: ConnectionResult) {
        AppLog.e("GoogleApiClient connection failed: " + result.toString())

        if (!result.hasResolution()) {
            // show the localized error dialog.
            listener.onConnectionError()
            GoogleApiAvailability.getInstance().getErrorDialog(activity, result.errorCode, 0).show()
            return
        }
        try {
            result.startResolutionForResult(activity, REQUEST_CODE_RESOLUTION)
        } catch (e: IntentSender.SendIntentException) {
            AppLog.e(e)
            listener.onConnectionError()
        }

    }

    /**
     * @return
     */
    val errorCodeText: String
        get() {
            val errorCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
            if (errorCode == ConnectionResult.SERVICE_MISSING) {
                return context.getString(R.string.gms_service_missing)
            }
            if (errorCode == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED) {
                return context.getString(R.string.gms_service_update_required)
            }
            if (errorCode == ConnectionResult.SERVICE_DISABLED) {
                return context.getString(R.string.gms_service_disabled)
            }
            if (errorCode == ConnectionResult.SERVICE_INVALID) {
                return context.getString(R.string.gms_service_invalid)
            }
            return GoogleApiAvailability.getInstance().getErrorString(errorCode)
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_RESOLUTION && resultCode == Activity.RESULT_OK) {
            connectWithAction(onConnectAction)
        }
    }


    fun connectWithAction(action: Int) {
        onConnectAction = action
        googleApiClient.connect()
    }

    companion object {
        internal const val REQUEST_CODE_RESOLUTION = 123
        private const val ACTION_CONNECT = 1
    }

}
