package com.anod.appwatcher.database

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.provider.BaseColumns
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.withTransaction
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.AppChange
import com.anod.appwatcher.database.entities.AppListItem
import com.anod.appwatcher.database.entities.AppTag
import com.anod.appwatcher.database.entities.PackageRowPair
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.preferences.Preferences
import info.anodsplace.ktx.chunked
import info.anodsplace.ktx.dayStartAgoMillis
import java.util.concurrent.Callable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

data class AppListRowSnapshot(
    @ColumnInfo(name = BaseColumns._ID)
    val rowId: Int,

    @ColumnInfo(name = AppListTable.Columns.STATUS)
    val status: Int,

    @ColumnInfo(name = AppListTable.Columns.RECENT_FLAG)
    val recentFlag: Boolean,
)

data class AppSyncUpdate(
    val rowId: Long,
    val expectedAppId: String,
    val expectedPackageName: String,
    val values: ContentValues,
    val changelogValues: ContentValues?
)

@Dao
interface AppListTable {

    @RawQuery(observedEntities = [(App::class), (AppChange::class), (AppTag::class)])
    fun observeRows(query: SupportSQLiteQuery): Flow<List<Int>>

    @RawQuery(observedEntities = [(App::class), (AppChange::class), (AppTag::class)])
    fun observe(query: SupportSQLiteQuery): Flow<List<AppListItem>>

    @RawQuery(observedEntities = [(App::class), (AppChange::class), (AppTag::class)])
    suspend fun load(query: SupportSQLiteQuery): List<AppListItem>

    @RawQuery
    suspend fun loadRowSnapshots(query: SupportSQLiteQuery): List<AppListRowSnapshot>

    @Query("SELECT * FROM $TABLE WHERE ${Columns.APP_ID} == :appId")
    fun observeApp(appId: String): Flow<App?>

    @Query("SELECT * FROM $TABLE WHERE ${Columns.APP_ID} == :appId")
    suspend fun loadApp(appId: String): App?

    @Query("SELECT * FROM $TABLE WHERE ${BaseColumns._ID} == :rowId")
    suspend fun loadAppRow(rowId: Int): App?

    @Suppress("FunctionName")
    @Query(
        "SELECT ${BaseColumns._ID}, ${Columns.PACKAGE_NAME} FROM $TABLE " +
            "WHERE ${Columns.PACKAGE_NAME} IN (:packageNames) AND ${Columns.STATUS} != ${App.STATUS_DELETED}"
    )
    suspend fun _loadRowIds(packageNames: List<String>): List<PackageRowPair>

    suspend fun loadRowIds(packageNames: List<String>): List<PackageRowPair> = packageNames.chunked({ _loadRowIds(it) })

    @Query(
        "SELECT ${BaseColumns._ID}, ${Columns.PACKAGE_NAME} FROM $TABLE WHERE " +
            "${Columns.STATUS} != ${App.STATUS_DELETED}"
    )
    fun observePackagesList(): Flow<List<PackageRowPair>>

    @Query(
        "SELECT ${BaseColumns._ID}, ${Columns.PACKAGE_NAME} FROM $TABLE WHERE " +
            "CASE :includeDeleted WHEN 0 THEN ${Columns.STATUS} != ${App.STATUS_DELETED} ELSE ${Columns.STATUS} >= ${App.STATUS_NORMAL} END"
    )
    suspend fun loadPackages(includeDeleted: Boolean): List<PackageRowPair>

    @Query(
        "SELECT $TABLE.*, " +
            "CASE WHEN ${Columns.SYNC_TIMESTAMP} > :recentTime THEN 1 ELSE 0 END ${Columns.RECENT_FLAG} " +
            "FROM $TABLE WHERE " +
            "CASE :includeDeleted WHEN 0 THEN ${Columns.STATUS} != ${App.STATUS_DELETED} ELSE ${Columns.STATUS} >= ${App.STATUS_NORMAL} END "
    )
    fun load(includeDeleted: Boolean, recentTime: Long): Cursor

