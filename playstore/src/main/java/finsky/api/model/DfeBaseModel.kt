package finsky.api.model

import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.RequestFuture
import finsky.protos.nano.Messages

import java.util.concurrent.ExecutionException

import info.anodsplace.android.log.AppLog

/**
 * @author alex
 * @date 2015-02-23
 */
abstract class DfeBaseModel : DfeModel(), Response.Listener<Messages.Response.ResponseWrapper> {

    fun startAsync() {
        execute(this, this)
    }

    fun startSync() {
        val future = RequestFuture.newFuture<Messages.Response.ResponseWrapper>()

        execute(future, future)
        val response: Messages.Response.ResponseWrapper?
        try {
            response = future.get()
        } catch (e: ExecutionException) {
            val cause = e.cause
            if (cause == null) {
                AppLog.e(e)
                onErrorResponse(VolleyError("Response exception: " + e.message, e))
            } else {
                AppLog.e(cause)
                onErrorResponse(VolleyError("Response exception: " + cause.message, cause))
            }
            return
        } catch (e: InterruptedException) {
            AppLog.e(e)
            onErrorResponse(VolleyError("Response exception: " + e.message, e))
            return
        }

        if (response == null) {
            onErrorResponse(VolleyError("Response exception: Response is null"))
            return
        }
        if (response.payload == null) {
            onErrorResponse(VolleyError("Response exception: Payload is null"))
            return
        }
        onResponse(response)
    }

    protected abstract fun execute(responseListener: Response.Listener<Messages.Response.ResponseWrapper>, errorListener: Response.ErrorListener)

}
