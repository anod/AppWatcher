package com.anod.appwatcher.search

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.anod.appwatcher.R
import com.anod.appwatcher.model.WatchAppList
import com.squareup.picasso.Picasso
import finsky.api.model.Document
import info.anodsplace.framework.app.ThemeCompat
import info.anodsplace.framework.content.InstalledApps

/**
 *  @author alex
 *  @date 6/3/2017
 */

abstract class ResultsAdapter(
        private val context: Context,
        private val watchAppList: WatchAppList): RecyclerView.Adapter<ResultsAppViewHolder>() {

    private val colorBgDisabled = ThemeCompat.getColor(context, R.attr.inactiveRow)
    private val colorBgNormal = ThemeCompat.getColor(context, R.attr.colorItemBackground)
    private val installedApps = InstalledApps.MemoryCache(InstalledApps.PackageManager(context.packageManager))

    val isEmpty: Boolean
        get() = this.itemCount == 0

    val isNotEmpty: Boolean
        get() = this.itemCount > 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultsAppViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_item_market_app, parent, false)
        return ResultsAppViewHolder(view, watchAppList)
    }

    abstract fun document(position: Int): Document

    override fun onBindViewHolder(holder: ResultsAppViewHolder, position: Int) {
        val doc = document(position)

        val app = doc.appDetails
        val uploadDate = app.uploadDate
        val packageName = app.packageName

        holder.doc = doc
        holder.title.text = doc.title
        holder.details.text = doc.creator
        holder.updated.text = uploadDate

        if (watchAppList.contains(packageName)) {
            holder.row.setBackgroundColor(colorBgDisabled)
        } else {
            holder.row.setBackgroundColor(colorBgNormal)
        }

        Picasso.get().load(doc.iconUrl)
                .placeholder(R.drawable.ic_notifications_black_24dp)
                .into(holder.icon)

        val isInstalled = installedApps.packageInfo(packageName).isInstalled
        if (isInstalled) {
            holder.price.setText(R.string.installed)
        } else {
            val offer = doc.offer
            when {
                offer.offerType == 0 -> holder.price.text = ""
                offer.micros.toInt() == 0 -> holder.price.setText(R.string.free)
                else -> holder.price.text = offer.formattedAmount
            }
        }
    }
}
