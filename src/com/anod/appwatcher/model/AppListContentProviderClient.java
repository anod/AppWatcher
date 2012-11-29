package com.anod.appwatcher.model;

import java.util.HashMap;
import java.util.Map;

import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.RemoteException;

import com.anod.appwatcher.AppListContentProvider;
import com.anod.appwatcher.utils.AppLog;
import com.anod.appwatcher.utils.BitmapUtils;

/**
 * Wrapper above ContentResolver to simplify access to AppInfo
 * @author alex
 *
 */
public class AppListContentProviderClient {
	private static final String DEFAULT_SORT_ORDER = 
		AppListTable.Columns.KEY_STATUS + " DESC, "
		+ AppListTable.Columns.KEY_TITLE
		+ " COLLATE LOCALIZED ASC";
	
	private ContentProviderClient mContentProviderClient;

	
	public AppListContentProviderClient(Context context) {
		mContentProviderClient = context.getContentResolver().acquireContentProviderClient(AppListContentProvider.AUTHORITY);
	}

	public AppListContentProviderClient(ContentProviderClient provider) {
		mContentProviderClient = provider;
	}
	
	/**
	 * Query all applications in db
	 * @return
	 */
	public AppListCursor queryAllSorted() {
		return query(DEFAULT_SORT_ORDER);
	}
	
	public AppListCursor queryAll() {
		return query(null);
	}
	
	public AppListCursor query(String sortOrder) {
		Cursor cr = null;
		try {
			cr = mContentProviderClient.query(AppListContentProvider.CONTENT_URI,
				AppListTable.APPLIST_PROJECTION, null, null, sortOrder
			);
		} catch (RemoteException e) {
			AppLog.e(e.getMessage());
			return null;
		}
		if (cr == null) {
			return null;
		}
		return new AppListCursor(cr);	
	}
	
	
	/**
	 * 
	 * @return map (AppId => TRUE)
	 */
	public Map<String,Boolean> queryIdsMap() {
		AppListCursor cursor = queryAll();
		HashMap<String, Boolean> result = new HashMap<String, Boolean>();
		if (cursor == null) {
			return result;
		}
		cursor.moveToPosition(-1);
		while(cursor.moveToNext()) {
			result.put(cursor.getAppInfo().getAppId(), true);
		}
		return result;
	}
	
	/**
	 * Insert a new app into db
	 * @param app
	 * @return
	 */
	public Uri insert(AppInfo app) {
		ContentValues values = createContentValues(app);
		try {
			return mContentProviderClient.insert(AppListContentProvider.CONTENT_URI, values);
		} catch (RemoteException e) {
			AppLog.e(e.getMessage());
		}
		return null;
	}

	public int update(AppInfo app) {
		int rowId = app.getRowId();
		ContentValues values = createContentValues(app);
		return update(rowId, values);
	}
	
	public int update(int rowId, ContentValues values) {
		Uri updateUri = AppListContentProvider.CONTENT_URI.buildUpon().appendPath(String.valueOf(rowId)).build();
		try {
			return mContentProviderClient.update(updateUri, values, null, null);
		} catch (RemoteException e) {
			AppLog.e(e.getMessage());
		}
		return 0;
	}
	
	public void release() {
		if (mContentProviderClient != null) {
			mContentProviderClient.release();
		}
	}
	
	/**
	 * @param app
	 * @return Content values for app
	 */
	private ContentValues createContentValues(AppInfo app) {
		ContentValues values = new ContentValues();

		values.put(AppListTable.Columns.KEY_APPID, app.getAppId());
		values.put(AppListTable.Columns.KEY_PACKAGE, app.getPackageName());
		values.put(AppListTable.Columns.KEY_TITLE, app.getTitle());
		values.put(AppListTable.Columns.KEY_VERSION_NUMBER,app.getVersionCode());
		values.put(AppListTable.Columns.KEY_VERSION_NAME, app.getVersionName());
		values.put(AppListTable.Columns.KEY_CREATOR, app.getCreator());
		values.put(AppListTable.Columns.KEY_STATUS, app.getStatus());
   	    values.put(AppListTable.Columns.KEY_UPDATE_DATE, app.getUpdateTime() );
		Bitmap icon = app.getIcon();
		if (icon != null) {
			byte[] iconData = BitmapUtils.flattenBitmap(icon);
			values.put(AppListTable.Columns.KEY_ICON_CACHE, iconData);
		}
		return values;
	}




}
