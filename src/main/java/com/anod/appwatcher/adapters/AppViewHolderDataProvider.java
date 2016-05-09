package com.anod.appwatcher.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

import com.anod.appwatcher.R;
import com.anod.appwatcher.utils.PackageManagerUtils;

/**
 * @author alex
 * @date 2015-08-30
 */
public class AppViewHolderDataProvider implements AppViewHolder.DataProvider {
    private final String mInstalledText;
    private final int mUpdateTextColor;
    private final PackageManagerUtils mPMUtils;
    private int mTotalCount;
    private int mNewAppsCount;
    private Bitmap mDefaultIcon;
    private Context mContext;

    public AppViewHolderDataProvider(Context context, PackageManagerUtils pmutils) {
        Resources r = context.getResources();
        mContext = context;
        mInstalledText = r.getString(R.string.installed);
        mUpdateTextColor = r.getColor(R.color.blue_new);

        mPMUtils = pmutils;

    }

    public void setNewAppsCount(int newAppsCount) {
        mNewAppsCount = newAppsCount;
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

    @Override
    public Bitmap getDefaultIcon() {
        if (mDefaultIcon == null) {
            mDefaultIcon = BitmapFactory.decodeResource(mContext.getResources(), getDefaultIconResource());
        }
        return mDefaultIcon;
    }

    @Override
    public int getDefaultIconResource() {
        return R.drawable.ic_android_black_48dp;
    }

    public void setTotalCount(int totalCount) {
        mTotalCount = totalCount;
    }
}