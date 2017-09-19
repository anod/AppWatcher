package com.anod.appwatcher.content

import android.database.Cursor

import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.model.schema.AppListTable

/**
 * @author alex
 */
class AppListCursor(cursor: Cursor?) : CursorIterator<AppInfo>(cursor) {

    override fun next(): AppInfo {
        return this.appInfo
    }

    val appInfo: AppInfo
        get() = AppInfo(
            getInt(AppListTable.Projection._ID),
            getString(AppListTable.Projection.appId),
            getString(AppListTable.Projection.packageName),
            getInt(AppListTable.Projection.versionNumber),
            getString(AppListTable.Projection.versionName),
            getString(AppListTable.Projection.title),
            getString(AppListTable.Projection.creator),
            getString(AppListTable.Projection.iconUrl),
            getInt(AppListTable.Projection.status),
            getString(AppListTable.Projection.uploadDate),
            getString(AppListTable.Projection.priceText),
            getString(AppListTable.Projection.priceCurrency),
            getInt(AppListTable.Projection.priceMicros),
            getString(AppListTable.Projection.detailsUrl),
            getLong(AppListTable.Projection.refreshTime),
            getString(AppListTable.Projection.appType),
            getInt(AppListTable.Projection.syncVersion)
        )

}
