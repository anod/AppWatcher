package info.anodsplace.framework.widget.recyclerview

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup

open class AdapterWrapper<VH : RecyclerView.ViewHolder>(
        protected val adapter: RecyclerView.Adapter<VH>) : RecyclerView.Adapter<VH>() {

    init {
        super.setHasStableIds(adapter.hasStableIds())
        this.adapter.registerAdapterDataObserver(ForwardingDataSetObserver())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return adapter.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        adapter.onBindViewHolder(holder, position)
    }

    override fun getItemViewType(position: Int): Int {
        return adapter.getItemViewType(position)
    }

    override fun onViewRecycled(holder: VH) {
        adapter.onViewRecycled(holder)
    }

    override fun onViewAttachedToWindow(holder: VH) {
        adapter.onViewAttachedToWindow(holder)
    }

    override fun onViewDetachedFromWindow(holder: VH) {
        adapter.onViewDetachedFromWindow(holder)
    }

    override fun onAttachedToRecyclerView(recyclerView: androidx.recyclerview.widget.RecyclerView) {
        adapter.onAttachedToRecyclerView(recyclerView)
    }

    override fun onDetachedFromRecyclerView(recyclerView: androidx.recyclerview.widget.RecyclerView) {
        adapter.onDetachedFromRecyclerView(recyclerView)
    }

    override fun getItemId(position: Int): Long {
        return adapter.getItemId(position)
    }

    override fun getItemCount(): Int {
        return adapter.itemCount
    }

    private inner class ForwardingDataSetObserver : androidx.recyclerview.widget.RecyclerView.AdapterDataObserver() {

        override fun onChanged() {
            notifyDataSetChanged()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            notifyItemRangeChanged(positionStart, itemCount)
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            notifyItemRangeInserted(positionStart, itemCount)
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            notifyItemRangeRemoved(positionStart, itemCount)
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            notifyItemMoved(fromPosition, toPosition)
        }
    }

}