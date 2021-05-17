package com.anod.appwatcher.watchlist

import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.anod.appwatcher.R
import com.anod.appwatcher.utils.EventFlow
import com.anod.appwatcher.utils.collect
import info.anodsplace.framework.view.setOnSafeClickListener
import kotlinx.coroutines.flow.Flow

open class SectionHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), PlaceholderViewHolder {
    val title: TextView = itemView as TextView
    var item: SectionHeader? = null

    init {
        title.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null)
    }

    class Expandable(
            itemView: View,
            lifecycleScope: LifecycleCoroutineScope,
            available: Flow<Boolean>,
            action: EventFlow<WishListAction>) : SectionHeaderViewHolder(itemView) {
        init {
            title.setOnSafeClickListener {
                if (this.item != null) {
                    action.tryEmit(SectionHeaderClick(this.item!!))
                }
            }
            available.collect(lifecycleScope) {
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
