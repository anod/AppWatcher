package com.anod.appwatcher.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.provider.BaseColumns
import com.anod.appwatcher.database.entities.AppTag
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.database.entities.TagAppsCount
import java.util.concurrent.Callable

/**
 * @author alex
 * *
 * @date 2015-03-01
 */
@Dao
interface AppTagsTable {

    @Query("SELECT * FROM $table WHERE ${Columns.tagId} = :tagId")
    fun forTag(tagId: Int): LiveData<List<AppTag>>

    @Query("SELECT * FROM $table WHERE ${Columns.appId} = :appId")
    fun forApp(appId: String): LiveData<List<AppTag>>

    @Query("SELECT ${Columns.tagId}, count() as count FROM $table GROUP BY ${Columns.tagId}")
    fun queryCounts(): LiveData<List<TagAppsCount>>

    @Query("SELECT * FROM $table WHERE ${Columns.appId} = :appId AND ${Columns.tagId} = :tagId")
    fun appWithTag(appId: String, tagId: Int): AppTag?

    @Query("SELECT * FROM $table")
    fun load(): List<AppTag>

    @Query("DELETE FROM $table WHERE ${TableColumns.appId} NOT IN (SELECT ${AppListTable.TableColumns.appId} FROM ${AppListTable.table})")
    fun clean(): Int

    @Query("DELETE FROM $table")
    fun delete()

    @Query("DELETE FROM $table WHERE ${Columns.tagId} = :tagId")
    fun delete(tagId: Int)

    @Query("DELETE FROM $table WHERE ${Columns.tagId} = :tagId AND ${Columns.appId} = :appId")
    fun delete(tagId: Int, appId: String): Int

    class Columns : BaseColumns {
        companion object {
            const val appId = "app_id"
            const val tagId = "tags_id"
        }
    }

    object TableColumns {
        const val _ID = table + "." + BaseColumns._ID
        const val appId = "$table.app_id"
        const val tagId = "$table.tags_id"
    }

    object Projection {
        const val _ID = 0
        const val appId = 1
        const val tagId = 2
    }

    companion object {
        const val table = "app_tags"
        val projection = arrayOf(TableColumns._ID, TableColumns.appId, TableColumns.tagId)
    }

    object Queries {
        fun insert(tag: Tag, apps: List<String>, db: AppsDatabase) {
            db.runInTransaction {
                for (appId in apps) {
                    val values = AppTag(appId, tag.id).contentValues
                    db.openHelper.writableDatabase.insert(table, SQLiteDatabase.CONFLICT_REPLACE, values)
                }
            }
            return
        }

        // Executed in transaction
        fun insert(appTags: List<AppTag>, db: AppsDatabase) {
            appTags.forEach { appTag ->
                db.openHelper.writableDatabase.insert(table, SQLiteDatabase.CONFLICT_REPLACE, appTag.contentValues)
            }
        }

        fun insert(tagId: Int, appId: String, db: AppsDatabase): Long {
            db.invalidationTracker.refreshVersionsAsync()
            return db.runInTransaction(Callable<Long> {
                db.openHelper.writableDatabase.insert(table, SQLiteDatabase.CONFLICT_REPLACE, AppTag(appId, tagId).contentValues)
            })
        }

        fun assignAppsToTag(appIds: List<String>, tagId: Int, db: AppsDatabase) {
            db.runInTransaction {
                db.appTags().delete(tagId)
                for (appId in appIds) {
                    val values = AppTag(appId, tagId).contentValues
                    db.openHelper.writableDatabase.insert(table, SQLiteDatabase.CONFLICT_REPLACE, values)
                }
            }
        }

    }
}

val AppTag.contentValues: ContentValues
    get() {
        val values = ContentValues()
        values.put(AppTagsTable.Columns.appId, appId)
        values.put(AppTagsTable.Columns.tagId, tagId)
        return values
    }
