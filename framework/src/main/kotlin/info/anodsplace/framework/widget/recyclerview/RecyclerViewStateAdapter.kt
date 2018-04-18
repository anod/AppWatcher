package info.anodsplace.framework.widget.recyclerview

import android.support.v7.widget.RecyclerView

open class RecyclerViewStateAdapter(adapters: Array<RecyclerView.Adapter<RecyclerView.ViewHolder>>): MergeRecyclerAdapter() {

    constructor() : this(emptyArray())

    init {
        adapters.forEach { add(it) }
    }

    var selectedId = 0

    override fun getItemCount(): Int {
        return get(selectedId).itemCount
    }

    override fun getOffsetForAdapterIndex(adapterIndex: Int): Int {
        return 0
    }

    override fun getAdapterOffsetForItem(position: Int): RecyclerView.Adapter<RecyclerView.ViewHolder>? {
        return get(selectedId)
    }

}