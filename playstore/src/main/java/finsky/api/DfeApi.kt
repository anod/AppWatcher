package finsky.api

import finsky.protos.AndroidCheckinResponse
import finsky.protos.DeliveryResponse
import finsky.protos.Details
import finsky.protos.ResponseWrapper

class BulkDocId(val packageName: String, val versionCode: Int) : Comparable<BulkDocId> {
    override fun compareTo(other: BulkDocId): Int {
        return packageName.compareTo(other.packageName)
    }

    override fun toString(): String {
        return "$packageName ($versionCode)"
    }
}

enum class PatchFormat(var value: Int) {
    GDIFF(1),
    GZIPPED_GDIFF(2),
    GZIPPED_BSDIFF(3),
    UNKNOWN_4(4),
    UNKNOWN_5(5);
}

interface DfeDeviceBuild {
    val id: String
    val fingerprint: String
    val hardware: String
    val brand: String
    val radio: String
    val bootloader: String
    val device: String
    val sdkVersion: Int
    val releaseVersion: String
    val model: String
    val manifacturer: String
    val product: String
    val abis: Array<String>
}

interface DfeDeviceConfiguration {
    val touchScreen: Int
    val keyboard: Int
    val navigation: Int
    val screenLayout: Int
    val hasHardKeyboard: Boolean
    val hasFiveWayNavigation: Boolean
    val lowRamDevice: Int
    val maxNumOfCPUCores: Int
    val totalMemoryBytes: Long
    val deviceClass: Int
    val screenDensity: Int
    val screenWidth: Int
    val screenHeight: Int
    val sharedLibraries: List<String>
    val features: List<String>
    val locales: List<String>
    val glEsVersion: Int
    val glExtensions: List<String>
    val isWideScreen: Boolean
}

class DfeLocale(val language: String, val country: String, val description: String) {
    val acceptLanguage = "$language-$country"
}

interface DfeDeviceInfoProvider {
    val deviceId: String
    val simOperator: String
    val cellOperator: String
    val roaming: String
    val build: DfeDeviceBuild
    val client: String
    val gsfVersion: Long
    val otaInstalled: Boolean
    val locale: DfeLocale
    val timeZone: String
    val configuration: DfeDeviceConfiguration
    val playVersionCode: Long
    val playVersionName: String
}

interface DfeAuthProvider {
    val gfsId: String
    val gfsToken: String
    val authToken: String
    val accountName: String
}

interface DfeApi {
    val authenticated: Boolean

    suspend fun search(initialQuery: String, nextPageUrl: String): ResponseWrapper

    suspend fun details(appDetailsUrl: String): Details.DetailsResponse

    suspend fun details(docIds: List<BulkDocId>, includeDetails: Boolean): Details.BulkDetailsResponse

    suspend fun delivery(
        docId: String,
        installedVersionCode: Int = 0,
        updateVersionCode: Int,
        offerType: Int,
        patchFormats: Array<PatchFormat> = arrayOf(
            PatchFormat.GDIFF,
            PatchFormat.GZIPPED_GDIFF,
            PatchFormat.GZIPPED_BSDIFF
        )
    ): DeliveryResponse

    suspend fun wishlist(nextPageUrl: String): ResponseWrapper

    suspend fun purchaseHistory(nextPageUrl: String): ResponseWrapper

    suspend fun checkIn(): AndroidCheckinResponse

    companion object {
        const val URL_BASE = "https://android.clients.google.com"
        const val URL_FDFE = "$URL_BASE/fdfe"
        const val SEARCH_CHANNEL_URI = "${URL_FDFE}/search"
        const val BULK_DETAILS_URI = "${URL_FDFE}/bulkDetails"
        const val LIBRARY_URI = "${URL_FDFE}/library"
        const val PURCHASE_HISTORY_URL = "${URL_FDFE}/purchaseHistory"
        const val DELIVERY_URL = "$URL_FDFE/delivery"
        const val URL_CHECK_IN = "$URL_BASE/checkin"
        const val wishlistBackendId = 0
        const val searchBackendId = 3
    }
}