    @Query(
        "SELECT $TABLE.*, ${ChangelogTable.TableColumns.DETAILS}, ${ChangelogTable.TableColumns.NO_NEW_DETAILS}, " +
            "CASE WHEN ${Columns.SYNC_TIMESTAMP} > :recentTime THEN 1 ELSE 0 END ${Columns.RECENT_FLAG} " +
            "FROM $TABLE " +
            "LEFT JOIN ${ChangelogTable.TABLE} ON " +
            "${TableColumns.APP_ID} == ${ChangelogTable.TableColumns.APP_ID} " +
            "AND ${TableColumns.VERSION_NUMBER} == ${ChangelogTable.TableColumns.VERSION_CODE} " +
            "WHERE " +
            "CASE :includeDeleted WHEN 0 THEN ${Columns.STATUS} != ${App.STATUS_DELETED} ELSE ${Columns.STATUS} >= ${App.STATUS_NORMAL} END " +
            "ORDER BY " +
            "CASE WHEN :sortId = 0 THEN ${Columns.TITLE} COLLATE NOCASE END ASC, " +
            "CASE WHEN :sortId = 1 THEN ${Columns.TITLE} COLLATE NOCASE END DESC, " +
            "CASE WHEN :sortId = 2 THEN ${Columns.UPLOAD_TIMESTAMP} END ASC, " +
            "CASE WHEN :sortId = 3 THEN ${Columns.UPLOAD_TIMESTAMP} END DESC, " +
            "$TABLE.${BaseColumns._ID} ASC "
    )
    fun loadAppList(includeDeleted: Boolean, sortId: Int, recentTime: Long): Cursor

    @Query(
        "SELECT COUNT(${BaseColumns._ID}) " +
            "FROM $TABLE WHERE " +
            "CASE :includeDeleted WHEN 0 THEN ${Columns.STATUS} != ${App.STATUS_DELETED} ELSE ${Columns.STATUS} >= ${App.STATUS_NORMAL} END "
    )
    suspend fun count(includeDeleted: Boolean): Int

    @Query("DELETE FROM $TABLE WHERE ${Columns.STATUS} == ${App.STATUS_DELETED}")
    suspend fun cleanDeleted(): Int

    @Query("DELETE FROM $TABLE")
    suspend fun delete()

    @Query("UPDATE $TABLE SET ${Columns.STATUS} = :status WHERE ${BaseColumns._ID} = :rowId")
    suspend fun updateStatus(rowId: Int, status: Int): Int

    @Query(
        "UPDATE $TABLE SET " +
            "${Columns.TITLE} = :title, " +
            "${Columns.CREATOR} = :creator, " +
            "${Columns.ICON_URL} = :iconUrl, " +
            "${Columns.DETAILS_URL} = :detailsUrl " +
            "WHERE ${BaseColumns._ID} = :rowId"
    )
    suspend fun updateMetadata(rowId: Int, title: String, creator: String, iconUrl: String, detailsUrl: String?): Int

    @Query(
        "INSERT INTO $TABLE (" +
            "${Columns.APP_ID}," +
            "${Columns.PACKAGE_NAME}," +
            "${Columns.VERSION_NUMBER}," +
            "${Columns.VERSION_NAME}," +
            "${Columns.TITLE}," +
            "${Columns.CREATOR}," +
            "${Columns.ICON_URL}," +
            "${Columns.STATUS}," +
            "${Columns.UPLOAD_DATE}," +
            "${Columns.PRICE_TEXT}," +
            "${Columns.PRICE_CURRENCY}," +
            "${Columns.PRICE_MICROS}," +
            "${Columns.DETAILS_URL}," +
            "${Columns.UPLOAD_TIMESTAMP}," +
            "${Columns.APP_TYPE}," +
            "${Columns.SYNC_TIMESTAMP}) VALUES (" +
            ":appId, :packageName, :versionNumber, :versionName, :title, " +
            ":creator, :iconUrl, :status, :uploadDate, " +
            ":priceText, :priceCurrency, :priceMicros, " +
            ":detailsUrl, :uploadTime, :appType, :updateTime" +
            ")"
    )
    suspend fun insert(
        appId: String,
        packageName: String,
        versionNumber: Int,
        versionName: String,
        title: String,
        creator: String,
        iconUrl: String,
        status: Int,
        uploadDate: String,
        priceText: String,
        priceCurrency: String,
        priceMicros: Int?,
        detailsUrl: String?,
        uploadTime: Long,
        appType: String,
        updateTime: Long
    ): Long

    object Queries {

