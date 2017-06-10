package com.anod.appwatcher.installed

import android.content.Context
import android.support.v4.util.SimpleArrayMap

import com.anod.appwatcher.adapters.AppViewHolderDataProvider
import com.anod.appwatcher.utils.InstalledAppsProvider

internal class ImportDataProvider(context: Context, installedAppsProvider: InstalledAppsProvider) : AppViewHolderDataProvider(context, installedAppsProvider) {

    private val mSelectedPackages = SimpleArrayMap<String, Boolean>()
    private var mDefaultSelected: Boolean = false
    private val mProcessingPackages = SimpleArrayMap<String, Int>()
    var isImportStarted: Boolean = false

    fun selectAllPackages(select: Boolean) {
        mSelectedPackages.clear()
        mDefaultSelected = select
    }

    fun selectPackage(packageName: String, select: Boolean) {
        mSelectedPackages.put(packageName, select)
    }

    fun isPackageSelected(packageName: String): Boolean {
        if (mSelectedPackages.containsKey(packageName)) {
            return mSelectedPackages.get(packageName)
        }
        return mDefaultSelected
    }

    fun getPackageStatus(packageName: String): Int {
        if (mProcessingPackages.containsKey(packageName)) {
            return mProcessingPackages.get(packageName)
        }
        return STATUS_DEFAULT
    }

    fun setPackageStatus(packageName: String, status: Int) {
        mProcessingPackages.put(packageName, status)
    }

    companion object {
        val STATUS_DEFAULT = 0
        val STATUS_IMPORTING = 1
        val STATUS_DONE = 2
        val STATUS_ERROR = 3
    }
}