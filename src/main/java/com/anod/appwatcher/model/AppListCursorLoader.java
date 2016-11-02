package com.anod.appwatcher.model;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.CursorLoader;
import android.text.TextUtils;

import com.anod.appwatcher.AppListContentProvider;
import com.anod.appwatcher.Preferences;
import com.anod.appwatcher.model.schema.AppListTable;
import com.anod.appwatcher.utils.FilterCursorWrapper;
import com.anod.appwatcher.utils.PackageManagerUtils;

import java.util.ArrayList;

import info.anodsplace.android.log.AppLog;

/**
 * @author alex
 * @date 8/11/13
 */
public class AppListCursorLoader extends CursorLoader {
    private static final Uri CONTENT_URI = AppListContentProvider.APPS_CONTENT_URI;
    private static final String ORDER_DEFAULT = AppListTable.Columns.KEY_STATUS + " DESC, "
            + AppListTable.Columns.KEY_TITLE + " COLLATE LOCALIZED ASC";
    private static final String SELECTION_TITLE = AppListTable.Columns.KEY_STATUS + " != ? AND " + AppListTable.Columns.KEY_TITLE + " LIKE ?";
    private static final String SELECTION_DEFAULT = AppListTable.Columns.KEY_STATUS + " != ? ";

    private final FilterCursorWrapper.CursorFilter mCursorFilter;
    protected final String mTitleFilter;

    private int mNewCount;
    private int mUpdatableNewCount;

    public AppListCursorLoader(Context context, String titleFilter, int sortId, FilterCursorWrapper.CursorFilter cursorFilter) {
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
        setSortOrder(createSortOrder(sortId));
    }

    private String createSortOrder(int sortId) {
        ArrayList<String> filter = new ArrayList<>();
        filter.add(AppListTable.Columns.KEY_STATUS + " DESC");
        if (sortId == Preferences.SORT_NAME_DESC) {
            filter.add(AppListTable.Columns.KEY_TITLE + " COLLATE LOCALIZED DESC");
        } else if (sortId == Preferences.SORT_DATE_ASC) {
            filter.add(AppListTable.Columns.KEY_REFRESH_TIMESTAMP + " ASC");
        } else if (sortId == Preferences.SORT_DATE_DESC) {
            filter.add(AppListTable.Columns.KEY_REFRESH_TIMESTAMP + " DESC");
        } else {
            filter.add(AppListTable.Columns.KEY_TITLE + " COLLATE LOCALIZED ASC");
        }
        AppLog.d(TextUtils.join(", ", filter));
        return TextUtils.join(", ", filter);
    }

    @Override
    public Cursor loadInBackground() {
        Cursor cr = super.loadInBackground();

        if (mCursorFilter == null) {
            loadNewCount();
            return new AppListCursor(cr);
        } else {
            if (mCursorFilter instanceof InstalledFilter)
            {
                ((InstalledFilter)mCursorFilter).resetNewCount();
            }
            return new AppListCursor(new FilterCursorWrapper(cr, mCursorFilter));
        }
    }

    public int getNewCountFiltered()
    {
        if (mCursorFilter instanceof InstalledFilter)
        {
            return ((InstalledFilter)mCursorFilter).getNewCount();
        }
        return mNewCount;
    }

    private void loadNewCount() {
        AppListContentProviderClient cl = new AppListContentProviderClient(getContext());
        AppListCursor apps = cl.queryUpdated();

        mNewCount = 0;
        mUpdatableNewCount = 0;

        if (apps == null) {
            return;
        }

        mNewCount = apps.getCount();
        if (mNewCount > 0) {
            PackageManagerUtils pm = new PackageManagerUtils(getContext().getPackageManager());
            apps.moveToPosition(-1);
            while (apps.moveToNext()) {
                AppInfo info = apps.getAppInfo();
                if (pm.isUpdatable(info.packageName, info.versionNumber))
                {
                    mUpdatableNewCount++;
                }
            }
        }

        apps.close();
        cl.release();
    }

    public int getUpdatableCountFiltered() {
        if (mCursorFilter instanceof InstalledFilter)
        {
            return ((InstalledFilter)mCursorFilter).getUpdatableNewCount();
        }
        return mUpdatableNewCount;
    }
}
