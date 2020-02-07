package com.anod.appwatcher.database

import android.content.ContentValues
import android.database.Cursor
import android.provider.BaseColumns
import androidx.room.Dao
import androidx.room.Query
import com.anod.appwatcher.database.entities.AppChange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @author alex
 * *
 * @date 2015-03-01
 */
@Dao
interface ChangelogTable {

    @Query("SELECT * FROM $table WHERE ${Columns.appId} == :appId ORDER BY ${Columns.versionCode} DESC")
    suspend fun ofApp(appId: String): List<AppChange>

    @Query("SELECT * FROM $table ORDER BY ${Columns._ID} DESC")
    fun all(): Cursor

    @Query("SELECT * FROM $table " +
            "WHERE ${Columns.versionCode} = (" +
            "SELECT MAX(${Columns.versionCode}) FROM $table WHERE ${Columns.versionCode} < :versionCode AND ${Columns.appId} == :appId" +
            ") LIMIT 1")
    suspend fun findPrevious(versionCode: Int, appId: String): AppChange?

    @Query("UPDATE $table SET ${Columns.noNewDetails} = 0")
    suspend fun resetNoNewDetails(): Int

    object Queries {
        suspend fun all(table: ChangelogTable): AppChangeCursor = withContext(Dispatchers.IO) {
            val cursor = table.all()
            return@withContext AppChangeCursor(cursor)
        }
    }

//    @Query("SELECT * FROM $table WHERE ${Columns.appId} == :appId AND ${Columns.versionCode} == :versionCode")
//    fun forVersion(appId: String, versionCode: Int): AppChange?
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    fun save(changelog: AppChange)

    class Columns : BaseColumns {
        companion object {
            const val _ID = BaseColumns._ID
            const val appId = "app_id"
            const val versionCode = "code"
            const val versionName = "name"
            const val details = "details"
            const val uploadDate = "upload_date"
            const val noNewDetails = "no_new_details"
        }
    }

    object TableColumns {
        const val _ID = table + "." + BaseColumns._ID
        const val appId = "$table.app_id"
        const val versionCode = "$table.code"
        const val versionName = "$table.name"
        const val details = "$table.details"
        const val uploadDate = "$table.upload_date"
        const val noNewDetails = "$table.no_new_details"
    }

    companion object {
        const val table = "changelog"
    }
}

val AppChange.contentValues: ContentValues
    get() = ContentValues().apply {
        put(ChangelogTable.Columns.appId, appId)
        put(ChangelogTable.Columns.versionCode, versionCode)
        put(ChangelogTable.Columns.versionName, versionName)
        put(ChangelogTable.Columns.details, details)
        put(ChangelogTable.Columns.uploadDate, uploadDate)
        put(ChangelogTable.Columns.noNewDetails, noNewDetails)
    }