package finsky.api.model

import com.android.volley.Request
import com.android.volley.Response
import finsky.api.DfeApi
import finsky.api.DfeUtils
import finsky.protos.Messages
import finsky.protos.Messages.ListResponse

class DfeListResponse(val items: List<Document>, val nextPageUrl: String?)

open class DfeList(private val dfeApi: DfeApi,
                   override val url: String)
    : DfeModel() {

    val count: Int
        get() = listResponse?.items?.size ?: 0

    var listResponse: DfeListResponse? = null
        private set

    override fun onResponse(responseWrapper: Messages.Response.ResponseWrapper) {
        val payload = payload(responseWrapper)
        val rootDoc = DfeUtils.getRootDoc(payload)
        if (rootDoc == null) {
            this.listResponse = DfeListResponse(emptyList(), null)
        } else {
            val docs = rootDoc.childList?.map { Document(it) } ?: listOf()
            val nextPageUrl =  getNextPageUrl(rootDoc)
            this.listResponse = DfeListResponse(docs, nextPageUrl)
        }
    }

    override val isReady: Boolean
        get() = this.listResponse != null

    override fun makeRequest(url: String, responseListener: Response.Listener<Messages.Response.ResponseWrapper>, errorListener: Response.ErrorListener): Request<*> {
        return this.dfeApi.list(url, responseListener, errorListener)
    }

    companion object {
        private fun getNextPageUrl(rootDoc: Messages.DocV2): String? {
            if (rootDoc.containerMetadata.hasNextPageUrl()) {
                return rootDoc.containerMetadata.nextPageUrl
            }
            if (rootDoc.hasRelatedLinks()
                    && rootDoc.relatedLinks.hasUnknown1()
                    && rootDoc.relatedLinks.unknown1.hasUnknown2()
                    && rootDoc.relatedLinks.unknown1.unknown2.hasNextPageUrl()
            ) {
                return rootDoc.relatedLinks.unknown1.unknown2.nextPageUrl;
            }
            return null
        }

        private fun payload(wrapper: Messages.Response.ResponseWrapper): Messages.Response.Payload {
            val payload = wrapper.payload
            return if (wrapper.preFetchList.isNotEmpty()
                    && (payload.searchResponse.docList.isEmpty() || payload.listResponse.docList.isEmpty())) {
                wrapper.getPreFetch(0).response.payload
            } else payload
        }
    }
}