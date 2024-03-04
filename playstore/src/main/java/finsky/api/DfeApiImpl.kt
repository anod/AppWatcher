package finsky.api

import android.accounts.Account
import android.content.Context
import finsky.protos.DeliveryResponse
import finsky.protos.Details
import finsky.protos.Details.BulkDetailsResponse
import finsky.protos.Details.DetailsResponse
import finsky.protos.ResponseWrapper
import info.anodsplace.applog.AppLog
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.CacheControl
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.net.HttpURLConnection
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * @author alex
 * @date 2015-02-15
 */
class DfeApiImpl(http: OkHttpClient, private val apiContext: DfeApiContext) : DfeApi {

    private val http = httpWithCache(http)

    constructor(http: OkHttpClient, context: Context, account: Account, authTokenProvider: DfeAuthTokenProvider, deviceInfoProvider: DfeDeviceInfoProvider)
            : this(http, DfeApiContext(context, account, authTokenProvider, deviceInfoProvider))

    override suspend fun search(initialQuery: String, nextPageUrl: String): ResponseWrapper {
        val url = if (nextPageUrl.isEmpty())
                DfeApi.SEARCH_CHANNEL_URI.toHttpUrl().newBuilder()
                    .addQueryParameter("c", DfeApi.searchBackendId.toString())
                    .addQueryParameter("q", initialQuery)
                    .build().toString()
            else
                DfeApi.URL_FDFE + "/" + nextPageUrl
        val cacheKey = DfeCacheKey(url, apiContext, emptyMap())
        val dfeRequest = createRequest(cacheKey)
        return newCall(dfeRequest)
    }

    override suspend fun details(appDetailsUrl: String): DetailsResponse {
        val cacheKey = DfeCacheKey(DfeApi.URL_FDFE + "/" + appDetailsUrl, apiContext, emptyMap())
        val dfeRequest = createRequest(cacheKey)
        val responseWrapper = newCall(dfeRequest)
        return responseWrapper.payload.detailsResponse
    }

    override suspend fun details(docIds: List<BulkDocId>, includeDetails: Boolean): BulkDetailsResponse {
        val bulkDetailsRequest = Details.BulkDetailsRequest.newBuilder()
                .setIncludeDetails(true)
                .addAllDocid(docIds.map { it.packageName }.sorted())
                .build()

        val cacheKey = DfeCacheKey(
                DfeApi.BULK_DETAILS_URI, apiContext,
                mapOf("docidhash" to computeDocumentIdHash(bulkDetailsRequest))
        )

        val dfeRequest = createRequest(cacheKey) { builder ->
            builder.post(bulkDetailsRequest.toByteArray().toRequestBody("application/x-protobuf".toMediaType()))
        }

        val responseWrapper = newCall(dfeRequest)
        return responseWrapper.payload.bulkDetailsResponse
    }

    override suspend fun delivery(
        docId: String,
        installedVersionCode: Int,
        updateVersionCode: Int,
        offerType: Int,
        patchFormats: Array<PatchFormat>
    ): DeliveryResponse {
        val url = DfeApi.DELIVERY_URL.toHttpUrl().newBuilder()
            .addQueryParameter("ot", offerType.toString())
            .addQueryParameter("doc", docId)
            .addQueryParameter("vc", updateVersionCode.toString())
            .build().toString()

        val cacheKey = DfeCacheKey(url, apiContext, emptyMap())
        val dfeRequest = createRequest(cacheKey)
        val responseWrapper = newCall(dfeRequest)
        return responseWrapper.payload.deliveryResponse
    }

    override suspend fun wishlist(nextPageUrl: String): ResponseWrapper {
        val libraryId = "u-wl"
        val url = if (nextPageUrl.isEmpty())
                createLibraryUrl(DfeApi.wishlistBackendId, libraryId, 7, null)
            else
                DfeApi.URL_FDFE + "/" + nextPageUrl
        val cacheKey = DfeCacheKey(url, apiContext, emptyMap())
        val dfeRequest = createRequest(cacheKey)
        return newCall(dfeRequest)
    }

    override suspend fun purchaseHistory(nextPageUrl: String): ResponseWrapper {
        val url = if (nextPageUrl.isEmpty())
            DfeApi.PURCHASE_HISTORY_URL + "?o=0"
        else
            DfeApi.URL_FDFE + "/" + nextPageUrl
        val cacheKey = DfeCacheKey(url, apiContext, emptyMap())
        val dfeRequest = createRequest(cacheKey)
        return newCall(dfeRequest)
    }

