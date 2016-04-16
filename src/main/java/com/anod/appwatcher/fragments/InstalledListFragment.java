package com.anod.appwatcher.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.View;

import com.anod.appwatcher.adapters.InstalledAppsAdapter;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.model.AppListContentProviderClient;

import java.util.Map;

/**
 * @author alex
 * @date 2015-08-31
 */
public class InstalledListFragment extends AppWatcherListFragment {
    private static final int ADAPTER_INSTALLED = 1;

    public static InstalledListFragment newInstance(int filterId) {
        InstalledListFragment frag = new InstalledListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_FILTER, filterId);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAdapter.addAdapter(ADAPTER_INSTALLED, new InstalledAppsAdapter(getActivity(), mPMUtils, this));
    }


    protected void refreshInstalledList() {
        // TODO: Background??
        AppListContentProviderClient cr = new AppListContentProviderClient(getActivity());
        Map<String, Integer> watchingPackages = cr.queryPackagesMap();
        cr.release();

        InstalledAppsAdapter downloadedAdapter = (InstalledAppsAdapter)mAdapter.getAdapter(ADAPTER_INSTALLED);
        downloadedAdapter.clear();
        downloadedAdapter.addAll(mPMUtils.getDownloadedApps(watchingPackages));

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        refreshInstalledList();
        super.onLoadFinished(loader, data);
    }
}