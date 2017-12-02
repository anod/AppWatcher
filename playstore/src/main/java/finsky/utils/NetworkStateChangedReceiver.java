package finsky.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public final class NetworkStateChangedReceiver extends BroadcastReceiver {
    private static NetworkInfo sCachedNetworkInfo;
    private static final Object sLock = new Object();

    public static void flushCachedState() {
        synchronized (sLock) {
            sCachedNetworkInfo = null;
        }
    }

    public static NetworkInfo getCachedNetworkInfo(Context context) {
        synchronized (sLock) {
            if (sCachedNetworkInfo == null) {
                updateCachedNetworkInfo(context);
            }
            return sCachedNetworkInfo;
        }
    }

    private static void updateCachedNetworkInfo(Context context) {
        synchronized (sLock) {
            sCachedNetworkInfo = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        }
    }

    public final void onReceive(Context paramContext, Intent paramIntent) {
        updateCachedNetworkInfo(paramContext);
    }
}