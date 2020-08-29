// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.watchlist

import android.view.View
import androidx.recyclerview.widget.RecyclerView

class EmptyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), BindableViewHolder<Void?> {
    override fun bind(item: Void?) {

    }

    override fun placeholder() {
    }
}
