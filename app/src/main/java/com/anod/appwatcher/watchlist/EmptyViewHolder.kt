// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.watchlist

import android.view.View
import android.widget.Button
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.anod.appwatcher.R
import com.anod.appwatcher.utils.SingleLiveEvent
import info.anodsplace.framework.view.setOnSafeClickListener


class EmptyViewHolder(itemView: View, topMargin: Boolean, action: SingleLiveEvent<WishListAction>) : RecyclerView.ViewHolder(itemView), PlaceholderViewHolder {

    init {
        itemView.findViewById<Button>(R.id.button1).setOnSafeClickListener {
            action.value = EmptyButton(1)
        }
        itemView.findViewById<Button>(R.id.button2).setOnSafeClickListener {
            action.value = EmptyButton(2)
        }
        itemView.findViewById<Button>(R.id.button3).setOnSafeClickListener {
            action.value = EmptyButton(3)
        }
        itemView.findViewById<View>(R.id.topMargin).isVisible = topMargin
    }

    override fun placeholder() {
    }

    fun bind() {

    }
}
