package finsky.api.model

import com.android.volley.Request
import com.android.volley.Response
import com.google.protobuf.InvalidProtocolBufferException
import finsky.api.DfeApi
import finsky.protos.Messages
import finsky.protos.Messages.DocV2
import finsky.protos.Messages.ListResponse
import finsky.protos.Messages.Response.ResponseWrapper
import finsky.protos.Messages.Search.SearchResponse

class DfeListResponse(val items: List<Document>, val nextPageUrl: String?)

open class DfeList(private val dfeApi: DfeApi,
                   override val url: String,
                   private val listType: Int = ALL
) : DfeModel() {

    val count: Int
        get() = listResponse?.items?.size ?: 0

    var listResponse: DfeListResponse? = null
        private set

    override fun onResponse(responseWrapper: ResponseWrapper) {
        val nextPageUrls = mutableListOf<Pair<String, String>>()
        val items = mutableListOf<DocV2>()
        collect(responseWrapper, items, nextPageUrls)
        val docs = items.map { Document(it) }
        val nextPageUrl = nextPageUrls.lastOrNull()?.second
        this.listResponse = DfeListResponse(docs, nextPageUrl)
    }

    private fun collect(rw: ResponseWrapper, items: MutableList<DocV2>, nextPageUrls: MutableList<Pair<String, String>>) {
        searchResponse(rw).docList.forEach {
            append(it, items, nextPageUrls)
        }
        listResponse(rw).docList.forEach {
            append(it, items, nextPageUrls)
        }
        for (pf in rw.preFetchList) {
            try {
                val responseWrapper = ResponseWrapper.parseFrom(pf.response.toByteString())
                collect(responseWrapper, items, nextPageUrls)
            } catch (ignored: InvalidProtocolBufferException) {
            }
        }
    }

    private fun append(doc: DocV2, items: MutableList<DocV2>, nextPageUrls: MutableList<Pair<String, String>>) {
        when (doc.docType) {
            46 -> {
                for (child in doc.childList) {
                    if (accept(child)) {
                        append(child, items, nextPageUrls)
                    }
                }
            }
            45 -> {
                for (docV2 in doc.childList) {
                    if (docV2.docType == 1) {
                        items.add(docV2)
                    }
                }
                if (doc.hasContainerMetadata() && doc.containerMetadata.nextPageUrl.isNotBlank()) {
                    nextPageUrls.add(Pair(doc.title ?: "", doc.containerMetadata.nextPageUrl))
                }
            }
            else -> {
                for (child in doc.childList) {
                    append(child, items, nextPageUrls)
                }
            }
        }
    }

    private fun accept(doc: DocV2): Boolean {
        val backendDocId = doc.backendDocid
        return when (listType) {
            ALL -> {
                true
            }
            SEARCH -> {
                backendDocId != null && backendDocId.matches(Regex(".*search.*"))
            }
            SIMILAR -> {
                backendDocId != null && backendDocId.matches(Regex("similar_apps"))
            }
            RELATED -> {
                backendDocId != null && backendDocId
                        .matches(Regex("pre_install_users_also_installed"))
            }
            else -> {
                false
            }
        }
    }

    override val isReady: Boolean
        get() = this.listResponse != null

    override fun makeRequest(url: String, responseListener: Response.Listener<ResponseWrapper>, errorListener: Response.ErrorListener): Request<*> {
        return this.dfeApi.list(url, responseListener, errorListener)
    }

    companion object {

        private fun payload(responseWrapper: ResponseWrapper?): Messages.Response.Payload {
            return if (responseWrapper != null && responseWrapper.hasPayload()) {
                responseWrapper.payload
            } else Messages.Response.Payload.getDefaultInstance()
        }

        private fun searchResponse(responseWrapper: ResponseWrapper?): SearchResponse {
            val payload: Messages.Response.Payload = payload(responseWrapper)
            return if (payload(responseWrapper).hasSearchResponse()) {
                payload.searchResponse
            } else SearchResponse.getDefaultInstance()
        }

        private fun listResponse(responseWrapper: ResponseWrapper?): ListResponse {
            val payload: Messages.Response.Payload = payload(responseWrapper)
            return if (payload.hasListResponse()) {
                payload.listResponse
            } else ListResponse.getDefaultInstance()
        }

        const val ALL = 0
        const val SEARCH = 1
        const val SIMILAR = 2
        const val RELATED = 3
    }
}