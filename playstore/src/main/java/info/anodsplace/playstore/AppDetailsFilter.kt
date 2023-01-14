package info.anodsplace.playstore

import finsky.api.FilterPredicate


/**
 * @author Alex Gavrishev
 * *
 * @date 03/03/2017.
 */

object AppDetailsFilter {
    val predicate: FilterPredicate = {
        it?.appDetails?.packageName?.isNotEmpty() ?: false
    }
}

class AppNameFilter(private val query: String) {
    val predicate: FilterPredicate = {
        it?.title?.contains(query, true) ?: false
    }
}
