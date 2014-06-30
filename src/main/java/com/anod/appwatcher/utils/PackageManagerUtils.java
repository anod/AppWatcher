package com.anod.appwatcher.utils;

import android.content.Intent;
import android.content.pm.PackageManager;

import java.util.HashMap;

/**
 * @author alex
 * @date 9/18/13
 */
public class PackageManagerUtils {
	private PackageManager mPackageManager;
	private HashMap<String,Boolean> mInstalledCache;


	public PackageManagerUtils(PackageManager pm) {
		mPackageManager = pm;
		mInstalledCache = new HashMap<String,Boolean>();
	}
	/**
	 *
	 * @param packageName
	 * @return
	 */
	public boolean isAppInstalled(String packageName) {
		if (mInstalledCache.containsKey(packageName)) {
			return mInstalledCache.get(packageName);
		}

		Intent appIntent = mPackageManager.getLaunchIntentForPackage(packageName);
		boolean isInstalled = (appIntent != null);
		mInstalledCache.put(packageName, isInstalled);
		return isInstalled;
	}
}
