package com.anod.appwatcher.utils;

import android.content.pm.PackageInfo;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;

/**
 * @author algavris
 * @date 03/03/2017.
 */

public interface InstalledAppsProvider {

    @NonNull
    Info getInfo(String packageName);

    class Info {
        public final int versionCode;
        public final String versionName;

        public Info(int versionCode, String versionName) {
            this.versionCode = versionCode;
            this.versionName = versionName;
        }

        public boolean isUpdatable(int versionNumber) {
            return this.versionCode > 0 && this.versionCode != versionNumber;
        }

        public boolean isInstalled() {
            return this.versionCode > 0;
        }
    }

    class PackageManager implements InstalledAppsProvider {

        private final android.content.pm.PackageManager mPackageManager;

        public PackageManager(android.content.pm.PackageManager packageManager) {
            mPackageManager = packageManager;
        }

        @NonNull
        @Override
        public Info getInfo(String packageName) {
            PackageInfo pkgInfo = null;
            try {
                pkgInfo = mPackageManager.getPackageInfo(packageName, 0);
            } catch (android.content.pm.PackageManager.NameNotFoundException e) {
                // skip
            }

            if (pkgInfo != null) {
                return new InstalledAppsProvider.Info(pkgInfo.versionCode, pkgInfo.versionName);
            }
            return new InstalledAppsProvider.Info(0, "");
        }

    }

    class MemoryCache implements InstalledAppsProvider
    {
        private final ArrayMap<String, InstalledAppsProvider.Info> mCache = new ArrayMap<>();
        private final InstalledAppsProvider mInstalledAppsProvider;

        public MemoryCache(InstalledAppsProvider installedAppsProvider) {
            mInstalledAppsProvider = installedAppsProvider;
        }

        public @NonNull InstalledAppsProvider.Info getInfo(String packageName) {
            if (mCache.containsKey(packageName)) {
                return mCache.get(packageName);
            }

            Info info = mInstalledAppsProvider.getInfo(packageName);
            mCache.put(packageName, info);
            return info;
        }

        public void reset()
        {
            mCache.clear();
        }

    }

}
