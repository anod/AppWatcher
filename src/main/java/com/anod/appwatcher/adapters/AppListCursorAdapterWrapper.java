package com.anod.appwatcher.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;

import com.anod.appwatcher.App;
import com.anod.appwatcher.R;
import com.anod.appwatcher.content.AppListCursor;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.recyclerview.RecyclerViewCursorAdapter;
import com.anod.appwatcher.utils.AppIconLoader;
import com.anod.appwatcher.utils.InstalledAppsProvider;

/**
 * @author alex
 * @date 2015-06-20
 */
public class AppListCursorAdapterWrapper extends RecyclerViewCursorAdapter<AppViewHolder, AppListCursor> {
    private final AppViewHolderDataProvider mDataProvider;
    private final AppIconLoader mIconLoader;
    private final AppViewHolder.OnClickListener mListener;

    public AppListCursorAdapterWrapper(Context context, InstalledAppsProvider iap, AppViewHolder.OnClickListener listener) {
        super(context, R.layout.list_item_app);
        mDataProvider = new AppViewHolderDataProvider(context, iap);
        mIconLoader = App.provide(context).iconLoader();
        mListener = listener;
    }

    @Override
    protected AppViewHolder onCreateViewHolder(View itemView) {
        return new AppViewHolder(itemView, mDataProvider, mIconLoader, mListener);
    }

    @Override
    protected void onBindViewHolder(AppViewHolder holder, int position, AppListCursor cursor) {
        AppInfo app = cursor.getAppInfo();
        holder.bindView(cursor.getPosition(), app);
    }

    @Override
    public int getItemViewType(int position) {
        return 1;
    }

    public void swapData(AppListCursor data) {
        int totalCount = (data == null) ? 0 : data.getCount();
        mDataProvider.setTotalCount(totalCount);
        super.swapData(data);
    }

    public void setNewAppsCount(int newCount, int updatableCount) {
        mDataProvider.setNewAppsCount(newCount, updatableCount);
    }
}
