package finsky.api.model

import finsky.api.DfeApi
import finsky.protos.ResponseWrapper
import finsky.protos.Details

class DfeDetails(private val api: DfeApi) : DfeModel() {
    private var detailsResponse: Details.DetailsResponse? = null
    var detailsUrl: String = ""

    override suspend fun makeRequest(): ResponseWrapper {
        return api.details(detailsUrl)
    }

    val document: Document?
        get() = if (this.detailsResponse == null || this.detailsResponse!!.docV2 == null) {
            null
        } else Document(this.detailsResponse!!.docV2)

    override val isReady: Boolean
        get() = this.detailsResponse != null

    override fun onResponse(responseWrapper: ResponseWrapper) {
        this.detailsResponse = responseWrapper.payload.detailsResponse
    }
}
