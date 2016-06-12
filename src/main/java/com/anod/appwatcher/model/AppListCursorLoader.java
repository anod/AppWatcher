package com.anod.appwatcher.model;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.CursorLoader;
import android.text.TextUtils;

import com.anod.appwatcher.AppListContentProvider;
import com.anod.appwatcher.model.schema.AppListTable;
import com.anod.appwatcher.utils.FilterCursorWrapper;

/**
 * @author alex
 * @date 8/11/13
 */
public class AppListCursorLoader extends CursorLoader {
    private static final Uri CONTENT_URI = AppListContentProvider.APPS_CONTENT_URI;
    private static final String ORDER_DEFAULT = AppListTable.Columns.KEY_STATUS + " DESC, " + AppListTable.Columns.KEY_TITLE + " COLLATE LOCALIZED ASC";
    private static final String SELECTION_TITLE = AppListTable.Columns.KEY_STATUS + " != ? AND " + AppListTable.Columns.KEY_TITLE + " LIKE ?";
    private static final String SELECTION_DEFAULT = AppListTable.Columns.KEY_STATUS + " != ? ";

    private final FilterCursorWrapper.CursorFilter mCursorFilter;
    protected final String mTitleFilter;

    private int mNewCount;

    public AppListCursorLoader(Context context, String titleFilter, FilterCursorWrapper.CursorFilter cursorFilter) {
        super(context, CONTENT_URI, AppListTable.PROJECTION, null, null, ORDER_DEFAULT);

        mCursorFilter = cursorFilter;
        mTitleFilter = titleFilter;

        if (!TextUtils.isEmpty(titleFilter)) {
            setSelection(SELECTION_TITLE);
            setSelectionArgs(new String[]{String.valueOf(AppInfoMetadata.STATUS_DELETED), "%" + titleFilter + "%"});
        } else {
            setSelection(SELECTION_DEFAULT);
            setSelectionArgs(new String[]{String.valueOf(AppInfoMetadata.STATUS_DELETED)});
        }
    }

    @Override
    public Cursor loadInBackground() {
        Cursor cr = super.loadInBackground();

        loadNewCount();

        if (mCursorFilter == null) {
            return new AppListCursor(cr);
        } else {
            return new AppListCursor(new FilterCursorWrapper(cr, mCursorFilter));
        }
    }

    public int getNewCount() {
        return mNewCount;
    }

    private void loadNewCount() {
        AppListContentProviderClient cl = new AppListContentProviderClient(getContext());
        mNewCount = cl.queryUpdatesCount();
        cl.release();
    }
}
