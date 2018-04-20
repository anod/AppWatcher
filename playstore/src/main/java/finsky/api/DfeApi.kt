package finsky.api

import android.net.Uri

import com.android.volley.Request
import com.android.volley.Response
import finsky.protos.nano.Messages

class BulkDocId(val packageName: String, val versionCode: Int) : Comparable<BulkDocId> {
    override fun compareTo(other: BulkDocId): Int {
        return packageName.compareTo(other.packageName)
    }
}

interface DfeApi {

    fun search(url: String, responseListener: Response.Listener<Messages.Response.ResponseWrapper>, errorListener: Response.ErrorListener): Request<*>

    fun details(url: String, noPrefetch: Boolean, noBulkCancel: Boolean, responseListener: Response.Listener<Messages.Response.ResponseWrapper>, errorListener: Response.ErrorListener): Request<*>

    fun details(docIds: List<BulkDocId>, includeDetails: Boolean, listener: Response.Listener<Messages.Response.ResponseWrapper>, errorListener: Response.ErrorListener): Request<*>

    fun createLibraryUrl(c: Int, libraryId: String, dt: Int, serverToken: ByteArray?): String

    fun list(url: String, listener: Response.Listener<Messages.Response.ResponseWrapper>, errorListener: Response.ErrorListener): Request<*>

    companion object {
        val BASE_URI = Uri.parse("https://android.clients.google.com/fdfe/")!!
        val SEARCH_CHANNEL_URI = Uri.parse("search")!!
        val BULK_DETAILS_URI = Uri.parse("bulkDetails")!!
        val LIBRARY_URI = Uri.parse("library")!!
    }
}

