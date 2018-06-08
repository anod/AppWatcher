package info.anodsplace.playstore

import finsky.api.model.FilterPredicate

/**
 * @author Alex Gavrishev
 * *
 * @date 03/03/2017.
 */

internal object AppDetailsFilter {
    val predicate: FilterPredicate = {
        it?.appDetails?.packageName?.isNotEmpty() ?: false
    }
}
