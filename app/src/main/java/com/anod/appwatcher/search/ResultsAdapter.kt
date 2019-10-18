package com.anod.appwatcher.search

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import com.anod.appwatcher.Application
import com.anod.appwatcher.R
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.utils.AdaptiveIconTransformation
import finsky.api.model.Document
import info.anodsplace.framework.app.ThemeCompat
import info.anodsplace.framework.content.InstalledApps

/**
 *  @author alex
 *  @date 6/3/2017
 */

interface ResultsViewModel {
    val packages: LiveData<List<String>>
    fun delete(info: AppInfo)
    fun add(info: AppInfo)
}

abstract class ResultsAdapter(
        private val context: Context,
        private val viewModel: ResultsViewModel): RecyclerView.Adapter<ResultsAppViewHolder>() {

    private val colorBgDisabled = ThemeCompat.getColor(context, R.attr.inactiveRow)
    private val colorBgNormal = ThemeCompat.getColor(context, R.attr.colorItemBackground)
    private val installedApps = InstalledApps.MemoryCache(InstalledApps.PackageManager(context.packageManager))

    val isEmpty: Boolean
        get() = this.itemCount == 0

    private val iconLoader = Application.provide(context).iconLoader

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultsAppViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_item_market_app, parent, false)
        return ResultsAppViewHolder(view, viewModel)
    }

    abstract fun document(position: Int): Document

    override fun onBindViewHolder(holder: ResultsAppViewHolder, position: Int) {
        val doc = document(position)

        val app = doc.appDetails
        val uploadDate = app.uploadDate
        val packageName = app.packageName

        holder.doc = doc
        holder.title.text = doc.title
        holder.creator.text = doc.creator
        holder.updated.text = uploadDate

        val packages = viewModel.packages.value ?: emptyList()
        if (packages.contains(packageName)) {
            holder.row.setBackgroundColor(colorBgDisabled)
        } else {
            holder.row.setBackgroundColor(colorBgNormal)
        }

        iconLoader.retrieve(doc.iconUrl ?: "")
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
