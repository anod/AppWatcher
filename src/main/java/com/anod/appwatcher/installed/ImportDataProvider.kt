package com.anod.appwatcher.installed

import android.content.Context
import android.support.v4.util.SimpleArrayMap
import info.anodsplace.framework.content.InstalledApps
import com.anod.appwatcher.watchlist.AppViewHolderDataProvider

internal class ImportDataProvider(context: Context, installedApps: InstalledApps) : AppViewHolderDataProvider(context, installedApps) {

    private val selectedPackages = SimpleArrayMap<String, Boolean>()
    private var defaultSelected: Boolean = false
    private val processingPackages = SimpleArrayMap<String, Int>()
    var isImportStarted: Boolean = false

    fun selectAllPackages(select: Boolean) {
        selectedPackages.clear()
        defaultSelected = select
    }

    fun selectPackage(packageName: String, select: Boolean) {
        selectedPackages.put(packageName, select)
    }

    fun isPackageSelected(packageName: String): Boolean {
        if (selectedPackages.containsKey(packageName)) {
            return selectedPackages.get(packageName)
        }
        return defaultSelected
    }

    fun getPackageStatus(packageName: String): Int {
        if (processingPackages.containsKey(packageName)) {
            return processingPackages.get(packageName)
        }
        return STATUS_DEFAULT
    }

    fun setPackageStatus(packageName: String, status: Int) {
        processingPackages.put(packageName, status)
    }

    companion object {
        const val STATUS_DEFAULT = 0
        const val STATUS_IMPORTING = 1
        const val STATUS_DONE = 2
        const val STATUS_ERROR = 3
    }
}