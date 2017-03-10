package com.anod.appwatcher.model;

import android.database.Cursor;

import com.anod.appwatcher.content.AppListCursor;
import com.anod.appwatcher.model.schema.AppListTable;
import com.anod.appwatcher.utils.FilterCursorWrapper;
import com.anod.appwatcher.utils.InstalledAppsProvider;

/**
 * @author alex
 * @date 8/4/14.
 */
public class InstalledFilter implements FilterCursorWrapper.CursorFilter {
    private final boolean mIncludeInstalled;
    private final InstalledAppsProvider mInstalledAppsProvider;
    private int mNewCount;
    private int mInstalledNewCount;

    public InstalledFilter(boolean includeInstalled, InstalledAppsProvider iap) {
        mIncludeInstalled = includeInstalled;
        mInstalledAppsProvider = iap;
    }

    @Override
    public boolean filterRecord(Cursor cursor) {
        String packageName = cursor.getString(AppListTable.Projection.PACKAGE);
        int status = cursor.getInt(AppListTable.Projection.STATUS);
        int versionCode = cursor.getInt(AppListTable.Projection.VERSION_NUMBER);

        InstalledAppsProvider.Info installedInfo = mInstalledAppsProvider.getInfo(packageName);
        boolean installed = installedInfo.isInstalled();

        if (mIncludeInstalled && !installed) {
            return true;
        }

        if (!mIncludeInstalled && installed) {
            return true;
        }

        if (status == AppInfo.STATUS_UPDATED) {
            mNewCount++;
            if (installedInfo.isUpdatable(versionCode)) {
                mInstalledNewCount++;
            }
        }
        return false;
    }

    int getNewCount() {
        return mNewCount;
    }

    int getUpdatableNewCount() {
        return mInstalledNewCount;
    }

    void resetNewCount() {
        mNewCount = 0;
        mInstalledNewCount = 0;
    }
}
