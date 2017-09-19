package com.anod.appwatcher.model.schema

import android.content.ContentValues
import android.provider.BaseColumns
import com.anod.appwatcher.model.AppInfo

class AppListTable {

    class Columns : BaseColumns {
        companion object {
            const val appId = "app_id"
            const val packageName = "package"
            const val versionNumber = "ver_num"
            const val versionName = "ver_name"
            const val title = "title"
            const val creator = "creator"
            const val iconCache = "icon"
            const val iconUrl = "iconUrl"
            const val status = "status"
            const val refreshTimestamp = "update_date"
            const val priceText = "price_text"
            const val priceCurrency = "price_currency"
            const val priceMicros = "price_micros"
            const val uploadDate = "upload_date"
            const val detailsUrl = "details_url"
            const val appType = "app_type"
            const val syncVersion = "sync_version"
        }
    }

    object TableColumns {
        val _ID = AppListTable.table + "." + BaseColumns._ID
        val appId = AppListTable.table + ".app_id"
    }

    object Projection {
        const val _ID = 0
        const val appId = 1
        const val packageName = 2
        const val versionNumber = 3
        const val versionName = 4
        const val title = 5
        const val creator = 6
        const val status = 7
        const val refreshTime = 8
        const val priceText = 9
        const val priceCurrency = 10
        const val priceMicros = 11
        const val uploadDate = 12
        const val detailsUrl = 13
        const val iconUrl = 14
        const val appType = 15
        const val syncVersion = 16
    }

    companion object {

        const val table = "app_list"

        val projection = arrayOf(
                TableColumns._ID,
                TableColumns.appId,
                Columns.packageName,
                Columns.versionNumber,
                Columns.versionName,
                Columns.title,
                Columns.creator,
                Columns.status,
                Columns.refreshTimestamp,
                Columns.priceText,
                Columns.priceCurrency,
                Columns.priceMicros,
                Columns.uploadDate,
                Columns.detailsUrl,
                Columns.iconUrl,
                Columns.appType,
                Columns.syncVersion)

        val sqlCreate =
                "CREATE TABLE " + table + " (" +
                    BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Columns.appId + " TEXT not null," +
                    Columns.packageName + " TEXT not null," +
                    Columns.versionNumber + " INTEGER," +
                    Columns.versionName + " TEXT," +
                    Columns.title + " TEXT not null," +
                    Columns.creator + " TEXT," +
                    Columns.status + " INTEGER," +
                    Columns.refreshTimestamp + " INTEGER," +
                    Columns.priceText + " TEXT," +
                    Columns.priceCurrency + " TEXT," +
                    Columns.priceMicros + " INTEGER," +
                    Columns.uploadDate + " TEXT," +
                    Columns.detailsUrl + " TEXT," +
                    Columns.iconUrl + " TEXT," +
                    Columns.appType + " TEXT," +
                    Columns.syncVersion + " INTEGER" +
                    ") "
    }
}

val AppInfo.contentValues: ContentValues
    get() {
        val values = ContentValues()

        if (rowId > 0) {
            values.put(BaseColumns._ID, rowId)
        }
        values.put(AppListTable.Columns.appId, appId)
        values.put(AppListTable.Columns.packageName, packageName)
        values.put(AppListTable.Columns.title, title)
        values.put(AppListTable.Columns.versionNumber, versionNumber)
        values.put(AppListTable.Columns.versionName, versionName)
        values.put(AppListTable.Columns.creator, creator)
        values.put(AppListTable.Columns.status, status)
        values.put(AppListTable.Columns.uploadDate, uploadDate)

        values.put(AppListTable.Columns.priceText, priceText)
        values.put(AppListTable.Columns.priceCurrency, priceCur)
        values.put(AppListTable.Columns.priceMicros, priceMicros)

        values.put(AppListTable.Columns.detailsUrl, detailsUrl)

        values.put(AppListTable.Columns.iconUrl, iconUrl)
        values.put(AppListTable.Columns.refreshTimestamp, refreshTime)

        values.put(AppListTable.Columns.appType, appType)
        values.put(AppListTable.Columns.syncVersion, syncVersion)
        return values
    }
