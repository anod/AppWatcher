package com.anod.appwatcher.database

import androidx.lifecycle.LiveData
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.provider.BaseColumns
import android.text.TextUtils
import androidx.room.*
import com.anod.appwatcher.database.entities.*
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.model.AppInfoMetadata
import com.anod.appwatcher.preferences.Preferences
import info.anodsplace.framework.AppLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.ArrayList
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

        suspend fun load(includeDeleted: Boolean, table: AppListTable): Cursor = withContext(Dispatchers.IO) {
            return@withContext table.load(includeDeleted, recentTime)
        }

        fun loadAppList(sortId: Int, titleFilter: String, table: AppListTable): LiveData<List<AppListItem>> {
            return loadAppList(sortId, null, titleFilter, table)
        }

        fun loadAppList(sortId: Int, tag: Tag?, titleFilter: String, table: AppListTable): LiveData<List<AppListItem>> {
            val tables = if (tag == null) AppListTable.table else AppTagsTable.table + ", " + AppListTable.table
            val selection = createSelection(tag, titleFilter)

            val sql = "SELECT ${AppListTable.table}.*, ${ChangelogTable.TableColumns.details}, " +
                    "CASE WHEN ${Columns.updateTimestamp} > $recentTime THEN 1 ELSE 0 END ${Columns.recentFlag} " +
                    "FROM $tables " +
                    "LEFT JOIN ${ChangelogTable.table} ON " +
                    "${TableColumns.appId} == ${ChangelogTable.TableColumns.appId} " +
                    "AND ${TableColumns.versionNumber} == ${ChangelogTable.TableColumns.versionCode} " +
                    "WHERE ${selection.first} " +
                    "ORDER BY ${createSortOrder(sortId)} "
            return table.observe(SimpleSQLiteQuery(sql, selection.second))
        }

        suspend fun insert(app: AppInfo, db: AppsDatabase): Long {
            var rowId: Long = -1
            withContext(Dispatchers.IO) {
                // Skip id to apply autoincrement
                rowId = db.runInTransaction(Callable<Long> {
                    db.openHelper.writableDatabase.insert(table, SQLiteDatabase.CONFLICT_REPLACE, app.contentValues)
                })
            }
            return rowId
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

        private fun createSortOrder(sortId: Int): String {
            val filter = ArrayList<String>()
            filter.add(Columns.status + " DESC")
            filter.add(Columns.recentFlag + " DESC")
            when (sortId) {
                Preferences.SORT_NAME_DESC -> filter.add(Columns.title + " COLLATE NOCASE DESC")
                Preferences.SORT_DATE_ASC -> filter.add(Columns.uploadTimestamp + " ASC")
                Preferences.SORT_DATE_DESC -> filter.add(Columns.uploadTimestamp + " DESC")
                else -> filter.add(Columns.title + " COLLATE NOCASE ASC")
            }
            return TextUtils.join(", ", filter)
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

            return Pair(TextUtils.join(" AND ", selc), args.toTypedArray())
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

    object Projection {
        const val _ID = 0
        const val appId = 1
        const val packageName = 2
        const val versionNumber = 3
        const val versionName = 4
        const val title = 5
        const val creator = 6
        const val status = 7
        const val uploadTime = 8
        const val priceText = 9
        const val priceCurrency = 10
        const val priceMicros = 11
        const val uploadDate = 12
        const val detailsUrl = 13
        const val iconUrl = 14
        const val appType = 15
        const val refreshTime = 16
        const val recentFlag = 17
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
    get() {
        val values = ContentValues()

        if (rowId > 0) {
            values.put(BaseColumns._ID, rowId)
        }
        values.put(AppListTable.Columns.appId, appId)
        values.put(AppListTable.Columns.packageName, packageName)
        values.put(AppListTable.Columns.title, title)
        values.put(AppListTable.Columns.versionNumber, versionNumber)
        values.put(AppListTable.Columns.versionName, versionName)
        values.put(AppListTable.Columns.creator, creator)
        values.put(AppListTable.Columns.status, status)
        values.put(AppListTable.Columns.uploadDate, uploadDate)

        values.put(AppListTable.Columns.priceText, priceText)
        values.put(AppListTable.Columns.priceCurrency, priceCur)
        values.put(AppListTable.Columns.priceMicros, priceMicros)

        values.put(AppListTable.Columns.detailsUrl, detailsUrl)

        values.put(AppListTable.Columns.iconUrl, iconUrl)
        values.put(AppListTable.Columns.uploadTimestamp, uploadTime)

        values.put(AppListTable.Columns.appType, appType)
        values.put(AppListTable.Columns.updateTimestamp, updateTime)
        return values
    }
