package finsky.api

import com.google.protobuf.InvalidProtocolBufferException
import finsky.protos.DocV2
import finsky.protos.ListResponse
import finsky.protos.Payload
import finsky.protos.ResponseWrapper
import finsky.protos.Search.SearchResponse

data class DfeListResponse(val items: List<Document>, val nextPageUrl: String?)

enum class DfeListType {
    ALL,
    SEARCH,
    SIMILAR,
    RELATED,
}

fun ResponseWrapper.toListResponse(listType: DfeListType): DfeListResponse {
    return DfeList(listType).convert(this)
}

private class DfeList(private val listType: DfeListType) {

    fun convert(responseWrapper: ResponseWrapper): DfeListResponse {
        val nextPageUrls = mutableListOf<Pair<String, String>>()
        val items = mutableListOf<DocV2>()
        collect(responseWrapper, items, nextPageUrls)
        val docs = items.map { Document(it) }
        val nextPageUrl = nextPageUrls.lastOrNull()?.second
        return DfeListResponse(docs, nextPageUrl)
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
            7 -> {
                if (doc.hasContainerMetadata() && doc.containerMetadata.nextPageUrl.isNotBlank()) {
                    nextPageUrls.add(Pair(doc.title ?: "", doc.containerMetadata.nextPageUrl))
                }
                if (doc.hasAnnotations()
                    && doc.annotations.hasPurchaseHistoryDetails()
                    && doc.annotations.purchaseHistoryDetails.hasPurchaseStatus()
                ) {
                    return
                }

                for (docV2 in doc.childList) {
                    if (docV2.docType == 1) {
                        items.add(docV2)
                    }
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
            DfeListType.ALL -> {
                true
            }
            DfeListType.SEARCH -> {
                backendDocId != null && backendDocId.matches(Regex(".*search.*"))
            }
            DfeListType.SIMILAR -> {
                backendDocId != null && backendDocId.matches(Regex("similar_apps"))
            }
            DfeListType.RELATED -> {
                backendDocId != null && backendDocId
                        .matches(Regex("pre_install_users_also_installed"))
            }
        }
    }

    companion object {
        private fun payload(responseWrapper: ResponseWrapper?): Payload {
            return if (responseWrapper != null && responseWrapper.hasPayload()) {
                responseWrapper.payload
            } else Payload.getDefaultInstance()
        }

        private fun searchResponse(responseWrapper: ResponseWrapper?): SearchResponse {
            val payload: Payload = payload(responseWrapper)
            return if (payload(responseWrapper).hasSearchResponse()) {
                payload.searchResponse
            } else SearchResponse.getDefaultInstance()
        }

        private fun listResponse(responseWrapper: ResponseWrapper?): ListResponse {
            val payload: Payload = payload(responseWrapper)
            return if (payload.hasListResponse()) {
                payload.listResponse
            } else ListResponse.getDefaultInstance()
        }
    }
}