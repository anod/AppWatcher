package info.anodsplace.framework.net

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLPeerUnverifiedException

class NetworkConnectivity(private val context: Context) {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val isNetworkAvailable: Boolean
        @SuppressLint("MissingPermission")
        get() = connectivityManager.activeNetworkInfo?.isConnected == true

    val isWifiEnabled: Boolean
        @SuppressLint("MissingPermission")
        get() = connectivityManager.activeNetworkInfo?.type == ConnectivityManager.TYPE_WIFI

    fun register(broadcastReceiver: NetworkConnectivityChange) {
        context.registerReceiver(broadcastReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    }

    fun unregister(broadcastReceiver: NetworkConnectivityChange) {
        context.unregisterReceiver(broadcastReceiver)
    }

    class NetworkConnectivityChange(private val listener: Listener): BroadcastReceiver() {

        interface Listener {
            fun onNetworkConnectivity(available: Boolean)
        }

        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent?) {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            listener.onNetworkConnectivity(connectivityManager.activeNetworkInfo?.isConnected == true)
        }
    }

    fun isNetworkException(tr: Throwable): Boolean {
        return tr is SocketException
                || tr is UnknownHostException
                || tr is SSLHandshakeException
                || tr is SSLPeerUnverifiedException
                || tr is ConnectException
                || tr is SocketTimeoutException
    }
}