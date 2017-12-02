package finsky.api

import com.android.volley.Response
import finsky.protos.nano.Messages
import com.google.protobuf.nano.MessageNano

internal open class ProtoDfeRequest(
        s: String,
        private val request: MessageNano,
        dfeApiContext: DfeApiContext,
        listener: Response.Listener<Messages.Response.ResponseWrapper>,
        errorListener: Response.ErrorListener) : DfeRequest(1, s, dfeApiContext, listener, errorListener) {

    init {
        this.setShouldCache(false)
    }

    override fun getBody(): ByteArray {
        return MessageNano.toByteArray(this.request)
    }

    override fun getBodyContentType(): String {
        return "application/x-protobuf"
    }
}