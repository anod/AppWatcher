package com.anod.appwatcher.utils

import android.content.ComponentName
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.support.v4.util.SimpleArrayMap
import android.util.DisplayMetrics
import com.anod.appwatcher.model.AppInfo
import info.anodsplace.android.log.AppLog
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*


/**
 * @author alex
 * *
 * @date 9/18/13
 */
object PackageManagerUtils {

    fun packageToApp(rowId: Int, packageName: String, pm: PackageManager): AppInfo {
        val packageInfo = getPackageInfo(packageName, pm) ?: return AppInfo.fromLocalPackage(rowId,null, packageName, "", null)
        val launchComponent = getLaunchComponent(packageInfo, pm)
        val appTitle = getAppTitle(packageInfo, pm)
        return AppInfo.fromLocalPackage(rowId, packageInfo, packageName, appTitle, launchComponent)
    }

    internal fun loadIcon(componentName: ComponentName, displayMetrics: DisplayMetrics, pm: PackageManager): Bitmap? {
        var d: Drawable? = null
        val icon: Bitmap
        try {
            d = pm.getActivityIcon(componentName)
        } catch (ignored: PackageManager.NameNotFoundException) {
        }

        if (d == null) {
            try {
                d = pm.getApplicationIcon(componentName.packageName)
            } catch (e1: PackageManager.NameNotFoundException) {
                AppLog.e(e1)
                return null
            }

        }

        if (d is BitmapDrawable) {
            // Ensure the bitmap has a density.
            val bitmapDrawable = d
            icon = bitmapDrawable.bitmap
            if (icon.density == Bitmap.DENSITY_NONE) {
                bitmapDrawable.setTargetDensity(displayMetrics)
            }
            return icon
        }
        return null
    }

    fun getAppTitle(packageName: String, pm: PackageManager): String {
        val info = getPackageInfo(packageName, pm) ?: return packageName
        return getAppTitle(info, pm)
    }

    fun getAppUpdateTime(packageName: String, pm: PackageManager): Long {
        val info = getPackageInfo(packageName, pm) ?: return 0
        return info.lastUpdateTime
    }

    fun getInstalledPackages(pm: PackageManager): List<String> {
        val packs: List<PackageInfo>
        try {
            packs = pm.getInstalledPackages(0)
        } catch (e: Exception) {
            AppLog.e(e)
            return getInstalledPackagesFallback()
        }

        val downloaded = ArrayList<String>(packs.size)
        for (i in packs.indices) {
            val packageInfo = packs[i]
            val applicationInfo = packageInfo.applicationInfo
            // Skips the system application (packages)
            if (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 1 && applicationInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP == 0) {
                continue
            }
            downloaded.add(packageInfo.packageName)
        }
        return downloaded
    }

    private fun getInstalledPackagesFallback(): List<String> {
        val downloaded = ArrayList<String>()
        var bufferedReader: BufferedReader? = null
        try {
            val process = Runtime.getRuntime().exec("pm list packages")
            bufferedReader = BufferedReader(InputStreamReader(process.inputStream))
            var line = bufferedReader.readLine()
            while (line != null) {
                val packageName = line.substring(line.indexOf(':') + 1)
                line = bufferedReader.readLine()
                downloaded.add(packageName)
            }
            process.waitFor()
        } catch (e: Exception) {
            AppLog.e(e)
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close()
                } catch (e: IOException) {
                    AppLog.e(e)
                }

            }
        }
        return downloaded
    }

    private fun getAppTitle(info: PackageInfo, pm: PackageManager): String {
        return info.applicationInfo.loadLabel(pm).toString()
    }

    private fun getPackageInfo(packageName: String, pm: PackageManager): PackageInfo? {
        var pkgInfo: PackageInfo? = null
        try {
            pkgInfo = pm.getPackageInfo(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            AppLog.e(e)
        }

        return pkgInfo
    }

    private fun getLaunchComponent(info: PackageInfo, pm: PackageManager): ComponentName? {
        val launchIntent = pm.getLaunchIntentForPackage(info.packageName)
        return launchIntent?.component
    }
}
