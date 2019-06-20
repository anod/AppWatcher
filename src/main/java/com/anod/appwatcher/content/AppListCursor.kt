package com.anod.appwatcher.content

import android.database.Cursor
import android.provider.BaseColumns
import androidx.room.util.CursorUtil

import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.database.AppListTable
import info.anodsplace.framework.database.CursorIterator

/**
 * @author alex
 */
class AppListCursor(cursor: Cursor?) : CursorIterator<AppInfo>(cursor) {

    private class Projection {
        var rowId = 0
        var appId = 1
        var packageName = 2
        var versionNumber = 3
        var versionName = 4
        var title = 5
        var creator = 6
        var status = 7
        var uploadTime = 8
        var priceText = 9
        var priceCurrency = 10
        var priceMicros = 11
        var uploadDate = 12
        var detailsUrl = 13
        var iconUrl = 14
        var appType = 15
        var refreshTime = 16
        var recentFlag = 17
    }

    private val projection = Projection()

    init {
        if (cursor != null) {
            projection.rowId = CursorUtil.getColumnIndexOrThrow(cursor, BaseColumns._ID)
            projection.appId = CursorUtil.getColumnIndexOrThrow(cursor, AppListTable.Columns.appId)
            projection.packageName = CursorUtil.getColumnIndexOrThrow(cursor, AppListTable.Columns.packageName)
            projection.versionNumber = CursorUtil.getColumnIndexOrThrow(cursor, AppListTable.Columns.versionNumber)
            projection.versionName = CursorUtil.getColumnIndexOrThrow(cursor, AppListTable.Columns.versionName)
            projection.title = CursorUtil.getColumnIndexOrThrow(cursor, AppListTable.Columns.title)
            projection.creator = CursorUtil.getColumnIndexOrThrow(cursor, AppListTable.Columns.creator)
            projection.iconUrl = CursorUtil.getColumnIndexOrThrow(cursor, AppListTable.Columns.iconUrl)
            projection.status = CursorUtil.getColumnIndexOrThrow(cursor, AppListTable.Columns.status)
            projection.uploadDate = CursorUtil.getColumnIndexOrThrow(cursor, AppListTable.Columns.uploadDate)
            projection.detailsUrl = CursorUtil.getColumnIndexOrThrow(cursor, AppListTable.Columns.detailsUrl)
            projection.uploadTime = CursorUtil.getColumnIndexOrThrow(cursor, AppListTable.Columns.uploadTimestamp)
            projection.appType = CursorUtil.getColumnIndexOrThrow(cursor, AppListTable.Columns.appType)
            projection.refreshTime = CursorUtil.getColumnIndexOrThrow(cursor, AppListTable.Columns.updateTimestamp)
            projection.priceText = CursorUtil.getColumnIndexOrThrow(cursor, AppListTable.Columns.priceText)
            projection.priceCurrency = CursorUtil.getColumnIndexOrThrow(cursor, AppListTable.Columns.priceCurrency)
            projection.priceMicros = CursorUtil.getColumnIndexOrThrow(cursor, AppListTable.Columns.priceMicros)
            projection.recentFlag = CursorUtil.getColumnIndexOrThrow(cursor, AppListTable.Columns.recentFlag)
        }
    }


    override val current: AppInfo
        get() = AppInfo(
            getInt(projection.rowId),
            getString(projection.appId),
            getString(projection.packageName),
            getInt(projection.versionNumber),
            getString(projection.versionName),
            getString(projection.title),
            getString(projection.creator),
            getString(projection.iconUrl),
            getInt(projection.status),
            getString(projection.uploadDate),
            getString(projection.priceText),
            getString(projection.priceCurrency),
            getInt(projection.priceMicros),
            getString(projection.detailsUrl),
            getLong(projection.uploadTime),
            getString(projection.appType),
            getLong(projection.refreshTime),
            getInt(projection.recentFlag) == 1
        )

}
