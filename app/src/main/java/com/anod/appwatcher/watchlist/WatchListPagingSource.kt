// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.watchlist

import android.content.pm.PackageManager
import androidx.compose.runtime.Immutable
import androidx.paging.PagingState
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.SqlOffset
import com.anod.appwatcher.database.entities.AppListItem
import com.anod.appwatcher.database.entities.packageToApp
import com.anod.appwatcher.installed.InstalledTaskWorker
import com.anod.appwatcher.model.AppListFilter
import com.anod.appwatcher.model.Filters
import com.anod.appwatcher.preferences.Preferences
import info.anodsplace.applog.AppLog
import info.anodsplace.framework.content.InstalledApps
import kotlin.math.max

class WatchListPagingSource(
    private val config: Config,
    private val prefs: Preferences,
    private val packageManager: PackageManager,
    private val database: AppsDatabase,
    private val installedApps: InstalledApps,
) : FilterablePagingSource() {
    override var filterQuery: String = ""
    private val itemFilter: AppListFilter = createFilter(config.filterId)

    @Immutable
    data class Config(val filterId: Int, val tagId: Int?, val showRecentlyDiscovered: Boolean, val showOnDevice: Boolean, val showRecentlyInstalled: Boolean,)

    private fun createFilter(filterId: Int): AppListFilter = when (filterId) {
        Filters.INSTALLED -> AppListFilter.Installed(installedApps)
        Filters.UNINSTALLED -> AppListFilter.Uninstalled(installedApps)
        Filters.UPDATABLE -> AppListFilter.Updatable(installedApps)
        else -> AppListFilter.All()
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SectionItem> {
        AppLog.d("$params")
        val (offset, initialLimit) = calculateOffsetAndLimit(
            key = params.key,
            loadSize = params.loadSize,
            showRecentlyInstalled = config.showRecentlyInstalled,
        )
        val sortId = prefs.sortIndex
        var limit = initialLimit
        val items = mutableListOf<SectionItem>()
        if (offset == 0 && config.showRecentlyInstalled) {
            items.add(SectionItem.Recent)
            // limit is already reduced in calculateOffsetAndLimit, but keep max guard
            limit = max(0, limit)
        }

        val data = AppListTable.Queries.loadAppList(
            sortId, config.showRecentlyDiscovered, config.tagId, filterQuery, SqlOffset(offset, limit), database.apps()
        )
        val filtered = data.filter { !itemFilter.filterRecord(it) }
        var totalItems = countTotalItems(
            offset = offset,
            dataSize = data.size,
            filteredSize = filtered.size
        )

        items.addAll(filtered.map {
            SectionItem.App(
                appListItem = it,
                isLocal = false,
                packageInfo = installedApps.packageInfo(it.app.packageName)
            )
        })

        if (filtered.isEmpty()) {
            if (params.key != null && config.showOnDevice) {
                totalItems = LoadResult.Page.COUNT_UNDEFINED
                val installed = InstalledTaskWorker(packageManager, sortId, filterQuery).run()
                val allInstalledPackageNames = installed.map { it.pkg.name }
                val watchingPackages = database.apps().loadRowIds(allInstalledPackageNames).associateBy({ it.packageName }, { it.rowId })
                allInstalledPackageNames
                    .asSequence()
                    .filterNot { watchingPackages.containsKey(it) }
                    .map { packageManager.packageToApp(-1, it) }
                    .map { app -> AppListItem(app, "", noNewDetails = false, recentFlag = false) }
                    .forEach { item ->
                        items.add(
                            SectionItem.OnDevice(
                                appListItem = item,
                                showSelection = false,
                                packageInfo = installedApps.packageInfo(item.app.packageName)
                            )
                        )
                    }
            } else if (offset == 0 && data.isEmpty() && items.firstOrNull() is SectionItem.Recent) {
                items.add(SectionItem.Empty)
                totalItems = items.size
            }
        }

        val (prevKey, nextKey) = calculateKeys(
            key = params.key,
            offset = offset,
            loadSize = params.loadSize,
            loadedDataSize = data.size,
            limit = limit
        )
        val itemsBefore = if (totalItems == LoadResult.Page.COUNT_UNDEFINED) {
            LoadResult.Page.COUNT_UNDEFINED
        } else {
            calculateItemsBefore(offset, config.showRecentlyInstalled)
        }
        val itemsAfter = calculateItemsAfter(
            totalItems = totalItems,
            itemsBefore = itemsBefore,
            loadedItems = items.size
        )
        val page = LoadResult.Page(
            data = items,
            prevKey = prevKey,
            nextKey = nextKey,
            itemsBefore = itemsBefore,
            itemsAfter = itemsAfter
        )
        AppLog.d("[Paging] prevKey=${page.prevKey} nextKey=${page.nextKey}, offsetKey=${params.key}, loadSize: ${params.loadSize}, itemsBefore=$itemsBefore, itemsAfter=$itemsAfter")
        return page
    }

    private suspend fun countTotalItems(offset: Int, dataSize: Int, filteredSize: Int): Int {
        if (config.filterId != Filters.ALL) {
            return LoadResult.Page.COUNT_UNDEFINED
        }
        val appsCount = AppListTable.Queries.countAppList(config.tagId, filterQuery, database.apps())
        return appsCount + if (config.showRecentlyInstalled) 1 else 0
    }

    override fun getRefreshKey(state: PagingState<Int, SectionItem>): Int {
        val anchorPosition = state.anchorPosition ?: 0
        val key = getRefreshKey(anchorPosition, config.showRecentlyInstalled)
        AppLog.d("[Paging] getRefreshKey=$key anchorPosition=$anchorPosition")
        return key
    }

    companion object {
        const val PAGE_SIZE = 20

        fun calculateOffsetAndLimit(key: Int?, loadSize: Int, showRecentlyInstalled: Boolean,): Pair<Int, Int> {
            val offset = key ?: 0
            var limit = loadSize
            if (offset == 0 && showRecentlyInstalled) {
                limit = max(0, loadSize - 1)
            }
            return offset to limit
        }

        fun calculateKeys(
            key: Int?,
            offset: Int,
            loadSize: Int,
            loadedDataSize: Int,
            limit: Int = loadSize,
        ): Pair<Int?, Int?> {
            val prevKey = when {
                offset <= 0 -> null
                offset <= loadSize -> 0
                else -> offset - loadSize
            }
            val nextKey = if (loadedDataSize < limit) null else offset + limit
            return prevKey to nextKey
        }

        fun getRefreshKey(position: Int, showRecentlyInstalled: Boolean = false): Int {
            val appPosition = if (showRecentlyInstalled) {
                max(0, position - 1)
            } else {
                position
            }
            if (showRecentlyInstalled) {
                val firstPageAppCount = PAGE_SIZE - 1
                if (appPosition < firstPageAppCount) {
                    return 0
                }
                return firstPageAppCount + ((appPosition - firstPageAppCount) / PAGE_SIZE) * PAGE_SIZE
            }
            val pages = appPosition / PAGE_SIZE
            return pages * PAGE_SIZE
        }

        fun calculateItemsBefore(offset: Int, showRecentlyInstalled: Boolean): Int = offset + if (showRecentlyInstalled && offset > 0) 1 else 0

        fun calculateItemsAfter(totalItems: Int, itemsBefore: Int, loadedItems: Int,): Int {
            if (totalItems == LoadResult.Page.COUNT_UNDEFINED || itemsBefore == LoadResult.Page.COUNT_UNDEFINED) {
                return LoadResult.Page.COUNT_UNDEFINED
            }
            return max(0, totalItems - itemsBefore - loadedItems)
        }
    }
}