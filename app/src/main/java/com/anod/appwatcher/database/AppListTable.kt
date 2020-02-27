package com.anod.appwatcher.database

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.provider.BaseColumns
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.withTransaction
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.anod.appwatcher.database.entities.*
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.model.AppInfoMetadata
import com.anod.appwatcher.preferences.Preferences
import info.anodsplace.framework.AppLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

@Dao
interface AppListTable {

    @RawQuery(observedEntities = [(App::class),(AppChange::class),(AppTag::class)])
    fun observe(query: SupportSQLiteQuery): LiveData<List<AppListItem>>

    @Query("SELECT * FROM $table WHERE ${Columns.appId} == :appId")
    suspend fun loadApp(appId: String): App?

    @Query("SELECT * FROM $table WHERE ${BaseColumns._ID} == :rowId")
    suspend fun loadAppRow(rowId: Int): App?

    @Query("SELECT ${BaseColumns._ID}, ${Columns.packageName} FROM $table WHERE " +
            "${Columns.status} != ${AppInfoMetadata.STATUS_DELETED}")
    fun observePackages(): LiveData<List<PackageRowPair>>

    @Query("SELECT ${BaseColumns._ID}, ${Columns.packageName} FROM $table WHERE " +
            "CASE :includeDeleted WHEN 'false' THEN ${Columns.status} != ${AppInfoMetadata.STATUS_DELETED} ELSE ${Columns.status} >= ${AppInfoMetadata.STATUS_NORMAL} END")
    suspend fun loadPackages(includeDeleted: Boolean): List<PackageRowPair>

    @Query("SELECT $table.*, " +
            "CASE WHEN ${Columns.updateTimestamp} > :recentTime THEN 1 ELSE 0 END ${Columns.recentFlag} " +
            "FROM $table WHERE " +
            "CASE :includeDeleted WHEN 'false' THEN ${Columns.status} != ${AppInfoMetadata.STATUS_DELETED} ELSE ${Columns.status} >= ${AppInfoMetadata.STATUS_NORMAL} END ")
    fun load(includeDeleted: Boolean, recentTime: Long): Cursor

    @Query("SELECT $table.*, ${ChangelogTable.TableColumns.details}, ${ChangelogTable.TableColumns.noNewDetails}, " +
            "CASE WHEN ${Columns.updateTimestamp} > :recentTime THEN 1 ELSE 0 END ${Columns.recentFlag} " +
            "FROM $table " +
            "LEFT JOIN ${ChangelogTable.table} ON " +
            "${TableColumns.appId} == ${ChangelogTable.TableColumns.appId} " +
            "AND ${TableColumns.versionNumber} == ${ChangelogTable.TableColumns.versionCode} ")
    fun loadAppList(recentTime: Long): Cursor

    @Query("SELECT COUNT(${BaseColumns._ID}) " +
            "FROM $table WHERE " +
            "CASE :includeDeleted WHEN 'false' THEN ${Columns.status} != ${AppInfoMetadata.STATUS_DELETED} ELSE ${Columns.status} >= ${AppInfoMetadata.STATUS_NORMAL} END ")
    suspend fun count(includeDeleted: Boolean): Int

    @Query("DELETE FROM $table WHERE ${Columns.status} == ${AppInfoMetadata.STATUS_DELETED}")
    suspend fun cleanDeleted(): Int

    @Query("DELETE FROM $table")
    suspend fun delete()

    @Query("UPDATE $table SET ${Columns.status} = :status WHERE ${BaseColumns._ID} = :rowId")
    suspend fun updateStatus(rowId: Int, status: Int): Int

    object Queries {

        suspend fun load(includeDeleted: Boolean, table: AppListTable): AppListCursor = withContext(Dispatchers.IO) {
            val cursor = table.load(includeDeleted, recentTime)
            return@withContext AppListCursor(cursor)
        }

        suspend fun loadAppList(table: AppListTable): AppListItemCursor = withContext(Dispatchers.IO) {
            val cursor = table.loadAppList(recentTime)
            return@withContext AppListItemCursor(cursor)
        }

