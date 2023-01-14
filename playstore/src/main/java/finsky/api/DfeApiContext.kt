package finsky.api

import android.accounts.Account
import android.content.Context
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import finsky.config.ContentLevel
import finsky.utils.NetworkStateChangedReceiver
import info.anodsplace.playstore.DfeDeviceInfoProvider
import java.util.*

class DfeApiContext private constructor(internal val context: Context, val account: Account, private val lastAuthToken: String, deviceId: String,
                                        locale: Locale, mccmnc: String,
                                        clientId: String, loggingId: String, filterLevel: Int) {

    private val headers: MutableMap<String, String> = mutableMapOf(
            "X-DFE-Device-Id" to deviceId,
            "Accept-Language" to "${locale.language}-${locale.country}",
            "X-DFE-Client-Id" to clientId,
            "User-Agent" to makeUserAgentString(PLAY_VERSION_NAME, PLAY_VERSION_CODE, false),
            "X-DFE-Filter-Level" to filterLevel.toString(),
            "X-DFE-Content-Filters" to "",
            "x-dfe-encoded-targets" to "CAESjALBlYEGpgrRAkLwA5IHgAKkCLUBWEDUBTKSAekKmAG6ATKGAS9o8gLdASfcARb7C7gDAQLPBa4DzxPRCLwB2xC2AQGlA54DMCjjC8MCowKtA7AC9AOvDbgC0wHfBlcBxQTLAVGDAxdu9gHlAUyIAhgC5QECxwEHYkJLYgHXCg2hBNwBQE4BYROnAl0y8wKiAoADzQK2AasEyAHeBArIApMCYgnaAmwP6wIxASLNAuYBhgK/Ad0BDhO5AaoBwQMD1wIcB6UBAcUBOgED8wGXAgEH5QGWBANGDgjrAcoBV8kB5QEFHOwCZ5sBlAKQAjjfAgElbI4KkwVwRYQINcwBKtAB3wk2RfoFnALeBvMGGowHEgEBAQTYAQ0ttgFhSlcCAwRrN4DTzwKCu7EDAQEDAgQJCAkBAggEAQIBAQYBAQMFBBUGAwUEBAQDAQ8CAQIDxwEBFgQPJsEBfS8CHAEBCpABDDMXASEKFA8GByI3hAEODBZNCVIBBX8RERgBA4sBGGkUECMIEXBkEQ9qnwHEAoQBBIgBigEZGAsrEwMWBQcBKmUCAiUocxQnLfQEMQ43GIUBjQG0AVlCjgEeJwskECYvW9QBYnoJAQreAXmqAQwDLGSeAQSBAXRQRdQBigHMAgUFCc0BBAFFoAE53wJgNS7OAQ1yqwEgiwM/+wImlwMeQ60ChAZ24wX2Aw8HAQL2AxZznAFVbQEJPAHeBSAQDntVXpsHKxjYAQEhAQcCIAgSHQemAzgBGkaEAQG7AnWnARgBIgKjAhIBARgWD8YLHYABhwGEAsoBAQIBwwEn6wIBOQHbAVLnA0H1AsIBdQETKQSLAbIDSpsBBhI/RDgUK1VF4gKDAgsMCC9cF1EbuQEO9wG3ASrnA98DlwEE6wGHAWIVGdMBBhMSC1ooJAECAoQFtQENBiNZKJ8BMh4YAQQgAlYBIwKQAR0SGyd+iQFdDA/9AUkjBCIqHoACPwQbAxcg3AE9sgHKAgsYsQIlfhtauwEMiAEjeYsBigEDOwErBTcCVAFipAIhMQ1FA54DzgHmAimUBAvsAQc4jAEeDP8DAQK1AWtsOlAKCAKKBBQUAgMBMTJRGgIDygGZAToBAQcBVYQBlwPJAWkIfYkBdw5eMhE03gFGRsECBA89YREcP4kBHCdRBleRATIBCgGdAtICBBJNJQKIAYcBhgEgMpACEcYBBQQCBgQCBAIHBAIEAgUGBAIHBgQBBwYEAgQBBiLUAX5VOSEkH14BAQUDKQGkAXQFBwUEAwICmwHtAoUCBQQCBgcVAisdFAIKAb4BCDoWFChoJoYCGwM9LocBAwERmQEEEgsCRNcBPCG0AQ4BV3EBCV0gFQoFFxqSASoBLB4vCB4DRwgJiwEUCX4E2gEFBgcFBwQCBQYEAz8OJAQDBAEEAwYEH5EBtAMmBzsCAwICAgECE10QDBhVqQEDAgMFAQIJJw5YASEJCiEtOg4KLBQsFAcmJTSdAXoEAQcEAgQBMjMkFitdHqECJwooAwEuGS8FAgIBAwICRhwaAcMBCKMDBQUaUaQBDMUBDCAVbzkFAhYJJBYEAgQDBgU"
    )

    internal val accountName: String
        get() = account.name

    constructor(context: Context, account: Account, authTokenStr: String, deviceInfo: DfeDeviceInfoProvider)
            : this(context, account, authTokenStr, deviceInfo.deviceId, deviceInfo.simOperator, ContentLevel().dfeValue)

    constructor(context: Context, account: Account, authTokenStr: String, deviceId: String, mccmnc: String, filterLevel: Int) : this(
            context, account, authTokenStr, deviceId,
            Locale.getDefault(), mccmnc, CLIENT_ID, "", filterLevel
    )

    init {
        if (mccmnc.isNotEmpty()) {
            this.headers["X-DFE-MCCMNC"] = mccmnc
        }
        if (loggingId.isNotEmpty()) {
            this.headers["X-DFE-Logging-Id"] = loggingId
        }
    }

    internal fun createHeaders(): MutableMap<String, String> {
        synchronized(this) {
            val hashMap = this.headers.toMutableMap()
            hashMap["X-DFE-Network-Type"] = NetworkStateChangedReceiver.getCachedNetworkType(context).value.toString()
            hashMap["Authorization"] = "GoogleLogin auth=$lastAuthToken"
            return hashMap
        }
    }

    override fun toString(): String {
        return "[PlayDfeApiContext headers={${this.headers.map { "${it.key} = ${it.value}," }}]"
    }

    companion object {
        private const val PLAY_VERSION_CODE = 80963000
        private const val PLAY_VERSION_NAME = "9.6.30-all%20%5B0%5D%20%5BPR%5D%20192669274"
        private const val CLIENT_ID = "am-android-google"

        private fun makeUserAgentString(versionName: String, versionCode: Int, isWideScreen: Boolean): String {
            val wideScreen = if (isWideScreen) 1 else 0
            return String.format(Locale.US, "Android-Finsky/%s (api=%d,versionCode=%d,sdk=%d,device=%s,hardware=%s,product=%s,platformVersionRelease=%s,model=%s,buildId=%s,isWideScreen=%d,supportedAbis=%s)",
                    versionName, 3, versionCode, Build.VERSION.SDK_INT, a(Build.DEVICE), a(Build.HARDWARE), a(Build.PRODUCT),
                    a(Build.VERSION.RELEASE), a(Build.MODEL), a(Build.ID), wideScreen, a(Build.SUPPORTED_ABIS))
        }

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