package com.anod.appwatcher.model

import com.anod.appwatcher.database.entities.AppListItem
import info.anodsplace.framework.content.InstalledApps
import info.anodsplace.ktx.hashCodeOf

/**
 * @author alex
 * *
 * @date 8/4/14.
 */

interface AppListFilter {
    val filterId: Int
    fun filterRecord(item: AppListItem): Boolean

    class All : AppListFilter {
        override val filterId = Filters.ALL
        override fun filterRecord(item: AppListItem): Boolean = false
        override fun hashCode(): Int = hashCodeOf(filterId)
        override fun equals(other: Any?): Boolean = (other as? Installed)?.hashCode() == hashCode()
    }

    class Installed(private val installedApps: InstalledApps) : AppListFilter {
        override val filterId = Filters.INSTALLED
        override fun filterRecord(item: AppListItem): Boolean {
            val packageName = item.app.packageName
            val installedInfo = installedApps.packageInfo(packageName)
            return !installedInfo.isInstalled
        }

        override fun hashCode(): Int = hashCodeOf(filterId)
        override fun equals(other: Any?): Boolean = (other as? Installed)?.hashCode() == hashCode()
    }

    class Uninstalled(private val installedApps: InstalledApps) : AppListFilter {
        override val filterId = Filters.UNINSTALLED
        override fun filterRecord(item: AppListItem): Boolean {
            val packageName = item.app.packageName
            val installedInfo = installedApps.packageInfo(packageName)
            return installedInfo.isInstalled
        }

        override fun hashCode(): Int = hashCodeOf(filterId)
        override fun equals(other: Any?): Boolean = (other as? Installed)?.hashCode() == hashCode()
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

        override fun hashCode(): Int = hashCodeOf(filterId)
        override fun equals(other: Any?): Boolean = (other as? Installed)?.hashCode() == hashCode()
    }
}