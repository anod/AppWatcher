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
            const val name = "name"
            const val color = "color"
        }
    }

    object TableColumns {
        val _ID = TagsTable.table + "." + BaseColumns._ID
        val name = TagsTable.table + ".name"
        val color = TagsTable.table + ".color"
    }

    object Projection {
        const val _ID = 0
        const val name = 1
        const val color = 2
    }

    companion object {
        const val table = "tags"
        val projection = arrayOf(TableColumns._ID, TableColumns.name, TableColumns.color)
        val sqlCreate =
                "CREATE TABLE " + table + " (" +
                        BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        Columns.name + " TEXT not null," +
                        Columns.color + " INTEGER" +
                        ") "
    }
}


val Tag.contentValues: ContentValues
    get() {
        val values = ContentValues()
        if (id > 0) {
            values.put(BaseColumns._ID, id)
        }
        values.put(TagsTable.Columns.name, name)
        values.put(TagsTable.Columns.color, color)
        return values
    }