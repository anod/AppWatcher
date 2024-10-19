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
    loggingId: String
) {
    val hasAuth: Boolean
        get() = authTokenProvider.authToken.isNotEmpty() && authTokenProvider.accountName.isNotEmpty()

    private val headers: MutableMap<String, String> = mutableMapOf(
        "Accept-Language" to deviceInfo.locale.acceptLanguage,
        "X-DFE-Client-Id" to CLIENT_ID,
        "User-Agent" to deviceInfo.makeUserAgentString(),
        "X-DFE-Content-Filters" to "",
        "X-DFE-Encoded-Targets" to "CAESN/qigQYC2AMBFfUbyA7SM5Ij/CvfBoIDgxHqGP8R3xzIBvoQtBKFDZ4HAY4FrwSVMasHBO0O2Q8akgYRAQECAQO7AQEpKZ0CnwECAwRrAQYBr9PPAoK7sQMBAQMCBAkIDAgBAwEDBAICBAUZEgMEBAMLAQEBBQEBAcYBARYED+cBfS8CHQEKkAEMMxcBIQoUDwYHIjd3DQ4MFk0JWGYZEREYAQOLAYEBFDMIEYMBAgICAgICOxkCD18LGQKEAcgDBIQBAgGLARkYCy8oBTJlBCUocxQn0QUBDkkGxgNZQq0BZSbeAmIDgAEBOgGtAaMCDAOQAZ4BBIEBKUtQUYYBQscDDxPSARA1oAEHAWmnAsMB2wFyywGLAxol+wImlwOOA80CtwN26A0WjwJVbQEJPAH+BRDeAfkHK/ABASEBCSAaHQemAzkaRiu2Ad8BdXeiAwEBGBUBBN4LEIABK4gB2AFLfwECAdoENq0CkQGMBsIBiQEtiwGgA1zyAUQ4uwS8AwhsvgPyAcEDF27vApsBHaICGhl3GSKxAR8MC6cBAgItmQYG9QIeywLvAeYBDArLAh8HASI4ELICDVmVBgsY/gHWARtcAsMBpALiAdsBA7QBpAJmIArpByn0AyAKBwHTARIHAX8D+AMBcRIBBbEDmwUBMacCHAciNp0BAQF0OgQLJDuSAh54kwFSP0eeAQQ4M5EBQgMEmwFXywFo0gFyWwMcapQBBugBPUW2AVgBKmy3AR6PAbMBGQxrUJECvQR+8gFoWDsYgQNwRSczBRXQAgtRswEW0ALMAREYAUEBIG6yATYCRE8OxgER8gMBvQEDRkwLc8MBTwHZAUOnAXiiBakDIbYBNNcCIUmuArIBSakBrgFHKs0EgwV/G3AD0wE6LgECtQJ4xQFwFbUCjQPkBS6vAQqEAUZF3QIM9wEhCoYCQhXsBCyZArQDugIziALWAdIBlQHwBdUErQE6qQaSA4EEIvYBHir9AQVLmgMCApsCKAwHuwgrENsBAjNYswEVmgIt7QJnN4wDEnta+wGfAcUBxgEtEFXQAQWdAUAeBcwBAQM7rAEJATJ0LENrdh73A6UBhAE+qwEeASxLZUMhDREuH0CGARbd7K0GlQo",
        "X-DFE-Phenotype" to "H4sIAAAAAAAAAB3OO3KjMAAA0KRNuWXukBkBQkAJ2MhgAZb5u2GCwQZbCH_EJ77QHmgvtDtbv-Z9_H63zXXU0NVPB1odlyGy7751Q3CitlPDvFd8lxhz3tpNmz7P92CFw73zdHU2Ie0Ad2kmR8lxhiErTFLt3RPGfJQHSDy7Clw10bg8kqf2owLokN4SecJTLoSwBnzQSd652_MOf2d1vKBNVedzg4ciPoLz2mQ8efGAgYeLou-l-PXn_7Sna1MfhHuySxt-4esulEDp8Sbq54CPPKjpANW-lkU2IZ0F92LBI-ukCKSptqeq1eXU96LD9nZfhKHdtjSWwJqUm_2r6pMHOxk01saVanmNopjX3YxQafC4iC6T55aRbC8nTI98AF_kItIQAJb5EQxnKTO7TZDWnr01HVPxelb9A2OWX6poidMWl16K54kcu_jhXw-JSBQkVcD_fPsLSZu6joIBAAA",
        "X-Limit-Ad-Tracking-Enabled" to "false",
        "X-Ad-Id" to "",
        "X-DFE-UserLanguages" to deviceInfo.locale.description,
        "X-DFE-Request-Params" to "timeoutMs=4000",
    )

    constructor(context: Context, authTokenProvider: DfeAuthProvider, deviceInfo: DfeDeviceInfoProvider)
            : this(context, authTokenProvider, deviceInfo, "")

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
            if (authTokenProvider.deviceConfigToken.isNotBlank()) {
                hashMap["X-DFE-Device-Config-Token"] = authTokenProvider.deviceConfigToken
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