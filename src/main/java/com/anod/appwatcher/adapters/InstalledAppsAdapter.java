package com.anod.appwatcher.adapters;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.anod.appwatcher.R;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.model.AppInfoMetadata;
import com.anod.appwatcher.utils.AppIconLoader;
import com.anod.appwatcher.utils.PackageManagerUtils;

import java.util.ArrayList;
import java.util.List;

import info.anodsplace.android.widget.recyclerview.ArrayAdapter;

/**
 * @author alex
 * @date 2015-08-30
 */
public class InstalledAppsAdapter extends ArrayAdapter<PackageInfo, AppViewHolder>{
    private final AppViewHolder.OnClickListener mListener;
    private final Context mContext;
    private final AppViewHolderDataProvider mDataProvider;
    private final PackageManagerUtils mPMUtils;

    private final AppIconLoader mIconLoader;
    private final Drawable mDefaultIconDrawable;

    public InstalledAppsAdapter(Context context, PackageManagerUtils pmutils, AppViewHolder.OnClickListener listener) {
        super(new ArrayList<PackageInfo>());
        mContext = context;
        mListener = listener;
        mDataProvider = new AppViewHolderDataProvider(context, pmutils);

        mPMUtils = pmutils;
        mIconLoader = new AppIconLoader(context);

        mDefaultIconDrawable = ContextCompat.getDrawable(context, R.drawable.ic_android_black_48dp);
    }

    @Override
    public int getItemViewType(int position) {
        return 2;
    }

    @Override
    public AppViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.list_item_app, parent, false);
        v.setClickable(true);
        v.setFocusable(true);

        return new InstalledAppViewHolder(v, mDataProvider, mListener);

    }

    @Override
    public void onBindViewHolder(AppViewHolder holder, int position) {
        PackageInfo packageInfo = getItem(position);
        AppInfo app = mPMUtils.packageToApp(packageInfo);
        /**
         *
         * int rowId, String appId, String pname, int versionNumber, String versionName,
         String title, String creator, Bitmap icon, int status, String uploadDate, String priceText, String priceCur, Integer priceMicros, String detailsUrl) {
         */
        holder.bindView(position, app);

        mIconLoader.picasso()
                .load(Uri.fromParts(AppIconLoader.SCHEME,mPMUtils.getLaunchComponent(packageInfo).flattenToShortString(),null))
                .placeholder(mDefaultIconDrawable)
                .into(holder.icon);
    }

    @Override
    public void addAll(List<PackageInfo> objects) {
        super.addAll(objects);
        mDataProvider.setTotalCount(getItemCount());
    }
}
