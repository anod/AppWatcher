package com.anod.appwatcher.database

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.provider.BaseColumns
import androidx.room.*
import com.anod.appwatcher.database.entities.Tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * @author alex
 * *
 * @date 2015-03-01
 */
@Dao
interface TagsTable {

    @Query("SELECT * FROM $TABLE ORDER BY ${Columns.name} COLLATE LOCALIZED ASC")
    fun observe(): Flow<List<Tag>>

    @Query("SELECT * FROM $TABLE WHERE ${BaseColumns._ID} = :tagId")
    fun observeTag(tagId: Int): Flow<Tag?>

    @Query("SELECT * FROM $TABLE ORDER BY ${Columns.name} COLLATE LOCALIZED ASC")
    suspend fun load(): List<Tag>

    @Query("SELECT * FROM $TABLE WHERE ${BaseColumns._ID} = :tagId")
    suspend fun loadById(tagId: Int): Tag?

    @Query("SELECT ${BaseColumns._ID} FROM $TABLE")
    suspend fun loadIds(): List<Int>

    @Query("DELETE FROM $TABLE WHERE ${BaseColumns._ID} = :tagId")
    suspend fun delete(tagId: Int)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(tag: Tag)

    @Query("DELETE FROM $TABLE")
    suspend fun delete()

    @Query("INSERT INTO $TABLE (${Columns.name}, ${Columns.color}) VALUES (:name, :color)")
    suspend fun insert(name: String, color: Int): Long

    object Queries {
        suspend fun delete(tag: Tag, db: AppsDatabase) {
            return db.withTransaction {
                db.tags().delete(tag.id)
                db.appTags().delete(tag.id)
            }
        }

        suspend fun insert(tag: Tag, db: AppsDatabase): Long {
            // Skip id to apply autoincrement
            val values = ContentValues().apply {
                put(Columns.name, tag.name)
                put(Columns.color, tag.color)
            }
            var rowId = 0L
            withContext(Dispatchers.IO) {
                db.runInTransaction {
                    rowId = db.openHelper.writableDatabase.insert(TABLE, SQLiteDatabase.CONFLICT_REPLACE, values)
                }
            }
            return rowId
        }
    }

    class Columns : BaseColumns {
        companion object {
            const val name = "name"
            const val color = "color"
        }
    }

    object TableColumns {
        const val _ID = TABLE + "." + BaseColumns._ID
        const val name = "$TABLE.name"
        const val color = "$TABLE.color"
    }

    object Projection {
        const val _ID = 0
        const val name = 1
        const val color = 2
    }

    companion object {
        const val TABLE = "tags"
        val projection = arrayOf(TableColumns._ID, TableColumns.name, TableColumns.color)
    }
}