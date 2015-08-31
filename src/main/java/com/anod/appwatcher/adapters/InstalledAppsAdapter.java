package com.anod.appwatcher.adapters;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.anod.appwatcher.R;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.model.AppInfoMetadata;
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

    public InstalledAppsAdapter(Context context, PackageManagerUtils pmutils, AppViewHolder.OnClickListener listener) {
        super(new ArrayList<PackageInfo>());
        mContext = context;
        mListener = listener;
        mDataProvider = new AppViewHolderDataProvider(context, pmutils);

        mPMUtils = pmutils;
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
        PackageInfo localInfo = getItem(position);

        AppInfo app = new AppInfo(0,
                localInfo.packageName,
                localInfo.packageName,
                localInfo.versionCode,
                localInfo.versionName,
                mPMUtils.getAppTitle(localInfo),
                null,
                mPMUtils.getAppIcon(localInfo),
                AppInfoMetadata.STATUS_NORMAL,
                null,
                null,
                null,
                0,
                null
        );

        /**
         *
         * int rowId, String appId, String pname, int versionNumber, String versionName,
         String title, String creator, Bitmap icon, int status, String uploadDate, String priceText, String priceCur, Integer priceMicros, String detailsUrl) {

         */

        holder.bindView(position, app);
    }

    @Override
    public void addAll(List<PackageInfo> objects) {
        super.addAll(objects);

        mDataProvider.setTotalCount(getItemCount());
    }
}
