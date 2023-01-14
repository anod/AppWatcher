package com.anod.appwatcher.database

import android.database.Cursor
import android.provider.BaseColumns
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
            projection.rowId = cursor.getColumnIndexOrThrow(BaseColumns._ID)
            projection.appId = cursor.getColumnIndexOrThrow(AppListTable.Columns.appId)
            projection.packageName = cursor.getColumnIndexOrThrow(AppListTable.Columns.packageName)
            projection.versionNumber = cursor.getColumnIndexOrThrow(AppListTable.Columns.versionNumber)
            projection.versionName = cursor.getColumnIndexOrThrow(AppListTable.Columns.versionName)
            projection.title = cursor.getColumnIndexOrThrow(AppListTable.Columns.title)
            projection.creator = cursor.getColumnIndexOrThrow(AppListTable.Columns.creator)
            projection.iconUrl = cursor.getColumnIndexOrThrow(AppListTable.Columns.iconUrl)
            projection.status = cursor.getColumnIndexOrThrow(AppListTable.Columns.status)
            projection.uploadDate = cursor.getColumnIndexOrThrow(AppListTable.Columns.uploadDate)
            projection.detailsUrl = cursor.getColumnIndexOrThrow(AppListTable.Columns.detailsUrl)
            projection.uploadTime = cursor.getColumnIndexOrThrow(AppListTable.Columns.uploadTimestamp)
            projection.appType = cursor.getColumnIndexOrThrow(AppListTable.Columns.appType)
            projection.refreshTime = cursor.getColumnIndexOrThrow(AppListTable.Columns.updateTimestamp)
            projection.priceText = cursor.getColumnIndexOrThrow(AppListTable.Columns.priceText)
            projection.priceCurrency = cursor.getColumnIndexOrThrow(AppListTable.Columns.priceCurrency)
            projection.priceMicros = cursor.getColumnIndexOrThrow(AppListTable.Columns.priceMicros)
            projection.changeDetails = cursor.getColumnIndexOrThrow(ChangelogTable.Columns.details)
            projection.newNewDetails = cursor.getColumnIndexOrThrow(ChangelogTable.Columns.noNewDetails)
            projection.recentFlag = cursor.getColumnIndexOrThrow(AppListTable.Columns.recentFlag)
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
