package finsky.api

import android.net.Uri
import android.text.TextUtils
import com.android.volley.Cache
import com.android.volley.NetworkResponse
import com.android.volley.ParseError
import com.android.volley.Request
import com.android.volley.ServerError
import com.android.volley.VolleyError
import com.android.volley.toolbox.HttpHeaderParser
import com.google.protobuf.InvalidProtocolBufferException
import finsky.protos.Messages.Response
import finsky.utils.Utils
import info.anodsplace.framework.AppLog
import java.io.ByteArrayInputStream
import java.io.IOException
import java.util.zip.GZIPInputStream
import kotlin.math.max

internal open class DfeRequest(
        method: Int,
        url: String,
        private val apiContext: DfeApiContext,
        private val listener: com.android.volley.Response.Listener<Response.ResponseWrapper>,
        errorListener: com.android.volley.Response.ErrorListener)
    : Request<Response.ResponseWrapper>(method, Uri.withAppendedPath(DfeApi.BASE_URI, url).toString(), errorListener) {

    init {
        this.setShouldCache(true)
    }

    constructor(url: String, dfeApiContext: DfeApiContext, listener: com.android.volley.Response.Listener<Response.ResponseWrapper>, errorListener: com.android.volley.Response.ErrorListener)
            : this(Method.GET, url, dfeApiContext, listener, errorListener)

    private fun getSignatureResponse(networkResponse: NetworkResponse): String {
        return networkResponse.headers?.get("X-DFE-Signature-Response") ?: ""
    }

    override fun getHeaders(): MutableMap<String, String> {
        return apiContext.createHeaders()
    }

    private fun handleServerCommands(responseWrapper: Response.ResponseWrapper): com.android.volley.Response<Response.ResponseWrapper>? {
        if (responseWrapper.commands != null) {
            val commands = responseWrapper.commands
            if (!TextUtils.isEmpty(commands.logErrorStacktrace)) {
                AppLog.d(commands.logErrorStacktrace)
            }
            if (!TextUtils.isEmpty(commands.displayErrorMessage)) {
                return com.android.volley.Response.error(DfeServerError(commands.displayErrorMessage))
            }
        }
        return null
    }

    private fun makeCacheKey(s: String): String {
        return StringBuilder(256).append(s).append("/account=").append(this.apiContext.accountName).toString()
    }

    private fun parseWrapperAndVerifySignature(networkResponse: NetworkResponse, gzip: Boolean): Response.ResponseWrapper? {
        try {
            return if (gzip) {
                val bytes = Utils.readBytes(GZIPInputStream(ByteArrayInputStream(networkResponse.data)))
                Response.ResponseWrapper.parseFrom(bytes)
            } else {
                Response.ResponseWrapper.parseFrom(networkResponse.data)
            }
        } catch (ex: InvalidProtocolBufferException) {
            AppLog.e("Cannot parse response as ResponseWrapper proto.", ex)
        } catch (ex: IOException) {
            AppLog.e("IOException while manually unzipping request.", ex)
        }
        return null
    }

    override fun deliverResponse(wrapper: Response.ResponseWrapper) {
        var payload: Response.Payload? = wrapper.payload
        try {
            if (payload!!.searchResponse != null || payload.listResponse != null) {
                if (wrapper.preFetchList.isNotEmpty()) {
                    payload = wrapper.getPreFetch(0).response.payload
                }
            }
        } catch (ex: Exception) {
            AppLog.e("Null wrapper parsed for request=[%s]", this)
            this.deliverError(ParseError(ex))
            return
        }

        if (payload == null) {
            AppLog.e("Null parsed response for request=[%s]", this)
            this.deliverError(VolleyError())
            return
        }
        this.listener.onResponse(wrapper)
    }

    override fun getCacheKey(): String {
        return this.makeCacheKey(super.getUrl())
    }

    override fun parseNetworkError(error: VolleyError): VolleyError {
        if (error is ServerError && error.networkResponse != null) {
            val wrapperAndVerifySignature = this.parseWrapperAndVerifySignature(error.networkResponse, false)
            if (wrapperAndVerifySignature != null) {
                val response = this.handleServerCommands(wrapperAndVerifySignature)
                if (response?.error != null) {
                    return response.error!!
                }
            }
        }
        return error
    }

    public override fun parseNetworkResponse(networkResponse: NetworkResponse): com.android.volley.Response<Response.ResponseWrapper>? {
        val wrapperAndVerifySignature = this.parseWrapperAndVerifySignature(networkResponse, false)
        val response: com.android.volley.Response<Response.ResponseWrapper>?
        if (wrapperAndVerifySignature == null) {
            response = com.android.volley.Response.error<Response.ResponseWrapper>(ParseError(networkResponse))
        } else {
            response = this.handleServerCommands(wrapperAndVerifySignature)
            if (response == null) {
                if (wrapperAndVerifySignature.serverMetadata != null) {
                    val serverMetadata = wrapperAndVerifySignature.serverMetadata
                    AppLog.d("Server metadata $serverMetadata")
                }
                val cacheHeaders = parseCacheHeaders(networkResponse)
                val success = com.android.volley.Response.success(wrapperAndVerifySignature, cacheHeaders)
                AppLog.d("DFE response ${this.url}")
                return success
            }
        }
        return response
    }

    companion object {

        fun parseCacheHeaders(networkResponse: NetworkResponse): Cache.Entry? {
            val cacheHeaders = HttpHeaderParser.parseCacheHeaders(networkResponse) ?: return null
            val currentTimeMillis = System.currentTimeMillis()
            try {
                val s = networkResponse.headers?.get("X-DFE-Soft-TTL")
                if (s != null) {
                    cacheHeaders.softTtl = currentTimeMillis + java.lang.Long.parseLong(s)
                }
                val s2 = networkResponse.headers?.get("X-DFE-Hard-TTL")
                if (s2 != null) {
                    cacheHeaders.ttl = currentTimeMillis + java.lang.Long.parseLong(s2)
                }
                cacheHeaders.ttl = max(cacheHeaders.ttl, cacheHeaders.softTtl)
            } catch (ex: NumberFormatException) {
                AppLog.e("Invalid TTL: ${networkResponse.headers}")
                cacheHeaders.softTtl = 0L
                cacheHeaders.ttl = 0L
            }

            return cacheHeaders
        }
    }
}
