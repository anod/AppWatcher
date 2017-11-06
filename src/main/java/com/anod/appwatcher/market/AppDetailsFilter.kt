package com.anod.appwatcher.market

import com.google.android.finsky.api.model.FilterPredicate

/**
 * @author algavris
 * *
 * @date 03/03/2017.
 */

internal object AppDetailsFilter {
    val predicate: FilterPredicate = {
        it?.appDetails != null
    }
}
