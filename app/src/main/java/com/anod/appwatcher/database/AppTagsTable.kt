package com.anod.appwatcher.database

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.provider.BaseColumns
import androidx.room.Dao
import androidx.room.Query
import androidx.room.withTransaction
import com.anod.appwatcher.database.entities.AppTag
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.database.entities.TagAppsCount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * @author alex
 * *
 * @date 2015-03-01
 */
@Dao
interface AppTagsTable {

    @Query("SELECT * FROM $TABLE WHERE ${Columns.TAGS_ID} = :tagId")
    fun forTag(tagId: Int): Flow<List<AppTag>>

    @Query("SELECT * FROM $TABLE WHERE ${Columns.APP_ID} = :appId")
    fun forApp(appId: String): Flow<List<AppTag>>

    @Query("SELECT IFNULL(tags_id, 0) AS tags_id, count() as count FROM app_list l " +
        "LEFT JOIN app_tags t ON l.app_id = t.app_id " +
        "GROUP BY tags_id")
    fun queryCounts(): Flow<List<TagAppsCount>>

    @Query("SELECT * FROM $TABLE")
    suspend fun load(): List<AppTag>

    @Query("SELECT DISTINCT ${TableColumns.TAG_ID} FROM $TABLE")
    suspend fun loadTagIds(): List<Int>

    @Query("DELETE FROM $TABLE WHERE ${TableColumns.APP_ID} NOT IN (SELECT ${AppListTable.TableColumns.APP_ID} FROM ${AppListTable.TABLE})")
    suspend fun clean(): Int

    @Query("DELETE FROM $TABLE")
    suspend fun delete()

    @Query("DELETE FROM $TABLE WHERE ${Columns.TAGS_ID} IN (:tagIds)")
    suspend fun deleteIds(tagIds: List<Int>)

    @Query("DELETE FROM $TABLE WHERE ${Columns.TAGS_ID} = :tagId")
    suspend fun delete(tagId: Int)

    @Query("DELETE FROM $TABLE WHERE ${Columns.TAGS_ID} = :tagId AND ${Columns.APP_ID} = :appId")
    suspend fun delete(tagId: Int, appId: String): Int

    @Query("INSERT INTO $TABLE (${Columns.APP_ID}, ${Columns.TAGS_ID}) VALUES (:appId, :tagId)")
    suspend fun insert(appId: String, tagId: Int): Long

    class Columns : BaseColumns {
        companion object {
            const val APP_ID = "app_id"
            const val TAGS_ID = "tags_id"
        }
    }

    object TableColumns {
        const val BASE_ID = TABLE + "." + BaseColumns._ID
        const val APP_ID = "$TABLE.app_id"
        const val TAG_ID = "$TABLE.tags_id"
    }

    companion object {
        const val TABLE = "app_tags"
    }

    object Queries {
        suspend fun clean(db: AppsDatabase) {
            val appTagIds = db.appTags().loadTagIds().toSet()
            val tagIds = db.tags().loadIds().toSet()
            val deletedIds = appTagIds.subtract(tagIds).toList()
            if (deletedIds.isNotEmpty()) {
                db.appTags().deleteIds(deletedIds)
            }
        }

        suspend fun insert(tag: Tag, apps: List<String>, db: AppsDatabase) {
            db.withTransaction {
                for (appId in apps) {
                    val values = AppTag(appId, tag.id).contentValues
                    db.openHelper.writableDatabase.insert(TABLE, SQLiteDatabase.CONFLICT_REPLACE, values)
                }
            }
        }

        // Executed in transaction
        suspend fun insert(appTags: List<AppTag>, db: AppsDatabase) = withContext(Dispatchers.IO) {
            appTags.forEach { appTag ->
                db.openHelper.writableDatabase.insert(TABLE, SQLiteDatabase.CONFLICT_REPLACE, appTag.contentValues)
            }
        }

        suspend fun insert(tagId: Int, appId: String, db: AppsDatabase): Long {
            return db.withTransaction {
                return@withTransaction db.openHelper.writableDatabase.insert(TABLE, SQLiteDatabase.CONFLICT_REPLACE, AppTag(appId, tagId).contentValues)
            }
        }

        suspend fun assignAppsToTag(appIds: List<String>, tagId: Int, db: AppsDatabase) {
            db.withTransaction {
                db.appTags().delete(tagId)
                for (appId in appIds) {
                    val values = AppTag(appId, tagId).contentValues
                    db.openHelper.writableDatabase.insert(TABLE, SQLiteDatabase.CONFLICT_REPLACE, values)
                }
            }
        }
    }
}

val AppTag.contentValues: ContentValues
    get() {
        val values = ContentValues()
        values.put(AppTagsTable.Columns.APP_ID, appId)
        values.put(AppTagsTable.Columns.TAGS_ID, tagId)
        return values
    }