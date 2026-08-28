package com.anod.appwatcher.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.anod.appwatcher.database.entities.DeletedTag

@Dao
interface DeletedTagsTable {

    @Query("SELECT ${Columns.NAME} FROM $TABLE")
    suspend fun loadNames(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tag: DeletedTag)

    @Query("DELETE FROM $TABLE WHERE ${Columns.NAME} = :name")
    suspend fun delete(name: String): Int

    @Query("DELETE FROM $TABLE WHERE ${Columns.NAME} IN (:names)")
    suspend fun delete(names: List<String>): Int

    @Query("DELETE FROM $TABLE")
    suspend fun deleteAll(): Int

    object Columns {
        const val NAME = "name"
    }

    companion object {
        const val TABLE = "deleted_tags"
    }
}