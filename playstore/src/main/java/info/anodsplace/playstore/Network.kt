package info.anodsplace.playstore

import com.android.volley.Header
import com.android.volley.Request
import com.android.volley.toolbox.BaseHttpStack
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.ByteArrayPool
import com.android.volley.toolbox.HttpResponse
import okhttp3.Headers.Companion.toHeaders
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * @author alex
 * *
 * @date 2015-02-23
 */


private class OkHttpStack @JvmOverloads constructor(client: OkHttpClient? = OkHttpClient()) : BaseHttpStack() {

    private val methods = mapOf(
            0 to "GET",
            1 to "POST"
    )

    private val client: OkHttpClient

    init {
        if (client == null) {
            throw NullPointerException("Client must not be null.")
        }
        this.client = client
    }

    override fun executeRequest(request: Request<*>, additionalHeaders: MutableMap<String, String>): HttpResponse {

        val method = methods[request.method] ?: "GET"
        val body: okhttp3.RequestBody? = if (method == "POST") {
            val mediaType = request.bodyContentType.toMediaTypeOrNull()
            request.body.toRequestBody(mediaType, 0, request.body.size)
        } else null

        val headers = (request.headers + additionalHeaders).toHeaders()

        val okRequest = okhttp3.Request.Builder()
                .url(request.url)
                .method(method, body)
                .headers(headers)
                .build()

        val response = client.newCall(okRequest).execute()

        val responseHeaders = mutableListOf<Header>()
        response.headers.names().forEach {
            val value = response.headers[it] ?: ""
            responseHeaders.add(Header(it, value))
        }

        val responseBody = response.body
        return HttpResponse(response.code, responseHeaders, responseBody?.contentLength()?.toInt() ?: -1, responseBody?.byteStream())
    }
}

class Network : BasicNetwork(OkHttpStack(), ByteArrayPool(1024 * 256))
