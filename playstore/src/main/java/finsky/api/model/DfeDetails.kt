package finsky.api.model

import com.android.volley.Response
import finsky.api.DfeApi
import finsky.protos.nano.Messages
import finsky.protos.nano.Messages.Details

class DfeDetails(private val api: DfeApi) : DfeRequestModel() {
    private var detailsResponse: Details.DetailsResponse? = null
    var detailsUrl: String = ""

    override fun execute(responseListener: Response.Listener<Messages.Response.ResponseWrapper>, errorListener: Response.ErrorListener) {
        api.details(detailsUrl, responseListener, errorListener)
    }

    val document: Document?
        get() = if (this.detailsResponse == null || this.detailsResponse!!.docV2 == null) {
            null
        } else Document(this.detailsResponse!!.docV2)

    override val isReady: Boolean
        get() = this.detailsResponse != null

    override fun onResponse(responseWrapper: Messages.Response.ResponseWrapper) {
        this.detailsResponse = responseWrapper.payload.detailsResponse
        this.notifyDataSetChanged()
    }
}
