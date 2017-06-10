package com.anod.appwatcher.search

import android.content.Context

import com.anod.appwatcher.market.SearchEndpoint
import com.anod.appwatcher.model.WatchAppList
import com.google.android.finsky.api.model.Document

/**
 * @author algavris
 * *
 * @date 26/08/2016.
 */

class ResultsAdapterSearch(context: Context, private val mSearchEngine: SearchEndpoint, newAppHandler: WatchAppList)
    : ResultsAdapter(context, newAppHandler) {

    override fun getDocument(position: Int): Document {
        val isLastPosition = mSearchEngine.count - 1 == position
        return mSearchEngine.searchData!!.getItem(position, isLastPosition)
    }

    override fun getItemCount(): Int {
        return mSearchEngine.count
    }

}