        fun loadAppList(sortId: Int, titleFilter: String, table: AppListTable): LiveData<List<AppListItem>> {
            return loadAppList(sortId, false, null, titleFilter, table)
        }

        fun loadAppList(sortId: Int, orederByRecentlyUpdated: Boolean, tag: Tag?, titleFilter: String, table: AppListTable): LiveData<List<AppListItem>> {
            val tables = if (tag == null) AppListTable.table else AppTagsTable.table + ", " + AppListTable.table
            val selection = createSelection(tag, titleFilter)

            val sql = "SELECT ${AppListTable.table}.*, ${ChangelogTable.TableColumns.details}, ${ChangelogTable.TableColumns.noNewDetails}, " +
                    "CASE WHEN ${Columns.updateTimestamp} > $recentTime THEN 1 ELSE 0 END ${Columns.recentFlag} " +
                    "FROM $tables " +
                    "LEFT JOIN ${ChangelogTable.table} ON " +
                    "${TableColumns.appId} == ${ChangelogTable.TableColumns.appId} " +
                    "AND ${TableColumns.versionNumber} == ${ChangelogTable.TableColumns.versionCode} " +
                    "WHERE ${selection.first} " +
                    "ORDER BY ${createSortOrder(sortId, orederByRecentlyUpdated)} "
            return table.observe(SimpleSQLiteQuery(sql, selection.second))
        }

        suspend fun insert(app: AppInfo, db: AppsDatabase): Long = withContext(Dispatchers.IO) {
            // Skip id to apply autoincrement
            return@withContext db.runInTransaction(Callable<Long> {
                db.openHelper.writableDatabase.insert(table, SQLiteDatabase.CONFLICT_REPLACE, app.contentValues)
            })
        }

        suspend fun insert(apps: List<AppInfo>, db: AppsDatabase) = withContext(Dispatchers.IO) {
            AppLog.d("insert " + apps.size)
            apps.forEach {
                AppLog.d("insert " + it.appId)
                val rowId = db.openHelper.writableDatabase.insert(table, SQLiteDatabase.CONFLICT_REPLACE, it.contentValues)
                AppLog.d("insert result $rowId")
            }
        }

        suspend fun delete(appId: String, db: AppsDatabase): Int {
            return db.withTransaction {
                return@withTransaction db.openHelper.writableDatabase.delete(table, "${Columns.appId} = ?", arrayOf(appId))
            }
        }

        private fun createSortOrder(sortId: Int, orederByRecentlyUpdated: Boolean): String {
            val filter = mutableListOf(
                    Columns.status + " DESC"
            )
            if (orederByRecentlyUpdated) {
                filter.add(Columns.recentFlag + " DESC")
            }
            when (sortId) {
                Preferences.SORT_NAME_DESC -> filter.add(Columns.title + " COLLATE NOCASE DESC")
                Preferences.SORT_DATE_ASC -> filter.add(Columns.uploadTimestamp + " ASC")
                Preferences.SORT_DATE_DESC -> filter.add(Columns.uploadTimestamp + " DESC")
                else -> filter.add(Columns.title + " COLLATE NOCASE ASC")
            }
            return filter.joinToString(", ")
        }

        private fun createSelection(tag: Tag?, titleFilter: String): Pair<String, Array<String>> {
            val selc = ArrayList<String>(3)
            val args = ArrayList<String>(3)

            selc.add(Columns.status + " != ?")
            args.add(AppInfoMetadata.STATUS_DELETED.toString())

            if (tag != null) {
                selc.add(AppTagsTable.TableColumns.tagId + " = ?")
                args.add(tag.id.toString())
                selc.add(AppTagsTable.TableColumns.appId + " = " + TableColumns.appId)
            }

            if (!TextUtils.isEmpty(titleFilter)) {
                selc.add(Columns.title + " LIKE ?")
                args.add("%$titleFilter%")
            }

            return Pair(selc.joinToString(" AND "), args.toTypedArray())
        }

