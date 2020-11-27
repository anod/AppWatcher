// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.watchlist

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.anod.appwatcher.databinding.ListItemEmptyBinding
import com.anod.appwatcher.utils.SingleLiveEvent
import info.anodsplace.framework.view.setOnSafeClickListener

class EmptyViewHolder(emptyBinding: ListItemEmptyBinding, topMargin: Boolean, action: SingleLiveEvent<WishListAction>) : RecyclerView.ViewHolder(emptyBinding.root) {
    init {
        emptyBinding.button1.setOnSafeClickListener {
            action.value = EmptyButton(1)
        }
        emptyBinding.button2.setOnSafeClickListener {
            action.value = EmptyButton(2)
        }
        emptyBinding.button3.setOnSafeClickListener {
            action.value = EmptyButton(3)
        }
        emptyBinding.topMargin.isVisible = topMargin
    }
}
