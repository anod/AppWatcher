package com.anod.appwatcher.adapters;

import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.anod.appwatcher.R;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.utils.PackageManagerUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author algavris
 * @date 14/05/2016.
 */
public class AppDetailsView {

    private final AppViewHolder.DataProvider mDataProvider;
    private final int mTextColor;
    private @ColorInt int mAccentColor;

    @BindView(android.R.id.title)
    public TextView title;
    @Nullable @BindView(R.id.details)
    public TextView details;
    @Nullable @BindView(R.id.updated)
    public TextView version;
    @Nullable  @BindView(R.id.price)
    public TextView price;
    @Nullable @BindView(R.id.update_date)
    public TextView updateDate;
    @Nullable @BindView(R.id.app_type)
    public TextView appType;

    public AppDetailsView(View view, AppViewHolder.DataProvider dataProvider)
    {
        mDataProvider = dataProvider;
        ButterKnife.bind(this, view);
        mAccentColor = mDataProvider.getColor(R.color.blue_new);
        mTextColor = mDataProvider.getColor(R.color.primary_text);
    }

    public void fillDetails(AppInfo app, boolean isLocalApp)
    {
        assert details != null;
        assert updateDate != null;
        assert price != null;
        assert version != null;

        title.setText(app.title);
        details.setText(app.creator);
        String uploadDate = app.uploadDate;

        if (TextUtils.isEmpty(uploadDate)) {
            updateDate.setVisibility(View.GONE);
        } else {
            updateDate.setText(uploadDate);
            updateDate.setVisibility(View.VISIBLE);
        }

        if (appType != null) {
            appType.setVisibility(View.GONE);

//            if (TextUtils.isEmpty(app.appType)) {
//                appType.setVisibility(View.GONE);
//            } else {
//                appType.setText(app.appType);
//                appType.setVisibility(View.VISIBLE);
//            }
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
        assert price != null;
        assert version != null;

        boolean isInstalled = mDataProvider.getPackageManagerUtils().isAppInstalled(app.packageName);
        version.setText(mDataProvider.formatVersionText(app.versionName, app.versionNumber));
        if (app.getStatus() == AppInfo.STATUS_UPDATED) {
            version.setTextColor(mAccentColor);
        } else {
            version.setTextColor(mTextColor);
        }

        price.setTextColor(mAccentColor);
        if (isInstalled) {
            PackageManagerUtils.InstalledInfo installed = mDataProvider.getPackageManagerUtils().getInstalledInfo(app.packageName);
            price.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_stat_communication_stay_primary_portrait, 0,0,0);
            if (TextUtils.isEmpty(installed.versionName)) {
                price.setText(mDataProvider.getInstalledText());
            } else {
                price.setText(mDataProvider.formatVersionText(installed.versionName, installed.versionCode));
            }
            if (app.versionNumber > installed.versionCode)
            {
                version.setTextColor(mDataProvider.getColor(R.color.material_amber_800));
            } else {
                version.setTextColor(mTextColor);
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
        assert price != null;
        assert version != null;

        mAccentColor = color;
        price.setTextColor(mAccentColor);
        if (app.getStatus() == AppInfo.STATUS_UPDATED) {
            version.setTextColor(mAccentColor);
        }
    }
}
