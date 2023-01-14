package com.anod.appwatcher.history

import com.anod.appwatcher.search.ListEndpointPagingSource
import finsky.api.DfeApi
import finsky.api.DfeListType
import finsky.protos.ResponseWrapper

class HistoryEndpointPagingSource(
    private val dfeApi: DfeApi,
) : ListEndpointPagingSource(DfeListType.ALL) {
    override suspend fun execute(nextPageUrl: String): ResponseWrapper {
        return dfeApi.purchaseHistory(nextPageUrl)
    }
}