package info.anodsplace.framework.widget.recyclerview

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView

abstract class EndlessOnScrollListener(private val linearLayoutManager: LinearLayoutManager, private val visibleThreshold: Int) : RecyclerView.OnScrollListener() {

    private var lastCall: Int = 0

    override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        val totalItemCount = recyclerView!!.adapter.itemCount
        val firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition()

        if (lastCall != totalItemCount && firstVisibleItem + visibleThreshold > lastCall) {
            lastCall = totalItemCount
            onLoadMore()
        }
    }

    fun reset() {
        lastCall = 0
    }

    /**
     * @return true if there is more data
     */
    abstract fun onLoadMore()
}