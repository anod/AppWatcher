// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.watchlist

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.anod.appwatcher.Application
import com.anod.appwatcher.R
import com.anod.appwatcher.database.entities.AppListItem
import com.anod.appwatcher.utils.PicassoAppIcon
import info.anodsplace.framework.content.InstalledApps

class AppListItemDiffCallback : DiffUtil.ItemCallback<AppListItem>() {
    override fun areItemsTheSame(oldItem: AppListItem, newItem: AppListItem): Boolean {
        return oldItem.app.appId == newItem.app.appId
    }

    override fun areContentsTheSame(oldItem: AppListItem, newItem: AppListItem): Boolean {
        return oldItem == newItem
    }
}

class SectionItemDiffCallback : DiffUtil.ItemCallback<SectionItem>() {
    override fun areItemsTheSame(oldItem: SectionItem, newItem: SectionItem) = when (oldItem) {
        is Header -> newItem is Header && oldItem.type.javaClass == newItem.type.javaClass
        is RecentItem -> newItem is RecentItem
        is AppItem -> newItem is AppItem && oldItem.appListItem.app.appId == newItem.appListItem.app.appId
        is OnDeviceItem -> newItem is OnDeviceItem && oldItem.installedPackage.packageName == newItem.installedPackage.packageName
        is Empty -> newItem is Empty
    }

    override fun areContentsTheSame(oldItem: SectionItem, newItem: SectionItem): Boolean {
        return oldItem == newItem
    }
}

class WatchListPagingAdapter(
        private val itemViewType: Int,
        installedApps: InstalledApps,
        private val listener: AppViewHolder.OnClickListener,
        private val context: Context
) : PagingDataAdapter<SectionItem, RecyclerView.ViewHolder>(SectionItemDiffCallback()) {

    private val itemDataProvider = AppViewHolderResourceProvider(context, installedApps)
    private val appIcon: PicassoAppIcon = Application.provide(context).iconLoader
    private val packageManager = Application.provide(context).packageManager

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is Header -> R.layout.list_apps_header
            is AppItem -> R.layout.list_item_app
            is RecentItem -> R.layout.list_item_recently_installed
            is OnDeviceItem -> R.layout.list_item_app
            else -> throw UnsupportedOperationException("Unknown view")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        holder as? BindableViewHolder<*> ?: throw UnsupportedOperationException("Unknown view")
        when (item) {
            is Header -> (holder as SectionHeaderViewHolder).bind(item.type)
            is AppItem -> (holder as AppViewHolder).bind(item.appListItem)
            is RecentItem -> (holder as RecentlyInstalledViewHolder).bind(item.packageNames)
            is Empty -> (holder as EmptyViewHolder).bind(null)
            else -> holder.placeholder()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.list_apps_header -> {
                val itemView = LayoutInflater.from(context).inflate(R.layout.list_apps_header, parent, false)
                SectionHeaderViewHolder(itemView)
            }
            R.layout.list_item_recently_installed -> {
                val view = LayoutInflater.from(context).inflate(R.layout.list_item_recently_installed, parent, false)
                return RecentlyInstalledViewHolder(view, appIcon, packageManager, listener)
            }
            R.layout.list_item_app -> {
                val itemView = LayoutInflater.from(context).inflate(R.layout.list_item_app, parent, false)
                AppViewHolder(itemView, itemDataProvider, appIcon, listener)
            }
            R.layout.list_item_empty -> {
                val itemView = LayoutInflater.from(context).inflate(R.layout.list_item_empty, parent, false)
                return EmptyViewHolder(itemView)
            }
            else -> throw UnsupportedOperationException("Unknown view")
        }
    }
}