        suspend fun load(includeDeleted: Boolean, table: AppListTable): AppListCursor = withContext(Dispatchers.IO) {
            val cursor = table.load(includeDeleted, recentTime)
            return@withContext AppListCursor(cursor)
        }

        suspend fun loadAppList(includeDeleted: Boolean, sortId: Int, table: AppListTable): AppListItemCursor = withContext(Dispatchers.IO) {
            val cursor = table.loadAppList(includeDeleted, sortId, recentTime)
            return@withContext AppListItemCursor(cursor)
        }

        fun loadAppList(sortId: Int, titleFilter: String, table: AppListTable): Flow<List<AppListItem>> = loadAppList(sortId, false, null, titleFilter, table)

        fun changes(table: AppListTable): Flow<List<Int>> = table.observeRows(
            SimpleSQLiteQuery(
                "SELECT ${BaseColumns._ID} FROM ${AppListTable.TABLE} LIMIT 1",
                emptyArray()
            )
        )

        private fun loadAppList(
            sortId: Int,
            orderByRecentlyUpdated: Boolean,
            tagId: Int?,
            titleFilter: String,
            table: AppListTable
        ): Flow<List<AppListItem>> {
            val query = createAppsListQuery(sortId, orderByRecentlyUpdated, tagId, titleFilter)
            return table.observe(SimpleSQLiteQuery(query.first, query.second))
        }

        suspend fun loadAppListRows(
            sortId: Int,
            orderByRecentlyDiscovered: Boolean,
            tagId: Int?,
            titleFilter: String,
            table: AppListTable
        ): List<AppListRowSnapshot> {
            val query = createAppsListRowsQuery(sortId, orderByRecentlyDiscovered, tagId, titleFilter)
            return table.loadRowSnapshots(SimpleSQLiteQuery(query.first, query.second))
        }

        suspend fun loadAppList(rowIds: List<Int>, table: AppListTable): List<AppListItem> {
            if (rowIds.isEmpty()) {
                return emptyList()
            }
            val query = createAppsListRowsQuery(rowIds)
            return table.load(SimpleSQLiteQuery(query))
        }

        internal fun createAppsListQuery(
            sortId: Int,
            orderByRecentlyDiscovered: Boolean,
            tagId: Int?,
            titleFilter: String
        ): Pair<String, Array<String>> {
            val selection = createSelection(tagId, titleFilter)

            val sql =
                "SELECT $TABLE.*, ${ChangelogTable.TableColumns.DETAILS}, ${ChangelogTable.TableColumns.NO_NEW_DETAILS}, " +
                    "CASE WHEN ${Columns.SYNC_TIMESTAMP} > $recentTime THEN 1 ELSE 0 END ${Columns.RECENT_FLAG} " +
                    "FROM $TABLE " +
                    "LEFT JOIN ${ChangelogTable.TABLE} ON ${TableColumns.APP_ID} == ${ChangelogTable.TableColumns.APP_ID} " +
                    "AND ${TableColumns.VERSION_NUMBER} == ${ChangelogTable.TableColumns.VERSION_CODE} " +
                    "WHERE ${selection.first} " +
                    "ORDER BY ${createSortOrder(sortId, orderByRecentlyDiscovered)} "
            return Pair(sql, selection.second)
        }

        internal fun createAppsListRowsQuery(
            sortId: Int,
            orderByRecentlyDiscovered: Boolean,
            tagId: Int?,
            titleFilter: String
        ): Pair<String, Array<String>> {
            val selection = createSelection(tagId, titleFilter)
            val sql =
                "SELECT $TABLE.${BaseColumns._ID}, ${Columns.STATUS}, " +
                    "CASE WHEN ${Columns.SYNC_TIMESTAMP} > $recentTime THEN 1 ELSE 0 END ${Columns.RECENT_FLAG} " +
                    "FROM $TABLE " +
                    "WHERE ${selection.first} " +
                    "ORDER BY ${createSortOrder(sortId, orderByRecentlyDiscovered)} "
            return Pair(sql, selection.second)
        }

