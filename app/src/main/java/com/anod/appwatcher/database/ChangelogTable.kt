package com.anod.appwatcher.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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

    @Query("SELECT * FROM $table WHERE ${Columns.appId} == :appId ORDER BY ${Columns.versionCode} DESC")
    fun ofApp(appId: String): LiveData<List<AppChange>>

//    @Query("SELECT * FROM $table WHERE ${Columns.appId} == :appId AND ${Columns.versionCode} == :versionCode")
//    fun forVersion(appId: String, versionCode: Int): AppChange?
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    fun save(changelog: AppChange)

    class Columns : BaseColumns {
        companion object {
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
    get() {
        val values = ContentValues()
        values.put(ChangelogTable.Columns.appId, appId)
        values.put(ChangelogTable.Columns.versionCode, versionCode)
        values.put(ChangelogTable.Columns.versionName, versionName)
        values.put(ChangelogTable.Columns.details, details)
        values.put(ChangelogTable.Columns.uploadDate, uploadDate)
        return values
    }