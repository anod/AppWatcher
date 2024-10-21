package finsky.api

import android.content.Context
import com.google.protobuf.MessageLite
import finsky.api.DfeApi.Companion.URL_CHECK_IN
import finsky.api.utils.checkinRequest
import finsky.api.utils.toProto
import finsky.protos.AndroidCheckinResponse
import finsky.protos.DeliveryResponse
import finsky.protos.Details
import finsky.protos.Details.BulkDetailsResponse
import finsky.protos.Details.DetailsResponse
import finsky.protos.ResponseWrapper
import finsky.protos.UploadDeviceConfigRequest
import finsky.protos.UploadDeviceConfigResponse
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
import java.io.InputStream
import java.net.HttpURLConnection
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * @author alex
 * @date 2015-02-15
 */
class DfeApiImpl(private val http: OkHttpClient, private val apiContext: DfeApiContext) : DfeApi {

    override val authenticated: Boolean
        get() = apiContext.hasAuth

    constructor(http: OkHttpClient, context: Context, authTokenProvider: DfeAuthProvider, deviceInfoProvider: DfeDeviceInfoProvider)
            : this(http, DfeApiContext(context, authTokenProvider, deviceInfoProvider))

    override suspend fun search(initialQuery: String, nextPageUrl: String): ResponseWrapper {
        val url = if (nextPageUrl.isEmpty())
                DfeApi.SEARCH_CHANNEL_URI.toHttpUrl().newBuilder()
                    .addQueryParameter("c", DfeApi.searchBackendId.toString())
                    .addQueryParameter("q", initialQuery)
//                    .addQueryParameter("ksm", "1")
                    .build().toString()
            else
                DfeApi.URL_FDFE + "/" + nextPageUrl
        val dfeRequest = createRequest(url)
        return newCall(dfeRequest)
    }

    override suspend fun details(appDetailsUrl: String): DetailsResponse {
        val dfeRequest = createRequest(DfeApi.URL_FDFE + "/" + appDetailsUrl)
        val responseWrapper = newCall(dfeRequest)
        return responseWrapper.payload.detailsResponse
    }

    override suspend fun details(docIds: List<BulkDocId>, includeDetails: Boolean): BulkDetailsResponse {
        val bulkDetailsRequest = Details.BulkDetailsRequest.newBuilder()
                .setIncludeDetails(true)
                .addAllDocid(docIds.map { it.packageName }.sorted())
                .build()

        val dfeRequest = createRequest(DfeApi.BULK_DETAILS_URI) { builder ->
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

        val dfeRequest = createRequest(url)
        val responseWrapper = newCall(dfeRequest)
        return responseWrapper.payload.deliveryResponse
    }

    override suspend fun wishlist(nextPageUrl: String): ResponseWrapper {
        val libraryId = "u-wl"
        val url = if (nextPageUrl.isEmpty())
                createLibraryUrl(DfeApi.wishlistBackendId, libraryId, 7, null)
            else
                DfeApi.URL_FDFE + "/" + nextPageUrl
        val dfeRequest = createRequest(url)
        return newCall(dfeRequest)
    }

    override suspend fun purchaseHistory(nextPageUrl: String): ResponseWrapper {
        val url = if (nextPageUrl.isEmpty())
            DfeApi.PURCHASE_HISTORY_URL + "?o=0"
        else
            DfeApi.URL_FDFE + "/" + nextPageUrl
        val dfeRequest = createRequest(url)
        return newCall(dfeRequest)
    }

    override suspend fun checkIn(): AndroidCheckinResponse {
        val checkin = checkinRequest(
            timeToReport = System.currentTimeMillis() / 1000,
            deviceInfo = apiContext.deviceInfo
        )
        val dfeRequest = Request.Builder()
            .url(URL_CHECK_IN)
            .apply {
                post(checkin.toByteArray().toRequestBody("application/x-protobuf".toMediaType()))
                apiContext.createAuthHeaders().forEach {
                    addHeader(it.key, it.value)
                }
                addHeader("Host", "android.clients.google.com")
                addHeader("Content-Type", "application/x-protobuffer")
            }.build()
        return newCall(dfeRequest) { AndroidCheckinResponse.parseFrom(it) }
    }

    override suspend fun uploadDeviceConfig(): UploadDeviceConfigResponse {
        val request = UploadDeviceConfigRequest.newBuilder()
            .setDeviceConfiguration(apiContext.deviceInfo.configuration.toProto(apiContext.deviceInfo.build.abis))
            .build()

        val dfeRequest = createRequest(DfeApi.URL_UPLOAD_DEVICE_CONFIG) { builder ->
            builder.post(request.toByteArray().toRequestBody("application/x-protobuf".toMediaType()))
        }
        val response = newCall(dfeRequest)
        return response.payload.uploadDeviceConfigResponse
    }

    private suspend fun newCall(dfeRequest: Request): ResponseWrapper {
        return newCall(dfeRequest) { DfeResponse().parseNetworkResponse(it) }
    }

    private suspend fun <R: MessageLite> newCall(dfeRequest: Request, responseParser: (InputStream) -> R): R = suspendCancellableCoroutine { continuation ->
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
                            DfeResponse().parseNetworkError(body.byteStream())
                            if (response.code == HttpURLConnection.HTTP_NOT_FOUND) {
                                throw DfeServerError("Not Found")
                            } else {
                                throw DfeServerError("Status code ${response.code}")
                            }
                        } else {
                            val parsed = responseParser(body.byteStream())
                            continuation.resume(parsed)
                        }
                    } ?: throw DfeError("Empty body $response")
                } catch (e: Throwable) {
                    continuation.resumeWithException(e)
                }
            }
        })

        continuation.invokeOnCancellation { call.cancel() }
    }

    private fun createRequest(url: String, customizer: ((Request.Builder) -> Unit)? = null): Request {
        return Request.Builder()
                .url(url)
                .apply {
                    if (customizer == null) {
                        get()
                    } else {
                        customizer(this)
                    }
                    apiContext.createDefaultHeaders().forEach { entry ->
                        addHeader(entry.key, entry.value)
                    }
                }
                .build()
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
}
