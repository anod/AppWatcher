package com.anod.appwatcher.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;

import com.anod.appwatcher.R;
import com.anod.appwatcher.utils.PackageManagerUtils;

/**
 * @author alex
 * @date 2015-08-30
 */
public class AppViewHolderDataProvider implements AppViewHolder.DataProvider {
    private final String mInstalledText;
    private final PackageManagerUtils mPMUtils;
    private int mTotalCount;
    private int mNewAppsCount;
    private Bitmap mDefaultIcon;
    private Context mContext;
    private int mUpdatableAppsCount;

    public AppViewHolderDataProvider(Context context, PackageManagerUtils pmutils) {
        Resources r = context.getResources();
        mContext = context;
        mInstalledText = r.getString(R.string.installed);
        mPMUtils = pmutils;
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
    public PackageManagerUtils getPackageManagerUtils() {
        return mPMUtils;
    }

    @Override
    public Bitmap getDefaultIcon() {
        if (mDefaultIcon == null) {
            mDefaultIcon = BitmapFactory.decodeResource(mContext.getResources(), getDefaultIconResource());
        }
        return mDefaultIcon;
    }

    @Override
    public int getDefaultIconResource() {
        return R.drawable.ic_android_black_24dp;
    }

    @Override
    public String formatVersionText(String versionName, int versionNumber) {
        return mContext.getString(R.string.version_text, versionName, versionNumber);
    }

    public void setTotalCount(int totalCount) {
        mTotalCount = totalCount;
    }
}
