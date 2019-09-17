package com.anod.appwatcher.installed

import android.content.Context
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.collection.SimpleArrayMap
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.observe
import com.anod.appwatcher.R
import com.anod.appwatcher.watchlist.AppViewHolderBase

internal class ImportAdapter(
        context: Context,
        pm: PackageManager,
        private val dataProvider: ImportResourceProvider,
        lifecycleOwner: LifecycleOwner)
    : InstalledAppsAdapter(context, pm, dataProvider, null) {

    private var packageIndex: SimpleArrayMap<String, Int> = SimpleArrayMap()

    init {
        dataProvider.packageStatus.observe(lifecycleOwner) {
            notifyItemChanged(packageIndex.get(it.first) ?: 0)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolderBase {
        val v = LayoutInflater.from(context).inflate(R.layout.list_item_import_app, parent, false)
        v.isClickable = true
        v.isFocusable = true

        return ImportAppViewHolder(v, dataProvider, iconLoader)
    }

    fun clearPackageIndex() {
        packageIndex = SimpleArrayMap()
    }

    fun storePackageIndex(packageName: String, idx: Int) {
        packageIndex.put(packageName, idx)
    }
}