        private fun createAppsListRowsQuery(rowIds: List<Int>): String {
            val rowIdsSql = rowIds.joinToString(",")
            val orderSql = rowIds.mapIndexed { index, rowId -> "WHEN $rowId THEN $index" }.joinToString(" ")
            return "SELECT $TABLE.*, ${ChangelogTable.TableColumns.DETAILS}, ${ChangelogTable.TableColumns.NO_NEW_DETAILS}, " +
                "CASE WHEN ${Columns.SYNC_TIMESTAMP} > $recentTime THEN 1 ELSE 0 END ${Columns.RECENT_FLAG} " +
                "FROM $TABLE " +
                "LEFT JOIN ${ChangelogTable.TABLE} ON ${TableColumns.APP_ID} == ${ChangelogTable.TableColumns.APP_ID} " +
                "AND ${TableColumns.VERSION_NUMBER} == ${ChangelogTable.TableColumns.VERSION_CODE} " +
                "WHERE $TABLE.${BaseColumns._ID} IN ($rowIdsSql) " +
                "AND ${Columns.STATUS} != ${App.STATUS_DELETED} " +
                "ORDER BY CASE $TABLE.${BaseColumns._ID} $orderSql END"
        }

        suspend fun insert(app: App, db: AppsDatabase): Long = withContext(Dispatchers.IO) {
            // Skip id to apply autoincrement
            return@withContext db.runInTransaction(Callable {
                db.openHelper.writableDatabase.insert(
                    TABLE,
                    SQLiteDatabase.CONFLICT_REPLACE,
                    app.contentValues
                )
            })
        }

        suspend fun applySyncUpdates(
            updates: List<AppSyncUpdate>,
            db: AppsDatabase
        ): Set<Long> = withContext(Dispatchers.IO) {
            val appliedRowIds = mutableSetOf<Long>()
            db.withTransaction {
                val writableDatabase = db.openHelper.writableDatabase
                updates.forEach { update ->
                    val updated = writableDatabase.update(
                        TABLE,
                        SQLiteDatabase.CONFLICT_REPLACE,
                        update.values,
                        "${BaseColumns._ID}=? AND " +
                            "${Columns.APP_ID}=? AND " +
                            "${Columns.PACKAGE_NAME}=? AND " +
                            "${Columns.STATUS}!=?",
                        arrayOf<Any>(
                            update.rowId,
                            update.expectedAppId,
                            update.expectedPackageName,
                            App.STATUS_DELETED
                        )
                    )
                    if (updated == 0) {
                        return@forEach
                    }
                    check(updated == 1) {
                        "Multiple app rows matched synchronization update ${update.rowId}"
                    }
                    if (update.changelogValues != null) {
                        val changelogRowId = writableDatabase.insert(
                            ChangelogTable.TABLE,
                            SQLiteDatabase.CONFLICT_REPLACE,
                            update.changelogValues
                        )
                        check(changelogRowId != -1L) {
                            "Unable to persist changelog for app row ${update.rowId}"
                        }
                    }
                    appliedRowIds.add(update.rowId)
                }
            }
            appliedRowIds
        }

        suspend fun delete(appId: String, db: AppsDatabase): Int {
            return db.withTransaction {
                return@withTransaction db.openHelper.writableDatabase.delete(
                    TABLE,
                    "${Columns.APP_ID} = ?",
                    arrayOf(appId)
                )
            }
        }

        private fun createSortOrder(sortId: Int, orderByRecentlyUpdated: Boolean): String {
            val filter = mutableListOf(
                Columns.STATUS + " DESC"
            )
            if (orderByRecentlyUpdated) {
                filter.add("CASE WHEN ${Columns.SYNC_TIMESTAMP} > $recentTime THEN 1 ELSE 0 END DESC")
            }
            when (sortId) {
                Preferences.SORT_NAME_DESC -> filter.add(Columns.TITLE + " COLLATE NOCASE DESC")
                Preferences.SORT_DATE_ASC -> filter.add(Columns.UPLOAD_TIMESTAMP + " ASC")
                Preferences.SORT_DATE_DESC -> filter.add(Columns.UPLOAD_TIMESTAMP + " DESC")
                else -> filter.add(Columns.TITLE + " COLLATE NOCASE ASC")
            }
            filter.add("$TABLE.${BaseColumns._ID} ASC")
            return filter.joinToString(", ")
        }

