package com.anod.appwatcher.database

import androidx.room.*
import com.anod.appwatcher.database.entities.Tag
import kotlinx.coroutines.flow.Flow

/**
 * @author alex
 * *
 * @date 2015-03-01
 */
@Dao
interface TagsTable {

    @Query("SELECT * FROM $table ORDER BY ${Columns.name} COLLATE LOCALIZED ASC")
    fun observe(): Flow<List<Tag>>

    @Query("SELECT * FROM $table WHERE ${BaseColumns._ID} = :tagId")
    fun observeTag(tagId: Int): Flow<Tag?>

    @Query("SELECT * FROM $table ORDER BY ${Columns.name} COLLATE LOCALIZED ASC")
    suspend fun load(): List<Tag>

    @Query("SELECT * FROM $table WHERE ${BaseColumns._ID} = :tagId")
    suspend fun loadById(tagId: Int): Tag?

    @Query("SELECT ${BaseColumns._ID} FROM $table")
    suspend fun loadIds(): List<Int>

    @Query("DELETE FROM $table WHERE ${BaseColumns._ID} = :tagId")
    suspend fun delete(tagId: Int)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(tag: Tag)

    @Query("DELETE FROM $table")
    suspend fun delete()

    @Query("INSERT INTO $table (${Columns.name}, ${Columns.color}) VALUES (:name, :color)")
    suspend fun insert(name: String, color: Int): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @Transaction
    suspend fun insert(tag: Tag): Long

    object Queries {
        suspend fun delete(tag: Tag, db: AppsDatabase) {
            return db.withTransaction {
                db.tags().delete(tag.id)
                db.appTags().delete(tag.id)
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

    companion object {
        const val table = "tags"
    }
}