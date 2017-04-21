package com.anod.appwatcher.adapters;

import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.utils.AppIconLoader;
import com.anod.appwatcher.utils.InstalledAppsProvider;

/**
 * @author algavris
 * @date 22/05/2016.
 */

public abstract class AppViewHolderBase extends RecyclerView.ViewHolder {
    protected final DataProvider mDataProvider;
    protected final AppIconLoader mIconLoader;

    protected interface DataProvider {
        String getInstalledText();
        int getTotalAppsCount();
        int getNewAppsCount();
        InstalledAppsProvider getInstalledAppsProvider();
        String formatVersionText(String versionName, int versionNumber);
        @ColorInt
        int getColor(@ColorRes int colorRes);

        int getUpdatableAppsCount();
    }

    public AppViewHolderBase(View itemView, DataProvider dataProvider, AppIconLoader iconLoader) {
        super(itemView);
        mDataProvider = dataProvider;
        mIconLoader = iconLoader;
    }

    public abstract void bindView(int position, AppInfo app);
}
