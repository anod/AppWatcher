package finsky.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo

class NetworkType(networkInfo: NetworkInfo?) {

    constructor(context: Context) : this(NetworkStateChangedReceiver.getCachedNetworkInfo(context))

    val value: Int = getNetworkTypeFromNetworkInfo(networkInfo)

    private fun getNetworkTypeFromNetworkInfo(networkInfo: NetworkInfo?): Int {
        if (networkInfo != null) {

            if (networkInfo.type == ConnectivityManager.TYPE_WIFI) {
                return 4
            }
            if (networkInfo.type == ConnectivityManager.TYPE_WIMAX) {
                return 3
            }
            if (networkInfo.type == ConnectivityManager.TYPE_MOBILE) {
                return when (networkInfo.subtype) {
                    1, 2, 4, 7, 11 -> {
                        1
                    }
                    3, 5, 6, 8, 9, 10, 12, 14, 15 -> {
                        2
                    }
                    13 -> {
                        3
                    }
                    else -> {
                        5
                    }
                }
            } else {
                if (networkInfo.type == ConnectivityManager.TYPE_ETHERNET) {
                    return 6
                }
                if (networkInfo.type == ConnectivityManager.TYPE_BLUETOOTH) {
                    return 7
                }
            }
        }
        return 0
    }
}