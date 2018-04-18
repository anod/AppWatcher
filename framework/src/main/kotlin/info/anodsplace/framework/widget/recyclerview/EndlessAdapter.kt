package info.anodsplace.framework.widget.recyclerview

import android.content.Context
import android.support.annotation.LayoutRes
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import java.util.concurrent.atomic.AtomicBoolean

class EndlessAdapter(adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>, private val context: Context, @param:LayoutRes private val loadMoreViewId: Int) : AdapterWrapper<RecyclerView.ViewHolder>(adapter) {
    private val keepOnAppending = AtomicBoolean(false)

    private val minItemCount: Int
        get() = if (adapter is HeaderAdapter) {
            (adapter as HeaderAdapter).headerCount
        } else 0

    override fun getItemCount(): Int {
        val count = adapter.itemCount
        if (keepOnAppending.get()) {
            if (count > minItemCount) {
                return count + 1 // one more for loading view
            }
        }

        return count
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == adapter.itemCount) {
            viewTypeLoadMore
        } else super.getItemViewType(position)

    }

    override fun getItemId(position: Int): Long {
        return if (position == adapter.itemCount) {
            position.toLong()
        } else super.getItemId(position)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == viewTypeLoadMore) {
            val view = LayoutInflater.from(context).inflate(loadMoreViewId, parent, false)
            return FooterViewHolder(view)
        }

        return super.onCreateViewHolder(parent, viewType)
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is FooterViewHolder) {
            return
        }
        super.onBindViewHolder(holder, position)
    }

    fun setKeepOnAppending(newValue: Boolean) {
        val same = newValue == keepOnAppending.get()

        keepOnAppending.set(newValue)

        if (!same) {
            val count = adapter.itemCount
            if (newValue) {
                if (count > minItemCount) {
                    notifyItemInserted(count)
                }
            } else {
                notifyItemRemoved(count + 1)
            }
        }
    }

    class FooterViewHolder(loadMoreView: View) : RecyclerView.ViewHolder(loadMoreView)

    companion object {
        val viewTypeLoadMore = -1
    }
}