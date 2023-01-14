package finsky.api

class DfeSearch(
        private val dfeApi: DfeApi,
        private val initialQuery: String,
        private val nextPageUrl: String
) : DfeList(SEARCH) {
    override suspend fun execute(): DfeListResponse {
        val responseWrapper = dfeApi.search(initialQuery, nextPageUrl)
        return onResponse(responseWrapper)
    }
}
