package com.anod.appwatcher.wishlist

import android.content.Context

import com.anod.appwatcher.content.WatchAppList
import com.anod.appwatcher.search.ResultsAdapter
import finsky.api.model.Document
import info.anodsplace.playstore.WishlistEndpoint

/**
 * @author Alex Gavrishev
 * *
 * @date 16/12/2016.
 */

internal class ResultsAdapterWishList(context: Context, private val endpoint: WishlistEndpoint, viewModel: WishlistViewModel) : ResultsAdapter(context, viewModel) {

    override fun document(position: Int): Document {
        val isLastPosition = endpoint.count - 1 == position
        return endpoint.listData!!.getItem(position, isLastPosition)!!
    }

    override fun getItemCount(): Int {
        return endpoint.count
    }

}
