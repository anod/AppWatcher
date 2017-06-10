package com.anod.appwatcher.installed

import android.content.Context
import android.database.Cursor
import android.support.v4.content.Loader
import com.anod.appwatcher.adapters.AppViewHolder
import com.anod.appwatcher.adapters.AppViewHolderDataProvider
import com.anod.appwatcher.fragments.AppWatcherListFragment
import com.anod.appwatcher.model.InstalledFilter
import com.anod.appwatcher.model.Tag
import com.anod.appwatcher.utils.InstalledAppsProvider
import info.anodsplace.android.widget.recyclerview.MergeRecyclerAdapter

/**
 * @author algavris
 * *
 * @date 01/04/2017.
 */

class InstalledSectionProvider : AppWatcherListFragment.DefaultSection() {

    override fun createLoader(context: Context, titleFilter: String, sortId: Int, filter: InstalledFilter?, tag: Tag?): Loader<Cursor> {
        return InstalledLoader(context, titleFilter, sortId, filter, tag, context.packageManager)
    }

    override fun fillAdapters(adapter: MergeRecyclerAdapter, context: Context, installedApps: InstalledAppsProvider, clickListener: AppViewHolder.OnClickListener) {
        super.fillAdapters(adapter, context, installedApps, clickListener)
        val dataProvider = AppViewHolderDataProvider(context, installedApps)
        adapter.addAdapter(ADAPTER_INSTALLED, InstalledAppsAdapter(context, context.packageManager, dataProvider, clickListener))
    }

    override fun loadFinished(adapter: MergeRecyclerAdapter, loader: Loader<Cursor>, data: Cursor) {
        super.loadFinished(adapter, loader, data)
        val downloadedAdapter = adapter.getAdapter(ADAPTER_INSTALLED) as InstalledAppsAdapter
        downloadedAdapter.clear()
        downloadedAdapter.addAll((loader as InstalledLoader).installedApps)
    }

    companion object {
        private val ADAPTER_INSTALLED = 1
    }
}
