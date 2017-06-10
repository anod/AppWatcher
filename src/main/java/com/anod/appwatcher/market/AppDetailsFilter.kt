package com.anod.appwatcher.market

import com.anod.appwatcher.utils.CollectionsUtils
import com.google.android.finsky.api.model.Document

/**
 * @author algavris
 * *
 * @date 03/03/2017.
 */

internal object AppDetailsFilter {
    val predicate: CollectionsUtils.Predicate<Document>
        get() {
        return object : CollectionsUtils.Predicate<Document> {
            override fun test(t: Document): Boolean {
                return t.appDetails == null
            }
        }
    }
}
