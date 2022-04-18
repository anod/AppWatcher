package com.anod.appwatcher.search

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.anod.appwatcher.R
import com.anod.appwatcher.utils.AppIconLoader
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

class DocumentDiffCallback : DiffUtil.ItemCallback<Document>() {

    override fun areItemsTheSame(oldItem: Document, newItem: Document): Boolean {
        return oldItem.docId == newItem.docId
    }

    override fun areContentsTheSame(oldItem: Document, newItem: Document): Boolean {
        return oldItem.docId == newItem.docId
    }
}

class ResultsAdapterList(
        private val context: Context,
        private val action: EventFlow<ResultAction>,
        private val packages: StateFlow<List<String>>,
        private val iconLoader: AppIconLoader
) : PagingDataAdapter<Document, ResultsAppViewHolder>(DocumentDiffCallback()) {

    private val colorBgDisabled = ThemeCompat.getColor(context, R.attr.inactiveRow)
    private val colorBgNormal = ThemeCompat.getColor(context, R.attr.colorItemBackground)
    private val installedApps = InstalledApps.MemoryCache(InstalledApps.PackageManager(context.packageManager))

    val isEmpty: Boolean
        get() = this.itemCount == 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultsAppViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_item_market_app, parent, false)
        return ResultsAppViewHolder(view, iconLoader, action, packages, colorBgDisabled, colorBgNormal, installedApps)
    }

    override fun onBindViewHolder(holder: ResultsAppViewHolder, position: Int) {
        val doc = getItem(position)
        if (doc == null) {
            holder.placeholder()
        } else {
            holder.bind(doc)
        }
    }
}