package info.anodsplace.appwatcher.framework

import android.support.v7.widget.RecyclerView
import info.anodsplace.android.widget.recyclerview.MergeRecyclerAdapter
import kotlin.math.min

open class RecyclerViewStateAdapter<VH : RecyclerView.ViewHolder>(adapters: Array<RecyclerView.Adapter<VH>>): MergeRecyclerAdapter() {

    constructor() : this(emptyArray())

    init {
        adapters.forEach { addAdapter(it) }
    }

    var selectedId = 0

    override fun getItemCount(): Int {
        return getAdapter(selectedId).itemCount
    }

    override fun getOffsetForAdapterIndex(adapterIndex: Int): Int {
        return 0
    }

    override fun getAdapterOffsetForItem(position: Int): RecyclerView.Adapter<*> {
        return getAdapter(selectedId)
    }

}