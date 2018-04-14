package com.anod.appwatcher.watchlist

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.content.Context
import android.graphics.*
import android.support.v7.widget.RecyclerView
import android.util.SparseArray
import android.view.View
import com.anod.appwatcher.R
import android.widget.TextView
import android.view.LayoutInflater
import androidx.core.util.containsKey
import android.view.ViewGroup
import androidx.core.graphics.withTranslation

sealed class SectionHeader
class New(val count: Int) : SectionHeader()
class RecentlyUpdated(val count: Int) : SectionHeader()
class Watching(val count: Int) : SectionHeader()
class RecentlyInstalled: SectionHeader()
class OnDevice: SectionHeader()

class HeaderItemDecorator(sections: LiveData<SparseArray<SectionHeader>>, lifecycleOwner: LifecycleOwner, context: Context) : RecyclerView.ItemDecoration() {

    val height = context.resources.getDimension(R.dimen.list_apps_header_height)

    private var headerView: View? = null
    private val title: TextView by lazy { headerView!!.findViewById<TextView>(android.R.id.text1) }
    private val count: TextView by lazy { headerView!!.findViewById<TextView>(android.R.id.text2) }
    private var values = SparseArray<SectionHeader>()

    private val textNew = context.getString(R.string.new_updates)
    private val textRecentlyUpdated = context.getString(R.string.recently_updated)
    private val textWatching = context.getString(R.string.watching)

    init {
        sections.observe(lifecycleOwner, Observer {
            this.values = it ?: SparseArray()
        })
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State?) {
        if (headerView == null) {
            headerView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_apps_header, parent, false)
            measureHeaderView(headerView!!, parent)
        }

        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            val position = parent.getChildAdapterPosition(child)

            if (values.containsKey(position)) {
                val section = values[position]
                when (section) {
                    is New -> {
                        title.text = this.textNew
                        count.text = section.count.toString()
                    }
                    is RecentlyUpdated -> {
                        title.text = this.textRecentlyUpdated
                        count.text = section.count.toString()
                    }
                    is Watching -> {
                        title.text = this.textWatching
                        count.text = section.count.toString()
                    }
                    is RecentlyInstalled -> {
                        title.setText(R.string.recently_installed)
                        count.text = ""
                    }
                    is OnDevice -> {
                        title.setText(R.string.downloaded)
                        count.text = ""
                    }
                }
                c.withTranslation(y = child.y - height) {
                    headerView!!.draw(c)
                }
            }
        }
    }

    override fun getItemOffsets(outRect: Rect, view: View?, parent: RecyclerView, state: RecyclerView.State?) {
        super.getItemOffsets(outRect, view, parent, state)

        val position = parent.getChildAdapterPosition(view)
        if (values.containsKey(position)) {
            outRect.top = this.height.toInt()
        } else {
            outRect.top = 0
        }
    }

    private fun measureHeaderView(view: View, parent: ViewGroup) {
        if (view.layoutParams == null) {
            view.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        val displayMetrics = parent.context.resources.displayMetrics

        val widthSpec = View.MeasureSpec.makeMeasureSpec(displayMetrics.widthPixels, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(displayMetrics.heightPixels, View.MeasureSpec.EXACTLY)

        val childWidth = ViewGroup.getChildMeasureSpec(widthSpec,
                parent.paddingLeft + parent.paddingRight, view.layoutParams.width)
        val childHeight = ViewGroup.getChildMeasureSpec(heightSpec,
                parent.paddingTop + parent.paddingBottom, view.layoutParams.height)

        view.measure(childWidth, childHeight)

        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
    }
}
