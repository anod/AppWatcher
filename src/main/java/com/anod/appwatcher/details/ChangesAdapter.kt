package com.anod.appwatcher.details

import android.content.Context
import android.support.annotation.LayoutRes
import android.support.v7.widget.RecyclerView
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.anod.appwatcher.R
import com.anod.appwatcher.content.AppChangeCursor
import com.anod.appwatcher.model.AppChange
import info.anodsplace.android.widget.recyclerview.ArrayAdapter
import info.anodsplace.appwatcher.framework.Html
import info.anodsplace.appwatcher.framework.RecyclerViewCursorAdapter
import info.anodsplace.appwatcher.framework.RecyclerViewStateAdapter

class ChangeView(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val changelog: TextView by lazy {
        val view = itemView.findViewById<TextView>(R.id.changelog)
        view.autoLinkMask = Linkify.ALL
        view
    }
    val version: TextView by lazy { itemView.findViewById<TextView>(R.id.version) }

    fun bindView(change: AppChange) {
        version.text = "${change.versionName} (${change.versionCode})"
        if (change.details.isEmpty()) {
            changelog.setText(R.string.no_recent_changes)
        } else {
            changelog.text = Html.parse(change.details)
        }
    }

}

class ChangesAdapter(private val context: Context, recentChange: AppChange): RecyclerViewStateAdapter<ChangeView>(arrayOf(ChangesCursorAdapter(context, recentChange)))  {
    private val arrayId = 1

    var recentChange = recentChange
        set(value) {
            field = value
            if (cursorAdapter.itemCount == 0) {
                addAdapter(arrayId, RecentChangeAdapter(context, value))
                selectedId = arrayId
            }
            notifyItemChanged(0)
        }

    private val cursorAdapter: ChangesCursorAdapter
        get() = getAdapter(0) as ChangesCursorAdapter

    fun swapData(cursor: AppChangeCursor?) {
        cursorAdapter.swapData(cursor)
    }

}

class RecentChangeAdapter(private val context: Context, recentChange: AppChange) : ArrayAdapter<AppChange, ChangeView>(mutableListOf(recentChange)) {

    @LayoutRes val resource: Int =  R.layout.list_item_change

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChangeView {
        val v = LayoutInflater.from(context).inflate(resource, parent, false)
        return ChangeView(v)
    }

    override fun onBindViewHolder(holder: ChangeView, position: Int) {
        holder.bindView(getItem(position))
    }
}

class ChangesCursorAdapter(context: Context,private var recentChange: AppChange) : RecyclerViewCursorAdapter<ChangeView, AppChangeCursor>(context, R.layout.list_item_change) {

    override fun onCreateViewHolder(itemView: View): ChangeView {
        return ChangeView(itemView)
    }

    override fun onBindViewHolder(holder: ChangeView, position: Int, cursor: AppChangeCursor) {
        val change = cursor.change
        if (recentChange.versionCode == change.versionCode) {
            holder.bindView(recentChange)
        } else {
            holder.bindView(change)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return 1
    }
}