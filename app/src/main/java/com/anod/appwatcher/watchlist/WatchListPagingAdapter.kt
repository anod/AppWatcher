// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.watchlist

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.anod.appwatcher.Application
import com.anod.appwatcher.R
import com.anod.appwatcher.database.entities.AppListItem
import com.anod.appwatcher.databinding.ListItemEmptyBinding
import com.anod.appwatcher.utils.EventFlow
import com.anod.appwatcher.utils.PicassoAppIcon
import info.anodsplace.framework.content.InstalledApps

class SectionItemDiffCallback : DiffUtil.ItemCallback<SectionItem>() {
    override fun areItemsTheSame(oldItem: SectionItem, newItem: SectionItem) = when (oldItem) {
        is Header -> newItem is Header && oldItem.type::class == newItem.type::class
        is RecentItem -> newItem is RecentItem
        is AppItem -> newItem is AppItem && oldItem.appListItem.app.appId == newItem.appListItem.app.appId
        is OnDeviceItem -> newItem is OnDeviceItem && oldItem.appListItem.app.appId == newItem.appListItem.app.appId
        is EmptyItem -> newItem is EmptyItem
    }

    override fun areContentsTheSame(oldItem: SectionItem, newItem: SectionItem) = when (oldItem) {
        is Header -> newItem is Header && oldItem.type::class == newItem.type::class
        is RecentItem -> newItem is RecentItem
        is AppItem -> newItem is AppItem && oldItem == newItem
        is OnDeviceItem -> newItem is OnDeviceItem && oldItem == newItem
        is EmptyItem -> newItem is EmptyItem
    }
}

class WatchListPagingAdapter(
        installedApps: InstalledApps,
        private val lifecycleOwner: LifecycleOwner,
        private val action: EventFlow<WishListAction>,
        private val emptyViewHolderFactory: (itemBinding: ListItemEmptyBinding) -> EmptyViewHolder,
        private val calcSelection: (appItem: AppListItem) -> AppViewHolder.Selection,
        selection: LiveData<Pair<Int, AppViewHolder.Selection>>,
        private val context: Context
) : PagingDataAdapter<SectionItem, RecyclerView.ViewHolder>(SectionItemDiffCallback()) {

    private val itemDataProvider = AppViewHolderResourceProvider(context, installedApps)
    private val appIcon: PicassoAppIcon = Application.provide(context).iconLoader
    private val packageManager = Application.provide(context).packageManager

    init {
        selection.observe(lifecycleOwner, Observer {
            if (it.first >= 0) {
                notifyItemChanged(it.first)
            } else {
                notifyDataSetChanged()
            }
        })
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is Header -> R.layout.list_apps_header
            is AppItem -> R.layout.list_item_app
            is RecentItem -> R.layout.list_item_recently_installed
            is OnDeviceItem -> R.layout.list_item_app
            is EmptyItem -> R.layout.list_item_empty
            else -> 0
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is Header -> (holder as SectionHeaderViewHolder).bind(item.type)
            is AppItem -> (holder as AppViewHolder).bind(position, item.appListItem, item.isLocal, calcSelection(item.appListItem))
            is RecentItem -> (holder as RecentlyInstalledViewHolder).bind(item)
            is EmptyItem -> {
            }
            is OnDeviceItem -> (holder as AppViewHolder).bind(position, item.appListItem, true, calcSelection(item.appListItem))
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
                return RecentlyInstalledViewHolder(view, lifecycleOwner.lifecycleScope, appIcon, packageManager, action)
            }
            R.layout.list_item_app -> {
                val itemView = LayoutInflater.from(context).inflate(R.layout.list_item_app, parent, false)
                AppViewHolder(itemView, itemDataProvider, appIcon, action)
            }
            R.layout.list_item_empty -> {
                val binding = ListItemEmptyBinding.inflate(LayoutInflater.from(context), parent, false)
                return emptyViewHolderFactory(binding)
            }
            else -> throw UnsupportedOperationException("Unknown view")
        }
    }

    fun notifyRecentlyInstalledChanged() {
        val count = itemCount
        if (count > 0) {
            for (i in 0 until count) {
                if (getItemViewType(i) == R.layout.list_item_recently_installed) {
                    notifyItemChanged(i)
                    break
                }
            }
        }
    }
}