package com.anod.appwatcher.watchlist

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.anod.appwatcher.R
import com.anod.appwatcher.utils.SingleLiveEvent

private fun View.requestMeasure(parent: ViewGroup) {
    if (this.layoutParams == null) {
        this.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
    val displayMetrics = parent.context.resources.displayMetrics

    val widthSpec = View.MeasureSpec.makeMeasureSpec(displayMetrics.widthPixels, View.MeasureSpec.EXACTLY)
    val heightSpec = View.MeasureSpec.makeMeasureSpec(displayMetrics.heightPixels, View.MeasureSpec.EXACTLY)

    val childWidth = ViewGroup.getChildMeasureSpec(widthSpec,
            parent.paddingLeft + parent.paddingRight, this.layoutParams.width)
    val childHeight = ViewGroup.getChildMeasureSpec(heightSpec,
            parent.paddingTop + parent.paddingBottom, this.layoutParams.height)

    this.measure(childWidth, childHeight)

    this.layout(0, 0, this.measuredWidth, this.measuredHeight)
}

class SectionHeaderViewHolder(itemView: View, private val action: SingleLiveEvent<WishListAction>) : RecyclerView.ViewHolder(itemView), BindableViewHolder<SectionHeader> {
    val title: TextView by lazy { itemView.findViewById<TextView>(R.id.sectionTitle) }
    val button: Button by lazy { itemView.findViewById<Button>(R.id.sectionButton) }

    override fun bind(item: SectionHeader) {
        button.setOnClickListener(null)
        when (item) {
            is NewHeader -> {
                button.isVisible = false
                title.setText(R.string.new_updates)
            }
            is RecentlyUpdatedHeader -> {
                button.isVisible = false
                title.setText(R.string.recently_updated)
            }
            is WatchingHeader -> {
                button.isVisible = false
                title.setText(R.string.watching)
            }
            is RecentlyInstalledHeader -> {
                button.setOnClickListener {
                    action.value = RecentlyInstalled
                }
                button.isVisible = true
                title.setText(R.string.recently_installed)
            }
            is OnDeviceHeader
            -> {
                button.isVisible = false
                title.setText(R.string.downloaded)
            }
        }
    }

    override fun placeholder() {
        title.text = ""
    }
}
