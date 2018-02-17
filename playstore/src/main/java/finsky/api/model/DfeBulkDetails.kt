package finsky.api.model

import com.android.volley.Response
import finsky.api.DfeApi
import finsky.protos.nano.Messages
import finsky.protos.nano.Messages.Details

import info.anodsplace.framework.AppLog
import info.anodsplace.playstore.BuildConfig

class DfeBulkDetails(private val api: DfeApi,private val filter: FilterPredicate) : DfeRequestModel() {
    private var bulkDetailsResponse: Details.BulkDetailsResponse? = null
    var docIds: List<String> = listOf()

    override fun execute(responseListener: Response.Listener<Messages.Response.ResponseWrapper>, errorListener: Response.ErrorListener) {
        api.details(docIds, true, responseListener, errorListener)
    }

    val documents: List<Document>
        get() {
            val response = this.bulkDetailsResponse ?: return emptyList()

            val list = mutableListOf<Document>()
            for (i in response.entry.indices) {
                val doc = response.entry[i].doc
                if (doc == null) {
                    if (BuildConfig.DEBUG) {
                        AppLog.d("Null document for requested docId: %s ", this.docIds[i])
                    }
                } else {
                    list.add(Document(doc))
                }
            }

            if (list.isEmpty()) {
                return list
            } else {
                return list.filter(filter)
            }
        }

    override val isReady: Boolean
        get() = this.bulkDetailsResponse != null

    override fun onResponse(responseWrapper: Messages.Response.ResponseWrapper) {
        this.bulkDetailsResponse = responseWrapper.payload.bulkDetailsResponse
        this.notifyDataSetChanged()
    }

}