        suspend fun insertSafetly(app: AppInfo, db: AppsDatabase): Int {
            val found = db.apps().loadApp(app.appId)
            if (found == null) {
                val rowId = insert(app, db)
                return if (rowId > 0) {
                    rowId.toInt()
                } else {
                    ERROR_INSERT
                }
            } else {
                if (found.status == AppInfoMetadata.STATUS_DELETED) {
                    db.apps().updateStatus(found.rowId, AppInfoMetadata.STATUS_NORMAL)
                    return found.rowId
                }
            }
            return ERROR_ALREADY_ADDED
        }
    }


    class Columns : BaseColumns {
        companion object {
            const val appId = "app_id"
            const val packageName = "package"
            const val versionNumber = "ver_num"
            const val versionName = "ver_name"
            const val title = "title"
            const val creator = "creator"
            const val iconCache = "icon"
            const val iconUrl = "iconUrl"
            const val status = "status"
            const val uploadTimestamp = "update_date"
            const val priceText = "price_text"
            const val priceCurrency = "price_currency"
            const val priceMicros = "price_micros"
            const val uploadDate = "upload_date"
            const val detailsUrl = "details_url"
            const val appType = "app_type"
            const val updateTimestamp = "sync_version"
            const val recentFlag = "recent_flag"
        }
    }

    object TableColumns {
        const val _ID = table + "." + BaseColumns._ID
        const val appId = "$table.app_id"
        const val versionNumber = "$table.ver_num"
    }

    companion object {

        const val ERROR_ALREADY_ADDED = -1
        const val ERROR_INSERT = -2

        const val table = "app_list"
        private const val recentDays: Long = 3

        val recentTime: Long
            get() {
                val timestamp = System.currentTimeMillis()
                // https://stackoverflow.com/questions/13892163/get-timestamp-for-start-of-day
                // val dayEnd = dayStart + 86399999
                val dayStart = timestamp - (timestamp % 86400000)
                return dayStart - TimeUnit.DAYS.toMillis(recentDays)
            }

        val projection: Array<String>
            get() = projection(recentTime)

        private fun projection(recentTime: Long): Array<String> {
            return arrayOf(
                    TableColumns._ID,
                    TableColumns.appId,
                    Columns.packageName,
                    Columns.versionNumber,
                    Columns.versionName,
                    Columns.title,
                    Columns.creator,
                    Columns.status,
                    Columns.uploadTimestamp,
                    Columns.priceText,
                    Columns.priceCurrency,
                    Columns.priceMicros,
                    Columns.uploadDate,
                    Columns.detailsUrl,
                    Columns.iconUrl,
                    Columns.appType,
                    Columns.updateTimestamp,
                    "case " +
                            "when ${Columns.updateTimestamp} > $recentTime then 1 " +
                            "else 0 end ${Columns.recentFlag}")
        }
    }
}

val AppInfo.contentValues: ContentValues
    get() = ContentValues().apply {
        if (rowId > 0) {
            put(BaseColumns._ID, rowId)
        }
        put(AppListTable.Columns.appId, appId)
        put(AppListTable.Columns.packageName, packageName)
        put(AppListTable.Columns.title, title)
        put(AppListTable.Columns.versionNumber, versionNumber)
        put(AppListTable.Columns.versionName, versionName)
        put(AppListTable.Columns.creator, creator)
        put(AppListTable.Columns.status, status)
        put(AppListTable.Columns.uploadDate, uploadDate)

        put(AppListTable.Columns.priceText, priceText)
        put(AppListTable.Columns.priceCurrency, priceCur)
        put(AppListTable.Columns.priceMicros, priceMicros)

        put(AppListTable.Columns.detailsUrl, detailsUrl)

        put(AppListTable.Columns.iconUrl, iconUrl)
        put(AppListTable.Columns.uploadTimestamp, uploadTime)

        put(AppListTable.Columns.appType, appType)
        put(AppListTable.Columns.updateTimestamp, updateTime)
    }