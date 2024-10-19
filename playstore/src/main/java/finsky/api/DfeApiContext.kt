package finsky.api

import android.content.Context
import finsky.api.utils.CLIENT_ID
import finsky.api.utils.makeUserAgentString
import finsky.config.ContentLevel
import finsky.utils.NetworkStateChangedReceiver

class DfeApiContext private constructor(
    private val context: Context,
    private val authTokenProvider: DfeAuthProvider,
    val deviceInfo: DfeDeviceInfoProvider,
    loggingId: String,
    filterLevel: Int
) {
    val accountName: String
        get() = authTokenProvider.accountName

    val hasAuth: Boolean
        get() = authTokenProvider.authToken.isNotEmpty() && authTokenProvider.accountName.isNotEmpty()

    private val headers: MutableMap<String, String> = mutableMapOf(
        "Accept-Language" to deviceInfo.locale.acceptLanguage,
        "X-DFE-Client-Id" to CLIENT_ID,
        "User-Agent" to deviceInfo.makeUserAgentString(),
        "X-DFE-Filter-Level" to filterLevel.toString(),
        "X-DFE-Content-Filters" to "",
        "X-DFE-Encoded-Targets" to "CAESjALBlYEGpgrRAkLwA5IHgAKkCLUBWEDUBTKSAekKmAG6ATKGAS9o8gLdASfcARb7C7gDAQLPBa4DzxPRCLwB2xC2AQGlA54DMCjjC8MCowKtA7AC9AOvDbgC0wHfBlcBxQTLAVGDAxdu9gHlAUyIAhgC5QECxwEHYkJLYgHXCg2hBNwBQE4BYROnAl0y8wKiAoADzQK2AasEyAHeBArIApMCYgnaAmwP6wIxASLNAuYBhgK/Ad0BDhO5AaoBwQMD1wIcB6UBAcUBOgED8wGXAgEH5QGWBANGDgjrAcoBV8kB5QEFHOwCZ5sBlAKQAjjfAgElbI4KkwVwRYQINcwBKtAB3wk2RfoFnALeBvMGGowHEgEBAQTYAQ0ttgFhSlcCAwRrN4DTzwKCu7EDAQEDAgQJCAkBAggEAQIBAQYBAQMFBBUGAwUEBAQDAQ8CAQIDxwEBFgQPJsEBfS8CHAEBCpABDDMXASEKFA8GByI3hAEODBZNCVIBBX8RERgBA4sBGGkUECMIEXBkEQ9qnwHEAoQBBIgBigEZGAsrEwMWBQcBKmUCAiUocxQnLfQEMQ43GIUBjQG0AVlCjgEeJwskECYvW9QBYnoJAQreAXmqAQwDLGSeAQSBAXRQRdQBigHMAgUFCc0BBAFFoAE53wJgNS7OAQ1yqwEgiwM/+wImlwMeQ60ChAZ24wX2Aw8HAQL2AxZznAFVbQEJPAHeBSAQDntVXpsHKxjYAQEhAQcCIAgSHQemAzgBGkaEAQG7AnWnARgBIgKjAhIBARgWD8YLHYABhwGEAsoBAQIBwwEn6wIBOQHbAVLnA0H1AsIBdQETKQSLAbIDSpsBBhI/RDgUK1VF4gKDAgsMCC9cF1EbuQEO9wG3ASrnA98DlwEE6wGHAWIVGdMBBhMSC1ooJAECAoQFtQENBiNZKJ8BMh4YAQQgAlYBIwKQAR0SGyd+iQFdDA/9AUkjBCIqHoACPwQbAxcg3AE9sgHKAgsYsQIlfhtauwEMiAEjeYsBigEDOwErBTcCVAFipAIhMQ1FA54DzgHmAimUBAvsAQc4jAEeDP8DAQK1AWtsOlAKCAKKBBQUAgMBMTJRGgIDygGZAToBAQcBVYQBlwPJAWkIfYkBdw5eMhE03gFGRsECBA89YREcP4kBHCdRBleRATIBCgGdAtICBBJNJQKIAYcBhgEgMpACEcYBBQQCBgQCBAIHBAIEAgUGBAIHBgQBBwYEAgQBBiLUAX5VOSEkH14BAQUDKQGkAXQFBwUEAwICmwHtAoUCBQQCBgcVAisdFAIKAb4BCDoWFChoJoYCGwM9LocBAwERmQEEEgsCRNcBPCG0AQ4BV3EBCV0gFQoFFxqSASoBLB4vCB4DRwgJiwEUCX4E2gEFBgcFBwQCBQYEAz8OJAQDBAEEAwYEH5EBtAMmBzsCAwICAgECE10QDBhVqQEDAgMFAQIJJw5YASEJCiEtOg4KLBQsFAcmJTSdAXoEAQcEAgQBMjMkFitdHqECJwooAwEuGS8FAgIBAwICRhwaAcMBCKMDBQUaUaQBDMUBDCAVbzkFAhYJJBYEAgQDBgU"
    )

    constructor(context: Context, authTokenProvider: DfeAuthProvider, deviceInfo: DfeDeviceInfoProvider)
            : this(context, authTokenProvider, deviceInfo, "", ContentLevel().dfeValue)

    init {
        if (deviceInfo.simOperator.isNotEmpty()) {
            this.headers["X-DFE-MCCMNC"] = deviceInfo.simOperator
        }
        if (loggingId.isNotEmpty()) {
            this.headers["X-DFE-Logging-Id"] = loggingId
        }
    }

    internal fun createDefaultHeaders(): MutableMap<String, String> {
        val authToken = authTokenProvider.authToken
        if (authToken.isBlank()) {
            throw IllegalStateException("Auth token is empty")
        }
        synchronized(this) {
            val hashMap = this.headers.toMutableMap()
            hashMap["X-DFE-Device-Id"] = authTokenProvider.gfsId.ifEmpty { deviceInfo.deviceId }
            if (authTokenProvider.gfsToken.isNotBlank()) {
                hashMap["X-DFE-Device-Checkin-Consistency-Token"] = authTokenProvider.gfsToken
            }
            hashMap["X-DFE-Network-Type"] = NetworkStateChangedReceiver.getCachedNetworkType(context).value.toString()
            hashMap["Authorization"] = "GoogleLogin auth=${authToken}"
            return hashMap
        }
    }

    override fun toString(): String {
        return "[PlayDfeApiContext headers={${this.headers.map { "${it.key} = ${it.value}," }}]"
    }

    fun createAuthHeaders(): MutableMap<String, String> {
        return mutableMapOf(
            "app" to "com.google.android.gms",
            "User-Agent" to deviceInfo.makeUserAgentString(),
        ).apply {
            if (authTokenProvider.gfsId.isNotEmpty()) {
                this["device"] = authTokenProvider.gfsId
            }
        }
    }
}