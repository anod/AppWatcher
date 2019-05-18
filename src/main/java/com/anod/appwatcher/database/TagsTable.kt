package com.anod.appwatcher.database

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.provider.BaseColumns
import androidx.lifecycle.LiveData
import androidx.room.*
import com.anod.appwatcher.database.entities.Tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @author alex
 * *
 * @date 2015-03-01
 */
@Dao
interface TagsTable {

    @Query("SELECT * FROM $table ORDER BY ${Columns.name} COLLATE LOCALIZED ASC")
    fun observe(): LiveData<List<Tag>>

    @Query("SELECT * FROM $table ORDER BY ${Columns.name} COLLATE LOCALIZED ASC")
    fun load(): List<Tag>

    @Delete
    fun delete(tag: Tag)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(tag: Tag)

    @Query("DELETE FROM ${AppListTable.table}")
    fun delete()

    object Queries {
        suspend fun insert(tag: Tag, db: AppsDatabase): Long {
            // Skip id to apply autoincrement
            val values = ContentValues().apply {
                put(Columns.name, tag.name)
                put(Columns.color, tag.color)
            }
            var rowId = 0L
            withContext(Dispatchers.IO) {
                db.runInTransaction {
                    rowId = db.openHelper.writableDatabase.insert(table, SQLiteDatabase.CONFLICT_REPLACE, values)
                }
            }
            return rowId
        }

        fun insert(tags: List<Tag>, db: AppsDatabase) {
            tags.forEach { tag ->
                val values = ContentValues().apply {
                    put(Columns.name, tag.name)
                    put(Columns.color, tag.color)
                }
                db.openHelper.writableDatabase.insert(table, SQLiteDatabase.CONFLICT_REPLACE, tag.contentValues)
            }
        }
    }

    class Columns : BaseColumns {
        companion object {
            const val name = "name"
            const val color = "color"
        }
    }

    object TableColumns {
        const val _ID = table + "." + BaseColumns._ID
        const val name = "$table.name"
        const val color = "$table.color"
    }

    object Projection {
        const val _ID = 0
        const val name = 1
        const val color = 2
    }

    companion object {
        const val table = "tags"
        val projection = arrayOf(TableColumns._ID, TableColumns.name, TableColumns.color)
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