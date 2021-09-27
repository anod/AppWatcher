package finsky.api.model

import finsky.api.DfeApi
import finsky.protos.ResponseWrapper

class DfeWishList(
        private val dfeApi: DfeApi,
        private val nextPageUrl: String
) : DfeList(ALL) {
    override suspend fun makeRequest(): ResponseWrapper {
        return dfeApi.wishlist(nextPageUrl)
    }
}