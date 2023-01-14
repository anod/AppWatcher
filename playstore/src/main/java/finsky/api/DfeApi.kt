package finsky.api

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

interface DfeApi {

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

    suspend fun purchaseHistory(url: String, offset: Int): ResponseWrapper

    companion object {
        const val URL_FDFE = "https://android.clients.google.com/fdfe"
        const val SEARCH_CHANNEL_URI = "${URL_FDFE}/search"
        const val BULK_DETAILS_URI = "${URL_FDFE}/bulkDetails"
        const val LIBRARY_URI = "${URL_FDFE}/library"
        const val PURCHASE_HISTORY_URL = "${URL_FDFE}/purchaseHistory"
        const val DELIVERY_URL = "$URL_FDFE/delivery"
        const val wishlistBackendId = 0
        const val searchBackendId = 3
    }
}