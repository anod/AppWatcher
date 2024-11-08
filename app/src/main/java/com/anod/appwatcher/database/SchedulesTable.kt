// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.database

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.provider.BaseColumns
import androidx.room.Dao
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.anod.appwatcher.database.entities.Schedule
import java.util.concurrent.Callable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

@Dao
interface SchedulesTable {

    @Query("SELECT * FROM $TABLE ORDER BY ${Columns.START} DESC")
    fun load(): Flow<List<Schedule>>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(schedule: Schedule)

    @Query("DELETE FROM $TABLE WHERE ${Columns.START} < :time")
    suspend fun clean(time: Long)

    @Query("UPDATE $TABLE SET ${Columns.NOTIFIED} = :notified WHERE ${BaseColumns._ID} = :id")
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
                db.openHelper.writableDatabase.insert(TABLE, SQLiteDatabase.CONFLICT_REPLACE, schedule.contentValues)
            })
        }
    }

    class Columns : BaseColumns {
        companion object {
            const val START = "start"
            const val FINISH = "finish"
            const val RESULT = "result"
            const val REASON = "reason"
            const val CHECKED = "checked"
            const val FOUND = "found"
            const val UNAVAILABLE = "unavailable"
            const val NOTIFIED = "notified"
        }
    }

    companion object {
        const val TABLE = "schedules"
        val projection = arrayOf(TagsTable.TableColumns.BASE_ID, TagsTable.TableColumns.NAME, TagsTable.TableColumns.COLOR)
    }
}

val Schedule.contentValues: ContentValues
    get() = ContentValues().apply {
        if (id > -1) {
            put(BaseColumns._ID, id)
        }
        put(SchedulesTable.Columns.START, start)
        put(SchedulesTable.Columns.FINISH, finish)
        put(SchedulesTable.Columns.RESULT, result)
        put(SchedulesTable.Columns.REASON, reason)
        put(SchedulesTable.Columns.CHECKED, checked)
        put(SchedulesTable.Columns.FOUND, found)
        put(SchedulesTable.Columns.UNAVAILABLE, unavailable)
        put(SchedulesTable.Columns.NOTIFIED, notified)
    }