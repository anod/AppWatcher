// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.watchlist

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.anod.appwatcher.Application
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.SqlOffset
import com.anod.appwatcher.database.entities.AppListItem
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.database.entities.packageToApp
import com.anod.appwatcher.installed.InstalledTaskWorker
import com.anod.appwatcher.model.AppListFilter
import info.anodsplace.applog.AppLog
import info.anodsplace.framework.app.ApplicationContext
import kotlin.math.max

class WatchListPagingSource(
        private val sortId: Int,
        private val titleFilter: String,
        private val config: Config,
        private val itemFilter: AppListFilter,
        private val tag: Tag? = null,
        private val appContext: ApplicationContext
) : PagingSource<Int, SectionItem>() {

    class Config(
            val showRecentlyUpdated: Boolean,
            val showOnDevice: Boolean,
            val showRecentlyInstalled: Boolean,
            val selectionMode: Boolean = false
    )

    private val database: AppsDatabase = Application.provide(appContext).database

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SectionItem> {
        AppLog.d("Load(key = ${params.key}, loadSize = ${params.loadSize})")
        val page = params.key ?: 0
        var limit = params.loadSize
        val offset = page * params.loadSize
        val items = mutableListOf<SectionItem>()
        if (params.key == null) {
            if (config.showRecentlyInstalled) {
                items.add(RecentItem)
                limit = max(0, limit - 1)
            }
        }

        val data = AppListTable.Queries.loadAppList(
                sortId, config.showRecentlyUpdated, tag, titleFilter, SqlOffset(offset, limit), database.apps()
        )
        val filtered = data.filter { !itemFilter.filterRecord(it) }

        items.addAll(filtered.map { AppItem(it, false) })

        if (filtered.isEmpty()) {
            if (params.key != null && config.showOnDevice) {
                val installed = InstalledTaskWorker(appContext, sortId, titleFilter).run()
                val allInstalledPackageNames = installed.map { it.pkg.name }
                val watchingPackages = database.apps().loadRowIds(allInstalledPackageNames).associateBy({ it.packageName }, { it.rowId })
                allInstalledPackageNames
                        .asSequence()
                        .filterNot { watchingPackages.containsKey(it) }
                        .map { appContext.packageManager.packageToApp(-1, it) }
                        .map { app -> AppListItem(app, "", noNewDetails = false, recentFlag = false) }
                        .forEach { item ->
                            items.add(OnDeviceItem(item, false))
                        }
            } else if (page == 0 && data.isEmpty() && items.firstOrNull() is RecentItem) {
                items.add(EmptyItem)
            }
        }

        return LoadResult.Page(
                data = items,
                prevKey = null,
                nextKey = if (data.isEmpty()) null else page + 1
        )
    }

    override fun getRefreshKey(state: PagingState<Int, SectionItem>): Int? = null
}