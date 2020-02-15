package finsky.api.model

import com.android.volley.Request
import com.android.volley.Response
import finsky.protos.nano.Messages
import info.anodsplace.framework.AppLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

typealias FilterPredicate = ((Document?) -> Boolean)

class FilterComposite(private val predicates: List<FilterPredicate>) {
    val predicate: FilterPredicate = ret@{ doc ->
        for (p in predicates) {
            if (!p(doc)) {
                return@ret false
            }
        }
        return@ret true
    }
}

abstract class DfeModel {
    abstract val isReady: Boolean
    abstract val url: String
    abstract fun onResponse(responseWrapper: Messages.Response.ResponseWrapper)

    open suspend fun execute() = withContext(Dispatchers.Main) {
        val responseWrapper = makeRequest(url)
        onResponse(responseWrapper)
    }

    private suspend fun makeRequest(url: String): Messages.Response.ResponseWrapper = suspendCancellableCoroutine { continuation ->
        val request = makeRequest(
                url,
                Response.Listener {
                    continuation.resume(it)
                },
                Response.ErrorListener { error ->
                    AppLog.e("ErrorResponse: " + error.message, error)
                    continuation.resumeWithException(error)
                }
        )
        continuation.invokeOnCancellation { request.cancel() }
    }

    abstract fun makeRequest(url: String, responseListener: Response.Listener<Messages.Response.ResponseWrapper>, errorListener: Response.ErrorListener): Request<*>
}
