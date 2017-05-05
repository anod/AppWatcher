package com.anod.appwatcher.content;

import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.util.SimpleArrayMap;

import com.anod.appwatcher.AppListContentProvider;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.model.AppInfoMetadata;
import com.anod.appwatcher.model.AppTag;
import com.anod.appwatcher.model.Tag;
import com.anod.appwatcher.model.schema.AppListTable;
import com.anod.appwatcher.model.schema.AppTagsTable;
import com.anod.appwatcher.model.schema.TagsTable;
import com.anod.appwatcher.utils.BitmapUtils;

import java.util.ArrayList;
import java.util.List;

import info.anodsplace.android.log.AppLog;

/**
 * Wrapper above ContentResolver to simplify access to AppInfo
 *
 * @author alex
 */
public class DbContentProviderClient {
    private static final String DEFAULT_SORT_ORDER =
            AppListTable.Columns.KEY_STATUS + " DESC, "
                    + AppListTable.Columns.KEY_TITLE
                    + " COLLATE LOCALIZED ASC";

    private final ContentProviderClient mContentProviderClient;

    public DbContentProviderClient(Context context) {
        mContentProviderClient = context.getContentResolver().acquireContentProviderClient(AppListContentProvider.AUTHORITY);
    }

    public DbContentProviderClient(ContentProviderClient provider) {
        mContentProviderClient = provider;
    }


    /**
     * Query all applications in db
     */
    public AppListCursor queryAllSorted(boolean includeDeleted) {
        return queryAll(includeDeleted, DEFAULT_SORT_ORDER);
    }

    public AppListCursor queryAll(boolean includeDeleted) {
        return queryAll(includeDeleted, null);
    }

    private AppListCursor queryAll(boolean includeDeleted, String sortOrder) {
        if (includeDeleted) {
            return queryApps(sortOrder, null, null);
        }
        String selection = AppListTable.Columns.KEY_STATUS + " != ?";
        String[] selectionArgs = new String[]{String.valueOf(AppInfo.STATUS_DELETED)};

        return queryApps(sortOrder, selection, selectionArgs);
    }

