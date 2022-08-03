// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.watchlist

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.anod.appwatcher.databinding.ListItemEmptyBinding
import info.anodsplace.framework.view.setOnSafeClickListener
import kotlinx.coroutines.flow.MutableSharedFlow

class EmptyViewHolder(emptyBinding: ListItemEmptyBinding, topMargin: Boolean, action: MutableSharedFlow<WatchListAction>) : RecyclerView.ViewHolder(emptyBinding.root) {
    init {
        emptyBinding.button1.setOnSafeClickListener {
            action.tryEmit(WatchListAction.EmptyButton(1))
        }
        emptyBinding.button2.setOnSafeClickListener {
            action.tryEmit(WatchListAction.EmptyButton(2))
        }
        emptyBinding.button3.setOnSafeClickListener {
            action.tryEmit(WatchListAction.EmptyButton(3))
        }
        emptyBinding.topMargin.isVisible = topMargin
    }
}