        private fun createSelection(tagId: Int?, titleFilter: String): Pair<String, Array<String>> {
            val selc = ArrayList<String>(3)
            val args = ArrayList<String>(3)

            selc.add(Columns.STATUS + " != ?")
            args.add(App.STATUS_DELETED.toString())

            if (tagId != null) {
                if (tagId == Tag.empty.id) {
                    selc.add(
                        "NOT EXISTS (" +
                            "SELECT 1 FROM ${AppTagsTable.TABLE} " +
                            "WHERE ${AppTagsTable.TableColumns.APP_ID} = ${TableColumns.APP_ID}" +
                            ")"
                    )
                } else {
                    selc.add(
                        "EXISTS (" +
                            "SELECT 1 FROM ${AppTagsTable.TABLE} " +
                            "WHERE ${AppTagsTable.TableColumns.APP_ID} = ${TableColumns.APP_ID} " +
                            "AND ${AppTagsTable.TableColumns.TAG_ID} = ?" +
                            ")"
                    )
                    args.add(tagId.toString())
                }
            }

            if (titleFilter.isNotEmpty()) {
                selc.add(Columns.TITLE + " LIKE ?")
                args.add("%$titleFilter%")
            }

            return Pair(selc.joinToString(" AND "), args.toTypedArray())
        }

        suspend fun insertSafetly(app: App, db: AppsDatabase): Int {
            val found = db.apps().loadApp(app.appId)
            if (found == null) {
                val rowId = insert(app, db)
                return if (rowId > 0) {
                    rowId.toInt()
                } else {
                    ERROR_INSERT
                }
            } else {
                if (found.status == App.STATUS_DELETED) {
                    db.apps().updateStatus(found.rowId, App.STATUS_NORMAL)
                    return found.rowId
                }
            }
            return ERROR_ALREADY_ADDED
        }
    }

    class Columns : BaseColumns {
        companion object {
            const val APP_ID = "app_id"
            const val PACKAGE_NAME = "package"
            const val VERSION_NUMBER = "ver_num"
            const val VERSION_NAME = "ver_name"
            const val TITLE = "title"
            const val CREATOR = "creator"
            const val ICON_CACHE = "icon"
            const val ICON_URL = "iconUrl"
            const val STATUS = "status"
            const val UPLOAD_TIMESTAMP = "update_date"
            const val PRICE_TEXT = "price_text"
            const val PRICE_CURRENCY = "price_currency"
            const val PRICE_MICROS = "price_micros"
            const val UPLOAD_DATE = "upload_date"
            const val DETAILS_URL = "details_url"
            const val APP_TYPE = "app_type"
            const val SYNC_TIMESTAMP = "sync_version"
            const val RECENT_FLAG = "recent_flag"
        }
    }

    object TableColumns {
        const val BASE_ID = TABLE + "." + BaseColumns._ID
        const val APP_ID = "$TABLE.app_id"
        const val VERSION_NUMBER = "$TABLE.ver_num"
    }

    companion object {
        const val ERROR_ALREADY_ADDED = -1
        const val ERROR_INSERT = -2
        const val TABLE = "app_list"

        val recentTime: Long
            get() = dayStartAgoMillis(daysAgo = Preferences.RECENT_DAYS)
    }
}

fun AppListTable.observePackages(): Flow<Map<String, Int>> = observePackagesList().map { list ->
    list.associateBy({ it.packageName }, { it.rowId })
}

val App.contentValues: ContentValues
    get() = ContentValues().apply {
        if (rowId > 0) {
            put(BaseColumns._ID, rowId)
        }
        put(AppListTable.Columns.APP_ID, appId)
        put(AppListTable.Columns.PACKAGE_NAME, packageName)
        put(AppListTable.Columns.TITLE, title)
        put(AppListTable.Columns.VERSION_NUMBER, versionNumber)
        put(AppListTable.Columns.VERSION_NAME, versionName)
        put(AppListTable.Columns.CREATOR, creator)
        put(AppListTable.Columns.STATUS, status)
        put(AppListTable.Columns.UPLOAD_DATE, uploadDate)

        put(AppListTable.Columns.PRICE_TEXT, price.text)
        put(AppListTable.Columns.PRICE_CURRENCY, price.cur)
        put(AppListTable.Columns.PRICE_MICROS, price.micros)

        put(AppListTable.Columns.DETAILS_URL, detailsUrl)

        put(AppListTable.Columns.ICON_URL, iconUrl)
        put(AppListTable.Columns.UPLOAD_TIMESTAMP, uploadTime)

        put(AppListTable.Columns.APP_TYPE, appType)
        put(AppListTable.Columns.SYNC_TIMESTAMP, syncTime)
    }