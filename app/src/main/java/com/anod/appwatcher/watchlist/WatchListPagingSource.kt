// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.watchlist

import androidx.paging.PagingSource
import com.anod.appwatcher.Application
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.SqlOffset
import com.anod.appwatcher.database.entities.AppListItem
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.database.entities.packageToApp
import com.anod.appwatcher.installed.InstalledTaskWorker
import info.anodsplace.framework.AppLog
import info.anodsplace.framework.app.ApplicationContext
import kotlin.math.max

class WatchListPagingSource(
        private val sortId: Int,
        private val titleFilter: String,
        private val config: Config,
        private val tag: Tag? = null,
        private val appContext: ApplicationContext
) : PagingSource<Int, SectionItem>() {

    class Config(
            val showRecentlyUpdated: Boolean,
            val showOnDevice: Boolean,
            val showRecentlyInstalled: Boolean
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
                items.add(RecentItem(sortId, titleFilter))
                limit = max(0, limit - 1)
            }
        }

        val data = AppListTable.Queries.loadAppList(
                sortId, config.showRecentlyUpdated, tag, titleFilter, SqlOffset(offset, limit), database.apps()
        )
        items.addAll(data.map { AppItem(it) })

        if (data.isEmpty()) {
            if (params.key != null && config.showOnDevice) {
                val installed = InstalledTaskWorker(appContext, sortId, titleFilter).run()
                val allInstalledPackageNames = installed.second.map { it.packageName }
                val watchingPackages = database.apps().loadRowIds(allInstalledPackageNames).associateBy({ it.packageName }, { it.rowId })
                allInstalledPackageNames
                        .asSequence()
                        .filterNot { watchingPackages.containsKey(it) }
                        .map { appContext.packageManager.packageToApp(-1, it) }
                        .forEach { app ->
                            items.add(OnDeviceItem(AppListItem(app, "", noNewDetails = false, recentFlag = false)))
                        }
            } else if (params.key == null) {
                items.add(Empty)
            }
        }

        return LoadResult.Page(
                data = items,
                prevKey = null,
                nextKey = if (data.isEmpty()) null else page + 1
        )
    }

}