package finsky.api

import com.android.volley.Response
import com.google.protobuf.MessageLite
import finsky.protos.Messages

internal open class ProtoDfeRequest(
        url: String,
        val request: MessageLite,
        dfeApiContext: DfeApiContext,
        listener: Response.Listener<Messages.Response.ResponseWrapper>,
        errorListener: Response.ErrorListener) : DfeRequest(Method.POST, url, dfeApiContext, listener, errorListener) {

    init {
        this.setShouldCache(false)
    }

    override fun getBody(): ByteArray {
        return request.toByteArray()
    }

    override fun getBodyContentType(): String {
        return "application/x-protobuf"
    }
}