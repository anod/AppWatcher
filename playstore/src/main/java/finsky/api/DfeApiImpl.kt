package finsky.api

import android.accounts.Account
import android.content.Context
import android.net.Uri
import finsky.protos.ResponseWrapper
import finsky.protos.Details
import info.anodsplace.applog.AppLog
import info.anodsplace.playstore.DeviceInfoProvider
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * @author alex
 * @date 2015-02-15
 */
class DfeApiImpl(http: OkHttpClient, private val apiContext: DfeApiContext) : DfeApi {

    private val http = httpWithCache(http)

    constructor(queue: OkHttpClient, context: Context, account: Account, authToken: String, deviceInfo: DeviceInfoProvider)
            : this(queue, DfeApiContext(context, account, authToken, deviceInfo))


    override suspend fun search(url: String): ResponseWrapper {
        val cacheKey = DfeCacheKey(DfeApi.URL_FDFE + url, apiContext, emptyMap())
        val dfeRequest = createRequest(cacheKey)
        return newCall(dfeRequest)
    }

    override suspend fun details(url: String): ResponseWrapper {
        val cacheKey = DfeCacheKey(DfeApi.URL_FDFE + url, apiContext, emptyMap())
        val dfeRequest = createRequest(cacheKey)
        return newCall(dfeRequest)
    }

    override suspend fun details(docIds: List<BulkDocId>, includeDetails: Boolean): ResponseWrapper {
        val bulkDetailsRequest = Details.BulkDetailsRequest.newBuilder()
                .setIncludeDetails(true)
                .addAllDocid(docIds.map { it.packageName }.sorted())
                .build()

        val cacheKey = DfeCacheKey(
                DfeApi.URL_FDFE + DfeApi.BULK_DETAILS_URI, apiContext,
                mapOf("docidhash" to computeDocumentIdHash(bulkDetailsRequest))
        )

        val dfeRequest = createRequest(cacheKey) { builder ->
            builder.post(bulkDetailsRequest.toByteArray().toRequestBody("application/x-protobuf".toMediaType()))
        }

        return newCall(dfeRequest)
    }

    override fun createLibraryUrl(c: Int, libraryId: String, dt: Int, serverToken: ByteArray?): String {
        val appendQueryParameter = Uri.parse(DfeApi.LIBRARY_URI).buildUpon()
                .appendQueryParameter("c", c.toString())
                .appendQueryParameter("dt", dt.toString())
                .appendQueryParameter("libid", libraryId)

        if (serverToken != null) {
            appendQueryParameter.appendQueryParameter("st", DfeUtils.base64Encode(serverToken))
        }
        return appendQueryParameter.toString()
    }

    override suspend fun list(url: String): ResponseWrapper {
        val cacheKey = DfeCacheKey(DfeApi.URL_FDFE + url, apiContext, emptyMap())
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
                AppLog.d("Network response [${dfeRequest.url}] $response")

                try {
                    val body = response.body ?: throw DfeError("Empty body $response")
                    if (!response.isSuccessful) {
                        dfeResponse.parseNetworkError(body.byteStream())
                    } else {
                        val responseWrapper = dfeResponse.parseNetworkResponse(body.byteStream())
                        continuation.resume(responseWrapper)
                    }
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
                    cacheControlBuilder.maxStale(networkCacheControl.maxStaleSeconds, TimeUnit.SECONDS)
                }
                val hardTtl = networkResponse.headers["X-DFE-Hard-TTL"]?.toLongOrNull()
                val maxAge = if (hardTtl == null) 0 else TimeUnit.MILLISECONDS.toSeconds(currentTimeMillis + hardTtl).toInt()
                if (maxAge > 0) {
                    cacheControlBuilder.maxAge(maxAge, TimeUnit.SECONDS)
                } else {
                    cacheControlBuilder.maxAge(networkCacheControl.maxAgeSeconds, TimeUnit.SECONDS)
                }
            } catch (ex: NumberFormatException) {
                AppLog.e("Invalid TTL: ${networkResponse.headers}", ex)
                cacheControlBuilder.maxStale(networkCacheControl.maxStaleSeconds, TimeUnit.SECONDS)
                cacheControlBuilder.maxAge(networkCacheControl.maxAgeSeconds, TimeUnit.SECONDS)
            }

            return cacheControlBuilder.build()
        }
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
