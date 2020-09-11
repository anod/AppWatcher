// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.watchlist

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.anod.appwatcher.Application
import com.anod.appwatcher.R
import com.anod.appwatcher.utils.PicassoAppIcon
import com.anod.appwatcher.utils.SingleLiveEvent
import info.anodsplace.framework.content.InstalledApps

class SectionItemDiffCallback : DiffUtil.ItemCallback<SectionItem>() {
    override fun areItemsTheSame(oldItem: SectionItem, newItem: SectionItem) = when (oldItem) {
        is Header -> newItem is Header && oldItem.type::class == newItem.type::class
        is RecentItem -> newItem is RecentItem
        is AppItem -> newItem is AppItem && oldItem.appListItem.app.appId == newItem.appListItem.app.appId
        is OnDeviceItem -> newItem is OnDeviceItem && oldItem.appListItem.app.appId == newItem.appListItem.app.appId
        is Empty -> newItem is Empty
    }

    override fun areContentsTheSame(oldItem: SectionItem, newItem: SectionItem) = when (oldItem) {
        is Header -> newItem is Header && oldItem.type::class == newItem.type::class
        is RecentItem -> newItem is RecentItem
        is AppItem -> newItem is AppItem && oldItem.appListItem.app == newItem.appListItem.app
        is OnDeviceItem -> newItem is OnDeviceItem && oldItem.appListItem.app == newItem.appListItem.app
        is Empty -> newItem is Empty
    }
}

class WatchListPagingAdapter(
        installedApps: InstalledApps,
        private val lifecycleScope: LifecycleCoroutineScope,
        private val action: SingleLiveEvent<WishListAction>,
        private val emptyViewHolderFactory: (itemView: View) -> EmptyViewHolder,
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
            is Empty -> R.layout.list_item_empty
            else -> throw UnsupportedOperationException("Unknown view")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        holder as? BindableViewHolder<*> ?: throw UnsupportedOperationException("Unknown view")
        when (item) {
            is Header -> (holder as SectionHeaderViewHolder).bind(item.type)
            is AppItem -> (holder as AppViewHolder).bind(item.appListItem)
            is RecentItem -> (holder as RecentlyInstalledViewHolder).bind(item)
            is Empty -> (holder as EmptyViewHolder).bind(null)
            is OnDeviceItem -> (holder as AppViewHolder).bind(item.appListItem)
            null -> holder.placeholder()
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
                return RecentlyInstalledViewHolder(view, lifecycleScope, appIcon, packageManager, action)
            }
            R.layout.list_item_app -> {
                val itemView = LayoutInflater.from(context).inflate(R.layout.list_item_app, parent, false)
                AppViewHolder(itemView, itemDataProvider, appIcon, action)
            }
            R.layout.list_item_empty -> {
                val itemView = LayoutInflater.from(context).inflate(R.layout.list_item_empty, parent, false)
                return emptyViewHolderFactory(itemView)
            }
            else -> throw UnsupportedOperationException("Unknown view")
        }
    }
}