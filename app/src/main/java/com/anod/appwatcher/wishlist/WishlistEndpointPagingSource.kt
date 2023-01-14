package com.anod.appwatcher.wishlist

import com.anod.appwatcher.search.ListEndpointPagingSource
import finsky.api.DfeApi
import finsky.api.DfeListType
import finsky.protos.ResponseWrapper

class WishListEndpointPagingSource(
    private val dfeApi: DfeApi,
) : ListEndpointPagingSource(DfeListType.ALL) {
    override suspend fun execute(nextPageUrl: String): ResponseWrapper {
        return dfeApi.wishlist(nextPageUrl)
    }
}