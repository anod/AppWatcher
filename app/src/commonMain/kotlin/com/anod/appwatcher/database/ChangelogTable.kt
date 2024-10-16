package com.anod.appwatcher.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import com.anod.appwatcher.database.entities.AppChange

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
    fun all(): PagingSource<Int, AppChange>

    @Query("SELECT * FROM $table " +
            "WHERE ${Columns.versionCode} = (" +
            "SELECT MAX(${Columns.versionCode}) FROM $table WHERE ${Columns.versionCode} < :versionCode AND ${Columns.appId} == :appId" +
            ") LIMIT 1")
    suspend fun findPrevious(versionCode: Int, appId: String): AppChange?

    @Query("UPDATE $table SET ${Columns.noNewDetails} = 0")
    suspend fun resetNoNewDetails(): Int

    @Suppress("FunctionName")
    @Query("SELECT l.* FROM changelog l " +
            "INNER JOIN (" +
            "SELECT app_id, MAX(code) as latestCode FROM changelog GROUP BY app_id" +
            ") r ON l.app_id = r.app_id AND l.code = r.latestCode " +
            "WHERE l.app_id IN (:appIds)")
    suspend fun _load(appIds: List<String>): List<AppChange>

    suspend fun load(appIds: List<String>): List<AppChange> {
        return appIds.chunked(998).flatMap { _load(it) }
    }

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