package finsky.api

class DfeWishList(
        private val dfeApi: DfeApi,
        private val nextPageUrl: String
) : DfeList(ALL) {
    override suspend fun execute(): DfeListResponse {
        val responseWrapper = dfeApi.wishlist(nextPageUrl)
        return onResponse(responseWrapper)
    }
}