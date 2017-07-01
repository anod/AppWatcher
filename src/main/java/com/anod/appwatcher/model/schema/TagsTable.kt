package com.anod.appwatcher.model.schema

import android.content.ContentValues
import android.provider.BaseColumns

import com.anod.appwatcher.model.Tag

/**
 * @author alex
 * *
 * @date 2015-03-01
 */
class TagsTable {

    class Columns : BaseColumns {
        companion object {
            const val NAME = "name"
            const val COLOR = "color"
        }
    }

    object TableColumns {
        val _ID = TagsTable.TABLE_NAME + "." + BaseColumns._ID
        val NAME = TagsTable.TABLE_NAME + ".name"
        val COLOR = TagsTable.TABLE_NAME + ".color"
    }

    object Projection {
        const val _ID = 0
        const val NAME = 1
        const val COLOR = 2
    }

    companion object {
        const val TABLE_NAME = "tags"
        val PROJECTION = arrayOf(TableColumns._ID, TableColumns.NAME, TableColumns.COLOR)
        val TABLE_CREATE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        Columns.NAME + " TEXT not null," +
                        Columns.COLOR + " INTEGER" +
                        ") "

        fun createContentValues(tag: Tag): ContentValues {
            val values = ContentValues()
            if (tag.id > 0) {
                values.put(BaseColumns._ID, tag.id)
            }
            values.put(Columns.NAME, tag.name)
            values.put(Columns.COLOR, tag.color)
            return values
        }
    }

}
