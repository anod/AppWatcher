// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.watchlist

import android.content.pm.PackageManager
import androidx.paging.PagingState
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.SqlOffset
import com.anod.appwatcher.database.entities.AppListItem
import com.anod.appwatcher.database.entities.packageToApp
import com.anod.appwatcher.installed.InstalledTaskWorker
import com.anod.appwatcher.model.AppListFilter
import com.anod.appwatcher.preferences.Preferences
import info.anodsplace.applog.AppLog
import kotlin.math.max

class WatchListPagingSource(
        override var filterQuery: String,
        private val config: Config,
        private val itemFilter: AppListFilter,
        private val prefs: Preferences,
        private val packageManager: PackageManager,
        private val database: AppsDatabase,
) : FilterablePagingSource() {

    data class Config(
            val filterId: Int,
            val tagId: Int?,
            val showRecentlyUpdated: Boolean,
            val showOnDevice: Boolean,
            val showRecentlyInstalled: Boolean,
    )

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SectionItem> {
        val offset = params.key ?: 0
        val sortId = prefs.sortIndex
        var limit = params.loadSize
        val items = mutableListOf<SectionItem>()
        if (offset == 0) {
            if (config.showRecentlyInstalled) {
                items.add(SectionItem.Recent)
                limit = max(0, limit - 1)
            }
        }

        val data = AppListTable.Queries.loadAppList(
                sortId, config.showRecentlyUpdated, config.tagId, filterQuery, SqlOffset(offset, limit), database.apps()
        )
        val filtered = data.filter { !itemFilter.filterRecord(it) }

        items.addAll(filtered.map { SectionItem.App(it, false) })

        if (filtered.isEmpty()) {
            if (params.key != null && config.showOnDevice) {
                val installed = InstalledTaskWorker(packageManager, sortId, filterQuery).run()
                val allInstalledPackageNames = installed.map { it.pkg.name }
                val watchingPackages = database.apps().loadRowIds(allInstalledPackageNames).associateBy({ it.packageName }, { it.rowId })
                allInstalledPackageNames
                        .asSequence()
                        .filterNot { watchingPackages.containsKey(it) }
                        .map { packageManager.packageToApp(-1, it) }
                        .map { app -> AppListItem(app, "", noNewDetails = false, recentFlag = false) }
                        .forEach { item ->
                            items.add(SectionItem.OnDevice(item, false))
                        }
            } else if (offset == 0 && data.isEmpty() && items.firstOrNull() is SectionItem.Recent) {
                items.add(SectionItem.Empty)
            }
        }

        val prevKey = when {
            params.key == null -> null
            (offset > 0 && offset < params.loadSize) -> 0
            (offset - params.loadSize) < 0 -> null
            else -> offset - params.loadSize
        }
        val page = LoadResult.Page(
                data = items,
                prevKey = prevKey,
                nextKey = if (data.isEmpty()) null else offset + params.loadSize
        )
        AppLog.d("Page prevKey=${page.prevKey} nextKey=${page.nextKey}, Params: Key=${params.key}")
        return page
    }

    override fun getRefreshKey(state: PagingState<Int, SectionItem>): Int {
        val anchorPosition = state.anchorPosition ?: 0
        val key = getRefreshKey(anchorPosition)
        AppLog.d("Page getRefreshKey=$key")
        return key
    }

    companion object {
        const val pageSize = 20
        fun getRefreshKey(position: Int): Int {
            val pages = position / pageSize
            return pages * pageSize
        }
    }
}