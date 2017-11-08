package com.anod.appwatcher.framework

import android.content.Context
import android.database.CursorWrapper
import android.support.annotation.LayoutRes
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * @author alex
 * *
 * @date 2015-06-20
 */
abstract class RecyclerViewCursorAdapter<VH : RecyclerView.ViewHolder, in CR : CursorWrapper>(
        private val context: Context,
        @param:LayoutRes private val resource: Int) : RecyclerView.Adapter<VH>() {

    private var mDataValid: Boolean = false
    private var mCursor: CR? = null

    protected abstract fun onCreateViewHolder(itemView: View): VH
    protected abstract fun onBindViewHolder(holder: VH, position: Int, cursor: CR)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return onCreateViewHolder(createItemView(parent, viewType))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        if (mCursor == null || !mCursor!!.moveToPosition(position)) {
            throw IllegalStateException("couldn't move cursor to position " + position)
        }
        onBindViewHolder(holder, position, mCursor!!)
    }

    override fun getItemCount(): Int {
        if (mDataValid && mCursor != null) {
            return mCursor!!.count
        } else {
            return 0
        }
    }

    override fun getItemViewType(position: Int): Int {
        return 1
    }

    protected fun createItemView(parent: ViewGroup, viewType: Int): View {
        val v = LayoutInflater.from(context).inflate(resource, parent, false)
        v.isClickable = true
        v.isFocusable = true
        return v
    }

    open fun swapData(newCursor: CR?) {
        if (newCursor === mCursor) {
            return
        }
        mCursor = newCursor
        if (newCursor != null) {
            mDataValid = true
            // notify the observers about the new cursor
            notifyDataSetChanged()
        } else {
            mDataValid = false
            // notify the observers about the lack of a data set
            notifyDataSetChanged()
        }
    }
}
