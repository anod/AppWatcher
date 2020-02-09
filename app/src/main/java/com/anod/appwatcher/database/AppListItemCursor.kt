package com.anod.appwatcher.database

import android.database.Cursor
import android.provider.BaseColumns
import androidx.room.util.CursorUtil
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.AppListItem
import com.anod.appwatcher.database.entities.Price
import info.anodsplace.framework.database.CursorIterator

/**
 * @author alex
 */
class AppListItemCursor(cursor: Cursor?) : CursorIterator<AppListItem>(cursor) {

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
        var changeDetails = 17
        var newNewDetails = 18
        var recentFlag = 19
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
            projection.changeDetails = CursorUtil.getColumnIndexOrThrow(cursor, ChangelogTable.Columns.details)
            projection.newNewDetails = CursorUtil.getColumnIndexOrThrow(cursor, ChangelogTable.Columns.noNewDetails)
            projection.recentFlag = CursorUtil.getColumnIndexOrThrow(cursor, AppListTable.Columns.recentFlag)
        }
    }

    override val current: AppListItem
        get() = AppListItem(
                app = App(
                        rowId = getInt(projection.rowId),
                        appId = getString(projection.appId),
                        packageName = getString(projection.packageName),
                        versionNumber = getInt(projection.versionNumber),
                        versionName = getString(projection.versionName),
                        title = getString(projection.title),
                        creator = getString(projection.creator),
                        iconUrl = getString(projection.iconUrl),
                        status = getInt(projection.status),
                        uploadDate = getString(projection.uploadDate),
                        price = Price(
                                getString(projection.priceText),
                                getString(projection.priceCurrency),
                                getInt(projection.priceMicros)
                        ),
                        detailsUrl = getString(projection.detailsUrl),
                        uploadTime = getLong(projection.uploadTime),
                        appType = getString(projection.appType),
                        updateTime = getLong(projection.refreshTime)
                ),
                changeDetails = getString(projection.changeDetails, ""),
                noNewDetails = getInt(projection.newNewDetails) == 1,
                recentFlag = getInt(projection.recentFlag) == 1
        )
}
