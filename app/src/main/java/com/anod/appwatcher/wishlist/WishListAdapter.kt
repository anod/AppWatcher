package com.anod.appwatcher.wishlist

import android.content.Context
import com.anod.appwatcher.search.ResultsAdapter
import finsky.api.model.DfeList
import finsky.api.model.Document

/**
 * @author Alex Gavrishev
 * *
 * @date 16/12/2016.
 */
internal class WishListAdapter(context: Context, viewModel: WishListViewModel)
    : ResultsAdapter(context, viewModel) {

    var listData: DfeList? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun document(position: Int): Document {
        val isLastPosition = itemCount - 1 == position
        return listData!!.getItem(position, isLastPosition)!!
    }

    override fun getItemCount() = listData?.count ?: 0
}
