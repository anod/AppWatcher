package finsky.api.model

import finsky.api.DfeApi
import finsky.protos.ResponseWrapper

class DfeSearch(
        private val dfeApi: DfeApi,
        private val initialQuery: String,
        private val nextPageUrl: String
) : DfeList(SEARCH) {
    override suspend fun makeRequest(): ResponseWrapper {
        return dfeApi.search(initialQuery, nextPageUrl)
    }
}
