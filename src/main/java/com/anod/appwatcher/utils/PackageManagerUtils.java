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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.support.v4.util.SimpleArrayMap;
import android.util.DisplayMetrics;

import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.model.AppInfoMetadata;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import info.anodsplace.android.log.AppLog;


/**
 * @author alex
 * @date 9/18/13
 */
public class PackageManagerUtils {

    public static AppInfo packageToApp(String packageName, PackageManager pm) {
        PackageInfo packageInfo = getPackageInfo(packageName, pm);
        if (packageInfo == null) {
            return AppInfo.fromLocalPackage(null, packageName, "", null);
        }
        ComponentName launchComponent = getLaunchComponent(packageInfo, pm);
        String appTitle = getAppTitle(packageInfo, pm);
        return AppInfo.fromLocalPackage(packageInfo, packageName, appTitle, launchComponent);
    }

    static Bitmap loadIcon(ComponentName componentName, DisplayMetrics displayMetrics, PackageManager pm) {
        Drawable d = null;
        Bitmap icon;
        try {
            d = pm.getActivityIcon(componentName);
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        if (d == null) {
            try {
                d = pm.getApplicationIcon(componentName.getPackageName());
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

    public static String getAppTitle(String packageName, PackageManager pm) {
        PackageInfo info = getPackageInfo(packageName, pm);
        if (info == null) {
            return packageName;
        }
        return getAppTitle(info, pm);
    }

    public static long getAppUpdateTime(String packageName, PackageManager pm) {
        PackageInfo info = getPackageInfo(packageName, pm);
        if (info == null) {
            return 0;
        }
        return getPackageInfo(packageName, pm).lastUpdateTime;
    }

    public static List<String> getDownloadedApps(SimpleArrayMap<String, Integer> filter, PackageManager pm) {
        List<PackageInfo> packs;
        try {
            packs = pm.getInstalledPackages(0);
        } catch (Exception e) {
            AppLog.e(e);
            return getDownloadedPackagesFallback(filter);
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

    private static List<String> getDownloadedPackagesFallback(SimpleArrayMap<String, Integer> filter) {
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

    private static String getAppTitle(@NonNull PackageInfo info, PackageManager pm) {
        return info.applicationInfo.loadLabel(pm).toString();
    }

    private static PackageInfo getPackageInfo(String packageName, PackageManager pm) {
        PackageInfo pkgInfo = null;
        try {
            pkgInfo = pm.getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            AppLog.e(e);
        }

        return pkgInfo;
    }

    private static ComponentName getLaunchComponent(PackageInfo info, PackageManager pm) {
        Intent launchIntent = pm.getLaunchIntentForPackage(info.packageName);
        return launchIntent == null ? null : launchIntent.getComponent();
    }
}
