package com.anod.appwatcher.watchlist;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.anod.appwatcher.R;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.model.AppListCursor;
import com.anod.appwatcher.utils.PackageManagerUtils;

public class ListCursorAdapter extends CursorAdapter {
    private final String mInstalledText;
    private final PackageManagerUtils mPMUtils;
    private LayoutInflater mInflater;
    private Bitmap mDefaultIcon;
    private String mVersionText;
    private String mUpdateText;
    private int mDefColor;
    private int mUpdateTextColor;
    private int mNewAppsCount;
    private CharSequence mTitleFilter;
    private int mTotalCount;

    public ListCursorAdapter(Context context, PackageManagerUtils packageManagerUtils) {
        super(context, null, 0);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Resources r = context.getResources();
        mVersionText = r.getString(R.string.version);
        mUpdateText = r.getString(R.string.update);
        mInstalledText = r.getString(R.string.installed);
        mUpdateTextColor = r.getColor(R.color.blue_new);
        mPMUtils = packageManagerUtils;

    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        AppListCursor wrapper = (AppListCursor) cursor;
        AppInfo app = wrapper.getAppInfo();

        AppViewHolder holder = (AppViewHolder) view.getTag();
        holder.position = cursor.getPosition();
        holder.app = app;
        holder.title.setText(app.getTitle());
        holder.details.setText(app.getCreator());
//        holder.removeBtn.setTag(app);
//        holder.marketBtn.setTag(app.getPackageName());
//        holder.changelogBtn.setTag(app.getAppId() + "," + app.getDetailsUrl());
//        holder.shareBtn.setTag(app);
        holder.icon.setTag(holder);
        Bitmap icon = app.getIcon();
        if (icon == null) {
            if (mDefaultIcon == null) {
                mDefaultIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_empty);
            }
            icon = mDefaultIcon;
        }

        holder.icon.setImageBitmap(icon);
        if (app.getStatus() == AppInfo.STATUS_UPDATED) {
            holder.version.setVisibility(View.VISIBLE);
            holder.version.setText(String.format(mUpdateText, app.getVersionName()));
            holder.version.setTextColor(mUpdateTextColor);
            holder.newIndicator.setVisibility(View.VISIBLE);
        } else {
            if (TextUtils.isEmpty(app.getVersionName())) {
                holder.version.setVisibility(View.INVISIBLE);
            } else {
                holder.version.setVisibility(View.VISIBLE);
                holder.version.setText(String.format(mVersionText, app.getVersionName()));
                holder.version.setTextColor(mDefColor);
            }
            holder.newIndicator.setVisibility(View.INVISIBLE);
        }

        boolean isInstalled = mPMUtils.isAppInstalled(app.getPackageName());
        if (isInstalled) {
            PackageManagerUtils.InstalledInfo installed = mPMUtils.getInstalledInfo(app.getPackageName());
            if (TextUtils.isEmpty(installed.versionName)) {
                holder.price.setText(mInstalledText);
            } else {
                holder.price.setText(mInstalledText + " " + installed.versionName);
            }
        } else {
            if (app.getPriceMicros() == 0) {
                holder.price.setText(R.string.free);
            } else {
                holder.price.setText(app.getPriceText());
            }
        }
        if (mNewAppsCount > 0 && TextUtils.isEmpty(mTitleFilter)) {
            if (holder.position == 0) {
                holder.sectionText.setText(context.getString(R.string.recently_updated));
                holder.sectionCount.setText(String.valueOf(mNewAppsCount));
                holder.section.setVisibility(View.VISIBLE);
            } else if (holder.position == mNewAppsCount) {
                holder.sectionText.setText(context.getString(R.string.watching));
                holder.sectionCount.setText(String.valueOf(mTotalCount - mNewAppsCount));
                holder.section.setVisibility(View.VISIBLE);
            } else {
                holder.section.setVisibility(View.GONE);
            }
        } else {
            holder.section.setVisibility(View.GONE);
        }

        String uploadDate = app.getUploadDate();

        if (!"".equals(uploadDate)) {
            holder.updateDate.setText(uploadDate);
            holder.updateDate.setVisibility(View.VISIBLE);
        } else {
            holder.updateDate.setVisibility(View.GONE);
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View v = mInflater.inflate(R.layout.list_item_app, parent, false);
        v.setClickable(true);
        v.setFocusable(true);
        return v;
    }

    @Override
    public Cursor swapCursor(Cursor newCursor) {
        mTotalCount = newCursor.getCount();
        return super.swapCursor(newCursor);
    }

    public void setNewAppsCount(int newAppsCount) {
        mNewAppsCount = newAppsCount;
    }
}