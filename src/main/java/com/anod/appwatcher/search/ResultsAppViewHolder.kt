package com.anod.appwatcher.search

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.anod.appwatcher.R
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.model.WatchAppList
import com.google.android.finsky.api.model.Document

class ResultsAppViewHolder(itemView: View, private val watchAppList: WatchAppList)
    : RecyclerView.ViewHolder(itemView), View.OnClickListener {
    var doc: Document? = null

    @BindView(android.R.id.content)
    lateinit var row: View
    @BindView(android.R.id.title)
    lateinit var title: TextView
    @BindView(R.id.details)
    lateinit var details: TextView
    @BindView(R.id.updated)
    lateinit var updated: TextView
    @BindView(R.id.price)
    lateinit var price: TextView
    @BindView(android.R.id.icon)
    lateinit var icon: ImageView

    init {
        ButterKnife.bind(this, itemView)
        this.row.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        val info = AppInfo(doc!!)
        if (watchAppList.contains(info.packageName)) {
            watchAppList.delete(info)
        } else {
            watchAppList.add(info)
        }
    }
}
