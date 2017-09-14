package com.google.android.finsky.api.model

import com.anod.appwatcher.utils.CollectionsUtils
import com.google.android.finsky.api.DfeUtils
import com.google.android.finsky.protos.nano.Messages

abstract class ContainerList<T> constructor(
        url: String,
        autoLoadNextPage: Boolean,
        responseFilter: CollectionsUtils.Predicate<Document>
    ) : PaginatedList<T, Document>(url, autoLoadNextPage) {

    private val responseFiler: CollectionsUtils.Predicate<Document>? = responseFilter

    override fun getItemsFromResponse(wrapper: Messages.Response.ResponseWrapper): Array<Document> {
        val payload = payload(wrapper)
        val doc = DfeUtils.getRootDoc(payload) ?: return arrayOf()

        val docs = doc.child?.map { Document(it) } ?: listOf()
        if (responseFiler == null) {
            return docs.toTypedArray()
        }
        val list = CollectionsUtils.filter(docs, responseFiler)
        return list.toTypedArray()
    }

    override fun getNextPageUrl(wrapper: Messages.Response.ResponseWrapper): String? {
        val payload = payload(wrapper)
        val doc = DfeUtils.getRootDoc(payload) ?: return null

        val containerMetadata = doc.containerMetadata
        var nextPageUrl: String? = null
        if (containerMetadata != null) {
            nextPageUrl = doc.containerMetadata.nextPageUrl
        }
        return nextPageUrl
    }

    private fun payload(wrapper: Messages.Response.ResponseWrapper): Messages.Response.Payload {
        val payload = wrapper.payload
        return if (wrapper.preFetch.isNotEmpty() && (payload.searchResponse != null && payload.searchResponse.doc.isEmpty() || payload.listResponse != null && payload.listResponse.doc.isEmpty())) {
            wrapper.preFetch[0].response.payload
        } else payload
    }

}
