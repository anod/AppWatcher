package finsky.api

import android.accounts.Account
import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import finsky.protos.nano.Messages
import finsky.protos.nano.Messages.Details
import info.anodsplace.playstore.DeviceInfoProvider

/**
 * @author alex
 * @date 2015-02-15
 */
class DfeApiImpl(private val queue: RequestQueue, private val apiContext: DfeApiContext) : DfeApi {

    constructor(queue: RequestQueue, context: Context, account: Account, authToken: String, deviceInfo: DeviceInfoProvider)
            : this(queue, DfeApiContext(context, account, authToken, deviceInfo))
    
    override fun search(url: String, responseListener: Response.Listener<Messages.Response.ResponseWrapper>, errorListener: Response.ErrorListener): Request<*> {
        val dfeRequest = DfeRequest(url, this.apiContext, responseListener, errorListener)
        return this.queue.add(dfeRequest)
    }

    override fun details(url: String, responseListener: Response.Listener<Messages.Response.ResponseWrapper>, errorListener: Response.ErrorListener): Request<*> {
        val dfeRequest = DfeRequest(url, this.apiContext, responseListener, errorListener)
        return this.queue.add(dfeRequest)
    }

    override fun details(docIds: List<BulkDocId>, includeDetails: Boolean, listener: Response.Listener<Messages.Response.ResponseWrapper>, errorListener: Response.ErrorListener): Request<*> {
        val bulkDetailsRequest = Details.BulkDetailsRequest()
        bulkDetailsRequest.includeDetails = true
        bulkDetailsRequest.docid = docIds.map { it.packageName }.sorted().toTypedArray()
//        bulkDetailsRequest.docs = docIds.sorted().map {
//            val doc = Details.BulkDetailsRequestDoc()
//            doc.docid = it.packageName
//            doc.versionCode = it.versionCode
//            doc.properties = Details.BulkDetailsRequestDocProperties()
//            doc
//        }.toTypedArray()

        val dfeRequest = object : ProtoDfeRequest(DfeApi.BULK_DETAILS_URI.toString(), bulkDetailsRequest, apiContext, listener, errorListener) {
            private fun computeDocumentIdHash(): String {
                var n = 0L
                for (item in (this.request as Details.BulkDetailsRequest).docid) {
                    n = 31L * n + item.hashCode()
                }
                return n.toString()
            }

            override fun getCacheKey(): String {
                return super.getCacheKey() + "/docidhash=" + this.computeDocumentIdHash()
            }
        }
        dfeRequest.setShouldCache(true)
        return this.queue.add(dfeRequest)
    }

    override fun createLibraryUrl(c: Int, libraryId: String, dt: Int, serverToken: ByteArray?): String {
        val appendQueryParameter = DfeApi.LIBRARY_URI.buildUpon()
                .appendQueryParameter("c", c.toString())
                .appendQueryParameter("dt", dt.toString())
                .appendQueryParameter("libid", libraryId)

        if (serverToken != null) {
            appendQueryParameter.appendQueryParameter("st", DfeUtils.base64Encode(serverToken))
        }
        return appendQueryParameter.toString()
    }

    override fun list(url: String, listener: Response.Listener<Messages.Response.ResponseWrapper>, errorListener: Response.ErrorListener): Request<*> {
        val dfeRequest = DfeRequest(url, this.apiContext, listener, errorListener)
        return this.queue.add<Messages.Response.ResponseWrapper>(dfeRequest)
    }

}
