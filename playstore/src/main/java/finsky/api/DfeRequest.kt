package finsky.api

import android.net.Uri
import android.text.TextUtils
import android.util.Log

import com.android.volley.AuthFailureError
import com.android.volley.Cache
import com.android.volley.NetworkResponse
import com.android.volley.ParseError
import com.android.volley.Request
import com.android.volley.ServerError
import com.android.volley.VolleyError
import com.android.volley.toolbox.HttpHeaderParser
import finsky.protos.nano.Messages.Response
import finsky.utils.Utils
import com.google.protobuf.nano.InvalidProtocolBufferNanoException
import com.google.protobuf.nano.MessageNanoPrinter

import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.util.zip.GZIPInputStream

import info.anodsplace.android.log.AppLog

internal open class DfeRequest(
        method: Int,
        url: String,
        private val apiContext: DfeApiContext,
        private val listener: com.android.volley.Response.Listener<Response.ResponseWrapper>,
        errorListener: com.android.volley.Response.ErrorListener) : Request<Response.ResponseWrapper>(method, Uri.withAppendedPath(DfeApi.BASE_URI, url).toString(), errorListener) {
    private var allowMultipleResponses: Boolean = false
    var avoidBulkCancel: Boolean = false
        private set
    private var extraHeaders: MutableMap<String, String> = mutableMapOf()
    private var responseDelivered: Boolean = false
    private var responseVerifier: DfeResponseVerifier? = null
    private var mServerLatencyMs: Long = 0

    init {
        this.allowMultipleResponses = false
        this.mServerLatencyMs = -1L
        this.avoidBulkCancel = false
        if (TextUtils.isEmpty(url)) {
            AppLog.e("Empty DFE URL")
        }
        this.setShouldCache(!SKIP_ALL_CACHES)
        this.retryPolicy = DfeRetryPolicy(apiContext)
    }

    constructor(url: String, dfeApiContext: DfeApiContext, listener: com.android.volley.Response.Listener<Response.ResponseWrapper>, errorListener: com.android.volley.Response.ErrorListener)
            : this(Request.Method.GET, url, dfeApiContext, listener, errorListener) {}

    private fun getSignatureResponse(networkResponse: NetworkResponse): String {
        return networkResponse.headers["X-DFE-Signature-Response"] ?: ""
    }

    private fun handleServerCommands(responseWrapper: Response.ResponseWrapper): com.android.volley.Response<Response.ResponseWrapper>? {
        if (responseWrapper.commands != null) {
            val commands = responseWrapper.commands
            if (!TextUtils.isEmpty(commands.logErrorStacktrace)) {
                AppLog.d("%s", commands.logErrorStacktrace)
            }
            if (commands.clearCache) {
                //  this.apiContext.getCache().clear();
            }
            if (!TextUtils.isEmpty(commands.displayErrorMessage)) {
                return com.android.volley.Response.error(DfeServerError(commands.displayErrorMessage))
            }
        }
        return null
    }

    private fun logProtoResponse(responseWrapper: Response.ResponseWrapper) {
        val s = ".*"
        if (this.url.matches(s.toRegex())) {
            synchronized(MessageNanoPrinter::class.java) {
                Log.v("DfeProto", "{ response: \"" + this.url + "\".\n")
                val split = MessageNanoPrinter.print<Response.ResponseWrapper>(responseWrapper).split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val length = split.size
                var i = 0
                while (i < length) {
                    Log.v("DfeProto", split[i])
                    ++i
                }
                Log.v("DfeProto", "}")
                return
            }
        }
        Log.v("DfeProto", "Url does not match regexp: url=" + this.url + " / regexp=" + s)
    }

    private fun makeCacheKey(s: String): String {
        return StringBuilder(256).append(s).append("/account=").append(this.apiContext.accountName).toString()
    }

    @Throws(InvalidProtocolBufferNanoException::class, DfeResponseVerifier.DfeResponseVerifierException::class)
    private fun parseWrapperAndVerifyFromBytes(networkResponse: NetworkResponse, s: String): Response.ResponseWrapper {
        val from = Response.ResponseWrapper.parseFrom(networkResponse.data)
        if (this.responseVerifier != null) {
            this.responseVerifier!!.verify(networkResponse.data, s)
            this.addMarker("signature-verification-succeeded")
        }
        return from
    }

    private fun parseWrapperAndVerifySignature(networkResponse: NetworkResponse, gzip: Boolean): Response.ResponseWrapper? {
        try {
            val signatureResponse = this.getSignatureResponse(networkResponse)
            return if (gzip) {
                this.parseWrapperAndVerifySignatureFromIs(GZIPInputStream(ByteArrayInputStream(networkResponse.data)), signatureResponse)
            } else this.parseWrapperAndVerifyFromBytes(networkResponse, signatureResponse)
        } catch (ex: InvalidProtocolBufferNanoException) {
            //            if (!gzip) {
            //                return this.parseWrapperAndVerifySignature(networkResponse, true);
            //            }
            AppLog.d("Cannot parse response as ResponseWrapper proto.")
        } catch (ex: IOException) {
            AppLog.w("IOException while manually unzipping request.")
        } catch (ex: DfeResponseVerifier.DfeResponseVerifierException) {
            this.addMarker("signature-verification-failed")
            AppLog.e("Could not verify request: %s, exception %s", this, ex)

        }
        return null
    }

    @Throws(IOException::class, DfeResponseVerifier.DfeResponseVerifierException::class)
    private fun parseWrapperAndVerifySignatureFromIs(inputStream: InputStream, s: String): Response.ResponseWrapper {
        val bytes = Utils.readBytes(inputStream)
        val from = Response.ResponseWrapper.parseFrom(bytes)
        if (this.responseVerifier != null) {
            this.responseVerifier!!.verify(bytes, s)
        }
        return from
    }

    fun addExtraHeader(key: String, value: String) {
        this.extraHeaders.put(key, value)
    }

    override fun deliverError(volleyError: VolleyError) {
        if (!this.responseDelivered) {
            super.deliverError(volleyError)
            return
        }
        AppLog.d("Not delivering error response for request=[%s], error=[%s] because response already delivered.", this, volleyError)
    }

    override fun deliverResponse(wrapper: Response.ResponseWrapper) {
        var payload: Response.Payload? = wrapper.payload
        try {
            if (payload!!.searchResponse != null || payload.listResponse != null) {
                if (wrapper.preFetch.isNotEmpty()) {
                    payload = wrapper.preFetch[0].response.payload
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
        if (this.allowMultipleResponses || !this.responseDelivered) {
            this.listener.onResponse(wrapper)
            this.responseDelivered = true
        } else {
            AppLog.d("Not delivering second response for request=[%s]", this)
        }
    }

    override fun getCacheKey(): String {
        return this.makeCacheKey(super.getUrl())
    }

    @Throws(AuthFailureError::class)
    override fun getHeaders(): Map<String, String> {
        val headers = this.apiContext.createHeaders()
        headers.putAll(this.extraHeaders)
        if (this.responseVerifier == null) {
            return headers
        }
        try {
            headers.put("X-DFE-Signature-Request", this.responseVerifier!!.signatureRequest)
            val retryPolicy = this.retryPolicy
            var s = "timeoutMs=" + retryPolicy.currentTimeout
            val currentRetryCount = retryPolicy.currentRetryCount
            if (currentRetryCount > 0) {
                s = s + "; retryAttempt=" + currentRetryCount
            }
            headers.put("X-DFE-Request-Params", s)
        } catch (ex: DfeResponseVerifier.DfeResponseVerifierException) {
            AppLog.d("Couldn't create signature request: %s", ex)
            this.cancel()
        }

        return headers
    }

//    fun handleNotifications(responseWrapper: Response.ResponseWrapper) {
//        //        if (this.apiContext.getNotificationManager() != null && responseWrapper.notification.length != 0) {
//        //            final Notifications.Notification[] notification = responseWrapper.notification;
//        //            for (int length = notification.length, i = 0; i < length; ++i) {
//        //                this.apiContext.getNotificationManager().processNotification(notification[i]);
//        //            }
//        //        }
//    }

    override fun parseNetworkError(error: VolleyError): VolleyError {
        if (error is ServerError && error.networkResponse != null) {
            val wrapperAndVerifySignature = this.parseWrapperAndVerifySignature(error.networkResponse, false)
            if (wrapperAndVerifySignature != null) {
                val response = this.handleServerCommands(wrapperAndVerifySignature)
                if (response != null) {
                    return response.error
                }
            }
        }
        return error
    }

    public override fun parseNetworkResponse(networkResponse: NetworkResponse): com.android.volley.Response<Response.ResponseWrapper>? {
        if (AppLog.LOG_DEBUG) {
            val headers = networkResponse.headers
            var n = 0
            if (headers != null) {
                val containsKey = networkResponse.headers.containsKey("X-DFE-Content-Length")
                n = 0
                if (containsKey) {
                    n = Integer.parseInt(networkResponse.headers["X-DFE-Content-Length"]) / 1024
                }
            }
            AppLog.v("Parsed response for url=[%s] contentLength=[%d KB]", this.url, n)
        }
        val wrapperAndVerifySignature = this.parseWrapperAndVerifySignature(networkResponse, false)
        val response: com.android.volley.Response<Response.ResponseWrapper>?
        if (wrapperAndVerifySignature == null) {
            response = com.android.volley.Response.error<Response.ResponseWrapper>(ParseError(networkResponse))
        } else {
            if (DfeRequest.PROTO_DEBUG) {
                this.logProtoResponse(wrapperAndVerifySignature)
            }
            response = this.handleServerCommands(wrapperAndVerifySignature)
            if (response == null) {
                if (wrapperAndVerifySignature.serverMetadata != null) {
                    val serverMetadata = wrapperAndVerifySignature.serverMetadata
                    if (serverMetadata.latencyMillis > 0) {
                        this.mServerLatencyMs = serverMetadata.latencyMillis
                    }
                }
                //this.handleNotifications(wrapperAndVerifySignature)
                val cacheHeaders: Cache.Entry?
                if (this.responseVerifier != null) {
                    cacheHeaders = null
                } else {
                    cacheHeaders = parseCacheHeaders(networkResponse)
                }
                if (cacheHeaders != null) {
                    // this.stripForCache(wrapperAndVerifySignature, cacheHeaders)
                }
                val success = com.android.volley.Response.success(wrapperAndVerifySignature, cacheHeaders)
                AppLog.d("DFE response %s", this.url)
                return success
            }
        }
        return response
    }

    fun setAllowMultipleResponses(mAllowMultipleResponses: Boolean) {
        this.allowMultipleResponses = mAllowMultipleResponses
    }

    fun setAvoidBulkCancel() {
        this.avoidBulkCancel = true
    }

    fun setRequireAuthenticatedResponse(mResponseVerifier: DfeResponseVerifier) {
        this.responseVerifier = mResponseVerifier
    }

//    fun stripForCache(responseWrapper: Response.ResponseWrapper, entry: Cache.Entry) {
//        //        if (responseWrapper.preFetch.length < 1 && responseWrapper.commands == null && responseWrapper.notification.length < 1) {
//        //            return;
//        //        }
//        //        //final Cache cache = this.apiContext.getCache();
//        //        final long currentTimeMillis = System.currentTimeMillis();
//        //        for (final ResponseMessages.PreFetch preFetch2 : responseWrapper.preFetch) {
//        //            final Cache.Entry entry2 = new Cache.Entry();
//        //            entry2.data = preFetch2.response;
//        //            entry2.etag = preFetch2.etag;
//        //            entry2.serverDate = entry.serverDate;
//        //            entry2.ttl = currentTimeMillis + preFetch2.ttl;
//        //            entry2.softTtl = currentTimeMillis + preFetch2.softTtl;
//        //            //cache.put(this.makeCacheKey(Uri.withAppendedPath(DfeApi.BASE_URI, preFetch2.url).toString()), entry2);
//        //        }
//        //        responseWrapper.preFetch = ResponseMessages.PreFetch.emptyArray();
//        //        responseWrapper.commands = null;
//        //        //responseWrapper.notification = Notifications.Notification.emptyArray();
//        //        entry.data = MessageNano.toByteArray(responseWrapper);
//    }

    companion object {
        private val SKIP_ALL_CACHES = false
        private val PROTO_DEBUG: Boolean = Log.isLoggable("AppWatcher.DfeProto", Log.VERBOSE)

        fun parseCacheHeaders(networkResponse: NetworkResponse): Cache.Entry? {
            val cacheHeaders = HttpHeaderParser.parseCacheHeaders(networkResponse) ?: return null
            val currentTimeMillis = System.currentTimeMillis()
            try {
                val s = networkResponse.headers["X-DFE-Soft-TTL"]
                if (s != null) {
                    cacheHeaders.softTtl = currentTimeMillis + java.lang.Long.parseLong(s)
                }
                val s2 = networkResponse.headers["X-DFE-Hard-TTL"]
                if (s2 != null) {
                    cacheHeaders.ttl = currentTimeMillis + java.lang.Long.parseLong(s2)
                }
                cacheHeaders.ttl = Math.max(cacheHeaders.ttl, cacheHeaders.softTtl)
            } catch (ex: NumberFormatException) {
                AppLog.d("Invalid TTL: %s", networkResponse.headers)
                cacheHeaders.softTtl = 0L
                cacheHeaders.ttl = 0L
            }

            return cacheHeaders
        }
    }
}
