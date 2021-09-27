package finsky.api

import android.text.TextUtils
import finsky.protos.Payload
import finsky.protos.ResponseWrapper
import finsky.utils.Utils
import info.anodsplace.applog.AppLog
import java.io.InputStream
import java.util.zip.GZIPInputStream

class DfeResponse {

    fun parseNetworkError(networkResponse: InputStream) {
        val wrapperAndVerifySignature = this.parseWrapperAndVerifySignature(networkResponse, false)
        if (wrapperAndVerifySignature != null) {
            this.handleServerCommands(wrapperAndVerifySignature)
        }
    }

    fun parseNetworkResponse(networkResponse: InputStream): ResponseWrapper {
        val wrapperAndVerifySignature = this.parseWrapperAndVerifySignature(networkResponse, false)
        if (wrapperAndVerifySignature == null) {
            throw DfeParseError("Parse ResponseWrapper returned null")
        } else {
            this.handleServerCommands(wrapperAndVerifySignature)
            if (wrapperAndVerifySignature.serverMetadata != null) {
                val serverMetadata = wrapperAndVerifySignature.serverMetadata
                AppLog.d("Server metadata latency ${serverMetadata.latencyMillis}")
            }
            return verifyPayload(wrapperAndVerifySignature)
        }
    }

    private fun verifyPayload(wrapper: ResponseWrapper): ResponseWrapper {
        var payload: Payload? = wrapper.payload
        try {
            if (payload!!.searchResponse != null || payload.listResponse != null) {
                if (wrapper.preFetchList.isNotEmpty()) {
                    payload = wrapper.getPreFetch(0).response.payload
                }
            }
        } catch (ex: Exception) {
            AppLog.e("Null wrapper parsed", ex)
            throw DfeParseError("Parse error", ex)
        }

        if (payload == null) {
            AppLog.e("Null parsed response")
            throw DfeParseError("No payload")
        }
        return wrapper
    }

    private fun parseWrapperAndVerifySignature(networkResponse: InputStream, gzip: Boolean): ResponseWrapper? {
        return if (gzip) {
            val bytes = Utils.readBytes(GZIPInputStream(networkResponse))
            ResponseWrapper.parseFrom(bytes)
        } else {
            ResponseWrapper.parseFrom(networkResponse)
        }
    }

    private fun handleServerCommands(responseWrapper: ResponseWrapper) {
        if (responseWrapper.commands != null) {
            val commands = responseWrapper.commands
            if (!TextUtils.isEmpty(commands.logErrorStacktrace)) {
                AppLog.d(commands.logErrorStacktrace)
            }
            if (!TextUtils.isEmpty(commands.displayErrorMessage)) {
                throw DfeServerError(commands.displayErrorMessage)
            }
        }
    }
}