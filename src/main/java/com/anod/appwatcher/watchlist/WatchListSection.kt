package com.anod.appwatcher.watchlist

import android.content.Context
import android.database.Cursor
import android.support.v4.content.Loader
import com.anod.appwatcher.installed.InstalledAppsAdapter
import com.anod.appwatcher.installed.InstalledLoader
import com.anod.appwatcher.installed.RecentlyInstalledAppsAdapter
import com.anod.appwatcher.model.AppListFilter
import com.anod.appwatcher.model.Tag
import info.anodsplace.android.widget.recyclerview.MergeRecyclerAdapter
import info.anodsplace.appwatcher.framework.InstalledApps

/**
 * @author algavris
 * @date 03/12/2017
 */

/**
 * @author algavris
 * *
 * @date 01/04/2017.
 */
class WatchListSection : WatchListFragment.DefaultSection() {

    override fun createLoader(context: Context, titleFilter: String, sortId: Int, filter: AppListFilter?, tag: Tag?): Loader<Cursor> {
        return InstalledLoader(context, titleFilter, sortId, filter, tag, context.packageManager)
    }

    override fun fillAdapters(adapter: MergeRecyclerAdapter, context: Context, installedApps: InstalledApps, clickListener: AppViewHolder.OnClickListener) {
        val recentIndex = adapter.addAdapter(RecentlyInstalledAppsAdapter(context, context.packageManager, clickListener))
        adapterIndexMap.put(ADAPTER_RECENT, recentIndex)
        super.fillAdapters(adapter, context, installedApps, clickListener)
        val dataProvider = AppViewHolderDataProvider(context, installedApps)
        val index = adapter.addAdapter(InstalledAppsAdapter(context, context.packageManager, dataProvider, clickListener))
        adapterIndexMap.put(ADAPTER_INSTALLED, index)
    }

    override fun loadFinished(adapter: MergeRecyclerAdapter, loader: Loader<Cursor>, data: Cursor) {
        super.loadFinished(adapter, loader, data)
        val installedLoader = (loader as InstalledLoader)
        val downloadedAdapter = getAdapter<InstalledAppsAdapter>(ADAPTER_INSTALLED, adapter)
        downloadedAdapter.clear()
        downloadedAdapter.addAll(installedLoader.installedApps)

        val recentAdapter = getAdapter<RecentlyInstalledAppsAdapter>(ADAPTER_RECENT, adapter)
        recentAdapter.recentlyInstalled = installedLoader.recentlyInstalled
    }

    companion object {
        private const val ADAPTER_INSTALLED = 1
        private const val ADAPTER_RECENT = 2
    }
}