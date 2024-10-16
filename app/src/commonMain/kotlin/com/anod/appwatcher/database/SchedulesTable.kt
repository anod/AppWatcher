// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.database

import androidx.room.*
import com.anod.appwatcher.database.entities.Schedule
import kotlinx.coroutines.flow.Flow

@Dao
interface SchedulesTable {

    @Query("SELECT * FROM $table ORDER BY ${Columns.start} DESC")
    fun load(): Flow<List<Schedule>>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(schedule: Schedule)

    @Query("DELETE FROM $table WHERE ${Columns.start} < :time")
    suspend fun clean(time: Long)

    @Query("UPDATE $table SET ${Columns.notified} = :notified WHERE ${BaseColumns._ID} = :id")
    suspend fun updateNotified(id: Long, notified: Int)

    // TODO: Check autoincrement
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @Transaction
    suspend fun insert(schedule: Schedule): Long

    object Queries {
        @Transaction
        suspend fun save(schedule: Schedule, db: AppsDatabase) {
            if (schedule.id == -1L) {
                schedule.id = db.schedules().insert(schedule)
            } else {
                db.schedules().update(schedule)
            }
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
            const val notified = "notified"
        }
    }

    companion object {
        const val table = "schedules"
    }
}