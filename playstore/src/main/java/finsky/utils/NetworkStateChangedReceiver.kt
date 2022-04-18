package finsky.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.telephony.TelephonyManager

class NetworkStateChangedReceiver : BroadcastReceiver() {
    override fun onReceive(paramContext: Context, paramIntent: Intent) {
        updateCachedNetworkInfo(paramContext)
    }

    companion object {
        private var sCachedNetworkType: NetworkType? = null
        private val sLock = Any()

        fun getCachedNetworkType(context: Context): NetworkType {
            synchronized(sLock) {
                if (sCachedNetworkType == null) {
                    updateCachedNetworkInfo(context)
                }
                return sCachedNetworkType ?: NetworkType.None
            }
        }

        private fun updateCachedNetworkInfo(context: Context) {
            synchronized(sLock) {
                val connectivityManager = (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
                val activeNetwork = connectivityManager.activeNetwork
                if (activeNetwork == null) {
                    sCachedNetworkType = null
                    return
                }
                val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
                sCachedNetworkType = getNetworkTypeFromNetworkInfo(capabilities, context)
            }
        }

        private fun getNetworkTypeFromNetworkInfo(networkCapabilities: NetworkCapabilities?, context: Context): NetworkType {
            networkCapabilities ?: return NetworkType.None
            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                return NetworkType.Wifi
            }
            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                return when (tm.dataNetworkType) {
                    TelephonyManager.NETWORK_TYPE_GPRS,
                    TelephonyManager.NETWORK_TYPE_EDGE,
                    TelephonyManager.NETWORK_TYPE_CDMA,
                    TelephonyManager.NETWORK_TYPE_1xRTT,
                    TelephonyManager.NETWORK_TYPE_IDEN,
                    TelephonyManager.NETWORK_TYPE_GSM -> NetworkType.Cell2g
                    TelephonyManager.NETWORK_TYPE_UMTS,
                    TelephonyManager.NETWORK_TYPE_EVDO_0,
                    TelephonyManager.NETWORK_TYPE_EVDO_A,
                    TelephonyManager.NETWORK_TYPE_HSDPA,
                    TelephonyManager.NETWORK_TYPE_HSUPA,
                    TelephonyManager.NETWORK_TYPE_HSPA,
                    TelephonyManager.NETWORK_TYPE_EVDO_B,
                    TelephonyManager.NETWORK_TYPE_EHRPD,
                    TelephonyManager.NETWORK_TYPE_HSPAP,
                    TelephonyManager.NETWORK_TYPE_TD_SCDMA -> NetworkType.Cell3g
                    TelephonyManager.NETWORK_TYPE_LTE,
                    TelephonyManager.NETWORK_TYPE_IWLAN, 19 -> NetworkType.CellLte
                    //TelephonyManager.NETWORK_TYPE_NR -> return "5G"
                    else -> NetworkType.CellOther
                }
            }
            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                return NetworkType.Ethernet
            }
            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH)) {
                return NetworkType.Bluetooth
            }
            return NetworkType.None
        }
    }
}