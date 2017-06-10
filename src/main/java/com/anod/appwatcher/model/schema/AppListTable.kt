package com.anod.appwatcher.model.schema

import android.content.ContentValues
import android.provider.BaseColumns

import com.anod.appwatcher.model.AppInfo

class AppListTable {

    class Columns : BaseColumns {
        companion object {
            val APPID = "app_id"
            val KEY_PACKAGE = "package"
            val KEY_VERSION_NUMBER = "ver_num"
            val KEY_VERSION_NAME = "ver_name"
            val KEY_TITLE = "title"
            val KEY_CREATOR = "creator"
            val KEY_ICON_CACHE = "icon"
            val KEY_ICON_URL = "iconUrl"
            val KEY_STATUS = "status"
            val KEY_REFRESH_TIMESTAMP = "update_date"
            val KEY_PRICE_TEXT = "price_text"
            val KEY_PRICE_CURRENCY = "price_currency"
            val KEY_PRICE_MICROS = "price_micros"
            val KEY_UPLOAD_DATE = "upload_date"
            val KEY_DETAILS_URL = "details_url"
            val KEY_APP_TYPE = "app_type"
            val KEY_SYNC_VERSION = "sync_version"
        }
    }

    object TableColumns {
        val _ID = AppListTable.TABLE_NAME + "." + BaseColumns._ID
        val APPID = AppListTable.TABLE_NAME + ".app_id"
    }

    object Projection {
        val _ID = 0
        val APPID = 1
        val PACKAGE = 2
        val VERSION_NUMBER = 3
        val VERSION_NAME = 4
        val TITLE = 5
        val CREATOR = 6
        val STATUS = 7
        val REFRESH_TIME = 8
        val PRICE_TEXT = 9
        val PRICE_CURRENCY = 10
        val PRICE_MICROS = 11
        val UPLOAD_DATE = 12
        val DETAILS_URL = 13
        val ICON_URL = 14
        val APP_TYPE = 15
        val SYNC_VERSION = 16
    }

    companion object {

        val TABLE_NAME = "app_list"

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
