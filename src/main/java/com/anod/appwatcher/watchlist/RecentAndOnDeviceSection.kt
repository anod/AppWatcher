package com.anod.appwatcher.watchlist

import androidx.lifecycle.ViewModelProviders
import com.anod.appwatcher.installed.InstalledAppsAdapter
import com.anod.appwatcher.installed.InstalledLoadResult
import com.anod.appwatcher.installed.InstalledWatchListViewModel
import com.anod.appwatcher.installed.RecentlyInstalledAppsAdapter
import info.anodsplace.framework.content.InstalledApps

/**
 * @author Alex Gavrishev
 * @date 03/12/2017
 */

/**
 * @author Alex Gavrishev
 * *
 * @date 01/04/2017.
 */

open class RecentSection : WatchListFragment.DefaultSection() {

    override fun attach(fragment: WatchListFragment, installedApps: InstalledApps, clickListener: AppViewHolder.OnClickListener) {
        val context = fragment.context!!
        val recentIndex = adapter.add(RecentlyInstalledAppsAdapter(context, context.packageManager, clickListener))
        adapterIndexMap.put(ADAPTER_RECENT, recentIndex)

        //
        super.attach(fragment, installedApps, clickListener)

        val viewModel = ViewModelProviders.of(fragment).get(InstalledWatchListViewModel::class.java)
        viewModel.hasSectionRecent = true
    }

    override fun viewModel(fragment: WatchListFragment): WatchListViewModel {
        return ViewModelProviders.of(fragment).get(InstalledWatchListViewModel::class.java)
    }

    override fun onModelLoaded(result: LoadResult) {
        super.onModelLoaded(result)
        val value = result as InstalledLoadResult
        val adapter = getInnerAdapter<RecentlyInstalledAppsAdapter>(ADAPTER_RECENT)
        adapter.recentlyInstalled = value.recentlyInstalled
    }

    companion object {
        const val ADAPTER_RECENT = 2
    }

}

class OnDeviceSection : WatchListFragment.DefaultSection() {

    override fun attach(fragment: WatchListFragment, installedApps: InstalledApps, clickListener: AppViewHolder.OnClickListener) {
        super.attach(fragment, installedApps, clickListener)
        OnDeviceSection.attach(fragment, installedApps, clickListener, this)
    }

    override fun viewModel(fragment: WatchListFragment): WatchListViewModel {
        return ViewModelProviders.of(fragment).get(InstalledWatchListViewModel::class.java)
    }

    override fun onModelLoaded(result: LoadResult) {
        super.onModelLoaded(result)
        val value = result as InstalledLoadResult
        val adapter = getInnerAdapter<InstalledAppsAdapter>(ADAPTER_INSTALLED)
        adapter.installedPackages = value.installedPackages
    }

    companion object {
        private const val ADAPTER_INSTALLED = 1

        fun attach(fragment: WatchListFragment, installedApps: InstalledApps, clickListener: AppViewHolder.OnClickListener, section: WatchListFragment.DefaultSection) {
            val context = fragment.context!!
            val dataProvider = AppViewHolderResourceProvider(context, installedApps)
            val index = section.adapter.add(InstalledAppsAdapter(context, context.packageManager, dataProvider, clickListener))

            section.adapterIndexMap.put(ADAPTER_INSTALLED, index)
            val viewModel = ViewModelProviders.of(fragment).get(InstalledWatchListViewModel::class.java)
            viewModel.hasSectionOnDevice = true
        }
    }
}

class RecentAndOnDeviceSection : RecentSection() {

    override fun attach(fragment: WatchListFragment, installedApps: InstalledApps, clickListener: AppViewHolder.OnClickListener) {
        super.attach(fragment, installedApps, clickListener)
        OnDeviceSection.attach(fragment, installedApps, clickListener, this)
    }

    override fun viewModel(fragment: WatchListFragment): WatchListViewModel {
        return ViewModelProviders.of(fragment).get(InstalledWatchListViewModel::class.java)
    }
}