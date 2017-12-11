package finsky.api

import android.accounts.Account
import android.content.Context
import android.net.Uri
import android.os.Build
import android.text.TextUtils

import com.android.volley.AuthFailureError
import finsky.utils.NetworkType

import java.util.HashMap
import java.util.Locale

class DfeApiContext private constructor(internal val context: Context, val account: Account, private val lastAuthToken: String, deviceId: String,
                                        locale: Locale, mccmnc: String,
                                        clientId: String, loggingId: String, filterLevel: Int) {
    private val headers: MutableMap<String, String> = HashMap()

    internal val accountName: String
        get() = account.name

    constructor(context: Context, account: Account, authTokenStr: String, deviceId: String,  mccmnc: String, filterLevel: Int) : this(
            context, account, authTokenStr, deviceId,
            Locale.getDefault(), mccmnc, CLIENT_ID, "", filterLevel
    )

    init {
        this.headers.put("X-DFE-Device-Id", deviceId)//Long.toHexString(PlayG.androidId.load()));
        this.headers.put("Accept-Language", locale.language + "-" + locale.country)
        if (mccmnc.isNotEmpty()) {
            this.headers.put("X-DFE-MCCMNC", mccmnc)
        }
        if (clientId.isNotEmpty()) {
            this.headers.put("X-DFE-Client-Id", clientId)
        }
        if (loggingId.isNotEmpty()) {
            this.headers.put("X-DFE-Logging-Id", loggingId)
        }
        val isWideScreen = false//context.getResources().getBoolean(2131492899);
        this.headers.put("User-Agent", makeUserAgentString(PLAY_VERSION_NAME, PLAY_VERSION_CODE, isWideScreen, DfeUtils.supportedAbis()))
        this.headers.put("X-DFE-Filter-Level", filterLevel.toString())
        this.headers.put("X-DFE-Encoded-Targets", "CAEScFfqlIEG6gUYogFWrAISK1WDAg+hAZoCDgIU1gYEOIACFkLMAeQBnASLATlASUuyAyqCAjY5igOMBQzfA/IClwFbApUC4ANbtgKVAS7OAX8YswHFBhgDwAOPAmGEBt4OfKkB5weSB5AFASkiN68akgMaxAMSAQEBA9kBO7UBFE1KVwIDBGs3go6BBgEBAgMECQgJAQIEAQMEAQMBBQEBBAUEFQYCBgUEAwMBDwIBAgOrARwBEwMEAg0mrwESfTEcAQEKG4EBMxghChMBDwYGASI3hAEODEwXCVh/EREZA4sBYwEdFAgIIwkQcGQRDzQ2fTC2AjfVAQIBAYoBGRg2FhYFBwEqNzACJShzFFblAo0CFxpFNBzaAd0DHjIRI4sBJZcBPdwBCQGhAUd2A7kBLBVPngEECHl0UEUMtQETigHMAgUFCc0BBUUlTywdHDgBiAJ+vgKhAU0uAcYCAWQ/5ALUAw1UwQHUBpIBCdQDhgL4AY4CBQICjARbGFBGWzA1CAEMOQH+BRAOCAZywAIDyQZ2MgM3BxsoAgUEBwcHFia3AgcGTBwHBYwBAlcBggFxSGgIrAEEBw4QEqUCASsWadsHCgUCBQMD7QICA3tXCUw7ugJZAwGyAUwpIwM5AwkDBQMJA5sBCw8BNxBVVBwVKhebARkBAwsQEAgEAhESAgQJEBCZATMdzgEBBwG8AQQYKSMUkAEDAwY/CTs4/wEaAUt1AwEDAQUBAgIEAwYEDx1dB2wGeBFgTQ")
        this.headers.put("X-DFE-Content-Filters", "")
    }

    private fun makeUserAgentString(versionName: String, versionCode: Int, isWideScreen: Boolean, supportedAbis: Array<String>): String {
        val wideScreen = if (isWideScreen) 1 else 0
        return String.format(Locale.US, "Android-Finsky/%s (api=%d,versionCode=%d,sdk=%d,device=%s,hardware=%s,product=%s,platformVersionRelease=%s,model=%s,buildId=%s,isWideScreen=%d,supportedAbis=%s)",
                versionName, 3, versionCode, Build.VERSION.SDK_INT, a(Build.DEVICE), a(Build.HARDWARE), a(Build.PRODUCT),
                a(Build.VERSION.RELEASE), a(Build.MODEL), a(Build.ID), wideScreen, a(supportedAbis))
    }

    @Throws(AuthFailureError::class)
    internal fun createHeaders(): MutableMap<String, String> {
        synchronized(this) {
            val hashMap = mutableMapOf<String, String>()
            hashMap.putAll(this.headers)
            hashMap.put("X-DFE-Network-Type", Integer.toString(NetworkType.get(context)))
            hashMap.put("Authorization", "GoogleLogin auth=" + this.lastAuthToken)
            //            if (AppLog.DEBUG) {
            //                for(String key: hashMap.keySet()) {
            //                    AppLog.d("HTTP Header: "+key+" = "+hashMap.load(key));
            //                }
            //            }
            return hashMap
        }
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("[PlayDfeApiContext headers={")
        var n = 1
        for (s in this.headers.keys) {
            if (n != 0) {
                n = 0
            } else {
                sb.append(", ")
            }
            sb.append(s).append(": ").append(this.headers[s])
        }
        sb.append("}]")
        return sb.toString()
    }

    companion object {
        private val PLAY_VERSION_CODE = 80841900
        private val PLAY_VERSION_NAME = "8.4.19.V-all [0] [FP] 175058788"
        private val CLIENT_ID = "am-android-google"

        private fun a(replace: String?): String? {
            if (replace.isNullOrBlank()) {
                return replace
            }
            return Uri.encode(replace).replace("(", "%28").replace(")", "%29")
        }

        private fun a(array: Array<String>): String {
            val array2 = arrayOfNulls<String>(array.size)
            for (i in array.indices) {
                array2[i] = a(array[i])
            }
            return TextUtils.join(";", array2)
        }
    }
}
