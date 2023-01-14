// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.search

import androidx.paging.PagingSource
import androidx.paging.PagingState
import finsky.api.DfeListType
import finsky.api.Document
import finsky.api.toListResponse
import finsky.protos.ResponseWrapper
import info.anodsplace.applog.AppLog

abstract class ListEndpointPagingSource(
    private val listType: DfeListType
) : PagingSource<String, Document>() {

    private var isFirst = true

    abstract suspend fun execute(nextPageUrl: String) : ResponseWrapper

    override suspend fun load(params: LoadParams<String>): LoadResult<String, Document> {
        try {
            if (params.key == null && !isFirst) {
                AppLog.d("ListPagingSource load: [${params.key}] null")
                return LoadResult.Page(emptyList(), null, null)
            }
            val nextPageUrl = params.key ?: ""
            val response = execute(nextPageUrl = nextPageUrl).toListResponse(listType)
            isFirst = false
            AppLog.d("ListPagingSource load: [${params.key}] $nextPageUrl")
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