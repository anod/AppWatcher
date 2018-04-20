package info.anodsplace.framework.content

import android.content.ComponentName
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import info.anodsplace.framework.AppLog
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*

/**
 * @author alex
 * *
 * @date 9/18/13
 */

typealias PackageWithCode = Pair<String, Int>

fun PackageManager.loadIcon(componentName: ComponentName, displayMetrics: DisplayMetrics): Bitmap? {
    var d: Drawable? = null
    try {
        d = this.getActivityIcon(componentName)
    } catch (ignored: PackageManager.NameNotFoundException) {
    }

    if (d == null) {
        try {
            d = this.getApplicationIcon(componentName.packageName)
        } catch (e1: PackageManager.NameNotFoundException) {
            AppLog.e(e1)
            return null
        }
    }

    if (d is BitmapDrawable) {
        // Ensure the bitmap has a density.
        val bitmapDrawable = d
        if (bitmapDrawable.bitmap.density == Bitmap.DENSITY_NONE) {
            bitmapDrawable.setTargetDensity(displayMetrics)
        }
        if (bitmapDrawable.bitmap.isRecycled) {
            AppLog.e("Bitmap is recycled for $componentName")
            return null
        }
        // copy to avoid recycling problems
        return bitmapDrawable.bitmap.copy(bitmapDrawable.bitmap.config, true)
    } else if (d != null) {
        val bitmap = Bitmap.createBitmap(d.intrinsicWidth, d.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        d.setBounds(0, 0, canvas.width, canvas.height)
        d.draw(canvas)
        return bitmap
    }
    return null
}

fun PackageManager.getAppTitle(packageName: String): String {
    val info = getPackageInfo(packageName, this) ?: return packageName
    return getAppTitle(info, this)
}

fun PackageManager.getAppUpdateTime(packageName: String): Long {
    val info = getPackageInfo(packageName, this) ?: return 0
    return info.lastUpdateTime
}

fun PackageManager.getInstalledPackagesCompat(): List<PackageWithCode> {
    val packs: List<PackageInfo>
    try {
        packs = this.getInstalledPackages(0)
    } catch (e: Exception) {
        AppLog.e(e)
        return getInstalledPackagesFallback()
    }

    val downloaded = ArrayList<PackageWithCode>(packs.size)
    for (i in packs.indices) {
        val packageInfo = packs[i]
        val applicationInfo = packageInfo.applicationInfo
        // Skips the system application (packages)
        if (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 1 && applicationInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP == 0) {
            continue
        }
        downloaded.add(Pair(packageInfo.packageName, packageInfo.versionCode))
    }
    return downloaded
}

private fun getInstalledPackagesFallback(): List<PackageWithCode> {
    val downloaded = ArrayList<PackageWithCode>()
    var bufferedReader: BufferedReader? = null
    try {
        val process = Runtime.getRuntime().exec("pm list packages")
        bufferedReader = BufferedReader(InputStreamReader(process.inputStream))
        var line = bufferedReader.readLine()
        while (line != null) {
            val packageName = line.substring(line.indexOf(':') + 1)
            line = bufferedReader.readLine()
            downloaded.add(Pair(packageName, 0))
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

fun PackageManager.getAppTitle(info: PackageInfo, pm: PackageManager): String {
    return info.applicationInfo.loadLabel(pm).toString()
}

fun PackageManager.getPackageInfo(packageName: String, pm: PackageManager): PackageInfo? {
    var pkgInfo: PackageInfo? = null
    try {
        pkgInfo = pm.getPackageInfo(packageName, 0)
    } catch (e: PackageManager.NameNotFoundException) {
        AppLog.e(e)
    }

    return pkgInfo
}

fun PackageManager.getLaunchComponent(info: PackageInfo, pm: PackageManager): ComponentName? {
    val launchIntent = pm.getLaunchIntentForPackage(info.packageName)
    return launchIntent?.component
}

