package com.google.android.finsky.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public final class NetworkType
{
    public static int get(final Context context) {
        NetworkInfo networkInfo = NetworkStateChangedReceiver.getCachedNetworkInfo(context);
        return getNetworkTypeFromNetworkInfo(networkInfo);
    }

    private static int getNetworkTypeFromNetworkInfo(final NetworkInfo networkInfo) {
        if (networkInfo != null) {

            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                return 4;
            }
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIMAX) {
                return 3;
            }
            if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                switch (networkInfo.getSubtype()) {
                    default:
                    {
                        return 5;
                    }
                    case 1:
                    case 2:
                    case 4:
                    case 7:
                    case 11:
                    {
                        return 1;
                    }
                    case 3:
                    case 5:
                    case 6:
                    case 8:
                    case 9:
                    case 10:
                    case 12:
                    case 14:
                    case 15:
                    {
                        return 2;
                    }
                    case 13:
                    {
                        return 3;
                    }
                }
            }
            else {
                if (networkInfo.getType() == ConnectivityManager.TYPE_ETHERNET) {
                    return 6;
                }
                if (networkInfo.getType() == ConnectivityManager.TYPE_BLUETOOTH) {
                    return 7;
                }
            }
        }
        return 0;
    }
}