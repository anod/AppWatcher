package com.anod.appwatcher.fragments;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.View;

import com.anod.appwatcher.adapters.AppViewHolderDataProvider;
import com.anod.appwatcher.adapters.InstalledAppsAdapter;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.model.AppListContentProviderClient;
import com.anod.appwatcher.model.AppListCursorLoader;
import com.anod.appwatcher.utils.FilterCursorWrapper;
import com.anod.appwatcher.utils.PackageManagerUtils;

import java.util.ArrayList;
import java.util.List;
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
        AppViewHolderDataProvider dataProvider = new AppViewHolderDataProvider(getActivity(), mPMUtils);
        mAdapter.addAdapter(ADAPTER_INSTALLED, new InstalledAppsAdapter(getActivity(), mPMUtils, dataProvider, this));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new InstalledLoader(getActivity(), mTitleFilter, mInstalledFilter, mPMUtils);
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        super.onLoadFinished(loader, data);

        InstalledAppsAdapter downloadedAdapter = (InstalledAppsAdapter)mAdapter.getAdapter(ADAPTER_INSTALLED);
        downloadedAdapter.clear();
        downloadedAdapter.addAll(((InstalledLoader)loader).getInstalledApps());
    }

    static class InstalledLoader extends AppListCursorLoader
    {
        private final PackageManagerUtils mPMUtils;

        public List<PackageInfo> getInstalledApps() {
            return mInstalledApps;
        }

        private List<PackageInfo> mInstalledApps = new ArrayList<>();

        public InstalledLoader(Context context, String titleFilter, FilterCursorWrapper.CursorFilter cursorFilter, PackageManagerUtils pmUtils) {
            super(context, titleFilter, cursorFilter);
            mPMUtils = pmUtils;
        }

        @Override
        public Cursor loadInBackground() {
            Cursor cursor = super.loadInBackground();

            AppListContentProviderClient cr = new AppListContentProviderClient(getContext());
            Map<String, Integer> watchingPackages = cr.queryPackagesMap();
            cr.release();

            mInstalledApps = mPMUtils.getDownloadedApps(watchingPackages);
            return cursor;
        }
    }
}