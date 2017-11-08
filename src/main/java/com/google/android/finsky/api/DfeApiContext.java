package com.google.android.finsky.api;

import android.accounts.Account;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.android.volley.AuthFailureError;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DfeApiContext
{
    private static final int PLAY_VERSION_CODE = 80807300;
    private static final String PLAY_VERSION_NAME = "8.0.73.R-all [0] [PR] 162689464";
    private static final String CLIENT_ID = "am-google";
    final Context context;
    public final Account account;
    private final Map<String, String> headers;
    private String lastAuthToken;


    private DfeApiContext(final Context context, final Account account, final String authToken, final String deviceId,
                            final Locale locale, final String mccmnc,
                            final String clientId, final String loggingId, final int filterLevel) {
        this.headers = new HashMap<>();
        this.context = context;
        this.account = account;
        lastAuthToken = authToken;
        this.headers.put("X-DFE-Device-Id", deviceId);//Long.toHexString(PlayG.androidId.load()));
        this.headers.put("Accept-Language", locale.getLanguage() + "-" + locale.getCountry());
        if (!TextUtils.isEmpty(mccmnc)) {
            this.headers.put("X-DFE-MCCMNC", mccmnc);
        }
        if (!TextUtils.isEmpty(clientId)) {
            this.headers.put("X-DFE-Client-Id", clientId);
        }
        if (!TextUtils.isEmpty(clientId)) {
            this.headers.put("X-DFE-Logging-Id", loggingId);
        }
        final boolean isWideScreen = false;//context.getResources().getBoolean(2131492899);
        this.headers.put("User-Agent", makeUserAgentString(PLAY_VERSION_NAME, PLAY_VERSION_CODE, isWideScreen, DfeUtils.supportedAbis()));
        this.headers.put("X-DFE-Filter-Level", String.valueOf(filterLevel));

        this.headers.put("X-DFE-Encoded-Targets", "CAEScFfqlIEG6gUYogFWrAISK1WDAg+hAZoCDgIU1gYEOIACFkLMAeQBnASLATlASUuyAyqCAjY5igOMBQzfA/IClwFbApUC4ANbtgKVAS7OAX8YswHFBhgDwAOPAmGEBt4OfKkB5weSB5AFASkiN68akgMaxAMSAQEBA9kBO7UBFE1KVwIDBGs3go6BBgEBAgMECQgJAQIEAQMEAQMBBQEBBAUEFQYCBgUEAwMBDwIBAgOrARwBEwMEAg0mrwESfTEcAQEKG4EBMxghChMBDwYGASI3hAEODEwXCVh/EREZA4sBYwEdFAgIIwkQcGQRDzQ2fTC2AjfVAQIBAYoBGRg2FhYFBwEqNzACJShzFFblAo0CFxpFNBzaAd0DHjIRI4sBJZcBPdwBCQGhAUd2A7kBLBVPngEECHl0UEUMtQETigHMAgUFCc0BBUUlTywdHDgBiAJ+vgKhAU0uAcYCAWQ/5ALUAw1UwQHUBpIBCdQDhgL4AY4CBQICjARbGFBGWzA1CAEMOQH+BRAOCAZywAIDyQZ2MgM3BxsoAgUEBwcHFia3AgcGTBwHBYwBAlcBggFxSGgIrAEEBw4QEqUCASsWadsHCgUCBQMD7QICA3tXCUw7ugJZAwGyAUwpIwM5AwkDBQMJA5sBCw8BNxBVVBwVKhebARkBAwsQEAgEAhESAgQJEBCZATMdzgEBBwG8AQQYKSMUkAEDAwY/CTs4/wEaAUt1AwEDAQUBAgIEAwYEDx1dB2wGeBFgTQ");
    }

    public static DfeApiContext create(final Context context, final Account account,final String authTokenStr, final String deviceId, final int filterLevel) {
        final TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);

        return new DfeApiContext(
                    context, account, authTokenStr,deviceId,
                    Locale.getDefault(), tm.getSimOperator(), CLIENT_ID, "", filterLevel
        );
    }

    private String makeUserAgentString(final String versionName, final int versionCode, boolean isWideScreen, String[] supportedAbis) {
        int wideScreen = isWideScreen ? 1 : 0;
        return String.format(Locale.US, "Android-Finsky/%s (api=%d,versionCode=%d,sdk=%d,device=%s,hardware=%s,product=%s,platformVersionRelease=%s,model=%s,buildId=%s,isWideScreen=%d,supportedAbis=%s)",
                a(versionName), 3, versionCode, Build.VERSION.SDK_INT, a(Build.DEVICE), a(Build.HARDWARE), a(Build.PRODUCT),
                a(Build.VERSION.RELEASE), a(Build.MODEL), a(Build.ID), wideScreen, a(supportedAbis));

    }

    private static String a(String replace) {
        if (replace == null) {
            replace = null;
        }
        else {
            replace = Uri.encode(replace).replace("(", "%28").replace(")", "%29");
        }
        return replace;
    }

    private static String a(final String[] array) {
        final String[] array2 = new String[array.length];
        for (int i = 0; i < array.length; ++i) {
            array2[i] = a(array[i]);
        }
        return TextUtils.join(";", array2);
    }

    String getAccountName() {
        return this.account == null ? "" : account.name;
    }

    Map<String, String> getHeaders() throws AuthFailureError {
        synchronized (this) {
            final HashMap<String, String> hashMap = new HashMap<>();
            hashMap.putAll(this.headers);
            hashMap.put("Authorization", "GoogleLogin auth=" + this.lastAuthToken);
//            if (AppLog.DEBUG) {
//                for(String key: hashMap.keySet()) {
//                    AppLog.d("HTTP Header: "+key+" = "+hashMap.load(key));
//                }
//            }
            return hashMap;
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("[PlayDfeApiContext headers={");
        int n = 1;
        for (final String s : this.headers.keySet()) {
            if (n != 0) {
                n = 0;
            }
            else {
                sb.append(", ");
            }
            sb.append(s).append(": ").append(this.headers.get(s));
        }
        sb.append("}]");
        return sb.toString();
    }
}
