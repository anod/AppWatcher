package com.anod.appwatcher

import android.net.ConnectivityManager

class NetworkConnection(private val connectivityManager: ConnectivityManager) {

    val isNetworkAvailable: Boolean
        get() = connectivityManager.activeNetworkInfo?.isConnected == true

    val isWifiEnabled: Boolean
        get() = connectivityManager.activeNetworkInfo?.type == ConnectivityManager.TYPE_WIFI

}