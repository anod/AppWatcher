package com.anod.appwatcher;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.anod.appwatcher.model.AppListCursor;
import com.anod.appwatcher.model.schema.AppListTable;
import com.anod.appwatcher.model.DbOpenHelper;
import com.anod.appwatcher.model.schema.AppTagsTable;
import com.anod.appwatcher.model.schema.TagsTable;

public class AppListContentProvider extends ContentProvider {
    public static final String AUTHORITY = "com.anod.appwatcher";

	private DbOpenHelper mDatabaseOpenHelper;
	
	private static final int APP_LIST = 10;
	private static final int APP_ROW = 20;
    private static final int TAG_LIST = 30;
    private static final int TAG_APPS = 40;

	public static final Uri APPS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/apps");
    public static final Uri TAGS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/tags");


	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

	static {
		sURIMatcher.addURI(AUTHORITY, "apps", APP_LIST);
		sURIMatcher.addURI(AUTHORITY, "apps/#", APP_ROW);

        sURIMatcher.addURI(AUTHORITY, "tags", TAG_LIST);
        sURIMatcher.addURI(AUTHORITY, "tag/#/apps", TAG_APPS);

    }

    private Query matchQuery(Uri uri) {
        int matched = sURIMatcher.match(uri);
        if (matched == -1) {
            return null;
        }
        Query query = new Query();
        query.type = matched;
        switch (matched) {
            case APP_ROW:
                query.table = AppListTable.TABLE_NAME;
                String rowId = uri.getLastPathSegment();
                query.selection = AppListTable.Columns._ID + "=?";
                query.selectionArgs = new String[]{rowId};
                query.notifyUri = APPS_CONTENT_URI;
                return query;
            case APP_LIST:
                query.table = AppListTable.TABLE_NAME;
                query.notifyUri = APPS_CONTENT_URI;
                return query;
            case TAG_LIST:
                query.table = TagsTable.TABLE_NAME;
                query.notifyUri = TAGS_CONTENT_URI;
                return query;
            case TAG_APPS:
                query.table = AppTagsTable.TABLE_NAME;
                query.notifyUri = TAGS_CONTENT_URI;
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
	public int delete(Uri uri, String selection, String[] selectionArgs) {
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
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
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
	public AppListCursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Query query = matchQuery(uri);
        if (query == null) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
		// Using SQLiteQueryBuilder instead of query() method
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(query.table);

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
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Query query = matchQuery(uri);
        if (query == null) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        if (values == null || values.size() == 0) {
        	throw new IllegalArgumentException("Values cannot be empty");
        }

        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        int count  = db.update(query.table, values,  query.selection, query.selectionArgs);
        if (count > 0 && query.notifyUri != null) {
        	getContext().getContentResolver().notifyChange(query.notifyUri, null);
        }
        return count;
	}

}
