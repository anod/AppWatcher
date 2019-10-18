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
    suspend fun load(): List<Tag>

    @Delete
    suspend fun delete(tag: Tag)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(tag: Tag)

    @Query("DELETE FROM ${AppListTable.table}")
    suspend fun delete()

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

        suspend fun insert(tags: List<Tag>, db: AppsDatabase) = withContext(Dispatchers.IO) {
            tags.forEach { tag ->
                val values = ContentValues().apply {
                    put(Columns.name, tag.name)
                    put(Columns.color, tag.color)
                }
                db.openHelper.writableDatabase.insert(table, SQLiteDatabase.CONFLICT_REPLACE, values)
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