    private suspend fun newCall(dfeRequest: Request): ResponseWrapper = suspendCancellableCoroutine { continuation ->
        val dfeResponse = DfeResponse()
        val call =  http.newCall(dfeRequest)

        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                AppLog.d("Network failure [${dfeRequest.url}] $e")
                continuation.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                AppLog.d("Network response $response")

                try {
                    response.body?.use { body ->
                        if (!response.isSuccessful) {
                            dfeResponse.parseNetworkError(body.byteStream())
                            if (response.code == HttpURLConnection.HTTP_NOT_FOUND) {
                                throw DfeServerError("Not Found")
                            } else {
                                throw DfeServerError("Status code ${response.code}")
                            }
                        } else {
                            val responseWrapper = dfeResponse.parseNetworkResponse(body.byteStream())
                            continuation.resume(responseWrapper)
                        }
                    } ?: throw DfeError("Empty body $response")
                } catch (e: Throwable) {
                    continuation.resumeWithException(e)
                }
            }
        })

        continuation.invokeOnCancellation { call.cancel() }
    }

    private fun createRequest(cacheKey: DfeCacheKey, customizer: ((Request.Builder) -> Unit)? = null): Request {
        return Request.Builder()
                .url(cacheKey.cacheUrl)
                .apply {
                    if (customizer == null) {
                        get()
                    } else {
                        customizer(this)
                    }
                    apiContext.createHeaders().forEach { entry ->
                        addHeader(entry.key, entry.value)
                    }
                }
                .tag(DfeCacheKey::class.java, cacheKey)
                .build()
    }

    class CacheInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val cacheKey = request.tag(DfeCacheKey::class.java)
            if (chain.call().isCanceled()) {
                return chain.proceed(request)
            }
            return if (cacheKey != null) {
                val networkRequest = request.newBuilder().url(cacheKey.networkUrl).build()
                val networkResponse = chain.proceed(networkRequest)
                val cacheControl = parseCacheHeaders(networkResponse)
                networkResponse.newBuilder().request(
                    request.newBuilder()
                            .url(cacheKey.cacheUrl)
                            .cacheControl(cacheControl)
                    .build()
                ).build()
            } else {
                chain.proceed(request)
            }
        }

        private fun parseCacheHeaders(networkResponse: Response): CacheControl {
            val networkCacheControl = networkResponse.cacheControl
            val cacheControlBuilder = CacheControl.Builder()
            val currentTimeMillis = System.currentTimeMillis()
            try {
                val softTtl = networkResponse.headers["X-DFE-Soft-TTL"]?.toLongOrNull()
                val stale = if (softTtl == null) 0 else TimeUnit.MILLISECONDS.toSeconds(currentTimeMillis + softTtl).toInt()
                if (stale > 0) {
                    cacheControlBuilder.maxStale(stale, TimeUnit.SECONDS)
                } else {
                    if (networkCacheControl.maxStaleSeconds > 0) {
                        cacheControlBuilder.maxStale(networkCacheControl.maxStaleSeconds, TimeUnit.SECONDS)
                    }
                }
                val hardTtl = networkResponse.headers["X-DFE-Hard-TTL"]?.toLongOrNull()
                val maxAge = if (hardTtl == null) 0 else TimeUnit.MILLISECONDS.toSeconds(currentTimeMillis + hardTtl).toInt()
                if (maxAge > 0) {
                    cacheControlBuilder.maxAge(maxAge, TimeUnit.SECONDS)
                } else {
                    if (networkCacheControl.maxAgeSeconds > 0) {
                        cacheControlBuilder.maxAge(networkCacheControl.maxAgeSeconds, TimeUnit.SECONDS)
                    }
                }
            } catch (ex: NumberFormatException) {
                AppLog.e("Invalid TTL: ${networkResponse.headers}", ex)
                cacheControlBuilder.maxStale(networkCacheControl.maxStaleSeconds, TimeUnit.SECONDS)
                cacheControlBuilder.maxAge(networkCacheControl.maxAgeSeconds, TimeUnit.SECONDS)
            }

            return cacheControlBuilder.build()
        }
    }

    private fun createLibraryUrl(c: Int, libraryId: String, dt: Int, serverToken: ByteArray?): String {
        val builder = DfeApi.LIBRARY_URI.toHttpUrl().newBuilder()
                .addQueryParameter("c", c.toString())
                .addQueryParameter("dt", dt.toString())
                .addQueryParameter("libid", libraryId)

        if (serverToken != null) {
            builder.addQueryParameter("st", DfeUtils.base64Encode(serverToken))
        }
        return builder.build().toString()
    }

    companion object {
        private fun httpWithCache(http: OkHttpClient): OkHttpClient {
            return http.newBuilder().addNetworkInterceptor(CacheInterceptor()).build()
        }

        private fun computeDocumentIdHash(request: Details.BulkDetailsRequest): String {
            var n = 0L
            for (item in request.docidList) {
                n = 31L * n + item.hashCode()
            }
            return n.toString()
        }
    }
}
