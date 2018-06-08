package finsky.api.model

import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.RequestFuture
import finsky.protos.nano.Messages
import info.anodsplace.framework.AppLog
import java.util.concurrent.ExecutionException

/**
* @author Alex Gavrishev
* @date 17-Feb-18
*/
class DfeSync<out T: DfeRequestModel>(val dfeModel: T): DfeRequestModel() {
    override fun execute(responseListener: Response.Listener<Messages.Response.ResponseWrapper>, errorListener: Response.ErrorListener) {
        dfeModel.execute(responseListener, errorListener)
    }

    override val isReady: Boolean
        get() = dfeModel.isReady

    override fun onResponse(response: Messages.Response.ResponseWrapper?) {
        dfeModel.onResponse(response)
    }

    override fun execute() {
        val future = RequestFuture.newFuture<Messages.Response.ResponseWrapper>()

        execute(future, future)
        val response: Messages.Response.ResponseWrapper?
        try {
            response = future.get()
        } catch (e: ExecutionException) {
            val message = e.cause?.message
            if (message == null) {
                AppLog.e(e)
                throw VolleyError("Response exception: " + e.message, e)
            } else {
                AppLog.e(e.cause!!)
                throw VolleyError("Response exception: " + e.cause!!.message, e.cause!!)
            }
        } catch (e: InterruptedException) {
            AppLog.e(e)
            throw VolleyError("Response exception: " + e.message, e)
        }

        if (response == null) {
            throw VolleyError("Response exception: Response is null")
        }
        if (response.payload == null) {
            throw VolleyError("Response exception: Payload is null")
        }
        onResponse(response)
    }
}