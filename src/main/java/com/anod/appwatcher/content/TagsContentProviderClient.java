package com.anod.appwatcher.content;

import android.content.ContentProviderClient;
import android.content.Context;
import android.database.Cursor;
import android.os.RemoteException;

import com.anod.appwatcher.AppListContentProvider;
import com.anod.appwatcher.model.schema.TagsTable;

import info.anodsplace.android.log.AppLog;

/**
 * @author algavris
 * @date 10/03/2017.
 */

public class TagsContentProviderClient {
    public static final String DEFAULT_SORT_ORDER = TagsTable.Columns.NAME + " COLLATE LOCALIZED ASC";

    private ContentProviderClient mContentProviderClient;

    public TagsContentProviderClient(Context context) {
        mContentProviderClient = context.getContentResolver().acquireContentProviderClient(AppListContentProvider.AUTHORITY);
    }

    public TagsContentProviderClient(ContentProviderClient provider) {
        mContentProviderClient = provider;
    }

    public TagsCursor queryAll() {
        return query(DEFAULT_SORT_ORDER, null, null);
    }

    public TagsCursor query(String sortOrder, String selection, String[] selectionArgs) {
        Cursor cr;
        try {
            cr = mContentProviderClient.query(AppListContentProvider.TAGS_CONTENT_URI,
                    TagsTable.PROJECTION, selection, selectionArgs, sortOrder
            );
        } catch (RemoteException e) {
            AppLog.e(e.getMessage());
            return null;
        }
        if (cr == null) {
            return null;
        }
        return new TagsCursor(cr);
    }

    public void close() {
        if (mContentProviderClient != null) {
            mContentProviderClient.release();
        }
    }
}
