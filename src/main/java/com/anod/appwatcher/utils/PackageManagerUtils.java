package com.anod.appwatcher.utils;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.util.ArrayMap;
import android.util.DisplayMetrics;

import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.model.AppInfoMetadata;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import info.anodsplace.android.log.AppLog;


/**
 * @author alex
 * @date 9/18/13
 */
public class PackageManagerUtils {
    private PackageManager mPackageManager;
    private ArrayMap<String, InstalledInfo> mInstalledVersionsCache;

    public AppInfo packageToApp(String packageName) {
        PackageInfo packageInfo = getPackageInfo(packageName);
        ComponentName launchComponent = this.getLaunchComponent(packageInfo);
        String iconUrl = null;
        if (launchComponent != null) {
            iconUrl = Uri.fromParts(AppIconLoader.SCHEME, launchComponent.flattenToShortString(), null).toString();
        }

        return new AppInfo(-1,
                packageInfo.packageName,
                packageInfo.packageName,
                packageInfo.versionCode,
                packageInfo.versionName,
                this.getAppTitle(packageInfo),
                null,
                iconUrl,
                AppInfoMetadata.STATUS_NORMAL,
                null,
                null,
                null,
                0,
                "details?doc=" + packageInfo.packageName,
                0
        );
    }

    public PackageInfo getPackageInfo(String packageName) {
        PackageInfo pkgInfo = null;
        try {
            pkgInfo = mPackageManager.getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            AppLog.e(e);
        }
        return pkgInfo;
    }

    public Bitmap loadIcon(ComponentName componentName, DisplayMetrics displayMetrics) {
        Drawable d = null;
        Bitmap icon;
        try {
            d = mPackageManager.getActivityIcon(componentName);
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        if (d == null) {
            try {
                d = mPackageManager.getApplicationIcon(componentName.getPackageName());
            } catch (PackageManager.NameNotFoundException e1) {
                AppLog.e(e1);
                return null;
            }
        }

        if (d instanceof BitmapDrawable) {
            // Ensure the bitmap has a density.
            BitmapDrawable bitmapDrawable = (BitmapDrawable) d;
            icon = bitmapDrawable.getBitmap();
            if (icon.getDensity() == Bitmap.DENSITY_NONE) {
                bitmapDrawable.setTargetDensity(displayMetrics);
            }
            return icon;
        }
        return null;
    }


    public static class InstalledInfo {
        public int versionCode = 0;
        public String versionName = null;
    }

    public PackageManagerUtils(PackageManager pm) {
        mPackageManager = pm;
        mInstalledVersionsCache = new ArrayMap<>();
    }

    public String getAppTitle(String packageName) {
        return getAppTitle(getPackageInfo(packageName));
    }

    public String getAppTitle(PackageInfo info) {
        return info.applicationInfo.loadLabel(mPackageManager).toString();
    }

    public ComponentName getLaunchComponent(PackageInfo info) {
        Intent launchIntent = mPackageManager.getLaunchIntentForPackage(info.packageName);
        return launchIntent == null ? null : launchIntent.getComponent();
    }

    public List<String> getDownloadedApps(Map<String, Integer> filter) {
        List<PackageInfo> packs;
        try {
            packs = mPackageManager.getInstalledPackages(0);
        } catch (Exception e) {
            AppLog.e(e);
            return this.getDownloadedPackagesFallback(filter);
        }
        List<String> downloaded = new ArrayList<>(packs.size());
        for (int i = 0; i < packs.size(); i++) {
            PackageInfo packageInfo = packs.get(i);
            ApplicationInfo applicationInfo = packageInfo.applicationInfo;
            // Skips the system application (packages)
            if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1 && (applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0) {
                continue;
            }
            if (filter != null && filter.containsKey(packageInfo.packageName)) {
                continue;
            }
            downloaded.add(packageInfo.packageName);
        }
        return downloaded;
    }

    private List<String> getDownloadedPackagesFallback(Map<String, Integer> filter) {
        List<String> downloaded = new ArrayList<>();
        BufferedReader bufferedReader = null;
        try {
            Process process = Runtime.getRuntime().exec("pm list packages");
            bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                final String packageName = line.substring(line.indexOf(':') + 1);
                if (filter != null && filter.containsKey(packageName)) {
                    continue;
                }
                downloaded.add(packageName);
            }
            process.waitFor();
        } catch (Exception e) {
            AppLog.e(e);
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    AppLog.e(e);
                }
            }
        }
        return downloaded;
    }

    public InstalledInfo getInstalledInfo(String packageName) {
        if (mInstalledVersionsCache.containsKey(packageName)) {
            return mInstalledVersionsCache.get(packageName);
        }

        PackageInfo pkgInfo = null;
        try {
            pkgInfo = mPackageManager.getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            // skip
        }

        InstalledInfo info = new InstalledInfo();
        if (pkgInfo != null) {
            info.versionCode = pkgInfo.versionCode;
            info.versionName = pkgInfo.versionName;
        }

        mInstalledVersionsCache.put(packageName, info);
        return info;
    }

    public String getAppVersionName(String packageName) {
        InstalledInfo info = getInstalledInfo(packageName);
        return info.versionName;
    }

    public int getAppVersionCode(String packageName) {
        InstalledInfo info = getInstalledInfo(packageName);
        return info.versionCode;
    }

    public boolean isAppInstalled(String packageName) {
        InstalledInfo info = getInstalledInfo(packageName);
        return info.versionCode > 0;
    }
}
