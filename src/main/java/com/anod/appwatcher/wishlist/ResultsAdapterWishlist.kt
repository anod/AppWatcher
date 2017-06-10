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

internal class ResultsAdapterWishlist(context: Context, private val mEngine: WishlistEndpoint, newAppHandler: WatchAppList) : ResultsAdapter(context, newAppHandler) {

    override fun getDocument(position: Int): Document {
        val isLastPosition = mEngine.count - 1 == position
        return mEngine.listData!!.getItem(position, isLastPosition)
    }

    override fun getItemCount(): Int {
        return mEngine.count
    }

}
