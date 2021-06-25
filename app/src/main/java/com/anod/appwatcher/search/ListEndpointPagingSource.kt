// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.search

import androidx.paging.PagingSource
import androidx.paging.PagingState
import finsky.api.model.Document
import info.anodsplace.applog.AppLog
import info.anodsplace.playstore.ListEndpoint

class ListEndpointPagingSource(
        private val endpoint: ListEndpoint
) : PagingSource<String, Document>() {

    private var isFirst = true
    override suspend fun load(params: LoadParams<String>): LoadResult<String, Document> {
        try {
            if (params.key == null && !isFirst) {
                AppLog.d("ListPagingSource load: [${params.key}] null")
                return LoadResult.Page(emptyList(), null, null)
            }
            endpoint.nextPageUrl = params.key ?: ""
            val listModel = endpoint.start()
            listModel.execute()
            val response = listModel.listResponse!!
            isFirst = false
            AppLog.d("ListPagingSource load: [${params.key}] ${response.nextPageUrl}")
            return LoadResult.Page(
                    data = response.items,
                    prevKey = null, // Only paging forward.
                    nextKey = response.nextPageUrl
            )
        } catch (e: Exception) {
            AppLog.e(e)
            return LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<String, Document>): String? = null
}