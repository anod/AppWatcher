package finsky.api.model

import finsky.api.DfeUtils
import finsky.protos.Messages

abstract class ContainerList<T> constructor(
        url: String,
        autoLoadNextPage: Boolean,
        private val filter: FilterPredicate?
) : PaginatedList<T, Document>(url, autoLoadNextPage) {

    override fun getItemsFromResponse(wrapper: Messages.Response.ResponseWrapper): FilteredDocumentList<Document> {
        val payload = payload(wrapper)
        val doc = DfeUtils.getRootDoc(payload) ?: return FilteredDocumentList(arrayOf(), 0)

        val docs = doc.childList?.map { Document(it) } ?: listOf()
        if (filter == null) {
            return Pair(docs.toTypedArray(), docs.size)
        }

        val list = docs.filter(filter)
        return Pair(list.toTypedArray(), docs.size)
    }

    override fun getNextPageUrl(wrapper: Messages.Response.ResponseWrapper): String? {
        val payload = payload(wrapper)
        val rootDoc = DfeUtils.getRootDoc(payload) ?: return null

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
