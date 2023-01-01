package com.anod.appwatcher.database

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.provider.BaseColumns
import com.anod.appwatcher.AppWatcherApplication
import com.anod.appwatcher.BuildConfig
import info.anodsplace.applog.AppLog

class DbContentProvider : ContentProvider() {

    companion object {
        const val authority = BuildConfig.APPLICATION_ID

        private const val apps = 10
        private const val app = 20
        private const val changelogVersion = 200

        val appsUri = Uri.parse("content://$authority/apps")!!
        val changelogUri = Uri.parse("content://$authority/changelog")!!

        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)

        init {
            uriMatcher.addURI(authority, "apps", apps)
            uriMatcher.addURI(authority, "apps/#", app)

            uriMatcher.addURI(authority, "changelog/apps/*/v/#", changelogVersion)
        }
    }

    private val dbSchemaManager: DbDataSource by lazy {
        DbDataSource((context as AppWatcherApplication).appsDatabase.openHelper)
    }

    private fun matchQuery(uri: Uri): Query? {
        val matched = uriMatcher.match(uri)
        if (matched == -1) {
            return null
        }
        val query = Query()
        query.type = matched
        val rowId: String
        when (matched) {
            app -> {
                query.table = AppListTable.table
                rowId = uri.lastPathSegment ?: "0"
                query.selection = BaseColumns._ID + "=?"
                query.selectionArgs = arrayOf(rowId)
                query.notifyUri = appsUri.buildUpon().appendPath(rowId).build()
                return query
            }
            changelogVersion -> {
                query.table = ChangelogTable.table
                query.selection = ChangelogTable.Columns.appId + "=? AND " + ChangelogTable.Columns.versionCode + "=?"
                query.selectionArgs = arrayOf(
                        uri.pathSegments[uri.pathSegments.size - 3],
                        uri.lastPathSegment ?: "-1"
                )
                query.notifyUri = changelogUri
                return query
            }
        }
        return null
    }

    private class Query {
        var type: Int = 0
        var table: String? = null
        var selection: String? = null
        var selectionArgs: Array<String>? = null
        var notifyUri: Uri? = null
        var groupBy: String? = null
        var projection: Array<String>? = null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        throw IllegalArgumentException("Unknown URI $uri")
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val query = matchQuery(uri) ?: throw IllegalArgumentException("Unknown URI $uri")
        require(!(values == null || values.size() == 0)) { "Values cannot be empty" }

        val db = dbSchemaManager.writableDatabase
        val rowId = db.insert(query.table, SQLiteDatabase.CONFLICT_REPLACE, values)
        if (rowId > 0 && query.notifyUri != null) {
            val noteUri = ContentUris.withAppendedId(query.notifyUri!!, rowId)
            context!!.contentResolver.notifyChange(noteUri, null)
            return noteUri
        }
        throw SQLException("Failed to insert row into $uri")
    }

    override fun onCreate(): Boolean {
        AppLog.d("Initializing ${(context as AppWatcherApplication).appsDatabase.openHelper.databaseName}")
        return false
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): AppListCursor? {
        var proj = projection
        var sel = selection
        var selArgs = selectionArgs
        val query = matchQuery(uri) ?: throw IllegalArgumentException("Unknown URI $uri")
// Using SQLiteQueryBuilder instead of queryApps() method
        val queryBuilder = SQLiteQueryBuilder()
        queryBuilder.tables = query.table

        if (selection == null) {
            sel = query.selection
            selArgs = query.selectionArgs
        }
        if (projection == null) {
            proj = query.projection
        }

        val db = dbSchemaManager.writableDatabase
        val sqlQuery = queryBuilder.buildQuery(proj, sel, query.groupBy, null, sortOrder, null)
        val cursor = db.query(sqlQuery, selArgs)

// Make sure that potential listeners are getting notified
        cursor.setNotificationUri(context?.contentResolver, uri)
        return AppListCursor(cursor)
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        val query = matchQuery(uri) ?: throw IllegalArgumentException("Unknown URI $uri")
        require(!(values == null || values.size() == 0)) { "Values cannot be empty" }

        val db = dbSchemaManager.writableDatabase
        val count = db.update(query.table, SQLiteDatabase.CONFLICT_REPLACE, values, query.selection, query.selectionArgs)
        if (count > 0 && query.notifyUri != null) {
            context?.contentResolver?.notifyChange(query.notifyUri!!, null)
        }
        return count
    }
}