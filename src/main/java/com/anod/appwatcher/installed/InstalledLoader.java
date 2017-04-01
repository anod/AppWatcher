package com.anod.appwatcher.installed;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.support.v4.util.SimpleArrayMap;
import android.text.TextUtils;

import com.anod.appwatcher.Preferences;
import com.anod.appwatcher.content.DbContentProviderClient;
import com.anod.appwatcher.model.AppListCursorLoader;
import com.anod.appwatcher.model.Tag;
import com.anod.appwatcher.utils.FilterCursorWrapper;
import com.anod.appwatcher.utils.PackageManagerUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class InstalledLoader extends AppListCursorLoader {
        private final PackageManager mPackageManager;
        private final SimpleArrayMap<String, String> mTitleCache = new SimpleArrayMap<>();
        private final SimpleArrayMap<String, Long> mUpdateTimeCache = new SimpleArrayMap<>();
        private final int mSortId;

        public List<String> getInstalledApps() {
            return mInstalledApps;
        }

        private List<String> mInstalledApps = new ArrayList<>();

        public InstalledLoader(Context context, String titleFilter, int sortId, FilterCursorWrapper.CursorFilter cursorFilter, @Nullable Tag tag, PackageManager pm) {
            super(context, titleFilter, sortId, cursorFilter, tag);
            mPackageManager = pm;
            mSortId = sortId;
        }

        @Override
        public Cursor loadInBackground() {
            Cursor cursor = super.loadInBackground();

            DbContentProviderClient cr = new DbContentProviderClient(getContext());
            SimpleArrayMap<String, Integer> watchingPackages = cr.queryPackagesMap(false);
            cr.close();

            List<String> list = PackageManagerUtils.getDownloadedApps(watchingPackages, mPackageManager);

            if (mSortId == Preferences.SORT_NAME_DESC) {
                Collections.sort(list, new AppTitleComparator(-1, this));
            } else if (mSortId == Preferences.SORT_DATE_ASC) {
                Collections.sort(list, new AppUpdateTimeComparator(1, this));
            } else if (mSortId == Preferences.SORT_DATE_DESC) {
                Collections.sort(list, new AppUpdateTimeComparator(-1, this));
            } else {
                Collections.sort(list, new AppTitleComparator(1, this));
            }

            if (!TextUtils.isEmpty(mTitleFilter)) {
                List<String> filtered = new ArrayList<>(list.size());
                String lcFilter = mTitleFilter.toLowerCase();
                for (String packageName : list) {
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

        private String getPackageTitle(String packageName) {
            if (mTitleCache.containsKey(packageName)) {
                return mTitleCache.get(packageName);
            } else {
                String title = PackageManagerUtils.getAppTitle(packageName, mPackageManager);
                mTitleCache.put(packageName, title);
                return title;
            }
        }

        private long getPackageUpdateTime(String packageName) {
            if (mUpdateTimeCache.containsKey(packageName)) {
                return mUpdateTimeCache.get(packageName);
            } else {
                long updateTime = PackageManagerUtils.getAppUpdateTime(packageName, mPackageManager);
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
