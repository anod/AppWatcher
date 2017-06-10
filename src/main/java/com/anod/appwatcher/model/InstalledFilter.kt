package com.anod.appwatcher.model

import android.database.Cursor
import com.anod.appwatcher.model.schema.AppListTable
import com.anod.appwatcher.utils.FilterCursorWrapper
import com.anod.appwatcher.utils.InstalledAppsProvider

/**
 * @author alex
 * *
 * @date 8/4/14.
 */
class InstalledFilter(private val mIncludeInstalled: Boolean, private val mInstalledAppsProvider: InstalledAppsProvider) : FilterCursorWrapper.CursorFilter {
    internal var newCount: Int = 0
        private set
    internal var updatableNewCount: Int = 0
        private set

    override fun filterRecord(cursor: Cursor): Boolean {
        val packageName = cursor.getString(AppListTable.Projection.PACKAGE)
        val status = cursor.getInt(AppListTable.Projection.STATUS)
        val versionCode = cursor.getInt(AppListTable.Projection.VERSION_NUMBER)

        val installedInfo = mInstalledAppsProvider.getInfo(packageName)
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
