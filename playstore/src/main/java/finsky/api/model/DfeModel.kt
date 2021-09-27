package finsky.api.model

import finsky.protos.ResponseWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
    abstract fun onResponse(responseWrapper: ResponseWrapper)

    open suspend fun execute() = withContext(Dispatchers.Main) {
        val responseWrapper = makeRequest()
        onResponse(responseWrapper)
    }

    abstract suspend fun makeRequest(): ResponseWrapper
}