    public AppListCursor queryUpdated() {
        String selection = AppListTable.Columns.KEY_STATUS + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(AppInfo.STATUS_UPDATED)};
        return queryApps(null, selection, selectionArgs);
    }

    public int getCount(boolean includeDeleted) {
        Cursor cr = queryAll(includeDeleted);
        if (cr == null) {
            return 0;
        }
        return cr.getCount();
    }

    private AppListCursor queryApps(String sortOrder, String selection, String[] selectionArgs) {
        Cursor cr = null;
        try {
            cr = mContentProviderClient.query(DbContentProvider.APPS_CONTENT_URI,
                    AppListTable.PROJECTION, selection, selectionArgs, sortOrder
            );
        } catch (RemoteException e) {
            AppLog.e(e.getMessage());
        }
        return new AppListCursor(cr);
    }

    /**
     * @return map (AppId => RowId)
     */
    public SimpleArrayMap<String, Integer> queryPackagesMap(boolean includeDeleted) {
        AppListCursor cursor = queryAll(includeDeleted);
        if (cursor == null) {
            return new SimpleArrayMap<>();
        }
        SimpleArrayMap<String, Integer> result = new SimpleArrayMap<>(cursor.getCount());
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
            AppInfo info = cursor.getAppInfo();
            result.put(info.packageName, info.getRowId());
        }
        cursor.close();
        return result;
    }

    public Uri insert(AppInfo app) {
        ContentValues values = AppListTable.createContentValues(app);
        try {
            return mContentProviderClient.insert(DbContentProvider.APPS_CONTENT_URI, values);
        } catch (RemoteException e) {
            AppLog.e(e.getMessage());
        }
        return null;
    }

    public int update(AppInfo app) {
        int rowId = app.getRowId();
        ContentValues values = AppListTable.createContentValues(app);
        return update(rowId, values);
    }

    public int update(int rowId, ContentValues values) {
        Uri updateUri = DbContentProvider.APPS_CONTENT_URI.buildUpon().appendPath(String.valueOf(rowId)).build();
        try {
            return mContentProviderClient.update(updateUri, values, null, null);
        } catch (RemoteException e) {
            AppLog.e(e.getMessage());
        }
        return 0;
    }

    public int updateStatus(int rowId, int status) {
        Uri updateUri = DbContentProvider.APPS_CONTENT_URI.buildUpon().appendPath(String.valueOf(rowId)).build();
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
                    DbContentProvider.APPS_CONTENT_URI,
                    AppListTable.Columns.KEY_STATUS + " = ?",
                    new String[]{String.valueOf(AppInfoMetadata.STATUS_DELETED)}
            );

            int tagsCleaned = mContentProviderClient.delete(
                    DbContentProvider.APPS_TAG_CLEAN_CONTENT_URI,
                    null,
                    null
            );
            AppLog.d("Deleted " + numRows + " rows, tags " + tagsCleaned + " cleaned");
        } catch (RemoteException e) {
            AppLog.e(e);
        }
        return numRows;
    }

    public void close() {
        if (mContentProviderClient != null) {
            mContentProviderClient.release();
        }
    }

    public AppInfo queryAppId(String packageName) {
        AppListCursor cr = queryApps(null,
                AppListTable.Columns.KEY_PACKAGE + " = ? AND " + AppListTable.Columns.KEY_STATUS + " != ?", new String[]{packageName, String.valueOf(AppInfo.STATUS_DELETED) });
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

    public AppInfo queryAppRow(int rowId) {
        AppListCursor cr = queryApps(null,
                AppListTable.Columns._ID + " = ?", new String[]{ String.valueOf(rowId) });
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

    public Bitmap queryAppIcon(Uri uri) {
        Cursor cr;
        try {
            cr = mContentProviderClient.query(uri,
                    new String[]{ AppListTable.Columns._ID, AppListTable.Columns.KEY_ICON_CACHE },
                    null, null, null
            );
        } catch (RemoteException e) {
            AppLog.e(e);
            return null;
        }
        if (cr == null) {
            return null;
        }
        cr.moveToPosition(-1);
        Bitmap icon = null;
        if (cr.moveToNext()) {
            byte[] iconData = cr.getBlob(1);
            icon = BitmapUtils.unFlattenBitmap(iconData);
        }
        cr.close();

        return icon;
    }

    public void discardAll() {
        try {
            mContentProviderClient.delete(AppListContentProvider.APPS_CONTENT_URI, null, null);
            mContentProviderClient.delete(AppListContentProvider.TAGS_CONTENT_URI, null, null);
            mContentProviderClient.delete(AppListContentProvider.TAGS_APPS_CONTENT_URI, null, null);
        } catch (RemoteException e) {
            AppLog.e(e);
        }
    }

    public void addApps(List<AppInfo> appList) {
        SimpleArrayMap<String, Integer> currentIds = queryPackagesMap(true);
        for (AppInfo app : appList) {
            Integer rowId = currentIds.get(app.packageName);
            if (rowId == null) {
                insert(app);
            } else {
                app.setRowId(rowId);
                update(app);
            }
        }
    }

    public TagsCursor queryTags() {
        try {
            Cursor cr = mContentProviderClient.query(
                    DbContentProvider.TAGS_CONTENT_URI,
                    TagsTable.PROJECTION, null, null, null
            );
            return new TagsCursor(cr);
        } catch (RemoteException e) {
            AppLog.e(e);
        }
        return null;
    }

    public void addTags(List<Tag> tags) {
        for (Tag tag: tags) {
            createTag(tag);
        }
    }

    public Uri createTag(@NonNull Tag tag) {
        ContentValues values = TagsTable.createContentValues(tag);
        try {
            return mContentProviderClient.insert(DbContentProvider.TAGS_CONTENT_URI, values);
        } catch (RemoteException e) {
            AppLog.e(e.getMessage());
        }
        return null;
    }

    public int saveTag(@NonNull Tag tag) {
        Uri updateUri = DbContentProvider.TAGS_CONTENT_URI.buildUpon().appendPath(String.valueOf(tag.id)).build();
        ContentValues values = TagsTable.createContentValues(tag);
        try {
            return mContentProviderClient.update(updateUri, values, null, null);
        } catch (RemoteException e) {
            AppLog.e(e.getMessage());
        }
        return 0;
    }

    public void deleteTag(@NonNull Tag tag) {
        Uri tagDeleteUri = DbContentProvider.TAGS_CONTENT_URI.buildUpon().appendPath(String.valueOf(tag.id)).build();
        Uri appsTagDeleteUri = DbContentProvider.TAGS_CONTENT_URI.buildUpon().appendPath(String.valueOf(tag.id)).appendPath("apps").build();
        try {
            mContentProviderClient.delete(tagDeleteUri, null, null);
            mContentProviderClient.delete(appsTagDeleteUri, null, null);
        } catch (RemoteException e) {
            AppLog.e(e);
        }
    }

    public Cursor queryAppTags() {
        try {
            Cursor cr = mContentProviderClient.query(
                    DbContentProvider.TAGS_APPS_CONTENT_URI,
                    AppTagsTable.PROJECTION, null, null, null
            );
            return cr == null ? new NullCursor() : cr;
        } catch (RemoteException e) {
            AppLog.e(e);
        }
        return null;
    }

    public boolean setAppsToTag(@NonNull List<String> appIds, int tagId)
    {
        try {
            Uri appsTagUri = DbContentProvider.TAGS_CONTENT_URI.buildUpon().appendPath(String.valueOf(tagId)).appendPath("apps").build();
            mContentProviderClient.delete(appsTagUri, null, null);
            for (String appId: appIds) {
                ContentValues values = AppTagsTable.createContentValues(appId, tagId);
                mContentProviderClient.insert(appsTagUri, values);
            }
            return true;
        } catch (RemoteException e) {
            AppLog.e(e);
        }
        return false;
    }

    public List<Integer> queryAppTags(int rowId) {
        Uri appTagsUri = DbContentProvider.APPS_CONTENT_URI.buildUpon()
                .appendPath(String.valueOf(rowId))
                .appendPath("tags")
                .build();

        List<Integer> tagIds = new ArrayList<>();
        try {
            Cursor cr = mContentProviderClient.query(appTagsUri, AppTagsTable.PROJECTION, null, null, null);
            if (cr == null || cr.getCount() == 0) {
                return tagIds;
            }
            cr.moveToPosition(-1);
            if (cr.moveToNext()) {
                int tagId = cr.getInt(AppTagsTable.Projection.TAGID);
                tagIds.add(tagId);
            }
            cr.close();
        } catch (RemoteException e) {
            AppLog.e(e);
        }
        return tagIds;
    }

    public boolean addAppToTag(@NonNull String appId, int tagId)
    {
        try {
            Uri appsTagUri = DbContentProvider.TAGS_CONTENT_URI
                    .buildUpon()
                    .appendPath(String.valueOf(tagId))
                    .appendPath("apps")
                    .build();
            ContentValues values = AppTagsTable.createContentValues(appId, tagId);
            mContentProviderClient.insert(appsTagUri, values);
            return true;
        } catch (RemoteException e) {
            AppLog.e(e);
        }
        return false;
    }


    public int deleteAppTags(@NonNull String appId) {
        try {
            return mContentProviderClient.delete(
                    DbContentProvider.TAGS_APPS_CONTENT_URI,
                    AppTagsTable.Columns.APPID + "=?",
                    new String[] { appId });
        } catch (RemoteException e) {
            AppLog.e(e);
        }
        return 0;
    }

    public boolean removeAppFromTag(String appId, int tagId) {
        try {
            Uri appsTagUri = DbContentProvider.TAGS_CONTENT_URI
                    .buildUpon()
                    .appendPath(String.valueOf(tagId))
                    .appendPath("apps")
                    .build();
            mContentProviderClient.delete(appsTagUri, AppTagsTable.Columns.APPID + "=?", new String[] {appId});
            return true;
        } catch (RemoteException e) {
            AppLog.e(e);
        }
        return false;
    }


    public void addAppTags(List<AppTag> appTags) {
        SimpleArrayMap<Integer, List<String>> tagApps = new SimpleArrayMap<>();
        for (AppTag appTag: appTags) {
            List<String> list = tagApps.get(appTag.tagId);
            if (list == null)
            {
                list = new ArrayList<String>();
                tagApps.put(appTag.tagId, list);
            }
            list.add(appTag.appId);
        }

        for (int i = 0; i < tagApps.size(); i++) {
            int tagId = tagApps.keyAt(i);
            List<String> list = tagApps.valueAt(i);
            setAppsToTag(list, tagId);
        }
    }

}
