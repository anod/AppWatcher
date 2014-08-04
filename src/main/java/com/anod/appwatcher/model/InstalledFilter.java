package com.anod.appwatcher.model;

import android.database.Cursor;

import com.anod.appwatcher.utils.FilterCursorWrapper;
import com.anod.appwatcher.utils.PackageManagerUtils;

/**
 * Created by alex on 8/4/14.
 */
public class InstalledFilter implements FilterCursorWrapper.CursorFilter {
    private final boolean mCheckInstalled;
    private final PackageManagerUtils mPMUtils;

    public InstalledFilter(boolean checkInstalled, PackageManagerUtils pmutils) {
        mCheckInstalled = checkInstalled;
        mPMUtils = pmutils;
    }

    @Override
    public boolean filterRecord(Cursor cursor) {
        String packageName = cursor.getString(AppListCursor.IDX_PACKAGE);
        boolean installed = mPMUtils.isAppInstalled(packageName);
        if (mCheckInstalled) {
            return !installed;
        }
        return installed;
    }
}
