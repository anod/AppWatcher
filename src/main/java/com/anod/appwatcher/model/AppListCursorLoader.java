package com.anod.appwatcher.model;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.CursorLoader;
import android.text.TextUtils;

import com.anod.appwatcher.AppListContentProvider;
import com.anod.appwatcher.Preferences;
import com.anod.appwatcher.content.DbContentProviderClient;
import com.anod.appwatcher.content.AppListCursor;
import com.anod.appwatcher.model.schema.AppListTable;
import com.anod.appwatcher.model.schema.AppTagsTable;
import com.anod.appwatcher.utils.FilterCursorWrapper;
import com.anod.appwatcher.utils.InstalledAppsProvider;

import java.util.ArrayList;

/**
 * @author alex
 * @date 8/11/13
 */
public class AppListCursorLoader extends CursorLoader {
    private static final String ORDER_DEFAULT = AppListTable.Columns.KEY_STATUS + " DESC, "
            + AppListTable.Columns.KEY_TITLE + " COLLATE LOCALIZED ASC";

    private final FilterCursorWrapper.CursorFilter mCursorFilter;
    protected final String mTitleFilter;

    private int mNewCount;
    private int mUpdatableNewCount;

    public AppListCursorLoader(Context context, String titleFilter, int sortId, FilterCursorWrapper.CursorFilter cursorFilter, Tag tag) {
        super(context, getContentUri(tag), AppListTable.PROJECTION, null, null, ORDER_DEFAULT);

        mCursorFilter = cursorFilter;
        mTitleFilter = titleFilter;

        ArrayList<String> selc = new ArrayList<>(3);
        ArrayList<String> args = new ArrayList<>(3);

        selc.add(AppListTable.Columns.KEY_STATUS + " != ?");
        args.add(String.valueOf(AppInfoMetadata.STATUS_DELETED));

        if (tag != null)
        {
            selc.add(AppTagsTable.TableColumns.TAGID + " = ?");
            args.add(String.valueOf(tag.id));
            selc.add(AppTagsTable.TableColumns.APPID + " = " + AppListTable.TableColumns.APPID);
        }

        if (!TextUtils.isEmpty(titleFilter)) {
            selc.add(AppListTable.Columns.KEY_TITLE + " LIKE ?");
            args.add("%" + titleFilter + "%");
        }

        String selection = TextUtils.join(" AND ", selc);
        String[] selectionArgs = args.toArray(new String[0]);

        setSelection(selection);
        setSelectionArgs(selectionArgs);

        setSortOrder(createSortOrder(sortId));
    }

    private static Uri getContentUri(Tag tag) {
        return tag == null ?
                AppListContentProvider.APPS_CONTENT_URI :
                AppListContentProvider.TAGS_CONTENT_URI
                     .buildUpon()
                     .appendPath(String.valueOf(tag.id))
                     .appendPath("apps").build();
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
        DbContentProviderClient cl = new DbContentProviderClient(getContext());
        AppListCursor apps = cl.queryUpdated();

        mNewCount = 0;
        mUpdatableNewCount = 0;

        if (apps == null) {
            return;
        }

        mNewCount = apps.getCount();
        if (mNewCount > 0) {
            InstalledAppsProvider iap = new InstalledAppsProvider.PackageManager(getContext().getPackageManager());
            apps.moveToPosition(-1);
            while (apps.moveToNext()) {
                AppInfo info = apps.getAppInfo();
                if (iap.getInfo(info.packageName).isUpdatable(info.versionNumber))
                {
                    mUpdatableNewCount++;
                }
            }
        }

        apps.close();
        cl.close();
    }

    public int getUpdatableCountFiltered() {
        if (mCursorFilter instanceof InstalledFilter)
        {
            return ((InstalledFilter)mCursorFilter).getUpdatableNewCount();
        }
        return mUpdatableNewCount;
    }
}
