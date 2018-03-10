package com.anod.appwatcher.model

import android.database.Cursor
import com.anod.appwatcher.model.schema.AppListTable
import info.anodsplace.framework.content.InstalledApps
import info.anodsplace.framework.database.FilterCursor

/**
 * @author alex
 * *
 * @date 8/4/14.
 */

interface CountableFilter {
    val newCount: Int
    val updatableNewCount: Int
    val recentlyUpdatedCount: Int

    fun resetNewCount()
}

interface AppListFilter: FilterCursor.CursorFilter, CountableFilter

class AppListFilterInclusion(private val inclusion: Inclusion, private val installedApps: InstalledApps) : AppListFilter {

    interface Inclusion {
        fun include(versionCode: Int, installedInfo: InstalledApps.Info): Boolean
    }

    class All : Inclusion {
        override fun include(versionCode: Int, installedInfo: InstalledApps.Info): Boolean {
            return true
        }
    }

    class Installed : Inclusion {
        override fun include(versionCode: Int, installedInfo: InstalledApps.Info): Boolean {
            return installedInfo.isInstalled
        }
    }

    class Uninstalled : Inclusion {
        override fun include(versionCode: Int, installedInfo: InstalledApps.Info): Boolean {
            return !installedInfo.isInstalled
        }
    }

    class Updatable : Inclusion {
        override fun include(versionCode: Int, installedInfo: InstalledApps.Info): Boolean {
            return installedInfo.isInstalled && installedInfo.isUpdatable(versionCode)
        }
    }

    override var newCount: Int = 0
        private set
    override var updatableNewCount: Int = 0
        private set
    override var recentlyUpdatedCount: Int = 0
        private set

    override fun filterRecord(cursor: Cursor): Boolean {
        val packageName = cursor.getString(AppListTable.Projection.packageName)
        val status = cursor.getInt(AppListTable.Projection.status)
        val versionCode = cursor.getInt(AppListTable.Projection.versionNumber)

        val installedInfo = installedApps.packageInfo(packageName)

        if (!inclusion.include(versionCode, installedInfo)) {
            return true
        }

        if (status == AppInfoMetadata.STATUS_UPDATED) {
            newCount++
            if (installedInfo.isUpdatable(versionCode)) {
                updatableNewCount++
            }
        } else if (status == AppInfoMetadata.STATUS_NORMAL) {
            val isRecent = cursor.getInt(AppListTable.Projection.recentFlag) == 1
            if (isRecent) {
                recentlyUpdatedCount++
            }
        }
        return false
    }

    override fun resetNewCount() {
        newCount = 0
        updatableNewCount = 0
        recentlyUpdatedCount = 0
    }
}
