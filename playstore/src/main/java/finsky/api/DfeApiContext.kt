package finsky.api

import android.accounts.Account
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import android.text.TextUtils

import com.android.volley.AuthFailureError
import finsky.utils.NetworkType
import java.net.URI

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
        this.headers.put("X-DFE-Content-Filters", "")

        //this.headers["x-dfe-device-config-token"] = "CisaKQoTMzg0OTM3NjYyNDk2MTQ5NjgxNRISChAxNTI0MjMyMzUxNTE1MzAw"
       // this.headers["x-dfe-cookie"] = "EAEYACICSUwoATI8Q2lzYUtRb1RNemcwT1RNM05qWXlORGsyTVRRNU5qZ3hOUklTQ2hBeE5USTBNak15TXpVeE5URTFNekF3"
        //this.headers["x-dfe-device-checkin-consistency-token"] = "ABFEt1UPzbDK9X12FRVrRuPrsuTSIcBk9c7kw5LPUhfSiE5wZNiQaLrRUGgtSKvZKPsrcdONNtgEtjuB6WfFC2MsdF-vP9PiHE0s6cEsTFmxzHpv3tS9EOv8ghbWCab2JKBcPuMXl5sfZwH07NYfSXBSIQ-7TTvQNawbo-qs4BrEC985bBmJKMRyyFACHw758CwC2xbRA1SLSDbHQY4u9fAYYkV042yncfbbY_9pgGzvFWkcw4GHpVBUjMDXndVCqg7OEqKE9a3w57S7t_E4OzB74F91czFzRlg1Plb0n77sGRaawUeJGyoGhsyTK2nD97nnw4Os1W7CSyvElLHlNQHZ7yUeqgu75R5EcdIxG8ICygASRrLWva0UEFcIpWGOYMOyUMSWYoDMezlTeCtx7qmfy5PouMjbr7qrdc0mhgNnMW6G8EZpQTc"
        //this.headers["x-dfe-phenotype"] = "H4sIAAAAAAAAABXPSW6DMAAAQPXaY_-BZFJKsNSLCXYgBAih2bggoCa2WUKJWb_ZD1WdH8zr78sG4agOMTIe0inMYenvPwKltN8DxK6UfMgF6ZGM3A6rXX1ssBaiy6HmHJ6QB5tDbOEkbofQcTnJInJR1meu2lXgxYQU8OCsJghkey7V0hSZvNBnu9aLRABFp9sFKFMOkW9kx0fKHP9JMencMi8ifMJ80cVGu43vjjq72tYPrp6ZKPbNFtJOm6yuvqQNbrwTNOiiWbA5vBcV21mceW-f_yk3tGYOmEHXG8ubAq0b4Hc69jmowkGO17wO9mS3WuYxroJ-tGnOjBy2x-QPf6LmcxQBAAA"
        //this.headers["x-dfe-logging-id"] = "-3f92be94f987cdb6"
        this.headers["x-dfe-encoded-targets"] = "CAESjALBlYEGpgrRAkLwA5IHgAKkCLUBWEDUBTKSAekKmAG6ATKGAS9o8gLdASfcARb7C7gDAQLPBa4DzxPRCLwB2xC2AQGlA54DMCjjC8MCowKtA7AC9AOvDbgC0wHfBlcBxQTLAVGDAxdu9gHlAUyIAhgC5QECxwEHYkJLYgHXCg2hBNwBQE4BYROnAl0y8wKiAoADzQK2AasEyAHeBArIApMCYgnaAmwP6wIxASLNAuYBhgK/Ad0BDhO5AaoBwQMD1wIcB6UBAcUBOgED8wGXAgEH5QGWBANGDgjrAcoBV8kB5QEFHOwCZ5sBlAKQAjjfAgElbI4KkwVwRYQINcwBKtAB3wk2RfoFnALeBvMGGowHEgEBAQTYAQ0ttgFhSlcCAwRrN4DTzwKCu7EDAQEDAgQJCAkBAggEAQIBAQYBAQMFBBUGAwUEBAQDAQ8CAQIDxwEBFgQPJsEBfS8CHAEBCpABDDMXASEKFA8GByI3hAEODBZNCVIBBX8RERgBA4sBGGkUECMIEXBkEQ9qnwHEAoQBBIgBigEZGAsrEwMWBQcBKmUCAiUocxQnLfQEMQ43GIUBjQG0AVlCjgEeJwskECYvW9QBYnoJAQreAXmqAQwDLGSeAQSBAXRQRdQBigHMAgUFCc0BBAFFoAE53wJgNS7OAQ1yqwEgiwM/+wImlwMeQ60ChAZ24wX2Aw8HAQL2AxZznAFVbQEJPAHeBSAQDntVXpsHKxjYAQEhAQcCIAgSHQemAzgBGkaEAQG7AnWnARgBIgKjAhIBARgWD8YLHYABhwGEAsoBAQIBwwEn6wIBOQHbAVLnA0H1AsIBdQETKQSLAbIDSpsBBhI/RDgUK1VF4gKDAgsMCC9cF1EbuQEO9wG3ASrnA98DlwEE6wGHAWIVGdMBBhMSC1ooJAECAoQFtQENBiNZKJ8BMh4YAQQgAlYBIwKQAR0SGyd+iQFdDA/9AUkjBCIqHoACPwQbAxcg3AE9sgHKAgsYsQIlfhtauwEMiAEjeYsBigEDOwErBTcCVAFipAIhMQ1FA54DzgHmAimUBAvsAQc4jAEeDP8DAQK1AWtsOlAKCAKKBBQUAgMBMTJRGgIDygGZAToBAQcBVYQBlwPJAWkIfYkBdw5eMhE03gFGRsECBA89YREcP4kBHCdRBleRATIBCgGdAtICBBJNJQKIAYcBhgEgMpACEcYBBQQCBgQCBAIHBAIEAgUGBAIHBgQBBwYEAgQBBiLUAX5VOSEkH14BAQUDKQGkAXQFBwUEAwICmwHtAoUCBQQCBgcVAisdFAIKAb4BCDoWFChoJoYCGwM9LocBAwERmQEEEgsCRNcBPCG0AQ4BV3EBCV0gFQoFFxqSASoBLB4vCB4DRwgJiwEUCX4E2gEFBgcFBwQCBQYEAz8OJAQDBAEEAwYEH5EBtAMmBzsCAwICAgECE10QDBhVqQEDAgMFAQIJJw5YASEJCiEtOg4KLBQsFAcmJTSdAXoEAQcEAgQBMjMkFitdHqECJwooAwEuGS8FAgIBAwICRhwaAcMBCKMDBQUaUaQBDMUBDCAVbzkFAhYJJBYEAgQDBgU"
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
        private const val PLAY_VERSION_CODE = 80963000
        private const val PLAY_VERSION_NAME = "9.6.30-all%20%5B0%5D%20%5BPR%5D%20192669274"
        private const val CLIENT_ID = "am-android-google"

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
