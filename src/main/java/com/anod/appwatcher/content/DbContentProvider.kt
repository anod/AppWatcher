package com.anod.appwatcher.content

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.SQLException
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.provider.BaseColumns

import com.anod.appwatcher.BuildConfig
import com.anod.appwatcher.model.DbSchemaManager
import com.anod.appwatcher.model.Tag
import com.anod.appwatcher.model.schema.AppListTable
import com.anod.appwatcher.model.schema.AppTagsTable
import com.anod.appwatcher.model.schema.ChangelogTable
import com.anod.appwatcher.model.schema.TagsTable

open class DbContentProvider : ContentProvider() {

    companion object {
        const val authority = BuildConfig.APPLICATION_ID

        private const val apps = 10
        private const val app = 20
        private const val appTags = 100
        private const val appsTags = 60
        private const val appsTagsClean = 110

        private const val tags = 30
        private const val tag = 40
        private const val tagApps = 50
        private const val tagsApps = 80
        private const val tagsAppsCount = 90

        private const val changelogVersion = 200
        private const val changelogApp = 201

        private const val icon = 70

        val appsUri = Uri.parse("content://$authority/apps")!!
        val APPS_TAG_CONTENT_URI = Uri.parse("content://$authority/apps/tags")!!
        val APPS_TAG_CLEAN_CONTENT_URI = Uri.parse("content://$authority/apps/tags/clean")!!
        val TAGS_CONTENT_URI = Uri.parse("content://$authority/tags")!!
        val TAGS_APPS_CONTENT_URI = Uri.parse("content://$authority/tags/apps")!!
        val TAGS_APPS_COUNT_CONTENT_URI = Uri.parse("content://$authority/tags/apps/count")!!
        val ICONS_CONTENT_URI = Uri.parse("content://$authority/icons")!!
        val changelogUri = Uri.parse("content://$authority/changelog")!!

        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)

        init {
            uriMatcher.addURI(authority, "apps", apps)
            uriMatcher.addURI(authority, "apps/#", app)
            uriMatcher.addURI(authority, "apps/tags/#", appsTags)
            uriMatcher.addURI(authority, "apps/tags/clean", appsTagsClean)
            uriMatcher.addURI(authority, "apps/#/tags", appTags)

            uriMatcher.addURI(authority, "tags", tags)
            uriMatcher.addURI(authority, "tags/#", tag)
            uriMatcher.addURI(authority, "tags/#/apps", tagApps)
            uriMatcher.addURI(authority, "tags/apps", tagsApps)
            uriMatcher.addURI(authority, "tags/apps/count", tagsAppsCount)

            uriMatcher.addURI(authority, "icons/#", icon)

            uriMatcher.addURI(authority, "changelog/apps/*", changelogApp)
            uriMatcher.addURI(authority, "changelog/apps/*/v/#", changelogVersion)
        }

        fun matchIconUri(uri: Uri): Boolean {
            return uriMatcher.match(uri) == icon
        }

