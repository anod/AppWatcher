package com.anod.appwatcher.watchlist

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import com.anod.appwatcher.installed.InstalledWatchListViewModel
import com.anod.appwatcher.installed.RecentlyInstalledAppsAdapter
import info.anodsplace.framework.content.InstalledApps

/**
 * @author algavris
 * @date 03/12/2017
 */

/**
 * @author algavris
 * *
 * @date 01/04/2017.
 */

open class RecentSection : WatchListFragment.DefaultSection() {

    override fun attach(fragment: WatchListFragment, installedApps: InstalledApps, clickListener: AppViewHolder.OnClickListener, onLoadFinished: () -> Unit) {
        val context = fragment.context!!
        val recentIndex = adapter.add(RecentlyInstalledAppsAdapter(context, context.packageManager, clickListener))
        adapterIndexMap.put(ADAPTER_RECENT, recentIndex)

        //
        super.attach(fragment, installedApps, clickListener, onLoadFinished)

        ViewModelProviders.of(fragment).get(InstalledWatchListViewModel::class.java).recentlyInstalled.observe(fragment, Observer {
            value ->
            val adapter = getInnerAdapter<RecentlyInstalledAppsAdapter>(ADAPTER_RECENT)
            adapter.recentlyInstalled = value ?: emptyList()
        })
    }

    override fun viewModel(fragment: WatchListFragment): WatchListViewModel {
        return ViewModelProviders.of(fragment).get(InstalledWatchListViewModel::class.java)
    }

    companion object {
        const val ADAPTER_RECENT = 2
    }

}

class OnDeviceSection : WatchListFragment.DefaultSection() {

    override fun attach(fragment: WatchListFragment, installedApps: InstalledApps, clickListener: AppViewHolder.OnClickListener, onLoadFinished: () -> Unit) {
        super.attach(fragment, installedApps, clickListener, onLoadFinished)
//        val dataProvider = AppViewHolderResourceProvider(context, installedApps)
//        val index = adapter.add(InstalledAppsAdapter(context, context.packageManager, dataProvider, clickListener) as RecyclerView.Adapter<RecyclerView.ViewHolder>)
//        adapterIndexMap.put(ADAPTER_INSTALLED, index)
    }

    companion object {
        const val ADAPTER_INSTALLED = 1
    }
}

class RecentAndOnDeviceSection : RecentSection() {

    override fun attach(fragment: WatchListFragment, installedApps: InstalledApps, clickListener: AppViewHolder.OnClickListener, onLoadFinished: () -> Unit) {
        super.attach(fragment, installedApps, clickListener, onLoadFinished)
//        val dataProvider = AppViewHolderResourceProvider(context, installedApps)
//        val index = adapter.add(InstalledAppsAdapter(context, context.packageManager, dataProvider, clickListener) as RecyclerView.Adapter<RecyclerView.ViewHolder>)
//        adapterIndexMap.put(ADAPTER_INSTALLED, index)
    }

}