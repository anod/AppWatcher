// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.watchlist

import androidx.paging.PagingSource
import com.anod.appwatcher.Application
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.SqlOffset
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.installed.InstalledResult
import com.anod.appwatcher.installed.InstalledTaskWorker
import info.anodsplace.framework.AppLog
import info.anodsplace.framework.app.ApplicationContext
import kotlin.math.max

class WatchListPagingSource(
        private val sortId: Int,
        private val showRecentlyUpdated: Boolean,
        private val showOnDevice: Boolean,
        private val showRecentlyInstalled: Boolean,
        private val titleFilter: String,
        private val tag: Tag? = null,
        private val appContext: ApplicationContext
) : PagingSource<Int, SectionItem>() {

    private val database: AppsDatabase = Application.provide(appContext).database

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SectionItem> {
        AppLog.d("Load(key = ${params.key}, loadSize = ${params.loadSize})")
        val page = params.key ?: 0
        var limit = params.loadSize
        val offset = page * params.loadSize
        val items = mutableListOf<SectionItem>()
        var installed: InstalledResult? = null
        if (params.key == null) {
            if (showRecentlyInstalled) {
                limit = max(0, limit - 1)
                installed = InstalledTaskWorker(appContext, sortId, titleFilter).run()
                if (installed.first.isNotEmpty()) {
                    val watchingPackages = database.apps().loadRowIds(installed.first).associateBy({ it.packageName }, { it.rowId })
                    items.add(RecentItem(installed.first.map {
                        Pair(it, watchingPackages[it] ?: -1)
                    }))
                }
            }
        }

        val data = AppListTable.Queries.loadAppList(
                sortId, showRecentlyUpdated, tag, titleFilter, SqlOffset(offset, limit), database.apps()
        )
        items.addAll(data.map { AppItem(it) })

        if (data.isEmpty() && showOnDevice) {
            if (installed == null) {
                installed = InstalledTaskWorker(appContext, sortId, titleFilter).run()
            }
            installed.second.forEach {
                items.add(OnDeviceItem(it))
            }
        }

        return LoadResult.Page(
                data = items,
                prevKey = null,
                nextKey = if (data.isEmpty()) null else page + 1
        )
    }

}