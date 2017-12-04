package com.anod.appwatcher.model

import android.database.Cursor
import com.anod.appwatcher.model.schema.AppListTable
import info.anodsplace.appwatcher.framework.FilterCursor
import info.anodsplace.appwatcher.framework.InstalledApps

/**
 * @author alex
 * *
 * @date 8/4/14.
 */

interface CountableFilter {
    val newCount: Int
    val updatableNewCount: Int

    fun resetNewCount()
}

interface AppListFilterInclusion {
    fun include(versionCode: Int, installedInfo: InstalledApps.Info): Boolean
}

class AppListFilterInclusionInstalled : AppListFilterInclusion {
    override fun include(versionCode: Int, installedInfo: InstalledApps.Info): Boolean {
        return installedInfo.isInstalled
    }
}

class AppListFilterInclusionUninstalled : AppListFilterInclusion {
    override fun include(versionCode: Int, installedInfo: InstalledApps.Info): Boolean {
        return !installedInfo.isInstalled
    }
}

class AppListFilterInclusionUpdatable : AppListFilterInclusion {
    override fun include(versionCode: Int, installedInfo: InstalledApps.Info): Boolean {
        return installedInfo.isInstalled && installedInfo.isUpdatable(versionCode)
    }
}

class AppListFilter(private val inclusion: AppListFilterInclusion, private val installedApps: InstalledApps) : FilterCursor.CursorFilter, CountableFilter {
    override var newCount: Int = 0
        private set
    override var updatableNewCount: Int = 0
        private set

    override fun filterRecord(cursor: Cursor): Boolean {
        val packageName = cursor.getString(AppListTable.Projection.packageName)
        val status = cursor.getInt(AppListTable.Projection.status)
        val versionCode = cursor.getInt(AppListTable.Projection.versionNumber)

        val installedInfo = installedApps.getInfo(packageName)

        if (!inclusion.include(versionCode, installedInfo)) {
            return true
        }

        if (status == AppInfoMetadata.STATUS_UPDATED) {
            newCount++
            if (installedInfo.isUpdatable(versionCode)) {
                updatableNewCount++
            }
        }
        return false
    }

    override fun resetNewCount() {
        newCount = 0
        updatableNewCount = 0
    }
}
