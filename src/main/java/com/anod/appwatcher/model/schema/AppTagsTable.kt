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
            const val APPID = "app_id"
            const val TAGID = "tags_id"
        }
    }

    object TableColumns {
        val _ID = AppTagsTable.table + "." + BaseColumns._ID
        val APPID = AppTagsTable.table + ".app_id"
        val TAGID = AppTagsTable.table + ".tags_id"
    }

    object Projection {
        const val _ID = 0
        const val APPID = 1
        const val TAGID = 2
    }

    companion object {

        const val table = "app_tags"

        val PROJECTION = arrayOf(TableColumns._ID, TableColumns.APPID, TableColumns.TAGID)

        val sqlCreate =
                "CREATE TABLE " + table + " (" +
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
