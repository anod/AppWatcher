package com.anod.appwatcher.model.schema

import android.content.ContentValues
import android.provider.BaseColumns

import com.anod.appwatcher.model.AppInfo

class AppListTable {

    class Columns : BaseColumns {
        companion object {
            const val APPID = "app_id"
            const val KEY_PACKAGE = "package"
            const val KEY_VERSION_NUMBER = "ver_num"
            const val KEY_VERSION_NAME = "ver_name"
            const val KEY_TITLE = "title"
            const val KEY_CREATOR = "creator"
            const val KEY_ICON_CACHE = "icon"
            const val KEY_ICON_URL = "iconUrl"
            const val KEY_STATUS = "status"
            const val KEY_REFRESH_TIMESTAMP = "update_date"
            const val KEY_PRICE_TEXT = "price_text"
            const val KEY_PRICE_CURRENCY = "price_currency"
            const val KEY_PRICE_MICROS = "price_micros"
            const val KEY_UPLOAD_DATE = "upload_date"
            const val KEY_DETAILS_URL = "details_url"
            const val KEY_APP_TYPE = "app_type"
            const val KEY_SYNC_VERSION = "sync_version"
        }
    }

    object TableColumns {
        val _ID = AppListTable.TABLE_NAME + "." + BaseColumns._ID
        val APPID = AppListTable.TABLE_NAME + ".app_id"
    }

    object Projection {
        const val _ID = 0
        const val APPID = 1
        const val PACKAGE = 2
        const val VERSION_NUMBER = 3
        const val VERSION_NAME = 4
        const val TITLE = 5
        const val CREATOR = 6
        const val STATUS = 7
        const val REFRESH_TIME = 8
        const val PRICE_TEXT = 9
        const val PRICE_CURRENCY = 10
        const val PRICE_MICROS = 11
        const val UPLOAD_DATE = 12
        const val DETAILS_URL = 13
        const val ICON_URL = 14
        const val APP_TYPE = 15
        const val SYNC_VERSION = 16
    }

    companion object {

        const val TABLE_NAME = "app_list"

        val PROJECTION = arrayOf(
                TableColumns._ID,
                TableColumns.APPID,
                Columns.KEY_PACKAGE,
                Columns.KEY_VERSION_NUMBER,
                Columns.KEY_VERSION_NAME,
                Columns.KEY_TITLE,
                Columns.KEY_CREATOR,
                Columns.KEY_STATUS,
                Columns.KEY_REFRESH_TIMESTAMP,
                Columns.KEY_PRICE_TEXT,
                Columns.KEY_PRICE_CURRENCY,
                Columns.KEY_PRICE_MICROS,
                Columns.KEY_UPLOAD_DATE,
                Columns.KEY_DETAILS_URL,
                Columns.KEY_ICON_URL,
                Columns.KEY_APP_TYPE,
                Columns.KEY_SYNC_VERSION)

        val TABLE_CREATE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        Columns.APPID + " TEXT not null," +
                        Columns.KEY_PACKAGE + " TEXT not null," +
                        Columns.KEY_VERSION_NUMBER + " INTEGER," +
                        Columns.KEY_VERSION_NAME + " TEXT," +
                        Columns.KEY_TITLE + " TEXT not null," +
                        Columns.KEY_CREATOR + " TEXT," +
                        Columns.KEY_STATUS + " INTEGER," +
                        Columns.KEY_REFRESH_TIMESTAMP + " INTEGER," +
                        Columns.KEY_PRICE_TEXT + " TEXT," +
                        Columns.KEY_PRICE_CURRENCY + " TEXT," +
                        Columns.KEY_PRICE_MICROS + " INTEGER," +
                        Columns.KEY_UPLOAD_DATE + " TEXT," +
                        Columns.KEY_DETAILS_URL + " TEXT," +
                        Columns.KEY_ICON_URL + " TEXT," +
                        Columns.KEY_APP_TYPE + " TEXT," +
                        Columns.KEY_SYNC_VERSION + " INTEGER" +
                        ") "

        /**
         * @return Content values for app
         */
        fun createContentValues(app: AppInfo): ContentValues {
            val values = ContentValues()

            values.put(AppListTable.Columns.APPID, app.appId)
            values.put(AppListTable.Columns.KEY_PACKAGE, app.packageName)
            values.put(AppListTable.Columns.KEY_TITLE, app.title)
            values.put(AppListTable.Columns.KEY_VERSION_NUMBER, app.versionNumber)
            values.put(AppListTable.Columns.KEY_VERSION_NAME, app.versionName)
            values.put(AppListTable.Columns.KEY_CREATOR, app.creator)
            values.put(AppListTable.Columns.KEY_STATUS, app.status)
            values.put(AppListTable.Columns.KEY_UPLOAD_DATE, app.uploadDate)

            values.put(AppListTable.Columns.KEY_PRICE_TEXT, app.priceText)
            values.put(AppListTable.Columns.KEY_PRICE_CURRENCY, app.priceCur)
            values.put(AppListTable.Columns.KEY_PRICE_MICROS, app.priceMicros)

            values.put(AppListTable.Columns.KEY_DETAILS_URL, app.detailsUrl)

            values.put(AppListTable.Columns.KEY_ICON_URL, app.iconUrl)
            values.put(AppListTable.Columns.KEY_REFRESH_TIMESTAMP, app.refreshTime)

            values.put(AppListTable.Columns.KEY_APP_TYPE, app.appType)
            values.put(AppListTable.Columns.KEY_SYNC_VERSION, app.syncVersion)
            return values
        }
    }
}
