package com.anod.appwatcher.wishlist

import android.content.Context

import com.anod.appwatcher.market.WishlistEndpoint
import com.anod.appwatcher.model.WatchAppList
import com.anod.appwatcher.search.ResultsAdapter
import com.google.android.finsky.api.model.Document

/**
 * @author algavris
 * *
 * @date 16/12/2016.
 */

internal class ResultsAdapterWishList(context: Context, private val endpoint: WishlistEndpoint, newAppHandler: WatchAppList) : ResultsAdapter(context, newAppHandler) {

    override fun getDocument(position: Int): Document {
        val isLastPosition = endpoint.count - 1 == position
        return endpoint.listData!!.getItem(position, isLastPosition)!!
    }

    override fun getItemCount(): Int {
        return endpoint.count
    }

}
