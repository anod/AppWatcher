package com.anod.appwatcher.watchlist

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.anod.appwatcher.R


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

class SectionHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), BindableViewHolder<SectionHeader> {
    val title: TextView by lazy { itemView.findViewById<TextView>(android.R.id.text1) }
    val count: TextView by lazy { itemView.findViewById<TextView>(android.R.id.text2) }

    override fun bind(item: SectionHeader) {
        when (item) {
            is New -> {
                title.setText(R.string.new_updates)
                count.text = item.count.toString()
            }
            is RecentlyUpdated -> {
                title.setText(R.string.recently_updated)
                count.text = item.count.toString()
            }
            is Watching -> {
                title.setText(R.string.watching)
                count.text = item.count.toString()
            }
            is RecentlyInstalled -> {
                title.setText(R.string.recently_installed)
                count.text = ""
            }
            is OnDevice -> {
                title.setText(R.string.downloaded)
            }
        }
    }

    override fun placeholder() {
        title.text = ""
        count.text = ""
    }
}
