package com.anod.appwatcher.model;

import android.database.Cursor;

import com.anod.appwatcher.utils.FilterCursorWrapper;
import com.anod.appwatcher.utils.PackageManagerUtils;

/**
 * @author alex
 * @date 8/4/14.
 */
public class InstalledFilter implements FilterCursorWrapper.CursorFilter {
    private final boolean mCheckInstalled;
    private final PackageManagerUtils mPMUtils;
    private int mNewCount;

    public InstalledFilter(boolean checkInstalled, PackageManagerUtils pmutils) {
        mCheckInstalled = checkInstalled;
        mPMUtils = pmutils;
    }

    @Override
    public boolean filterRecord(Cursor cursor) {
        String packageName = cursor.getString(AppListCursor.IDX_PACKAGE);
        int status = cursor.getInt(AppListCursor.IDX_STATUS);

        boolean installed = mPMUtils.isAppInstalled(packageName);

        boolean filterRecord = (mCheckInstalled) ? !installed : installed;

        if (filterRecord) {
            return true;
        }
        if (status == AppInfo.STATUS_UPDATED) {
            mNewCount++;
        }
        return false;
    }

    int getNewCount() {
        return mNewCount;
    }

    void resetNewCount() {
        mNewCount = 0;
    }
}
