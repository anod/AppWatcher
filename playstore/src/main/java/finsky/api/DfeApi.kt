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

    suspend fun search(url: String): ResponseWrapper

    suspend fun details(url: String): ResponseWrapper

    suspend fun details(docIds: List<BulkDocId>, includeDetails: Boolean): ResponseWrapper

    fun createLibraryUrl(c: Int, libraryId: String, dt: Int, serverToken: ByteArray?): String

    suspend fun list(url: String): ResponseWrapper

    companion object {
        const val URL_FDFE = "https://android.clients.google.com/fdfe/"
        const val SEARCH_CHANNEL_URI = "search"
        const val BULK_DETAILS_URI = "bulkDetails"
        const val LIBRARY_URI = "library"
        const val PURCHASE_URL = "purchase"
        const val PURCHASE_HISTORY_URL = "purchaseHistory"
        const val URL_TESTING_PROGRAM = "apps/testingProgram"
    }
}

