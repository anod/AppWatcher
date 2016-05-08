package com.anod.appwatcher.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.anod.appwatcher.AppListContentProvider;
import com.anod.appwatcher.R;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.utils.AppIconLoader;
import com.anod.appwatcher.utils.PackageManagerUtils;

public class AppViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private final DataProvider mDataProvider;
    private final AppIconLoader mIconLoader;

    public AppInfo app;
    public int position;
    public View section;
    public TextView sectionText;
    public TextView sectionCount;
    public TextView title;
    public TextView details;
    public TextView version;
    public TextView price;
    public ImageView icon;
    public View newIndicator;
    public TextView updateDate;
    private OnClickListener mListener;

    public interface OnClickListener {
        void onItemClick(AppInfo app);
    }

    public interface DataProvider {
        String getInstalledText();
        int getUpdateTextColor();
        int getTotalAppsCount();
        int getNewAppsCount();
        PackageManagerUtils getPackageManagerUtils();
        Bitmap getDefaultIcon();
        int getDefaultIconResource();
    }

    public AppViewHolder(View itemView, DataProvider dataProvider, AppIconLoader iconLoader, OnClickListener listener) {
        super(itemView);

        mListener = listener;
        mDataProvider = dataProvider;
        mIconLoader = iconLoader;

        this.app = null;
        this.position = 0;
        this.section = itemView.findViewById(R.id.sec_header);
        this.sectionText = (TextView) itemView.findViewById(R.id.sec_header_title);
        this.sectionCount = (TextView) itemView.findViewById(R.id.sec_header_count);
        this.title = (TextView) itemView.findViewById(android.R.id.title);
        this.details = (TextView) itemView.findViewById(R.id.details);
        this.icon = (ImageView) itemView.findViewById(android.R.id.icon);
        this.version = (TextView) itemView.findViewById(R.id.updated);
        this.price = (TextView) itemView.findViewById(R.id.price);
        this.newIndicator = itemView.findViewById(R.id.new_indicator);
        this.updateDate = (TextView) itemView.findViewById(R.id.update_date);

        itemView.findViewById(android.R.id.content).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        mListener.onItemClick(this.app);
    }

    public void bindView(int position, AppInfo app) {
        this.position = position;
        this.app = app;
        title.setText(app.title);
        details.setText(app.creator);

        if (app.getStatus() == AppInfo.STATUS_UPDATED) {
            newIndicator.setVisibility(View.VISIBLE);
        } else {
            newIndicator.setVisibility(View.INVISIBLE);
        }

        bindIcon(app);

        bindVersionText(app);

        bindPriceView(app);

        bindSectionView();

        String uploadDate = app.uploadDate;

        if (TextUtils.isEmpty(uploadDate)) {
            updateDate.setVisibility(View.GONE);
        } else {
            updateDate.setText(uploadDate);
            updateDate.setVisibility(View.VISIBLE);
        }
    }

    protected void bindVersionText(AppInfo app) {
        if (app.getStatus() == AppInfo.STATUS_UPDATED) {
            version.setVisibility(View.VISIBLE);
            version.setText(app.versionName);
            version.setTextColor(mDataProvider.getUpdateTextColor());
        } else {
            if (TextUtils.isEmpty(app.versionName)) {
                version.setVisibility(View.INVISIBLE);
            } else {
                version.setVisibility(View.VISIBLE);
                version.setText(app.versionName);
            }
        }
    }

    protected void bindIcon(AppInfo app) {
        if (TextUtils.isEmpty(app.iconUrl)) {
            if (app.getRowId() > 0) {
                Uri dbImageUri = AppListContentProvider.ICONS_CONTENT_URI.buildUpon().appendPath(String.valueOf(app.getRowId())).build();
                mIconLoader.retrieve(dbImageUri)
                        .placeholder(mDataProvider.getDefaultIconResource())
                        .into(this.icon);
            } else {
                this.icon.setImageBitmap(mDataProvider.getDefaultIcon());
            }
        } else {
            mIconLoader.retrieve(app.iconUrl)
                    .placeholder(mDataProvider.getDefaultIconResource())
                    .into(this.icon);
        }
    }

    protected void bindPriceView(AppInfo app) {
        boolean isInstalled = mDataProvider.getPackageManagerUtils().isAppInstalled(app.packageName);
        if (isInstalled) {
            PackageManagerUtils.InstalledInfo installed = mDataProvider.getPackageManagerUtils().getInstalledInfo(app.packageName);
            price.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_stat_communication_stay_primary_portrait, 0,0,0);
            if (TextUtils.isEmpty(installed.versionName)) {
                price.setText(mDataProvider.getInstalledText());
            } else {
                price.setText(installed.versionName);
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

    protected void bindSectionView() {
        if (position == mDataProvider.getNewAppsCount()) {
            sectionText.setText(R.string.watching);
            sectionCount.setText(String.valueOf(mDataProvider.getTotalAppsCount() - mDataProvider.getNewAppsCount()));
            section.setVisibility(View.VISIBLE);
        } else if (position == 0 && mDataProvider.getNewAppsCount() > 0) {
            sectionText.setText(R.string.recently_updated);
            sectionCount.setText(String.valueOf(mDataProvider.getNewAppsCount()));
            section.setVisibility(View.VISIBLE);
        } else {
            section.setVisibility(View.GONE);
        }
    }
}