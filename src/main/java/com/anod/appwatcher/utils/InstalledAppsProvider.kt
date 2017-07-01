package com.anod.appwatcher.utils

import android.content.pm.PackageInfo
import android.support.v4.util.ArrayMap

/**
 * @author algavris
 * *
 * @date 03/03/2017.
 */

interface InstalledAppsProvider {

    fun getInfo(packageName: String): Info

    class Info(val versionCode: Int, val versionName: String) {

        fun isUpdatable(versionNumber: Int): Boolean {
            return this.versionCode > 0 && this.versionCode != versionNumber
        }

        val isInstalled: Boolean
            get() = this.versionCode > 0
    }

    class PackageManager(private val mPackageManager: android.content.pm.PackageManager) : InstalledAppsProvider {

        override fun getInfo(packageName: String): Info {
            var pkgInfo: PackageInfo? = null
            try {
                pkgInfo = mPackageManager.getPackageInfo(packageName, 0)
            } catch (e: android.content.pm.PackageManager.NameNotFoundException) {
                // skip
            }

            if (pkgInfo != null) {
                val versionName = pkgInfo.versionName ?: ""
                return InstalledAppsProvider.Info(pkgInfo.versionCode, versionName)
            }
            return InstalledAppsProvider.Info(0, "")
        }

    }

    class MemoryCache(private val mInstalledAppsProvider: InstalledAppsProvider) : InstalledAppsProvider {
        private val mCache = ArrayMap<String, InstalledAppsProvider.Info>()

        override fun getInfo(packageName: String): InstalledAppsProvider.Info {
            if (mCache.containsKey(packageName)) {
                return mCache[packageName]!!
            }

            val info = mInstalledAppsProvider.getInfo(packageName)
            mCache.put(packageName, info)
            return info
        }

        fun reset() {
            mCache.clear()
        }

    }

}
