package com.anod.appwatcher.adapters;

import android.content.Context;
import android.content.res.Resources;

import com.anod.appwatcher.R;
import com.anod.appwatcher.utils.PackageManagerUtils;

/**
 * @author alex
 * @date 2015-08-30
 */
public class AppViewHolderDataProvider implements AppViewHolder.DataProvider {
    private final String mVersionText;
    private final String mUpdateText;
    private final String mInstalledText;
    private final int mUpdateTextColor;
    private final PackageManagerUtils mPMUtils;
    private int mTotalCount;
    private int mNewAppsCount;

    public AppViewHolderDataProvider(Context context, PackageManagerUtils pmutils) {
        Resources r = context.getResources();
        mVersionText = r.getString(R.string.version);
        mUpdateText = r.getString(R.string.update);
        mInstalledText = r.getString(R.string.installed);
        mUpdateTextColor = r.getColor(R.color.blue_new);

        mPMUtils = pmutils;

    }

    public void setNewAppsCount(int newAppsCount) {
        mNewAppsCount = newAppsCount;
    }

    @Override
    public String getVersionText() {
        return mVersionText;
    }

    @Override
    public String getUpdateText() {
        return mUpdateText;
    }

    @Override
    public String getInstalledText() {
        return mInstalledText;
    }

    @Override
    public int getUpdateTextColor() {
        return mUpdateTextColor;
    }

    @Override
    public int getTotalAppsCount() {
        return mTotalCount;
    }

    @Override
    public int getNewAppsCount() {
        return mNewAppsCount;
    }

    @Override
    public PackageManagerUtils getPackageManagerUtils() {
        return mPMUtils;
    }

    public void setTotalCount(int totalCount) {
        mTotalCount = totalCount;
    }
}
