package finsky.api.model

import finsky.api.DfeApi
import finsky.protos.ResponseWrapper
import finsky.protos.Details

class DfeDetails(private val api: DfeApi) : DfeModel() {
    private var detailsResponse: Details.DetailsResponse? = null
    var detailsUrl: String = ""
    override val url: String
        get() = detailsUrl

    override suspend fun makeRequest(url: String): ResponseWrapper {
        return api.details(url)
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
