// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.search

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.anod.appwatcher.database.entities.App
import finsky.api.DfeListType
import finsky.api.Document
import finsky.api.toListResponse
import finsky.protos.ResponseWrapper
import info.anodsplace.applog.AppLog
import info.anodsplace.framework.content.InstalledApps

data class ListItem(
    val document: Document,
    val installedInfo: InstalledApps.Info,
    val app: App
) {
    val isInstalled = installedInfo.isInstalled
    val isWatched: Boolean
        get() = app.rowId > -1
    val stableKey = hashCode()
}

abstract class ListEndpointPagingSource(
    private val listType: DfeListType,
    private val installedApps: InstalledApps,
    private val appAdapter: (Document) -> App
) : PagingSource<String, ListItem>() {

    private var isFirst = true

    abstract suspend fun execute(nextPageUrl: String): ResponseWrapper

    override suspend fun load(params: LoadParams<String>): LoadResult<String, ListItem> {
        try {
            if (params.key == null && !isFirst) {
                AppLog.d("ListPagingSource load: [${params.key}] null")
                return LoadResult.Page(emptyList(), null, null)
            }
            val nextPageUrl = params.key ?: ""
            val response = execute(nextPageUrl = nextPageUrl).toListResponse(listType)

            val items = response.items.map {
                val installedInfo = installedApps.packageInfo(it.docId)
                ListItem(
                    document = it,
                    installedInfo = installedInfo,
                    app = appAdapter(it)
                )
            }

            isFirst = false
            AppLog.d("ListPagingSource load: [${params.key}] $nextPageUrl")
            return LoadResult.Page(
                data = items,
                prevKey = null, // Only paging forward.
                nextKey = response.nextPageUrl
            )
        } catch (e: Exception) {
            AppLog.e(e)
            return LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<String, ListItem>): String? = null
}