package com.anod.appwatcher.model

import com.anod.appwatcher.database.entities.AppListItem
import info.anodsplace.framework.content.InstalledApps

/**
 * @author alex
 * *
 * @date 8/4/14.
 */

interface AppListFilter {
    val filterId: Int
    fun filterRecord(item: AppListItem): Boolean

    class All : AppListFilter {
        override val filterId = Filters.TAB_ALL
        override fun filterRecord(item: AppListItem): Boolean {
            return false
        }
    }

    class Installed(private val installedApps: InstalledApps) : AppListFilter {
        override val filterId = Filters.INSTALLED
        override fun filterRecord(item: AppListItem): Boolean {
            val packageName = item.app.packageName
            val installedInfo = installedApps.packageInfo(packageName)
            return !installedInfo.isInstalled
        }
    }

    class Uninstalled(private val installedApps: InstalledApps) : AppListFilter {
        override val filterId = Filters.UNINSTALLED
        override fun filterRecord(item: AppListItem): Boolean {
            val packageName = item.app.packageName
            val installedInfo = installedApps.packageInfo(packageName)
            return installedInfo.isInstalled
        }
    }

    class Updatable(private val installedApps: InstalledApps) : AppListFilter {
        override val filterId = Filters.UPDATABLE
        override fun filterRecord(item: AppListItem): Boolean {
            val packageName = item.app.packageName
            val installedInfo = installedApps.packageInfo(packageName)
            val versionCode = item.app.versionNumber
            val updatable = installedInfo.isInstalled && installedInfo.isUpdatable(versionCode)
            return !updatable
        }
    }

}