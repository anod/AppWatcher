package com.anod.appwatcher.installed

import android.content.Context
import android.database.Cursor
import android.support.v4.content.Loader
import android.support.v7.widget.RecyclerView
import com.anod.appwatcher.watchlist.AppViewHolder
import com.anod.appwatcher.watchlist.AppViewHolderDataProvider
import com.anod.appwatcher.watchlist.WatchListFragment
import com.anod.appwatcher.model.AppListFilter
import com.anod.appwatcher.model.Tag
import info.anodsplace.framework.widget.recyclerview.MergeRecyclerAdapter
import info.anodsplace.framework.content.InstalledApps

/**
 * @author algavris
 * *
 * @date 01/04/2017.
 */
class InstalledSection : WatchListFragment.DefaultSection() {

    override fun createLoader(context: Context, titleFilter: String, sortId: Int, filter: AppListFilter?, tag: Tag?): Loader<Cursor> {
        return InstalledLoader(context, titleFilter, sortId, filter, tag, context.packageManager)
    }

    override fun initAdapter(context: Context, installedApps: InstalledApps, clickListener: AppViewHolder.OnClickListener) {
        val recentIndex = adapter.add(RecentlyInstalledAppsAdapter(context, context.packageManager, clickListener) as RecyclerView.Adapter<RecyclerView.ViewHolder>)
        adapterIndexMap.put(ADAPTER_RECENT, recentIndex)
        super.initAdapter(context, installedApps, clickListener)
        val dataProvider = AppViewHolderDataProvider(context, installedApps)
        val index = adapter.add(InstalledAppsAdapter(context, context.packageManager, dataProvider, clickListener) as RecyclerView.Adapter<RecyclerView.ViewHolder>)
        adapterIndexMap.put(ADAPTER_INSTALLED, index)
    }

    override fun loadFinished(loader: Loader<Cursor>, data: Cursor) {
        super.loadFinished(loader, data)
        val installedLoader = (loader as InstalledLoader)
        val downloadedAdapter = getInnerAdapter<InstalledAppsAdapter>(ADAPTER_INSTALLED)
        downloadedAdapter.clear()
        downloadedAdapter.addAll(installedLoader.installedApps)

        val recentAdapter = getInnerAdapter<RecentlyInstalledAppsAdapter>(ADAPTER_RECENT)
        recentAdapter.recentlyInstalled = installedLoader.recentlyInstalled
    }

    companion object {
        private const val ADAPTER_INSTALLED = 1
        private const val ADAPTER_RECENT = 2
    }
}
