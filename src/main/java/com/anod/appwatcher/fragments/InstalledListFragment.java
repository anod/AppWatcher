package com.anod.appwatcher.fragments;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.support.v4.util.ArrayMap;
import android.support.v4.util.SimpleArrayMap;
import android.text.TextUtils;
import android.view.View;

import com.anod.appwatcher.Preferences;
import com.anod.appwatcher.adapters.AppViewHolderDataProvider;
import com.anod.appwatcher.installed.InstalledAppsAdapter;
import com.anod.appwatcher.model.AppListContentProviderClient;
import com.anod.appwatcher.model.AppListCursorLoader;
import com.anod.appwatcher.utils.FilterCursorWrapper;
import com.anod.appwatcher.utils.PackageManagerUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author alex
 * @date 2015-08-31
 */
public class InstalledListFragment extends AppWatcherListFragment {
    private static final int ADAPTER_INSTALLED = 1;

    public static InstalledListFragment newInstance(int filterId, int sortId) {
        InstalledListFragment frag = new InstalledListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_FILTER, filterId);
        args.putInt(ARG_SORT, sortId);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSortId = getArguments().getInt(ARG_SORT);

        AppViewHolderDataProvider dataProvider = new AppViewHolderDataProvider(getActivity(), mPMUtils);
        mAdapter.addAdapter(ADAPTER_INSTALLED, new InstalledAppsAdapter(getActivity(), mPMUtils, dataProvider, this));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new InstalledLoader(getActivity(), mTitleFilter, mSortId, mInstalledFilter, mPMUtils);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        InstalledAppsAdapter downloadedAdapter = (InstalledAppsAdapter)mAdapter.getAdapter(ADAPTER_INSTALLED);
        downloadedAdapter.clear();
        downloadedAdapter.addAll(((InstalledLoader)loader).getInstalledApps());

        super.onLoadFinished(loader, data);
    }

    static class InstalledLoader extends AppListCursorLoader
    {
        private final PackageManagerUtils mPMUtils;
        private final SimpleArrayMap<String, String> mTitleCache = new SimpleArrayMap<>();
        private final SimpleArrayMap<String, Long> mUpdateTimeCache = new SimpleArrayMap<>();
        private final int mSortId;

        List<String> getInstalledApps() {
            return mInstalledApps;
        }

        private List<String> mInstalledApps = new ArrayList<>();

        InstalledLoader(Context context, String titleFilter, int sortId, FilterCursorWrapper.CursorFilter cursorFilter, PackageManagerUtils pmUtils) {
            super(context, titleFilter, sortId, cursorFilter);
            mPMUtils = pmUtils;
            mSortId = sortId;
        }

        @Override
        public Cursor loadInBackground() {
            Cursor cursor = super.loadInBackground();

            AppListContentProviderClient cr = new AppListContentProviderClient(getContext());
            Map<String, Integer> watchingPackages = cr.queryPackagesMap(false);
            cr.release();

            List<String> list = mPMUtils.getDownloadedApps(watchingPackages);

            if (mSortId == Preferences.SORT_NAME_DESC) {
                Collections.sort(list, new AppTitleComparator(-1, this));
            } else if (mSortId == Preferences.SORT_DATE_ASC) {
                Collections.sort(list, new AppUpdateTimeComparator(1, this));
            } else if (mSortId == Preferences.SORT_DATE_DESC) {
                Collections.sort(list, new AppUpdateTimeComparator(-1, this));
            } else{
                Collections.sort(list, new AppTitleComparator(1, this));
            }

            if (!TextUtils.isEmpty(mTitleFilter)) {
                List<String> filtered = new ArrayList<>(list.size());
                String lcFilter = mTitleFilter.toLowerCase();
                for(String packageName : list) {
                    String title = getPackageTitle(packageName);
                    if (title.toLowerCase().contains(lcFilter)) {
                        filtered.add(packageName);
                    }
                }
                mInstalledApps = filtered;
            } else {
                mInstalledApps = list;
            }
            mTitleCache.clear();
            return cursor;
        }

        private String getPackageTitle(String packageName)
        {
            if (mTitleCache.containsKey(packageName)) {
                return mTitleCache.get(packageName);
            } else {
                String title = mPMUtils.getAppTitle(packageName);
                mTitleCache.put(packageName, title);
                return title;
            }
        }

        private long getPackageUpdateTime(String packageName)
        {
            if (mUpdateTimeCache.containsKey(packageName)) {
                return mUpdateTimeCache.get(packageName);
            } else {
                long updateTime = mPMUtils.getAppUpdateTime(packageName);
                mUpdateTimeCache.put(packageName, updateTime);
                return updateTime;
            }
        }

        private final static class AppTitleComparator implements Comparator<String> {
            private final int mOrder;
            private final InstalledLoader mLoader;

            private AppTitleComparator(int order, InstalledLoader loader) {
                mLoader = loader;
                mOrder = order;
            }

            @Override
            public int compare(String lPackageName, String rPackageName) {
                return mOrder * (mLoader.getPackageTitle(lPackageName).compareTo(mLoader.getPackageTitle(rPackageName)));
            }
        }

        private final static class AppUpdateTimeComparator implements Comparator<String> {
            private final int mOrder;
            private final InstalledLoader mLoader;

            private AppUpdateTimeComparator(int order, InstalledLoader loader) {
                mLoader = loader;
                mOrder = order;
            }

            @Override
            public int compare(String lPackageName, String rPackageName) {
                if (mLoader.getPackageUpdateTime(lPackageName) > mLoader.getPackageUpdateTime(rPackageName)) {
                    return mOrder;
                } else {
                    return mOrder * -1;
                }
            }
        }
    }

}