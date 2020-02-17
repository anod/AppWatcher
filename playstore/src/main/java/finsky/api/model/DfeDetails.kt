package finsky.api.model

import com.android.volley.Request
import com.android.volley.Response
import finsky.api.DfeApi
import finsky.protos.Messages
import finsky.protos.Messages.Details

class DfeDetails(private val api: DfeApi) : DfeModel() {
    private var detailsResponse: Details.DetailsResponse? = null
    var detailsUrl: String = ""
    override val url: String
        get() = detailsUrl

    override fun makeRequest(url: String, responseListener: Response.Listener<Messages.Response.ResponseWrapper>, errorListener: Response.ErrorListener): Request<*> {
        return api.details(url, responseListener, errorListener)
    }

    val document: Document?
        get() = if (this.detailsResponse == null || this.detailsResponse!!.docV2 == null) {
            null
        } else Document(this.detailsResponse!!.docV2)

    override val isReady: Boolean
        get() = this.detailsResponse != null

    override fun onResponse(responseWrapper: Messages.Response.ResponseWrapper) {
        this.detailsResponse = responseWrapper.payload.detailsResponse
    }
}
