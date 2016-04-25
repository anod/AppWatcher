package com.anod.appwatcher.installed;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.anod.appwatcher.R;
import com.anod.appwatcher.adapters.AppViewHolder;
import com.anod.appwatcher.adapters.AppViewHolderDataProvider;
import com.anod.appwatcher.model.AppInfo;
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
    protected final AppViewHolder.OnClickListener mListener;
    protected final Context mContext;
    protected final AppViewHolderDataProvider mDataProvider;
    protected final PackageManagerUtils mPMUtils;

    protected final AppIconLoader mIconLoader;

    public InstalledAppsAdapter(Context context, PackageManagerUtils pmutils, AppViewHolderDataProvider dataProvider, AppViewHolder.OnClickListener listener) {
        super(new ArrayList<PackageInfo>());
        mContext = context;
        mListener = listener;
        mDataProvider = dataProvider;

        mPMUtils = pmutils;
        mIconLoader = new AppIconLoader(context);

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

        ComponentName launchComponent = mPMUtils.getLaunchComponent(packageInfo);
        if (launchComponent != null) {
            mIconLoader.picasso()
                    .load(Uri.fromParts(AppIconLoader.SCHEME, launchComponent.flattenToShortString(), null))
                    .placeholder(mDataProvider.getDefaultIconResource())
                    .into(holder.icon);
        }
    }

    @Override
    public void addAll(List<PackageInfo> objects) {
        super.addAll(objects);
        mDataProvider.setTotalCount(getItemCount());
    }
}
