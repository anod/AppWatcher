package com.anod.appwatcher.model;

import java.util.HashMap;
import java.util.Map;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;

import com.anod.appwatcher.AppListContentProvider;
import com.anod.appwatcher.utils.BitmapUtils;

/**
 * Wrapper above ContentResolver to simplify access to AppInfo
 * @author alex
 *
 */
public class AppListContentResolver {
	private ContentResolver mContentResolver;

	public AppListContentResolver(Context context) {
		mContentResolver = context.getContentResolver();
	}

	/**
	 * Query all applications in db
	 * @return
	 */
	public AppListCursor queryAll() {
		Cursor cr = mContentResolver.query(AppListContentProvider.CONTENT_URI,
				AppListTable.APPLIST_PROJECTION, null, null,
				AppListTable.Columns.KEY_STATUS + " DESC, "
						+ AppListTable.Columns.KEY_TITLE
						+ " COLLATE LOCALIZED ASC");
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
		return mContentResolver.insert(AppListContentProvider.CONTENT_URI,
				values);
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
		values.put(AppListTable.Columns.KEY_STATUS, AppInfo.STATUS_NORMAL);
		Bitmap icon = app.getIcon();
		if (icon != null) {
			byte[] iconData = BitmapUtils.flattenBitmap(icon);
			values.put(AppListTable.Columns.KEY_ICON_CACHE, iconData);
		}

		return values;
	}


}
