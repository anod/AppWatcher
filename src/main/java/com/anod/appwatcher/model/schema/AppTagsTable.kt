package com.anod.appwatcher.model.schema

import android.content.ContentValues
import android.provider.BaseColumns

/**
 * @author alex
 * *
 * @date 2015-03-01
 */
class AppTagsTable {

    class Columns : BaseColumns {
        companion object {
            val APPID = "app_id"
            val TAGID = "tags_id"
        }
    }

    object TableColumns {
        val _ID = AppTagsTable.TABLE_NAME + "." + BaseColumns._ID
        val APPID = AppTagsTable.TABLE_NAME + ".app_id"
        val TAGID = AppTagsTable.TABLE_NAME + ".tags_id"
    }

    object Projection {
        val _ID = 0
        val APPID = 1
        val TAGID = 2
    }

    companion object {

        const val TABLE_NAME = "app_tags"

        val PROJECTION = arrayOf(TableColumns._ID, TableColumns.APPID, TableColumns.TAGID)

        val TABLE_CREATE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        AppTagsTable.Columns.APPID + " TEXT not null," +
                        AppTagsTable.Columns.TAGID + " INTEGER" +
                        ") "

        fun createContentValues(appId: String, tagId: Int): ContentValues {
            val values = ContentValues()
            values.put(Columns.APPID, appId)
            values.put(Columns.TAGID, tagId)
            return values
        }
    }
}
