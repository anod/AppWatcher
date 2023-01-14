package finsky.api

import finsky.api.Document

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
