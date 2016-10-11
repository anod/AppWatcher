package com.anod.appwatcher.adapters;

import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.anod.appwatcher.AppListContentProvider;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.utils.AppIconLoader;
import com.anod.appwatcher.utils.PackageManagerUtils;

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
        PackageManagerUtils getPackageManagerUtils();
        Bitmap getDefaultIcon();
        int getDefaultIconResource();
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

    protected void bindIcon(AppInfo app, ImageView iconView) {
        if (TextUtils.isEmpty(app.iconUrl)) {
            if (app.getRowId() > 0) {
                Uri dbImageUri = AppListContentProvider.ICONS_CONTENT_URI.buildUpon().appendPath(String.valueOf(app.getRowId())).build();
                mIconLoader.retrieve(dbImageUri)
                        .placeholder(mDataProvider.getDefaultIconResource())
                        .into(iconView);
            } else {
                iconView.setImageBitmap(mDataProvider.getDefaultIcon());
            }
        } else {
            mIconLoader.retrieve(app.iconUrl)
                    .placeholder(mDataProvider.getDefaultIconResource())
                    .into(iconView);
        }
    }
}
