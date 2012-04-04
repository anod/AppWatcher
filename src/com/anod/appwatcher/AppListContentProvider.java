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
import com.anod.appwatcher.model.AppListTable;
import com.anod.appwatcher.model.DbOpenHelper;

public class AppListContentProvider extends ContentProvider {
	private DbOpenHelper mDatabaseOpenHelper;
	
	
	// Used for the UriMacher
	private static final int LIST = 10;
	private static final int ROW = 20;
	
	public static final String AUTHORITY = "com.anod.appwatcher";	
	private static final String BASE_PATH = "apps";
	private static final String APP_PATH = "apps/#";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + BASE_PATH);
	
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/list";
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/app";
	
	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		sURIMatcher.addURI(AUTHORITY, BASE_PATH, LIST);
		sURIMatcher.addURI(AUTHORITY, APP_PATH, ROW);
		
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		if (sURIMatcher.match(uri) != ROW) { 
			throw new IllegalArgumentException("Unknown URI " + uri); 
		}
		String rowId = uri.getLastPathSegment();
		
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        int count = db.delete(AppListTable.TABLE_NAME, AppListTable.Columns._ID+"=?", new String[] { rowId });
        if (count > 0) {
        	getContext().getContentResolver().notifyChange(CONTENT_URI, null);
        }
        return count;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		if (sURIMatcher.match(uri) != LIST) { 
			throw new IllegalArgumentException("Unknown URI " + uri); 
		}
        if (values == null || values.size() == 0) {
        	throw new IllegalArgumentException("Values cannot be empty");
        }

        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        long rowId = db.insert(AppListTable.TABLE_NAME, null, values);
        if (rowId > 0) {
        	Uri noteUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
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
	public AppListCursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// Using SQLiteQueryBuilder instead of query() method
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(AppListTable.TABLE_NAME);

		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
			case LIST:
				
				break;
			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
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
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		if (sURIMatcher.match(uri) != ROW) { 
			throw new IllegalArgumentException("Unknown URI " + uri); 
		}
        if (values == null || values.size() == 0) {
        	throw new IllegalArgumentException("Values cannot be empty");
        }
		String rowId = uri.getLastPathSegment();

        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        int count  = db.update(AppListTable.TABLE_NAME, values,  AppListTable.Columns._ID+"=?", new String[] { rowId });
        if (count > 0) {
        	getContext().getContentResolver().notifyChange(CONTENT_URI, null);
        }
        return count;
	}

}
