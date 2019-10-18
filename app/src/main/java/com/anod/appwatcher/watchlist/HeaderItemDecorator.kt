package com.anod.appwatcher.watchlist

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.graphics.withTranslation
import androidx.core.util.containsKey
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.anod.appwatcher.R

sealed class SectionHeader
class New(val count: Int, val updatable: Int) : SectionHeader()
class RecentlyUpdated(val count: Int) : SectionHeader()
class Watching(val count: Int) : SectionHeader()
object RecentlyInstalled : SectionHeader()
object OnDevice : SectionHeader()

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

interface SectionHeaderView {
    val height: Int
    val view: View
    val title: TextView
    val count: TextView
}

class CountHeaderView(parent: ViewGroup) : SectionHeaderView {
    override val height = parent.context.resources.getDimension(R.dimen.list_apps_header_height).toInt()
    override val view: View = LayoutInflater.from(parent.context).inflate(R.layout.list_apps_header, parent, false)
    override val title: TextView by lazy { view.findViewById<TextView>(android.R.id.text1) }
    override val count: TextView by lazy { view.findViewById<TextView>(android.R.id.text2) }

    init {
        view.requestMeasure(parent)
    }
}

class HeaderItemDecorator(sections: LiveData<SparseArray<SectionHeader>>, lifecycleOwner: LifecycleOwner, context: Context) : RecyclerView.ItemDecoration() {

    private var values = SparseArray<SectionHeader>()

    private val textNew = context.getString(R.string.new_updates)
    private val textRecentlyUpdated = context.getString(R.string.recently_updated)
    private val textWatching = context.getString(R.string.watching)

    private var header: CountHeaderView? = null

    init {
        sections.observe(lifecycleOwner, Observer {
            this.values = it ?: SparseArray()
        })
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {

        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            val position = parent.getChildAdapterPosition(child)
            if (values.containsKey(position)) {
                val section = values[position]
                val header = header(parent)
                when (section) {
                    is New -> {
                        header.title.text = this.textNew
                        header.count.text = section.count.toString()
                    }
                    is RecentlyUpdated -> {
                        header.title.text = this.textRecentlyUpdated
                        header.count.text = section.count.toString()
                    }
                    is Watching -> {
                        header.title.text = this.textWatching
                        header.count.text = section.count.toString()
                    }
                    is RecentlyInstalled -> {
                        header.title.setText(R.string.recently_installed)
                        header.count.text = ""
                    }
                    is OnDevice -> {
                        header.title.setText(R.string.downloaded)
                    }
                }
                c.withTranslation(y = child.y - header.height) {
                    header.view.draw(c)
                }
            }
        }
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)

        val position = parent.getChildAdapterPosition(view)
        if (values.containsKey(position)) {
            val header = header(parent)
            outRect.top = header.height
        } else {
            outRect.top = 0
        }
    }

    private fun header(parent: RecyclerView): SectionHeaderView {
        if (this.header == null) {
            this.header = CountHeaderView(parent)
        }
        return this.header!!
    }
}
