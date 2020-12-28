package com.anod.appwatcher.search

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anod.appwatcher.Application
import com.anod.appwatcher.R
import com.anod.appwatcher.utils.EventFlow
import finsky.api.model.Document
import info.anodsplace.framework.app.ThemeCompat
import info.anodsplace.framework.content.InstalledApps
import kotlinx.coroutines.flow.StateFlow

/**
 * @author Alex Gavrishev
 * *
 * @date 26/08/2016.
 */

class ResultsAdapterSingle(
        private val context: Context,
        private val action: EventFlow<ResultAction>,
        private val packages: StateFlow<List<String>>,
        private val document: Document): RecyclerView.Adapter<ResultsAppViewHolder>() {

    private val colorBgDisabled = ThemeCompat.getColor(context, R.attr.inactiveRow)
    private val colorBgNormal = ThemeCompat.getColor(context, R.attr.colorItemBackground)
    private val installedApps = InstalledApps.MemoryCache(InstalledApps.PackageManager(context.packageManager))

    val isEmpty: Boolean
        get() = this.itemCount == 0

    private val iconLoader = Application.provide(context).iconLoader

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultsAppViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_item_market_app, parent, false)
        return ResultsAppViewHolder(view, iconLoader, action, packages, colorBgDisabled, colorBgNormal, installedApps)
    }

    override fun onBindViewHolder(holder: ResultsAppViewHolder, position: Int) {
        holder.bind(document)
    }

    override fun getItemCount() = 1
}
