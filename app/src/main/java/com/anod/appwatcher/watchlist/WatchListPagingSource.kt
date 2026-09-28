// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.watchlist

import android.content.pm.PackageManager
import androidx.compose.runtime.Immutable
import androidx.paging.PagingState
import com.anod.appwatcher.database.AppListRowSnapshot
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.AppListItem
import com.anod.appwatcher.database.entities.packageToApp
import com.anod.appwatcher.installed.InstalledTaskWorker
import com.anod.appwatcher.model.AppListFilter
import com.anod.appwatcher.model.Filters
import com.anod.appwatcher.preferences.Preferences
import info.anodsplace.applog.AppLog
import info.anodsplace.framework.content.InstalledApps
import kotlin.math.max
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class WatchListPagingSource(
    private val config: Config,
    private val prefs: Preferences,
    private val packageManager: PackageManager,
    private val database: AppsDatabase,
    private val installedApps: InstalledApps,
    private val packageEnabled: (String) -> Boolean,
) : FilterablePagingSource() {
    override var filterQuery: String = ""
        set(value) {
            val changed = field != value
            field = value
            if (changed) {
                appListSnapshot = null
            }
        }
    private val itemFilter: AppListFilter = createFilter(config.filterId)
    private val sortId: Int = prefs.sortIndex
    private val appListSnapshotMutex = Mutex()

    @Volatile
    private var appListSnapshot: AppListSnapshot? = null

    private data class AppListSnapshot(
        val filterQuery: String,
        val rows: List<AppListSnapshotRow>,
    )

    private data class AppListSnapshotRow(
        val databaseRow: AppListRowSnapshot,
        val packageInfo: InstalledApps.Info,
        val isPackageEnabled: Boolean,
        val sectionRank: Int,
        val sortPosition: Int,
    )

    @Immutable
    data class Config(val filterId: Int, val tagId: Int?, val showRecentlyDiscovered: Boolean, val showOnDevice: Boolean, val showRecentlyInstalled: Boolean,)

    private fun createFilter(filterId: Int): AppListFilter = when (filterId) {
        Filters.INSTALLED -> AppListFilter.Installed()
        Filters.UNINSTALLED -> AppListFilter.Uninstalled()
        Filters.UPDATABLE -> AppListFilter.Updatable()
        else -> AppListFilter.All()
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SectionItem> {
        AppLog.d("$params")
        val (offset, initialLimit) = calculateOffsetAndLimit(
            key = params.key,
            loadSize = params.loadSize,
            showRecentlyInstalled = config.showRecentlyInstalled,
        )
        var limit = initialLimit
        val items = mutableListOf<SectionItem>()
        if (offset == 0 && config.showRecentlyInstalled) {
            items.add(SectionItem.Recent)
            // limit is already reduced in calculateOffsetAndLimit, but keep max guard
            limit = max(0, limit)
        }

        val snapshot = loadAppListSnapshot()
        val pageRows = if (offset >= snapshot.rows.size) {
            emptyList()
        } else {
            snapshot.rows.subList(offset, minOf(snapshot.rows.size, offset + limit))
        }
        val pageRowsById = pageRows.associateBy { it.databaseRow.rowId }
        val data = AppListTable.Queries.loadAppList(pageRows.map { it.databaseRow.rowId }, database.apps())
            .mapNotNull { item ->
                val snapshotRow = pageRowsById.getValue(item.app.rowId)
                if (
                    item.app.packageName != snapshotRow.databaseRow.packageName ||
                    item.app.versionNumber != snapshotRow.databaseRow.versionNumber
                ) {
                    return@mapNotNull null
                }
                Pair(
                    item.copy(
                        app = item.app.copy(status = snapshotRow.databaseRow.status),
                        recentFlag = snapshotRow.databaseRow.recentFlag,
                    ),
                    snapshotRow
                )
            }
        var totalItems = countTotalItems(
            snapshotSize = snapshot.rows.size,
            hasMissingSnapshotRows = data.size < pageRows.size,
        )

        items.addAll(data.map { (item, snapshotRow) ->
            SectionItem.App(
                appListItem = item,
                isLocal = false,
                packageInfo = snapshotRow.packageInfo,
                isPackageEnabled = snapshotRow.isPackageEnabled
            )
        })

        if (config.showOnDevice && pageRows.size < limit) {
            items.addAll(loadOnDeviceItems(snapshot.filterQuery))
        }

        if (offset == 0 && data.isEmpty() && items.firstOrNull() is SectionItem.Recent && items.size == 1) {
            items.add(SectionItem.Empty)
            totalItems = items.size
        }

        val (prevKey, nextKey) = calculateKeys(
            key = params.key,
            offset = offset,
            loadSize = params.loadSize,
            loadedDataSize = pageRows.size,
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

    private fun countTotalItems(snapshotSize: Int, hasMissingSnapshotRows: Boolean): Int {
        if (config.filterId != Filters.ALL || config.showOnDevice || hasMissingSnapshotRows) {
            return LoadResult.Page.COUNT_UNDEFINED
        }
        return snapshotSize + if (config.showRecentlyInstalled) 1 else 0
    }

    private suspend fun loadAppListSnapshot(): AppListSnapshot {
        val currentFilterQuery = filterQuery
        val existingSnapshot = appListSnapshot
        if (existingSnapshot?.filterQuery == currentFilterQuery) {
            return existingSnapshot
        }
        return appListSnapshotMutex.withLock {
            val lockedFilterQuery = filterQuery
            val lockedSnapshot = appListSnapshot
            if (lockedSnapshot?.filterQuery == lockedFilterQuery) {
                lockedSnapshot
            } else {
                val databaseRows = AppListTable.Queries.loadAppListRows(
                    sortId,
                    config.tagId,
                    lockedFilterQuery,
                    database.apps()
                )
                val rows = databaseRows
                    .mapIndexed { index, row ->
                        val packageInfo = installedApps.packageInfo(row.packageName)
                        val isPackageEnabled = !packageInfo.isInstalled || packageEnabled(row.packageName)
                        AppListSnapshotRow(
                            databaseRow = row,
                            packageInfo = packageInfo,
                            isPackageEnabled = isPackageEnabled,
                            sectionRank = sectionRank(row, isPackageEnabled),
                            sortPosition = index,
                        )
                    }
                    .filterNot { row ->
                        itemFilter.filterRecord(
                            versionCode = row.databaseRow.versionNumber,
                            packageInfo = row.packageInfo,
                            isPackageEnabled = row.isPackageEnabled
                        )
                    }
                    .sortedWith(compareBy(AppListSnapshotRow::sectionRank, AppListSnapshotRow::sortPosition))
                AppListSnapshot(
                    filterQuery = lockedFilterQuery,
                    rows = rows,
                ).also {
                    appListSnapshot = it
                }
            }
        }
    }

    private fun sectionRank(row: AppListRowSnapshot, isPackageEnabled: Boolean): Int =
        when {
            isPackageEnabled && row.status == App.STATUS_UPDATED -> 0
            isPackageEnabled && config.showRecentlyDiscovered && row.recentFlag -> 1
            else -> 2
        }

    private suspend fun loadOnDeviceItems(titleFilter: String): List<SectionItem.OnDevice> {
        val installed = InstalledTaskWorker(packageManager, sortId, titleFilter).run()
        val allInstalledPackageNames = installed.map { it.pkg.name }
        val watchingPackages = database.apps().loadRowIds(allInstalledPackageNames).associateBy({ it.packageName }, { it.rowId })
        return allInstalledPackageNames
            .asSequence()
            .filterNot { watchingPackages.containsKey(it) }
            .map { packageManager.packageToApp(-1, it) }
            .map { app -> AppListItem(app, "", noNewDetails = false, recentFlag = false) }
            .map { item ->
                val packageInfo = installedApps.packageInfo(item.app.packageName)
                SectionItem.OnDevice(
                    appListItem = item,
                    showSelection = false,
                    packageInfo = packageInfo,
                    isPackageEnabled = !packageInfo.isInstalled || packageEnabled(item.app.packageName)
                )
            }.toList()
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