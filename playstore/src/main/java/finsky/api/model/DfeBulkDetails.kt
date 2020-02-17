package finsky.api.model

import com.android.volley.Request
import com.android.volley.Response
import finsky.api.BulkDocId
import finsky.api.DfeApi
import finsky.protos.Messages
import finsky.protos.Messages.Details

class DfeBulkDetails(private val api: DfeApi, private val filter: FilterPredicate) : DfeModel() {
    private var bulkDetailsResponse: Details.BulkDetailsResponse? = null
    var docIds: List<BulkDocId> = listOf()
    override val url: String
        get() = ""

    override fun makeRequest(url: String, responseListener: Response.Listener<Messages.Response.ResponseWrapper>, errorListener: Response.ErrorListener): Request<*> {
        return api.details(docIds, true, responseListener, errorListener)
    }

    val documents: List<Document>
        get() {
            val response = this.bulkDetailsResponse ?: return emptyList()

            val list = mutableListOf<Document>()
            for (i in response.entryList.indices) {
                response.getEntry(i).doc?.let {
                    list.add(Document(it))
                }
            }

            return if (list.isEmpty()) {
                list
            } else {
                list.filter(filter)
            }
        }

    override val isReady: Boolean
        get() = this.bulkDetailsResponse != null

    override fun onResponse(responseWrapper: Messages.Response.ResponseWrapper) {
        this.bulkDetailsResponse = responseWrapper.payload.bulkDetailsResponse
    }
}