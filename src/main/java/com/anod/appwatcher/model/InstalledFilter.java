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
    private int mInstalledNewCount;

    public InstalledFilter(boolean checkInstalled, PackageManagerUtils pmutils) {
        mCheckInstalled = checkInstalled;
        mPMUtils = pmutils;
    }

    @Override
    public boolean filterRecord(Cursor cursor) {
        String packageName = cursor.getString(AppListCursor.IDX_PACKAGE);
        int status = cursor.getInt(AppListCursor.IDX_STATUS);
        int versionCode = cursor.getInt(AppListCursor.IDX_VERSION_NUMBER);

        PackageManagerUtils.InstalledInfo installedInfo = mPMUtils.getInstalledInfo(packageName);
        boolean installed = installedInfo != null && installedInfo.versionCode > 0;

        boolean filterRecord = (mCheckInstalled) ? !installed : installed;

        if (filterRecord) {
            return true;
        }
        if (status == AppInfo.STATUS_UPDATED) {
            mNewCount++;
            if (installed && installedInfo.versionCode != versionCode) {
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
