package com.anod.appwatcher.search

import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.utils.date.UploadDateParserCache
import finsky.api.DfeApi
import finsky.api.DfeListType
import finsky.protos.ResponseWrapper
import info.anodsplace.framework.content.InstalledApps

class SearchEndpointPagingSource(
    private val dfeApi: DfeApi,
    private val searchQuery: String,
    installedApps: InstalledApps,
    uploadDateParserCache: UploadDateParserCache
) : ListEndpointPagingSource(
    listType = DfeListType.SEARCH,
    installedApps = installedApps,
    appAdapter = { document -> App(document, uploadDateParserCache) }
) {
    override suspend fun execute(nextPageUrl: String): ResponseWrapper {
        return dfeApi.search(initialQuery = searchQuery, nextPageUrl = nextPageUrl)
    }
}