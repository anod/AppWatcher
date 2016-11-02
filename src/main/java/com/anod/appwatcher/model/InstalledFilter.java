package com.anod.appwatcher.model;

import android.database.Cursor;

import com.anod.appwatcher.utils.FilterCursorWrapper;
import com.anod.appwatcher.utils.PackageManagerUtils;

/**
 * @author alex
 * @date 8/4/14.
 */
public class InstalledFilter implements FilterCursorWrapper.CursorFilter {
    private final boolean mIncludeInstalled;
    private final PackageManagerUtils mPMUtils;
    private int mNewCount;
    private int mInstalledNewCount;

    public InstalledFilter(boolean includeInstalled, PackageManagerUtils pmutils) {
        mIncludeInstalled = includeInstalled;
        mPMUtils = pmutils;
    }

    @Override
    public boolean filterRecord(Cursor cursor) {
        String packageName = cursor.getString(AppListCursor.IDX_PACKAGE);
        int status = cursor.getInt(AppListCursor.IDX_STATUS);
        int versionCode = cursor.getInt(AppListCursor.IDX_VERSION_NUMBER);

        PackageManagerUtils.InstalledInfo installedInfo = mPMUtils.getInstalledInfo(packageName);
        boolean installed = installedInfo.versionCode > 0;

        if (mIncludeInstalled && !installed) {
            return true;
        }

        if (!mIncludeInstalled && installed) {
            return true;
        }

        if (status == AppInfo.STATUS_UPDATED) {
            mNewCount++;
            if (mPMUtils.isUpdatable(packageName, versionCode)) {
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
