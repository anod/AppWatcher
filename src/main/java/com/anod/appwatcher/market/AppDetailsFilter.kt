package com.anod.appwatcher.market

import com.google.android.finsky.api.model.Document

/**
 * @author algavris
 * *
 * @date 03/03/2017.
 */

internal object AppDetailsFilter {
    val predicate: (Document?) -> Boolean = {
        it?.appDetails == null
    }
}
