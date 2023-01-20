package info.anodsplace.playstore

import finsky.api.FilterPredicate


/**
 * @author Alex Gavrishev
 * *
 * @date 03/03/2017.
 */

object AppDetailsFilter {
    val hasAppDetails: FilterPredicate = {
        it?.appDetails?.packageName?.isNotEmpty() ?: false
    }
}

class AppNameFilter(private val query: String) {
    val containsQuery: FilterPredicate = {
        it?.title?.contains(query, true) ?: false
    }
}

object PaidHistoryFilter {
    val hasPrice: FilterPredicate = {
        it?.purchaseOffer?.micros?.let { micros -> micros > 0 } ?: false
    }
    val noPurchaseStatus: FilterPredicate = {
        it?.purchaseStatus.isNullOrEmpty()
    }
}