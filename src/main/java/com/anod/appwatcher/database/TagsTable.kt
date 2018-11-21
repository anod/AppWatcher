package com.anod.appwatcher.database

import androidx.room.Dao
import android.content.ContentValues
import android.provider.BaseColumns

import com.anod.appwatcher.database.entities.Tag

/**
 * @author alex
 * *
 * @date 2015-03-01
 */
@Dao
interface TagsTable {

    class Columns : BaseColumns {
        companion object {
            const val name = "name"
            const val color = "color"
        }
    }

    object TableColumns {
        const val _ID = table + "." + BaseColumns._ID
        const val name = table + ".name"
        const val color = table + ".color"
    }

    object Projection {
        const val _ID = 0
        const val name = 1
        const val color = 2
    }

    companion object {
        const val table = "tags"
        val projection = arrayOf(TableColumns._ID, TableColumns.name, TableColumns.color)
        const val sqlCreate =
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