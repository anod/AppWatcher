package finsky.api

import finsky.protos.ResponseWrapper

class BulkDocId(val packageName: String, val versionCode: Int) : Comparable<BulkDocId> {
    override fun compareTo(other: BulkDocId): Int {
        return packageName.compareTo(other.packageName)
    }

    override fun toString(): String {
        return "$packageName ($versionCode)"
    }
}

interface DfeApi {

    suspend fun search(initialQuery: String, nextPageUrl: String): ResponseWrapper

    suspend fun details(appDetailsUrl: String): ResponseWrapper

    suspend fun details(docIds: List<BulkDocId>, includeDetails: Boolean): ResponseWrapper

    suspend fun wishlist(nextPageUrl: String): ResponseWrapper

    suspend fun purchaseHistory(url: String, offset: Int): ResponseWrapper

    companion object {
        const val URL_FDFE = "https://android.clients.google.com/fdfe"
        const val SEARCH_CHANNEL_URI = "${URL_FDFE}/search"
        const val BULK_DETAILS_URI = "${URL_FDFE}/bulkDetails"
        const val LIBRARY_URI = "${URL_FDFE}/library"
        const val PURCHASE_HISTORY_URL = "${URL_FDFE}/purchaseHistory"

        const val wishlistBackendId = 0
        const val searchBackendId = 3
    }
}

