package com.anod.appwatcher.watchlist

import android.content.pm.PackageManager
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.packageToApp
import info.anodsplace.framework.content.getRecentlyInstalled
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecentlyInstalledPackagesLoader(private val packageManager: PackageManager, private val database: AppsDatabase) {

    suspend fun load(): List<App> {
        val packages = withContext(Dispatchers.Default) {
            packageManager.getRecentlyInstalled()
                    .take(WatchListViewModel.recentlyInstalledViews)
                    .map { it.name }
        }
        return if (packages.isNotEmpty()) {
            val watchingPackages = database.apps().loadRowIds(packages).associateBy({ it.packageName }, { it.rowId })
            withContext(Dispatchers.Default) {
                packages.map {
                    packageManager.packageToApp(rowId = watchingPackages[it] ?: -1, packageName = it)
                }
            }
        } else {
            emptyList()
        }
    }
}