package com.anod.appwatcher.database

import android.arch.persistence.room.Dao
import android.content.ContentValues
import android.provider.BaseColumns
import com.anod.appwatcher.database.entities.AppTag

/**
 * @author alex
 * *
 * @date 2015-03-01
 */
@Dao
interface AppTagsTable {

    class Columns : BaseColumns {
        companion object {
            const val appId = "app_id"
            const val tagId = "tags_id"
        }
    }

    object TableColumns {
        const val _ID = table + "." + BaseColumns._ID
        const val appId = table + ".app_id"
        const val tagId = table + ".tags_id"
    }

    object Projection {
        const val _ID = 0
        const val appId = 1
        const val tagId = 2
    }

    companion object {

        const val table = "app_tags"

        val projection = arrayOf(TableColumns._ID, TableColumns.appId, TableColumns.tagId)

        const val sqlCreate =
                "CREATE TABLE " + table + " (" +
                        BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        Columns.appId + " TEXT not null," +
                        Columns.tagId + " INTEGER" +
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
