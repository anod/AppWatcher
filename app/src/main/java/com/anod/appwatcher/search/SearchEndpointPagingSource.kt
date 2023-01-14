package com.anod.appwatcher.search

import finsky.api.DfeApi
import finsky.api.DfeListType
import finsky.protos.ResponseWrapper

class SearchEndpointPagingSource(
    private val dfeApi: DfeApi,
    private val searchQuery: String
) : ListEndpointPagingSource(DfeListType.SEARCH) {
    override suspend fun execute(nextPageUrl: String): ResponseWrapper {
        return dfeApi.search(initialQuery = searchQuery, nextPageUrl = nextPageUrl)
    }
}