        fun appsContentUri(tag: Tag?): Uri {
            return if (tag == null)
                DbContentProvider.appsUri
            else
                DbContentProvider.APPS_TAG_CONTENT_URI.buildUpon().appendPath(tag.id.toString()).build()
        }
    }

    private lateinit var dbSchemaManager: DbSchemaManager

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
                rowId = uri.lastPathSegment
                query.selection = BaseColumns._ID + "=?"
                query.selectionArgs = arrayOf(rowId)
                query.notifyUri = appsUri
                return query
            }
            apps -> {
                query.table = AppListTable.table
                query.notifyUri = appsUri
                return query
            }
            appsTags -> {
                query.table = AppTagsTable.table + ", " + AppListTable.table
                val tagId = uri.lastPathSegment
                query.selection = AppTagsTable.TableColumns.TAGID + "=?"
                query.selectionArgs = arrayOf(tagId)
                query.notifyUri = appsUri
                return query
            }
            appTags -> {
                query.table = AppTagsTable.table + ", " + AppListTable.table
                rowId = uri.pathSegments[uri.pathSegments.size - 2]
                query.selection = AppListTable.TableColumns._ID + "=? AND " + AppListTable.TableColumns.APPID + "=" + AppTagsTable.TableColumns.APPID
                query.selectionArgs = arrayOf(rowId)
                query.notifyUri = APPS_TAG_CONTENT_URI
                return query
            }
            appsTagsClean -> {
                query.table = AppTagsTable.table
                query.selection = AppTagsTable.TableColumns.APPID+" NOT IN (SELECT " + AppTagsTable.TableColumns.APPID + " FROM " + AppTagsTable.table + ")"
                query.notifyUri = APPS_TAG_CONTENT_URI
                return query
            }
            tags -> {
                query.table = TagsTable.table
                query.notifyUri = TAGS_CONTENT_URI
                return query
            }
            tag -> {
                query.table = TagsTable.table
                rowId = uri.lastPathSegment
                query.selection = BaseColumns._ID + "=?"
                query.selectionArgs = arrayOf(rowId)
                query.notifyUri = TAGS_CONTENT_URI
                return query
            }
            tagApps -> {
                query.table = AppTagsTable.table
                val tagId = uri.pathSegments[uri.pathSegments.size - 2]
                query.selection = AppTagsTable.Columns.TAGID + "=?"
                query.selectionArgs = arrayOf(tagId)
                query.notifyUri = APPS_TAG_CONTENT_URI
                return query
            }
            tagsApps -> {
                query.table = AppTagsTable.table
                query.notifyUri = APPS_TAG_CONTENT_URI
                return query
            }
            tagsAppsCount -> {
                query.table = AppTagsTable.table
                query.notifyUri = APPS_TAG_CONTENT_URI
                query.projection = arrayOf(AppTagsTable.Columns.TAGID, "count() as count")
                query.groupBy = AppTagsTable.Columns.TAGID
                return query
            }
            icon -> {
                query.table = AppListTable.table
                rowId = uri.lastPathSegment
                query.selection = BaseColumns._ID + "=?"
                query.selectionArgs = arrayOf(rowId)
                query.notifyUri = ICONS_CONTENT_URI
                return query
            }
            changelogApp -> {
                query.table = ChangelogTable.table
                query.selection = ChangelogTable.Columns.appId + "=?"
                query.selectionArgs = arrayOf(uri.lastPathSegment)
                query.notifyUri = changelogUri
                return query
            }
            changelogVersion -> {
                query.table = ChangelogTable.table
                query.selection = ChangelogTable.Columns.appId + "=? AND " + ChangelogTable.Columns.versionCode + "=?"
                query.selectionArgs = arrayOf(
                        uri.pathSegments[uri.pathSegments.size - 3],
                        uri.lastPathSegment
                )
                query.notifyUri = changelogUri
                return query
            }
        }
        return null
    }

    private class Query {
        internal var type: Int = 0
        internal var table: String? = null
        internal var selection: String? = null
        internal var selectionArgs: Array<String>? = null
        internal var notifyUri: Uri? = null
        internal var groupBy: String? = null
        internal var projection: Array<String>? = null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val query = matchQuery(uri) ?: throw IllegalArgumentException("Unknown URI " + uri)

        val db = dbSchemaManager.writableDatabase
        val count: Int
        if (selection != null) {
            count = db.delete(query.table, selection, selectionArgs)
        } else {
            count = db.delete(query.table, query.selection, query.selectionArgs)
        }
        if (count > 0 && query.notifyUri != null) {
            context.contentResolver.notifyChange(query.notifyUri!!, null)
        }
        return count
    }


    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val query = matchQuery(uri) ?: throw IllegalArgumentException("Unknown URI " + uri)
        if (values == null || values.size() == 0) {
            throw IllegalArgumentException("Values cannot be empty")
        }

        val db = dbSchemaManager.writableDatabase
        val rowId = db.insert(query.table, null, values)
        if (rowId > 0 && query.notifyUri != null) {
            val noteUri = ContentUris.withAppendedId(query.notifyUri, rowId)
            context.contentResolver.notifyChange(noteUri, null)
            return noteUri
        }
        throw SQLException("Failed to insert row into " + uri)
    }

    override fun onCreate(): Boolean {
        dbSchemaManager = DbSchemaManager(context)
        return false
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): AppListCursor? {
        var proj = projection
        var sel = selection
        var selArgs = selectionArgs
        val query = matchQuery(uri) ?: throw IllegalArgumentException("Unknown URI " + uri)
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
        val cursor = queryBuilder.query(db, proj, sel, selArgs, query.groupBy, null, sortOrder, null) ?: return null

// Make sure that potential listeners are getting notified
        cursor.setNotificationUri(context.contentResolver, uri)
        return AppListCursor(cursor)
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        val query = matchQuery(uri) ?: throw IllegalArgumentException("Unknown URI " + uri)
        if (values == null || values.size() == 0) {
            throw IllegalArgumentException("Values cannot be empty")
        }

        val db = dbSchemaManager.writableDatabase
        val count = db.update(query.table, values, query.selection, query.selectionArgs)
        if (count > 0 && query.notifyUri != null) {
            context.contentResolver.notifyChange(query.notifyUri!!, null)
        }
        return count
    }

}
