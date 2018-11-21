package info.anodsplace.framework.widget.recyclerview

import android.content.Context
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import info.anodsplace.framework.AppLog
import info.anodsplace.framework.database.CursorIterator

/**
 * @author Alex Gavrishev
 * @date 2015-06-20
 */
abstract class RecyclerViewCursorListAdapter<VH : RecyclerView.ViewHolder, O, in CR : CursorIterator<O>>(
        private val context: Context,
        @param:LayoutRes private val resource: Int) : RecyclerView.Adapter<VH>() {

    private var data: List<O> = emptyList()

    protected abstract fun onCreateViewHolder(itemView: View): VH
    protected abstract fun onBindViewHolder(holder: VH, position: Int, item: O)

    private val itemCallback = object: DiffUtil.ItemCallback<O>() {
        override fun areItemsTheSame(oldItem: O, newItem: O): Boolean {
            return this@RecyclerViewCursorListAdapter.areItemsTheSame(oldItem, newItem)
        }

        override fun areContentsTheSame(oldItem: O, newItem: O): Boolean {
            return this@RecyclerViewCursorListAdapter.areContentsTheSame(oldItem, newItem)
        }
    }

    protected abstract fun areItemsTheSame(oldItem: O, newItem: O): Boolean
    protected abstract fun areContentsTheSame(oldItem: O, newItem: O): Boolean

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(context).inflate(resource, parent, false)
        return onCreateViewHolder(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        onBindViewHolder(holder, position, data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun getItemViewType(position: Int): Int {
        return 1
    }

    class DiffCallback<OB>(private val oldList: List<OB>, private val newList: List<OB>, private val callback: DiffUtil.ItemCallback<OB>): DiffUtil.Callback() {

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return callback.areItemsTheSame(oldList[oldItemPosition], newList[newItemPosition])
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return callback.areContentsTheSame(oldList[oldItemPosition], newList[newItemPosition])
        }

        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }
    }

    open fun swapData(newCursor: CR?) {
        if (newCursor == null || newCursor.count == 0) {
            data = emptyList()
            notifyDataSetChanged()
            return
        }
        if (data.isEmpty()) {
            data = newCursor.toList()
            notifyDataSetChanged()
            return
        }
        val newData = newCursor.toList()
        val callback = DiffCallback(data, newData, itemCallback)
        val result = DiffUtil.calculateDiff(callback)
        data = newData
        result.dispatchUpdatesTo(this)
    }
}
