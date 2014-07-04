package com.anod.appwatcher.utils;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.util.HashMap;

/**
 * @author alex
 * @date 9/18/13
 */
public class PackageManagerUtils {
	private PackageManager mPackageManager;
	private HashMap<String,InstalledInfo> mInstalledVersionsCache;

    public static class InstalledInfo {
        public int versionCode = 0;
        public String versionName = null;
    }

	public PackageManagerUtils(PackageManager pm) {
		mPackageManager = pm;
        mInstalledVersionsCache = new HashMap<String,InstalledInfo>();
	}
	/**
	 *
	 * @param packageName
	 * @return
	 */
	public InstalledInfo getInstalledInfo(String packageName) {
		if (mInstalledVersionsCache.containsKey(packageName)) {
			return mInstalledVersionsCache.get(packageName);
		}

        PackageInfo pkgInfo = null;
        try {
            pkgInfo  = mPackageManager.getPackageInfo(packageName, 0);
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
