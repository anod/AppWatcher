package com.anod.appwatcher.database

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import android.content.ContentValues
import android.provider.BaseColumns
import com.anod.appwatcher.database.entities.AppChange

/**
 * @author alex
 * *
 * @date 2015-03-01
 */
@Dao
interface ChangelogTable {

    @Query("SELECT * FROM $table WHERE ${ChangelogTable.Columns.appId} == :appId")
    fun ofApp(appId: String): List<AppChange>

    @Query("SELECT * FROM $table WHERE ${ChangelogTable.Columns.appId} == :appId AND ${ChangelogTable.Columns.versionCode} == :versionCode")
    fun forVersion(appId: String, versionCode: Int): AppChange?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(changelog: AppChange)

    class Columns : BaseColumns {
        companion object {
            const val appId = "app_id"
            const val versionCode = "code"
            const val versionName = "name"
            const val details = "details"
            const val uploadDate = "upload_date"
        }
    }

    object TableColumns {
        const val _ID = table + "." + BaseColumns._ID
        const val appId = table + ".app_id"
        const val versionCode = table + ".code"
        const val versionName = table + ".name"
        const val details = table + ".details"
        const val uploadDate = table + ".upload_date"
    }

    object Projection {
        const val _ID = 0
        const val appId = 1
        const val versionCode = 2
        const val versionName = 3
        const val details = 4
        const val uploadDate = 5
    }

    companion object {

        const val table = "changelog"

        val projection = arrayOf(
                TableColumns._ID,
                TableColumns.appId,
                TableColumns.versionCode,
                TableColumns.versionName,
                TableColumns.details,
                TableColumns.uploadDate)

        const val sqlCreate =
                "CREATE TABLE " + table + " (" +
                        BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        Columns.appId + " TEXT not null," +
                        Columns.versionCode + " INTEGER," +
                        Columns.versionName + " TEXT not null," +
                        Columns.details + " TEXT not null," +
                        Columns.uploadDate + " TEXT not null," +
                        "UNIQUE(${Columns.appId}, ${Columns.versionCode}) ON CONFLICT REPLACE" +
                        ") "

    }
}

val AppChange.contentValues: ContentValues
    get() {
        val values = ContentValues()
        values.put(ChangelogTable.Columns.appId, appId)
        values.put(ChangelogTable.Columns.versionCode, versionCode)
        values.put(ChangelogTable.Columns.versionName, versionName)
        values.put(ChangelogTable.Columns.details, details)
        values.put(ChangelogTable.Columns.uploadDate, uploadDate)
        return values
    }