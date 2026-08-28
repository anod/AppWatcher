package com.anod.appwatcher.database

import android.provider.BaseColumns
import androidx.room.Dao
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.withTransaction
import com.anod.appwatcher.database.entities.DeletedTag
import com.anod.appwatcher.database.entities.Tag
import kotlinx.coroutines.flow.Flow

/**
 * @author alex
 * *
 * @date 2015-03-01
 */
@Dao
interface TagsTable {

    @Query("SELECT * FROM $TABLE ORDER BY ${Columns.NAME} COLLATE LOCALIZED ASC")
    fun observe(): Flow<List<Tag>>

    @Query("SELECT * FROM $TABLE WHERE ${BaseColumns._ID} = :tagId")
    fun observeTag(tagId: Int): Flow<Tag?>

    @Query("SELECT * FROM $TABLE ORDER BY ${Columns.NAME} COLLATE LOCALIZED ASC")
    suspend fun load(): List<Tag>

    @Query("SELECT * FROM $TABLE WHERE ${BaseColumns._ID} = :tagId")
    suspend fun loadById(tagId: Int): Tag?

    @Query("SELECT ${BaseColumns._ID} FROM $TABLE")
    suspend fun loadIds(): List<Int>

    @Query("SELECT COUNT(*) FROM $TABLE WHERE ${Columns.NAME} = :name")
    suspend fun countByName(name: String): Int

    @Query("DELETE FROM $TABLE WHERE ${BaseColumns._ID} = :tagId")
    suspend fun delete(tagId: Int)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(tag: Tag)

    @Query("DELETE FROM $TABLE")
    suspend fun delete()

    @Query("INSERT INTO $TABLE (${Columns.NAME}, ${Columns.COLOR}) VALUES (:name, :color)")
    suspend fun insert(name: String, color: Int): Long

    object Queries {
        suspend fun delete(tag: Tag, db: AppsDatabase) = db.withTransaction {
            db.tags().delete(tag.id)
            db.appTags().delete(tag.id)
            if (db.tags().countByName(tag.name) == 0) {
                db.deletedTags().insert(DeletedTag(tag.name))
            }
        }

        suspend fun insert(tag: Tag, db: AppsDatabase): Long = db.withTransaction {
            val rowId = db.tags().insert(tag.name, tag.color)
            if (rowId > 0) {
                db.deletedTags().delete(tag.name)
            }
            rowId
        }

        suspend fun update(tag: Tag, db: AppsDatabase) = db.withTransaction {
            val previousTag = db.tags().loadById(tag.id)
            db.tags().update(tag)
            db.deletedTags().delete(tag.name)
            if (previousTag != null && previousTag.name != tag.name) {
                if (db.tags().countByName(previousTag.name) == 0) {
                    db.deletedTags().insert(DeletedTag(previousTag.name))
                }
            }
        }
    }

    class Columns : BaseColumns {
        companion object {
            const val NAME = "name"
            const val COLOR = "color"
        }
    }

    object TableColumns {
        const val BASE_ID = TABLE + "." + BaseColumns._ID
        const val NAME = "$TABLE.name"
        const val COLOR = "$TABLE.color"
    }

    object Projection {
        const val BASE_ID = 0
        const val NAME = 1
        const val COLOR = 2
    }

    companion object {
        const val TABLE = "tags"
        val projection = arrayOf(TableColumns.BASE_ID, TableColumns.NAME, TableColumns.COLOR)
    }
}