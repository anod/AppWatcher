package com.anod.appwatcher.adapters;

import android.support.annotation.ColorInt;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.anod.appwatcher.R;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.utils.PackageManagerUtils;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @author algavris
 * @date 14/05/2016.
 */
public class AppDetailsView {

    private final AppViewHolder.DataProvider mDataProvider;
    private
    @ColorInt int mAccentColor;

    @Bind(android.R.id.title)
    public TextView title;
    @Bind(R.id.details)
    public TextView details;
    @Bind(R.id.updated)
    public TextView version;
    @Bind(R.id.price)
    public TextView price;
    @Bind(R.id.update_date)
    public TextView updateDate;

    public AppDetailsView(View view, AppViewHolder.DataProvider dataProvider)
    {
        mDataProvider = dataProvider;
        ButterKnife.bind(this, view);
        mAccentColor = mDataProvider.getUpdateTextColor();
    }


    public void fillDetails(AppInfo app, boolean isLocalApp)
    {
        title.setText(app.title);
        details.setText(app.creator);

        String uploadDate = app.uploadDate;

        if (TextUtils.isEmpty(uploadDate)) {
            updateDate.setVisibility(View.GONE);
        } else {
            updateDate.setText(uploadDate);
            updateDate.setVisibility(View.VISIBLE);
        }

        if (isLocalApp) {
            this.price.setVisibility(View.GONE);
            this.version.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_stat_communication_stay_primary_portrait, 0, 0, 0);
            this.version.setText(mDataProvider.formatVersionText(app.versionName, app.versionNumber));
        } else {
            this.fillWatchAppView(app);
        }
    }


    private void fillWatchAppView(AppInfo app) {
        version.setText(mDataProvider.formatVersionText(app.versionName, app.versionNumber));
        if (app.getStatus() == AppInfo.STATUS_UPDATED) {
            version.setTextColor(mAccentColor);
        }

        boolean isInstalled = mDataProvider.getPackageManagerUtils().isAppInstalled(app.packageName);
        price.setTextColor(mAccentColor);
        if (isInstalled) {
            PackageManagerUtils.InstalledInfo installed = mDataProvider.getPackageManagerUtils().getInstalledInfo(app.packageName);
            price.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_stat_communication_stay_primary_portrait, 0,0,0);
            if (TextUtils.isEmpty(installed.versionName)) {
                price.setText(mDataProvider.getInstalledText());
            } else {
                price.setText(mDataProvider.formatVersionText(installed.versionName, installed.versionCode));
            }
        } else {
            price.setCompoundDrawables(null, null, null, null);
            if (app.priceMicros == 0) {
                price.setText(R.string.free);
            } else {
                price.setText(app.priceText);
            }
        }
    }

    public void updateAccentColor(@ColorInt int color, AppInfo app) {
        mAccentColor = color;
        price.setTextColor(mAccentColor);
        if (app.getStatus() == AppInfo.STATUS_UPDATED) {
            version.setTextColor(mAccentColor);
        }
    }
}
