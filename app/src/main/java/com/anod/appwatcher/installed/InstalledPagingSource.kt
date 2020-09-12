// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.installed

import androidx.paging.PagingSource
import com.anod.appwatcher.Application
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.entities.AppListItem
import com.anod.appwatcher.database.entities.packageToApp
import com.anod.appwatcher.watchlist.OnDeviceItem
import com.anod.appwatcher.watchlist.SectionItem
import info.anodsplace.framework.app.ApplicationContext

class InstalledPagingSource(
        private val sortId: Int,
        private val titleFilter: String,
        private val showSelection: Boolean,
        private val appContext: ApplicationContext
) : PagingSource<Int, SectionItem>() {
    private val database: AppsDatabase = Application.provide(appContext).database

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SectionItem> {
        val installed = InstalledTaskWorker(appContext, sortId, titleFilter).run()
        val allInstalledPackageNames = installed.map { it.packageName }
        val watchingPackages = database.apps().loadRowIds(allInstalledPackageNames).associateBy({ it.packageName }, { it.rowId })
        val items: List<SectionItem> = allInstalledPackageNames
                .asSequence()
                .map { appContext.packageManager.packageToApp(watchingPackages[it] ?: -1, it) }
                .map { app -> OnDeviceItem(AppListItem(app, "", noNewDetails = false, recentFlag = false), showSelection) }
                .toList()

        return LoadResult.Page(
                data = items,
                prevKey = null,
                nextKey = null
        )
    }

}