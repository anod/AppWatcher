// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.watchlist

import android.content.Context
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.anod.appwatcher.R
import com.anod.appwatcher.database.entities.AppListItem
import com.anod.appwatcher.databinding.ListItemEmptyBinding
import com.anod.appwatcher.utils.AppIconLoader
import com.anod.appwatcher.utils.EventFlow
import info.anodsplace.framework.content.InstalledApps
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SectionItemDiffCallback : DiffUtil.ItemCallback<SectionItem>() {
    override fun areItemsTheSame(oldItem: SectionItem, newItem: SectionItem) = when (oldItem) {
        is SectionItem.Header -> newItem is SectionItem.Header && oldItem.type::class == newItem.type::class
        is SectionItem.Recent -> newItem is SectionItem.Recent
        is SectionItem.App -> newItem is SectionItem.App && oldItem.appListItem.app.appId == newItem.appListItem.app.appId
        is SectionItem.OnDevice -> newItem is SectionItem.OnDevice && oldItem.appListItem.app.appId == newItem.appListItem.app.appId
        is SectionItem.Empty -> newItem is SectionItem.Empty
    }

    override fun areContentsTheSame(oldItem: SectionItem, newItem: SectionItem) = when (oldItem) {
        is SectionItem.Header -> newItem is SectionItem.Header && oldItem.type::class == newItem.type::class
        is SectionItem.Recent -> newItem is SectionItem.Recent
        is SectionItem.App -> newItem is SectionItem.App && oldItem == newItem
        is SectionItem.OnDevice -> newItem is SectionItem.OnDevice && oldItem == newItem
        is SectionItem.Empty -> newItem is SectionItem.Empty
    }
}

class WatchListPagingAdapter(
        private val tagColor: Int?,
        installedApps: InstalledApps,
        private val recentlyInstalledPackages: Flow<List<InstalledPackageRow>>,
        private val lifecycleOwner: LifecycleOwner,
        private val action: EventFlow<WatchListAction>,
        private val emptyViewHolderFactory: (itemBinding: ListItemEmptyBinding) -> EmptyViewHolder,
        private val calcSelection: (appItem: AppListItem) -> AppViewHolder.Selection,
        selection: LiveData<Pair<Int, AppViewHolder.Selection>>,
        private val context: Context,
        private val iconLoader: AppIconLoader,
        private val packageManager: PackageManager
) : PagingDataAdapter<SectionItem, RecyclerView.ViewHolder>(SectionItemDiffCallback()) {

    private val itemDataProvider = AppViewHolderResourceProvider(context, installedApps)

    init {
        selection.observe(lifecycleOwner) {
            if (it.first >= 0) {
                notifyItemChanged(it.first)
            } else {
                notifyDataSetChanged()
            }
        }
    }

    object ViewType {
        const val simpleHeader = 1
        const val recentItemHeader = 2
        const val appItem = 3
        const val recentItem = 4
        const val emptyItem = 5
    }

    override fun getItemViewType(position: Int): Int {
        return when (val item = getItem(position)) {
            is SectionItem.Header -> when (item.type) {
                is SectionHeader.RecentlyInstalled -> ViewType.recentItemHeader
                else -> ViewType.simpleHeader
            }
            is SectionItem.App -> ViewType.appItem
            is SectionItem.OnDevice -> ViewType.appItem
            is SectionItem.Recent -> ViewType.recentItem
            is SectionItem.Empty -> ViewType.emptyItem
            else -> 0
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is SectionItem.Header -> (holder as SectionHeaderViewHolder).bind(item.type)
            is SectionItem.App -> (holder as AppViewHolder).bind(position, item.appListItem, item.isLocal, calcSelection(item.appListItem))
            is SectionItem.Recent -> (holder as RecentlyInstalledViewHolder).bind(item)
            is SectionItem.Empty -> {
            }
            is SectionItem.OnDevice -> (holder as AppViewHolder).bind(position, item.appListItem, true, calcSelection(item.appListItem))
            null -> {}
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ViewType.simpleHeader -> {
                val itemView = LayoutInflater.from(context).inflate(R.layout.list_apps_header, parent, false)
                SectionHeaderViewHolder(itemView, tagColor)
            }
            ViewType.recentItemHeader -> {
                val itemView = LayoutInflater.from(context).inflate(R.layout.list_apps_header, parent, false)
                SectionHeaderViewHolder.Expandable(
                        itemView,
                        tagColor,
                        lifecycleOwner,
                        recentlyInstalledPackages.map { it.isNotEmpty() },
                        action
                )
            }
            ViewType.recentItem -> {
                val view = LayoutInflater.from(context).inflate(R.layout.list_item_recently_installed, parent, false)
                return RecentlyInstalledViewHolder(view, lifecycleOwner, recentlyInstalledPackages, iconLoader, packageManager, action)
            }
            ViewType.appItem -> {
                val itemView = LayoutInflater.from(context).inflate(R.layout.list_item_app, parent, false)
                AppViewHolder(itemView, tagColor, itemDataProvider, iconLoader, action)
            }
            ViewType.emptyItem -> {
                val binding = ListItemEmptyBinding.inflate(LayoutInflater.from(context), parent, false)
                return emptyViewHolderFactory(binding)
            }
            else -> throw UnsupportedOperationException("Unknown view")
        }
    }
}