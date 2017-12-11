package com.anod.appwatcher.search

import android.content.Context

import com.anod.appwatcher.model.WatchAppList
import finsky.api.model.Document
import info.anodsplace.playstore.DetailsEndpoint

/**
 * @author algavris
 * *
 * @date 26/08/2016.
 */

class ResultsAdapterDetails(context: Context, private val endpoint: DetailsEndpoint, watchAppList: WatchAppList)
    : ResultsAdapter(context, watchAppList) {

    override fun document(position: Int): Document {
        return endpoint.document!!
    }

    override fun getItemCount(): Int {
        return if (endpoint.document != null) 1 else 0
    }
}
