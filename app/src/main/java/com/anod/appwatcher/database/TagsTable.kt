package com.anod.appwatcher.database

import android.provider.BaseColumns
import androidx.room.Dao
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.withTransaction
import com.anod.appwatcher.database.entities.Tag
import kotlinx.coroutines.flow.Flow

/**
 * @author alex
 * *
 * @date 2015-03-01
 */
@Dao
interface TagsTable {

    @Query("SELECT * FROM $TABLE WHERE ${Columns.STATUS} = ${Tag.STATUS_NORMAL} ORDER BY ${Columns.NAME} COLLATE LOCALIZED ASC")
    fun observe(): Flow<List<Tag>>

    @Query("SELECT * FROM $TABLE WHERE ${BaseColumns._ID} = :tagId AND ${Columns.STATUS} = ${Tag.STATUS_NORMAL}")
    fun observeTag(tagId: Int): Flow<Tag?>

    @Query("SELECT * FROM $TABLE WHERE ${Columns.STATUS} = ${Tag.STATUS_NORMAL} ORDER BY ${Columns.NAME} COLLATE LOCALIZED ASC")
    suspend fun load(): List<Tag>

    @Query("SELECT * FROM $TABLE WHERE ${BaseColumns._ID} = :tagId AND ${Columns.STATUS} = ${Tag.STATUS_NORMAL}")
    suspend fun loadById(tagId: Int): Tag?

    @Query("SELECT ${BaseColumns._ID} FROM $TABLE WHERE ${Columns.STATUS} = ${Tag.STATUS_NORMAL}")
    suspend fun loadIds(): List<Int>

    @Query("SELECT COUNT(*) FROM $TABLE WHERE ${Columns.NAME} = :name AND ${Columns.STATUS} = ${Tag.STATUS_NORMAL}")
    suspend fun countByName(name: String): Int

    @Query("SELECT * FROM $TABLE WHERE ${Columns.NAME} = :name AND ${Columns.STATUS} = ${Tag.STATUS_DELETED} LIMIT 1")
    suspend fun loadDeletedByName(name: String): Tag?

    @Query("SELECT ${BaseColumns._ID} FROM $TABLE WHERE ${Columns.STATUS} = ${Tag.STATUS_DELETED}")
    suspend fun loadDeletedIds(): List<Int>

    @Query("SELECT ${Columns.NAME} FROM $TABLE WHERE ${Columns.STATUS} = ${Tag.STATUS_DELETED}")
    suspend fun loadDeletedNames(): List<String>

    @Query("DELETE FROM $TABLE WHERE ${BaseColumns._ID} = :tagId")
    suspend fun delete(tagId: Int)

    @Query("DELETE FROM $TABLE WHERE ${BaseColumns._ID} IN (:tagIds) AND ${Columns.STATUS} = ${Tag.STATUS_DELETED}")
    suspend fun deleteDeleted(tagIds: List<Int>): Int

    @Query("DELETE FROM $TABLE WHERE ${Columns.NAME} = :name AND ${Columns.STATUS} = ${Tag.STATUS_DELETED}")
    suspend fun deleteDeleted(name: String): Int

    @Query("UPDATE $TABLE SET ${Columns.STATUS} = :status WHERE ${BaseColumns._ID} = :tagId")
    suspend fun updateStatus(tagId: Int, status: Int): Int

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(tag: Tag)

    @Query("DELETE FROM $TABLE")
    suspend fun delete()

    @Query("INSERT INTO $TABLE (${Columns.NAME}, ${Columns.COLOR}, ${Columns.STATUS}) VALUES (:name, :color, ${Tag.STATUS_NORMAL})")
    suspend fun insert(name: String, color: Int): Long

    @Query("INSERT INTO $TABLE (${Columns.NAME}, ${Columns.COLOR}, ${Columns.STATUS}) VALUES (:name, :color, ${Tag.STATUS_DELETED})")
    suspend fun insertDeleted(name: String, color: Int): Long

    object Queries {
        suspend fun delete(tag: Tag, db: AppsDatabase) = db.withTransaction {
            db.appTags().delete(tag.id)
            if (db.tags().countByName(tag.name) > 1) {
                db.tags().delete(tag.id)
            } else {
                db.tags().updateStatus(tag.id, Tag.STATUS_DELETED)
            }
        }

        suspend fun insert(tag: Tag, db: AppsDatabase): Long = db.withTransaction {
            val deletedTag = db.tags().loadDeletedByName(tag.name)
            if (deletedTag != null) {
                db.tags().update(deletedTag.copy(color = tag.color, status = Tag.STATUS_NORMAL))
                deletedTag.id.toLong()
            } else {
                db.tags().insert(tag.name, tag.color)
            }
        }

        suspend fun update(tag: Tag, db: AppsDatabase) = db.withTransaction {
            val previousTag = db.tags().loadById(tag.id)
            if (previousTag != null && previousTag.name != tag.name) {
                db.tags().deleteDeleted(tag.name)
                if (db.tags().countByName(previousTag.name) == 1) {
                    db.tags().insertDeleted(previousTag.name, previousTag.color)
                }
            }
            db.tags().update(tag.copy(status = Tag.STATUS_NORMAL))
        }
    }

    class Columns : BaseColumns {
        companion object {
            const val NAME = "name"
            const val COLOR = "color"
            const val STATUS = "status"
        }
    }

    object TableColumns {
        const val BASE_ID = TABLE + "." + BaseColumns._ID
        const val NAME = "$TABLE.name"
        const val COLOR = "$TABLE.color"
        const val STATUS = "$TABLE.status"
    }

    object Projection {
        const val BASE_ID = 0
        const val NAME = 1
        const val COLOR = 2
        const val STATUS = 3
    }

    companion object {
        const val TABLE = "tags"
        val projection = arrayOf(TableColumns.BASE_ID, TableColumns.NAME, TableColumns.COLOR, TableColumns.STATUS)
    }
}