// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.database

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.provider.BaseColumns
import androidx.lifecycle.LiveData
import androidx.room.*
import com.anod.appwatcher.database.entities.Schedule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.Callable

@Dao
interface SchedulesTable {

    @Query("SELECT * FROM $table ORDER BY ${Columns.start} DESC")
    fun load(): LiveData<List<Schedule>>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(schedule: Schedule)

    @Query("DELETE FROM $table WHERE ${Columns.start} < :time")
    suspend fun clean(time: Long)

    @Query("UPDATE $table SET ${Columns.notified} = :notified WHERE ${BaseColumns._ID} = :id")
    suspend fun updateNotified(id: Long, notified: Int)

    object Queries {
        @Transaction
        suspend fun save(schedule: Schedule, db: AppsDatabase) {
            if (schedule.id == -1L) {
                schedule.id = insert(schedule, db)
            } else {
                db.schedules().update(schedule)
            }
        }

        suspend fun insert(schedule: Schedule, db: AppsDatabase): Long = withContext(Dispatchers.IO) {
            // Skip id to apply autoincrement
            return@withContext db.runInTransaction(Callable {
                db.openHelper.writableDatabase.insert(table, SQLiteDatabase.CONFLICT_REPLACE, schedule.contentValues)
            })
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
        val projection = arrayOf(TagsTable.TableColumns._ID, TagsTable.TableColumns.name, TagsTable.TableColumns.color)
    }
}

val Schedule.contentValues: ContentValues
    get() = ContentValues().apply {
        if (id > -1) {
            put(BaseColumns._ID, id)
        }
        put(SchedulesTable.Columns.start, start)
        put(SchedulesTable.Columns.finish, finish)
        put(SchedulesTable.Columns.result, result)
        put(SchedulesTable.Columns.reason, reason)
        put(SchedulesTable.Columns.checked, checked)
        put(SchedulesTable.Columns.found, found)
        put(SchedulesTable.Columns.unavailable, unavailable)
        put(SchedulesTable.Columns.notified, notified)
    }