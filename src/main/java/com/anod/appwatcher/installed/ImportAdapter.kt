package com.anod.appwatcher.installed

import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.util.SimpleArrayMap
import android.view.LayoutInflater
import android.view.ViewGroup
import com.anod.appwatcher.R
import com.anod.appwatcher.watchlist.AppViewHolderBase

internal class ImportAdapter(
        context: Context,
        pm: PackageManager,
        private val mDataProvider: ImportDataProvider)
    : InstalledAppsAdapter(context, pm, mDataProvider, null) {

    private var packageIndex: SimpleArrayMap<String, Int> = SimpleArrayMap()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolderBase {
        val v = LayoutInflater.from(context).inflate(R.layout.list_item_import_app, parent, false)
        v.isClickable = true
        v.isFocusable = true

        return ImportAppViewHolder(v, mDataProvider, mIconLoader)
    }

    fun clearPackageIndex() {
        packageIndex = SimpleArrayMap<String, Int>()
    }

    fun storePackageIndex(packageName: String, idx: Int) {
        packageIndex.put(packageName, idx)
    }

    fun notifyPackageStatusChanged(packageName: String) {
        notifyItemChanged(packageIndex.get(packageName))
    }
}