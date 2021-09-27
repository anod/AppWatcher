package finsky.api

import okhttp3.HttpUrl.Companion.toHttpUrl

class DfeCacheKey(val cacheUrl: String, val networkUrl: String) {
    constructor(networkUrl: String, context: DfeApiContext, extra: Map<String, String>) :
            this(networkUrl
                    .toHttpUrl()
                    .newBuilder()
                    .addQueryParameter("cacheAccount", context.accountName)
                    .apply {
                        extra.forEach { item ->
                            addQueryParameter(item.key, item.value)
                        }
                    }
                    .build()
                    .toString(), networkUrl
            )
}