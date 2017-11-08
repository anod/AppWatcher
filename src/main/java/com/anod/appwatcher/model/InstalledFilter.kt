package com.anod.appwatcher.model

import android.database.Cursor
import com.anod.appwatcher.model.schema.AppListTable
import com.anod.appwatcher.framework.FilterCursor
import com.anod.appwatcher.framework.InstalledApps

/**
 * @author alex
 * *
 * @date 8/4/14.
 */
class InstalledFilter(private val mIncludeInstalled: Boolean, private val InstalledApps: InstalledApps) : FilterCursor.CursorFilter {
    internal var newCount: Int = 0
        private set
    internal var updatableNewCount: Int = 0
        private set

    override fun filterRecord(cursor: Cursor): Boolean {
        val packageName = cursor.getString(AppListTable.Projection.packageName)
        val status = cursor.getInt(AppListTable.Projection.status)
        val versionCode = cursor.getInt(AppListTable.Projection.versionNumber)

        val installedInfo = InstalledApps.getInfo(packageName)
        val installed = installedInfo.isInstalled

        if (mIncludeInstalled && !installed) {
            return true
        }

        if (!mIncludeInstalled && installed) {
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

    internal fun resetNewCount() {
        newCount = 0
        updatableNewCount = 0
    }
}
