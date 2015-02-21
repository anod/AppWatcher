package com.google.android.vending.remoting.api;

import android.accounts.Account;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Build.VERSION;
import android.telephony.TelephonyManager;

import com.android.volley.RequestQueue;

import java.util.Locale;

public class VendingApiFactory {
    private Context mContext;
    private RequestQueue mQueue;

    public VendingApiFactory(Context paramContext, RequestQueue paramRequestQueue) {
        this.mContext = paramContext;
        this.mQueue = paramRequestQueue;
    }

    private VendingApiContext getApiContext(Account account, String deviceId) {
        try {
            int versionCode = this.mContext.getPackageManager().getPackageInfo(this.mContext.getPackageName(), 0).versionCode;
            TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService(Context.TELEPHONY_SERVICE);
            // Long.toHexString(((Long)DfeApiConfig.androidId.get()).longValue())
            VendingApiContext localVendingApiContext = new VendingApiContext(
                    this.mContext, account, Locale.getDefault(), deviceId, versionCode,
                    tm.getNetworkOperatorName(), tm.getSimOperatorName(), tm.getNetworkOperator(), tm.getSimOperator(),
                    Build.DEVICE, VERSION.SDK,
                    "am-google", ""
            );
            return localVendingApiContext;
        } catch (NameNotFoundException localNameNotFoundException) {
            throw new RuntimeException("Can't find our own package", localNameNotFoundException);
        }
    }

    public VendingApi getApi(String accountName, String deviceId) {
        return new VendingApi(this.mQueue, getApiContext(new Account(accountName, "com.google"), deviceId));
    }
}

/* Location:           /Users/alex/Documents/android-tools/com.android.vending-5.0.31-80300031-minAPI9-dex2jar.jar
 * Qualified Name:     com.google.android.vending.remoting.api.VendingApiFactory
 * JD-Core Version:    0.6.2
 */