package com.anod.appwatcher.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;

import com.anod.appwatcher.R;
import com.anod.appwatcher.utils.InstalledAppsProvider;

/**
 * @author alex
 * @date 2015-08-30
 */
public class AppViewHolderDataProvider implements AppViewHolder.DataProvider {
    private final String mInstalledText;
    private final InstalledAppsProvider mInstalledAppsProvider;
    private int mTotalCount;
    private int mNewAppsCount;
    private Context mContext;
    private int mUpdatableAppsCount;

    public AppViewHolderDataProvider(Context context, InstalledAppsProvider installedAppsProvider) {
        Resources r = context.getResources();
        mContext = context;
        mInstalledText = r.getString(R.string.installed);
        mInstalledAppsProvider = installedAppsProvider;
    }

    void setNewAppsCount(int newAppsCount, int updatableAppsCount) {
        mNewAppsCount = newAppsCount;
        mUpdatableAppsCount = updatableAppsCount;
    }

    @Override
    public String getInstalledText() {
        return mInstalledText;
    }

    @Override
    public @ColorInt int getColor(@ColorRes int colorRes) {
        return ContextCompat.getColor(mContext, colorRes);
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
    public int getUpdatableAppsCount() {
        return mUpdatableAppsCount;
    }

    @Override
    public InstalledAppsProvider getInstalledAppsProvider() {
        return mInstalledAppsProvider;
    }

    @Override
    public String formatVersionText(String versionName, int versionNumber) {
        return mContext.getString(R.string.version_text, versionName, versionNumber);
    }

    public void setTotalCount(int totalCount) {
        mTotalCount = totalCount;
    }
}
