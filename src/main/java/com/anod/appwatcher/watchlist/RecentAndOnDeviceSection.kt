package com.anod.appwatcher.watchlist

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import com.anod.appwatcher.installed.InstalledAppsAdapter
import com.anod.appwatcher.installed.InstalledWatchListViewModel
import com.anod.appwatcher.installed.RecentlyInstalledAppsAdapter
import com.anod.appwatcher.watchlist.OnDeviceSection.Companion.ADAPTER_INSTALLED
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

        val viewModel = ViewModelProviders.of(fragment).get(InstalledWatchListViewModel::class.java)
        viewModel.hasSectionRecent = true
        viewModel.recentlyInstalled.observe(fragment, Observer {
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

        OnDeviceSection.attach(fragment, installedApps, clickListener, this)
    }

    override fun viewModel(fragment: WatchListFragment): WatchListViewModel {
        return ViewModelProviders.of(fragment).get(InstalledWatchListViewModel::class.java)
    }

    companion object {
        const val ADAPTER_INSTALLED = 1

        fun attach(fragment: WatchListFragment, installedApps: InstalledApps, clickListener: AppViewHolder.OnClickListener, section: WatchListFragment.DefaultSection) {
            val context = fragment.context!!
            val dataProvider = AppViewHolderResourceProvider(context, installedApps)
            val index = section.adapter.add(InstalledAppsAdapter(context, context.packageManager, dataProvider, clickListener))

            section.adapterIndexMap.put(ADAPTER_INSTALLED, index)
            val viewModel = ViewModelProviders.of(fragment).get(InstalledWatchListViewModel::class.java)
            viewModel.hasSectionOnDevice = true
            viewModel.installedPackages.observe(fragment, Observer {
                value ->
                val adapter = section.getInnerAdapter<InstalledAppsAdapter>(ADAPTER_INSTALLED)
                adapter.installedPackages = value ?: emptyList()
            })
        }
    }
}

class RecentAndOnDeviceSection : RecentSection() {

    override fun attach(fragment: WatchListFragment, installedApps: InstalledApps, clickListener: AppViewHolder.OnClickListener, onLoadFinished: () -> Unit) {
        super.attach(fragment, installedApps, clickListener, onLoadFinished)
        OnDeviceSection.attach(fragment, installedApps, clickListener, this)
    }

    override fun viewModel(fragment: WatchListFragment): WatchListViewModel {
        return ViewModelProviders.of(fragment).get(InstalledWatchListViewModel::class.java)
    }
}