package com.anod.appwatcher.search

import android.content.Context

import info.anodsplace.playstore.SearchEndpoint
import com.anod.appwatcher.model.WatchAppList
import finsky.api.model.Document

/**
 * @author algavris
 * *
 * @date 26/08/2016.
 */

class ResultsAdapterSearch(context: Context, private val endpoint: SearchEndpoint, watchAppList: WatchAppList)
    : ResultsAdapter(context, watchAppList) {

    override fun document(position: Int): Document {
        val isLastPosition = endpoint.count - 1 == position
        return endpoint.searchData!!.getItem(position, isLastPosition)!!
    }

    override fun getItemCount(): Int {
        return endpoint.count
    }

}
