package com.anod.appwatcher.model;

import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.RemoteException;
import android.support.v4.util.ArrayMap;

import com.anod.appwatcher.AppListContentProvider;
import com.anod.appwatcher.model.schema.AppListTable;
import com.anod.appwatcher.utils.BitmapUtils;

import java.util.List;
import java.util.Map;

import info.anodsplace.android.log.AppLog;

/**
 * Wrapper above ContentResolver to simplify access to AppInfo
 *
 * @author alex
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
     */
    public AppListCursor queryAllSorted() {
        return query(DEFAULT_SORT_ORDER, null, null);
    }

    public AppListCursor queryAll(boolean includeDeleted) {
        if (includeDeleted) {
            return query(null, null, null);
        }
        String selection = AppListTable.Columns.KEY_STATUS + " != ?";
        String[] selectionArgs = new String[]{String.valueOf(AppInfo.STATUS_DELETED)};

        return query(null, selection, selectionArgs);
    }

    public int getCount(boolean includeDeleted) {
        Cursor cr = queryAll(includeDeleted);
        if (cr == null) {
            return 0;
        }
        return cr.getCount();
    }

    public AppListCursor query(String sortOrder, String selection, String[] selectionArgs) {
        Cursor cr;
        try {
            cr = mContentProviderClient.query(AppListContentProvider.APPS_CONTENT_URI,
                    AppListTable.PROJECTION, selection, selectionArgs, sortOrder
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


    public int queryUpdatesCount() {
        String selection = AppListTable.Columns.KEY_STATUS + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(AppInfo.STATUS_UPDATED)};
        AppListCursor cr = query(null, selection, selectionArgs);

        if (cr == null) {
            return 0;
        }
        int count = cr.getCount();
        cr.close();
        return count;
    }

    /**
     * @return map (AppId => RowId)
     */
    public Map<String, Integer> queryPackagesMap(boolean includeDeleted) {
        AppListCursor cursor = queryAll(includeDeleted);
        ArrayMap<String, Integer> result = new ArrayMap<String, Integer>();
        if (cursor == null) {
            return result;
        }
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
            AppInfo info = cursor.getAppInfo();
            result.put(info.getPackageName(), info.getRowId());
        }
        cursor.close();
        return result;
    }

    /**
     * Insert a new app into db
     *
     * @param app
     */
    public Uri insert(AppInfo app) {
        ContentValues values = createContentValues(app);
        try {
            return mContentProviderClient.insert(AppListContentProvider.APPS_CONTENT_URI, values);
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
        Uri updateUri = AppListContentProvider.APPS_CONTENT_URI.buildUpon().appendPath(String.valueOf(rowId)).build();
        try {
            return mContentProviderClient.update(updateUri, values, null, null);
        } catch (RemoteException e) {
            AppLog.e(e.getMessage());
        }
        return 0;
    }

    public int markDeleted(int rowId) {
        return updateStatus(rowId, AppInfo.STATUS_DELETED);
    }

    public int updateStatus(int rowId, int status) {
        Uri updateUri = AppListContentProvider.APPS_CONTENT_URI.buildUpon().appendPath(String.valueOf(rowId)).build();
        ContentValues values = new ContentValues();
        values.put(AppListTable.Columns.KEY_STATUS, status);
        try {
            return mContentProviderClient.update(updateUri, values, null, null);
        } catch (RemoteException e) {
            AppLog.e(e);
        }
        return 0;
    }

    public int cleanDeleted() {
        int numRows = 0;
        try {
            numRows = mContentProviderClient.delete(
                    AppListContentProvider.APPS_CONTENT_URI,
                    AppListTable.Columns.KEY_STATUS + " = ?",
                    new String[]{String.valueOf(AppInfoMetadata.STATUS_DELETED)}
            );
        } catch (RemoteException e) {
            AppLog.e(e);
        }
        return numRows;
    }

    public void release() {
        if (mContentProviderClient != null) {
            mContentProviderClient.release();
        }
    }

    /**
     * @return Content values for app
     */
    private ContentValues createContentValues(AppInfo app) {
        ContentValues values = new ContentValues();

        values.put(AppListTable.Columns.KEY_APPID, app.getAppId());
        values.put(AppListTable.Columns.KEY_PACKAGE, app.getPackageName());
        values.put(AppListTable.Columns.KEY_TITLE, app.getTitle());
        values.put(AppListTable.Columns.KEY_VERSION_NUMBER, app.getVersionCode());
        values.put(AppListTable.Columns.KEY_VERSION_NAME, app.getVersionName());
        values.put(AppListTable.Columns.KEY_CREATOR, app.getCreator());
        values.put(AppListTable.Columns.KEY_STATUS, app.getStatus());
        values.put(AppListTable.Columns.KEY_UPLOAD_DATE, app.getUploadDate());

        values.put(AppListTable.Columns.KEY_PRICE_TEXT, app.getPriceText());
        values.put(AppListTable.Columns.KEY_PRICE_CURRENCY, app.getPriceCur());
        values.put(AppListTable.Columns.KEY_PRICE_MICROS, app.getPriceMicros());

        values.put(AppListTable.Columns.KEY_DETAILS_URL, app.getDetailsUrl());

        Bitmap icon = app.getIcon();
        if (icon != null) {
            byte[] iconData = BitmapUtils.flattenBitmap(icon);
            values.put(AppListTable.Columns.KEY_ICON_CACHE, iconData);
        }
        return values;
    }


    public AppInfo queryAppId(String id) {
        AppListCursor cr = query(null,
                AppListTable.Columns.KEY_APPID + " = ?", new String[]{id});
        if (cr == null || cr.getCount() == 0) {
            return null;
        }
        cr.moveToPosition(-1);
        AppInfo info = null;
        if (cr.moveToNext()) {
            info = cr.getAppInfo();
        }
        cr.close();

        return info;
    }

    public void addList(List<AppInfo> appList) {
        Map<String, Integer> currentIds = queryPackagesMap(true);
        for (AppInfo app : appList) {
            Integer rowId = currentIds.get(app.getPackageName());
            if (rowId == null) {
                insert(app);
            } else {
                app.setRowId(rowId);
                update(app);
            }
        }
    }
}
