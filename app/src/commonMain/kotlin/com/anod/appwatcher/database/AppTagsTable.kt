package com.anod.appwatcher.database

import androidx.room.*
import com.anod.appwatcher.database.entities.AppTag
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.database.entities.TagAppsCount
import kotlinx.coroutines.flow.Flow

/**
 * @author alex
 * *
 * @date 2015-03-01
 */
@Dao
interface AppTagsTable {

    @Query("SELECT * FROM $table WHERE ${Columns.tagId} = :tagId")
    fun forTag(tagId: Int): Flow<List<AppTag>>

    @Query("SELECT * FROM $table WHERE ${Columns.appId} = :appId")
    fun forApp(appId: String): Flow<List<AppTag>>

    @Query("SELECT IFNULL(tags_id, 0) AS tags_id, count() as count FROM app_list l " +
            "LEFT JOIN app_tags t ON l.app_id = t.app_id " +
            "GROUP BY tags_id")
    fun queryCounts(): Flow<List<TagAppsCount>>

    @Query("SELECT * FROM $table")
    suspend fun load(): List<AppTag>

    @Query("SELECT DISTINCT ${TableColumns.tagId} FROM $table")
    suspend fun loadTagIds(): List<Int>

    @Query("DELETE FROM $table WHERE ${TableColumns.appId} NOT IN (SELECT ${AppListTable.TableColumns.appId} FROM ${AppListTable.table})")
    suspend fun clean(): Int

    @Query("DELETE FROM $table")
    suspend fun delete()

    @Query("DELETE FROM $table WHERE ${Columns.tagId} IN (:tagIds)")
    suspend fun deleteIds(tagIds: List<Int>)

    @Query("DELETE FROM $table WHERE ${Columns.tagId} = :tagId")
    suspend fun delete(tagId: Int)

    @Query("DELETE FROM $table WHERE ${Columns.tagId} = :tagId AND ${Columns.appId} = :appId")
    suspend fun delete(tagId: Int, appId: String): Int

    @Query("INSERT INTO $table (${Columns.appId}, ${Columns.tagId}) VALUES (:appId, :tagId)")
    suspend fun insert(appId: String, tagId: Int): Long
    
    // TODO: autoincrement check
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @Transaction
    suspend fun insert(tag: AppTag): Long

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

    companion object {
        const val table = "app_tags"
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
                    db.appTags().insert(AppTag(appId, tag.id))
                }
            }
        }

        // Executed in transaction
        suspend fun insert(appTags: List<AppTag>, db: AppsDatabase)  {
            appTags.forEach { appTag ->
                db.appTags().insert(appTag)
            }
        }

        suspend fun assignAppsToTag(appIds: List<String>, tagId: Int, db: AppsDatabase) {
            db.withTransaction {
                db.appTags().delete(tagId)
                for (appId in appIds) {
                    db.appTags().insert(AppTag(appId, tagId))
                }
            }
        }
    }
}
