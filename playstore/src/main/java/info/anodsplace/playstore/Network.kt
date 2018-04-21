package info.anodsplace.playstore

import com.android.volley.Header
import com.android.volley.Request
import com.android.volley.toolbox.BaseHttpStack
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.ByteArrayPool
import com.android.volley.toolbox.HttpResponse
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

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

        val method = methods[request.method]!!
        val body: okhttp3.RequestBody? = if (method == "POST") {
            val mediaType = okhttp3.MediaType.parse(request.bodyContentType)
            okhttp3.RequestBody.create(mediaType, request.body)
        } else null

        val headers = okhttp3.Headers.of(request.headers + additionalHeaders)

        val okRequest = okhttp3.Request.Builder()
                .url(request.url)
                .method(method, body)
                .headers(headers)
                .build()

        val response = client.newCall(okRequest).execute()

        val responseHeaders = mutableListOf<Header>()
        response.headers().names().forEach {
            val value = response.headers().get(it) ?: ""
            responseHeaders.add(Header(it, value))
        }

        val responseBody = response.body()
        return HttpResponse(response.code(), responseHeaders, responseBody?.contentLength()?.toInt() ?: -1, responseBody?.byteStream())
    }
}

class Network : BasicNetwork(OkHttpStack(
        OkHttpClient.Builder()
                .connectTimeout(2, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()), ByteArrayPool(1024 * 256))
