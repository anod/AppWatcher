package com.anod.appwatcher.watchlist

import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.anod.appwatcher.R
import com.anod.appwatcher.utils.EventFlow
import com.anod.appwatcher.utils.collect
import info.anodsplace.framework.view.setOnSafeClickListener
import kotlinx.coroutines.flow.Flow

open class SectionHeaderViewHolder(itemView: View, titleColorOverride: Int?) : RecyclerView.ViewHolder(itemView), PlaceholderViewHolder {
    val title: TextView = itemView as TextView
    var item: SectionHeader? = null

    init {
        title.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null)
        if (titleColorOverride != null) {
            title.setTextColor(titleColorOverride)
        }
    }

    class Expandable(
            itemView: View,
            titleColorOverride: Int?,
            lifecycleOwner: LifecycleOwner,
            available: Flow<Boolean>,
            action: EventFlow<WishListAction>
    ) : SectionHeaderViewHolder(itemView, titleColorOverride) {
        init {
            title.setOnSafeClickListener {
                if (this.item != null) {
                    action.tryEmit(SectionHeaderClick(this.item!!))
                }
            }
            available
                    .flowWithLifecycle(lifecycleOwner.lifecycle, minActiveState = Lifecycle.State.STARTED)
                    .collect(lifecycleOwner.lifecycleScope) {
                        if (it) {
                            val drawable = ContextCompat.getDrawable(title.context, R.drawable.ic_baseline_arrow_forward_ios_24)
                            title.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, drawable, null)
                        } else {
                            title.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null)
                        }
                    }
        }
    }

    fun bind(item: SectionHeader) {
        this.item = item
        when (item) {
            is NewHeader -> {
                title.setText(R.string.new_updates)
            }
            is RecentlyUpdatedHeader -> {
                title.setText(R.string.recently_updated)
            }
            is WatchingHeader -> {
                title.setText(R.string.watching)
            }
            is RecentlyInstalledHeader -> {
                title.setText(R.string.recently_installed)
            }
            is OnDeviceHeader -> {
                title.setText(R.string.downloaded)
            }
        }
    }

    override fun placeholder() {
        title.text = ""
    }
}