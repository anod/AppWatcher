// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.watchlist

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anod.appwatcher.R
import com.anod.appwatcher.utils.SingleLiveEvent

class EmptyAdapter(
        private val itemViewType: Int,
        private val action: SingleLiveEvent<WishListAction>,
        private val configure: (emptyView: View, action: SingleLiveEvent<WishListAction>) -> Unit,
        private val context: Context
) : RecyclerView.Adapter<EmptyAdapter.EmptyViewHolder>() {
    var isVisible = false

    class EmptyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmptyViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.list_item_empty, parent, false)
        configure(itemView, action)
        return EmptyViewHolder(itemView)
    }

    override fun getItemCount() = if (isVisible) 1 else 0

    override fun getItemViewType(position: Int) = itemViewType

    override fun onBindViewHolder(holder: EmptyViewHolder, position: Int) {

    }

}