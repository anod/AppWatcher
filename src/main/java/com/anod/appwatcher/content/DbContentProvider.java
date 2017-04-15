package com.anod.appwatcher.content;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.anod.appwatcher.BuildConfig;
import com.anod.appwatcher.model.DbOpenHelper;
import com.anod.appwatcher.model.schema.AppListTable;
import com.anod.appwatcher.model.schema.AppTagsTable;
import com.anod.appwatcher.model.schema.TagsTable;

public class DbContentProvider extends ContentProvider {
    public static final String AUTHORITY = BuildConfig.APPLICATION_ID;

    private DbOpenHelper mDatabaseOpenHelper;

    private static final int APP_LIST = 10;
    private static final int APP_ROW = 20;
    private static final int TAG_LIST = 30;
    private static final int TAG_ROW = 40;
    private static final int TAG_APPS = 50;
    private static final int APP_TAG_LIST = 60;
    private static final int ICON_ROW = 70;

    public static final Uri APPS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/apps");
    public static final Uri APPS_TAG_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/apps/tag");
    public static final Uri TAGS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/tags");
    public static final Uri ICONS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/icons");

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(AUTHORITY, "apps", APP_LIST);
        sURIMatcher.addURI(AUTHORITY, "apps/#", APP_ROW);
        sURIMatcher.addURI(AUTHORITY, "apps/tag/#", APP_TAG_LIST);

        sURIMatcher.addURI(AUTHORITY, "tags", TAG_LIST);
        sURIMatcher.addURI(AUTHORITY, "tags/#", TAG_ROW);
        sURIMatcher.addURI(AUTHORITY, "tags/#/apps", TAG_APPS);

        sURIMatcher.addURI(AUTHORITY, "icons/#", ICON_ROW);
    }

    public static boolean matchIconUri(Uri uri)
    {
        return sURIMatcher.match(uri) == ICON_ROW;
    }

    private Query matchQuery(Uri uri) {
        int matched = sURIMatcher.match(uri);
        if (matched == -1) {
            return null;
        }
        Query query = new Query();
        query.type = matched;
        String rowId;
        switch (matched) {
            case APP_ROW:
                query.table = AppListTable.TABLE_NAME;
                rowId = uri.getLastPathSegment();
                query.selection = AppListTable.Columns._ID + "=?";
                query.selectionArgs = new String[]{rowId};
                query.notifyUri = APPS_CONTENT_URI;
                return query;
            case APP_LIST:
                query.table = AppListTable.TABLE_NAME;
                query.notifyUri = APPS_CONTENT_URI;
                return query;
            case APP_TAG_LIST:
                query.table = AppTagsTable.TABLE_NAME + ", " + AppListTable.TABLE_NAME;
                query.notifyUri = APPS_CONTENT_URI;
                return query;
            case TAG_LIST:
                query.table = TagsTable.TABLE_NAME;
                query.notifyUri = TAGS_CONTENT_URI;
                return query;
            case TAG_ROW:
                query.table = TagsTable.TABLE_NAME;
                rowId = uri.getLastPathSegment();
                query.selection = TagsTable.Columns._ID + "=?";
                query.selectionArgs = new String[]{rowId};
                query.notifyUri = TAGS_CONTENT_URI;
                return query;
            case TAG_APPS:
                query.table = AppTagsTable.TABLE_NAME;
                String tagId = uri.getPathSegments().get(uri.getPathSegments().size() - 2);
                query.selection = AppTagsTable.Columns.TAGID + "=?";
                query.selectionArgs = new String[]{tagId};
                query.notifyUri = APPS_TAG_CONTENT_URI;
                return query;
            case ICON_ROW:
                query.table = AppListTable.TABLE_NAME;
                rowId = uri.getLastPathSegment();
                query.selection = AppListTable.Columns._ID + "=?";
                query.selectionArgs = new String[]{rowId};
                query.notifyUri = ICONS_CONTENT_URI;
                return query;
        }
        return null;
    }

    private static class Query {
        int type;
        String table;
        String selection;
        String[] selectionArgs;
        Uri notifyUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        Query query = matchQuery(uri);
        if (query == null) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        int count;
        if (query.selection != null) {
            count = db.delete(query.table, query.selection, query.selectionArgs);
        } else {
            count = db.delete(query.table, selection, selectionArgs);
        }
        if (count > 0 && query.notifyUri != null) {
            getContext().getContentResolver().notifyChange(query.notifyUri, null);
        }
        return count;
    }


    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        Query query = matchQuery(uri);
        if (query == null) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        if (values == null || values.size() == 0) {
            throw new IllegalArgumentException("Values cannot be empty");
        }

        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        long rowId = db.insert(query.table, null, values);
        if (rowId > 0 && query.notifyUri != null) {
            Uri noteUri = ContentUris.withAppendedId(query.notifyUri, rowId);
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }
        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public boolean onCreate() {
        mDatabaseOpenHelper = new DbOpenHelper(getContext());
        return false;
    }

    @Override
    public AppListCursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Query query = matchQuery(uri);
        if (query == null) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        // Using SQLiteQueryBuilder instead of queryApps() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(query.table);

        if (selection == null) {
            selection = query.selection;
            selectionArgs = query.selectionArgs;
        }

        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        if (cursor == null) {
            return null;
        }
        // Make sure that potential listeners are getting notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return new AppListCursor(cursor);
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Query query = matchQuery(uri);
        if (query == null) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        if (values == null || values.size() == 0) {
            throw new IllegalArgumentException("Values cannot be empty");
        }

        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        int count = db.update(query.table, values, query.selection, query.selectionArgs);
        if (count > 0 && query.notifyUri != null) {
            getContext().getContentResolver().notifyChange(query.notifyUri, null);
        }
        return count;
    }

}
