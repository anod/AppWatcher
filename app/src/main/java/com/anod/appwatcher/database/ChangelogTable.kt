package com.anod.appwatcher.database

import android.content.ContentValues
import android.database.Cursor
import android.provider.BaseColumns
import androidx.room.Dao
import androidx.room.Query
import com.anod.appwatcher.database.entities.AppChange
import info.anodsplace.ktx.chunked
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @author alex
 * *
 * @date 2015-03-01
 */
@Dao
interface ChangelogTable {

    @Query("SELECT * FROM $TABLE WHERE ${Columns.APP_ID} == :appId ORDER BY ${Columns.VERSION_CODE} DESC")
    suspend fun ofApp(appId: String): List<AppChange>

    @Query("SELECT * FROM $TABLE ORDER BY ${Columns.BASE_ID} DESC")
    fun all(): Cursor

    @Query(
        "SELECT * FROM $TABLE " +
            "WHERE ${Columns.VERSION_CODE} = (" +
            "SELECT MAX(${Columns.VERSION_CODE}) FROM $TABLE WHERE ${Columns.VERSION_CODE} < :versionCode AND ${Columns.APP_ID} == :appId" +
            ") LIMIT 1"
    )
    suspend fun findPrevious(versionCode: Int, appId: String): AppChange?

    @Query("UPDATE $TABLE SET ${Columns.NO_NEW_DETAILS} = 0")
    suspend fun resetNoNewDetails(): Int

    @Suppress("FunctionName")
    @Query("SELECT l.* FROM changelog l " +
        "INNER JOIN (" +
        "SELECT app_id, MAX(code) as latestCode FROM changelog GROUP BY app_id" +
        ") r ON l.app_id = r.app_id AND l.code = r.latestCode " +
        "WHERE l.app_id IN (:appIds)")
    suspend fun _load(appIds: List<String>): List<AppChange>

    suspend fun load(appIds: List<String>): List<AppChange> {
        return appIds.chunked({
            _load(it)
        })
    }

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
            const val BASE_ID = BaseColumns._ID
            const val APP_ID = "app_id"
            const val VERSION_CODE = "code"
            const val VERSION_NAME = "name"
            const val DETAILS = "details"
            const val UPLOAD_DATE = "upload_date"
            const val NO_NEW_DETAILS = "no_new_details"
        }
    }

    object TableColumns {
        const val BASE_ID = TABLE + "." + BaseColumns._ID
        const val APP_ID = "$TABLE.app_id"
        const val VERSION_CODE = "$TABLE.code"
        const val VERSION_NAME = "$TABLE.name"
        const val DETAILS = "$TABLE.details"
        const val UPLOAD_DATE = "$TABLE.upload_date"
        const val NO_NEW_DETAILS = "$TABLE.no_new_details"
    }

    companion object {
        const val TABLE = "changelog"
    }
}

val AppChange.contentValues: ContentValues
    get() = ContentValues().apply {
        put(ChangelogTable.Columns.APP_ID, appId)
        put(ChangelogTable.Columns.VERSION_CODE, versionCode)
        put(ChangelogTable.Columns.VERSION_NAME, versionName)
        put(ChangelogTable.Columns.DETAILS, details)
        put(ChangelogTable.Columns.UPLOAD_DATE, uploadDate)
        put(ChangelogTable.Columns.NO_NEW_DETAILS, noNewDetails)
    }