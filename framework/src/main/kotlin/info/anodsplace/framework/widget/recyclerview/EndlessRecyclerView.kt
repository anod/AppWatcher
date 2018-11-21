package info.anodsplace.framework.widget.recyclerview

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.AttributeSet

import info.anodsplace.framework.R

/**
 * @author alex
 * @date 2015-06-22
 */
class EndlessRecyclerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : RecyclerView(context, attrs, defStyle) {

    private var scrollListener: EndlessOnScrollListener? = null
    var listener: OnLoadMoreListener? = null
    var hasMoreData: Boolean = false
        set(value) {
            scrollListener?.reset()
            if (field == value) {
                return
            }
            field = value
            if (value) {
                if (scrollListener != null) {
                    addOnScrollListener(scrollListener!!)
                }
                (adapter as EndlessAdapter).setKeepOnAppending(true)
            } else {
                if (scrollListener != null) {
                    removeOnScrollListener(scrollListener!!)
                }
                (adapter as EndlessAdapter).setKeepOnAppending(false)
            }
        }

    fun init(layout: LinearLayoutManager, adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>, scrollLimit: Int) {
        super.setLayoutManager(layout)

        val endlessAdapter = EndlessAdapter(adapter, context, R.layout.list_item_loadmore)
        super.setAdapter(endlessAdapter)

        scrollListener = object : EndlessOnScrollListener(layout, scrollLimit) {
            override fun onLoadMore() {
                listener?.onLoadMore()
            }
        }
    }

    interface OnLoadMoreListener {
        fun onLoadMore()
    }
}
