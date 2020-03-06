// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.database

import android.provider.BaseColumns
import androidx.room.*
import com.anod.appwatcher.database.entities.Schedule

@Dao
interface SchedulesTable {

    @Query("SELECT * FROM $table ORDER BY ${Columns.start} DESC")
    suspend fun load(): List<Schedule>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(schedule: Schedule): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(schedule: Schedule)

    @Query("DELETE FROM $table WHERE ${Columns.start} > :time")
    suspend fun clean(time: Long)

    @Transaction
    suspend fun save(schedule: Schedule) {
        if (schedule.id == -1L) {
            schedule.id = insert(schedule)
        } else {
            update(schedule)
        }
    }

    class Columns : BaseColumns {
        companion object {
            const val start = "start"
            const val finish = "finish"
            const val result = "result"
            const val reason = "reason"
            const val checked = "checked"
            const val found = "found"
            const val unavailable = "unavailable"
        }
    }

    companion object {
        const val table = "schedules"
        val projection = arrayOf(TagsTable.TableColumns._ID, TagsTable.TableColumns.name, TagsTable.TableColumns.color)
    }
}