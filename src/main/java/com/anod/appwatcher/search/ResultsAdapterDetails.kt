package com.anod.appwatcher.search

import android.content.Context

import com.anod.appwatcher.market.DetailsEndpoint
import com.anod.appwatcher.model.WatchAppList
import com.google.android.finsky.api.model.Document

/**
 * @author algavris
 * *
 * @date 26/08/2016.
 */

class ResultsAdapterDetails(context: Context, private val mDetailsEndpoint: DetailsEndpoint, newAppHandler: WatchAppList)
    : ResultsAdapter(context, newAppHandler) {

    override fun getDocument(position: Int): Document {
        return mDetailsEndpoint.document!!
    }

    override fun getItemCount(): Int {
        return if (mDetailsEndpoint.document != null) 1 else 0
    }
}
