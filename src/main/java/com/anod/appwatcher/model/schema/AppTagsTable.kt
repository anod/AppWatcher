package com.anod.appwatcher.model.schema

import android.content.ContentValues
import android.provider.BaseColumns
import com.anod.appwatcher.model.AppTag

/**
 * @author alex
 * *
 * @date 2015-03-01
 */
class AppTagsTable {

    class Columns : BaseColumns {
        companion object {
            const val appId = "app_id"
            const val tagId = "tags_id"
        }
    }

    object TableColumns {
        val _ID = AppTagsTable.table + "." + BaseColumns._ID
        val appId = AppTagsTable.table + ".app_id"
        val tagId = AppTagsTable.table + ".tags_id"
    }

    object Projection {
        const val _ID = 0
        const val appId = 1
        const val tagId = 2
    }

    companion object {

        const val table = "app_tags"

        val projection = arrayOf(TableColumns._ID, TableColumns.appId, TableColumns.tagId)

        val sqlCreate =
                "CREATE TABLE " + table + " (" +
                        BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        AppTagsTable.Columns.appId + " TEXT not null," +
                        AppTagsTable.Columns.tagId + " INTEGER" +
                        ") "
    }
}

val AppTag.contentValues: ContentValues
    get() {
        val values = android.content.ContentValues()
        values.put(AppTagsTable.Columns.appId, appId)
        values.put(AppTagsTable.Columns.tagId, tagId)
        return values
    }
