package com.anod.appwatcher.installed;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.View;

import com.anod.appwatcher.adapters.AppViewHolder;
import com.anod.appwatcher.adapters.AppViewHolderDataProvider;
import com.anod.appwatcher.fragments.AppWatcherListFragment;
import com.anod.appwatcher.model.InstalledFilter;
import com.anod.appwatcher.model.Tag;
import com.anod.appwatcher.utils.InstalledAppsProvider;

import info.anodsplace.android.widget.recyclerview.MergeRecyclerAdapter;

/**
 * @author algavris
 * @date 01/04/2017.
 */

public class InstalledSectionProvider extends AppWatcherListFragment.DefaultSection {
    private static final int ADAPTER_INSTALLED = 1;

    @Override
    public Loader<Cursor> createLoader(Context context, String titleFilter, int sortId, InstalledFilter filter, Tag tag) {
        return new InstalledLoader(context, titleFilter, sortId, filter, tag, context.getPackageManager());
    }

    @Override
    public void fillAdapters(MergeRecyclerAdapter adapter, Context context, InstalledAppsProvider installedApps, AppViewHolder.OnClickListener clickListener) {
        super.fillAdapters(adapter, context, installedApps, clickListener);
        AppViewHolderDataProvider dataProvider = new AppViewHolderDataProvider(context, installedApps);
        adapter.addAdapter(ADAPTER_INSTALLED, new InstalledAppsAdapter(context, context.getPackageManager(), dataProvider, clickListener));
    }

    @Override
    public void loadFinished(MergeRecyclerAdapter adapter, Loader<Cursor> loader, Cursor data) {
        super.loadFinished(adapter, loader, data);
        InstalledAppsAdapter downloadedAdapter = (InstalledAppsAdapter) adapter.getAdapter(ADAPTER_INSTALLED);
        downloadedAdapter.clear();
        downloadedAdapter.addAll(((InstalledLoader) loader).getInstalledApps());
    }
}
