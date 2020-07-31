package com.anod.appwatcher.watchlist

import androidx.lifecycle.ViewModelProvider
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

open class RecentSection : DefaultSection() {

    override fun attach(fragment: WatchListFragment, installedApps: InstalledApps, clickListener: AppViewHolder.OnClickListener) {
        val context = fragment.requireContext()
        val recentIndex = adapter.add(RecentlyInstalledAppsAdapter(AdapterViewType.recent, context, context.packageManager, clickListener))
        adapterIndexMap.put(AdapterViewType.recent, recentIndex)

        //
        super.attach(fragment, installedApps, clickListener)

        val viewModel = ViewModelProvider(fragment).get(InstalledWatchListViewModel::class.java)
        viewModel.hasSectionRecent = true
    }

    override fun viewModel(fragment: WatchListFragment): WatchListViewModel {
        return ViewModelProvider(fragment).get(InstalledWatchListViewModel::class.java)
    }

    override fun onModelLoaded(result: LoadResult) {
        super.onModelLoaded(result)
        val value = result as InstalledLoadResult
        val adapter = getInnerAdapter<RecentlyInstalledAppsAdapter>(AdapterViewType.recent)
        val watchingPackages = value.appsList.associate { Pair(it.app.packageName, it.app.rowId) }
        adapter.recentlyInstalled = value.recentlyInstalled.map {
            val rowId = watchingPackages[it] ?: -1
            Pair(it, rowId)
        }
    }
}

class OnDeviceSection : DefaultSection() {

    override fun attach(fragment: WatchListFragment, installedApps: InstalledApps, clickListener: AppViewHolder.OnClickListener) {
        super.attach(fragment, installedApps, clickListener)
        attach(fragment, installedApps, clickListener, this)
    }

    override fun viewModel(fragment: WatchListFragment): WatchListViewModel {
        return ViewModelProvider(fragment).get(InstalledWatchListViewModel::class.java)
    }

    override fun onModelLoaded(result: LoadResult) {
        super.onModelLoaded(result)
        val adapter = getInnerAdapter<InstalledAppsAdapter>(AdapterViewType.installed)
        onModelLoaded(result, adapter)
    }

    companion object {

        fun attach(fragment: WatchListFragment, installedApps: InstalledApps, clickListener: AppViewHolder.OnClickListener, section: DefaultSection) {
            val context = fragment.requireContext()
            val dataProvider = AppViewHolderResourceProvider(context, installedApps)
            val index = section.adapter.add(InstalledAppsAdapter(AdapterViewType.installed, context, context.packageManager, dataProvider, clickListener))

            section.adapterIndexMap.put(AdapterViewType.installed, index)
            val viewModel = ViewModelProvider(fragment).get(InstalledWatchListViewModel::class.java)
            viewModel.hasSectionOnDevice = true
        }

        fun onModelLoaded(result: LoadResult, adapter: InstalledAppsAdapter) {
            val value = result as InstalledLoadResult
            val watchingPackages = value.appsList.map { it.app.packageName }.associateWith { true }
            adapter.installedPackages = value.installedPackages.filter {
                !watchingPackages.contains(it.packageName)
            }
        }
    }
}

class RecentAndOnDeviceSection : RecentSection() {

    override fun attach(fragment: WatchListFragment, installedApps: InstalledApps, clickListener: AppViewHolder.OnClickListener) {
        super.attach(fragment, installedApps, clickListener)
        OnDeviceSection.attach(fragment, installedApps, clickListener, this)
    }

    override fun viewModel(fragment: WatchListFragment): WatchListViewModel {
        return ViewModelProvider(fragment).get(InstalledWatchListViewModel::class.java)
    }

    override fun onModelLoaded(result: LoadResult) {
        super.onModelLoaded(result)
        val adapter = getInnerAdapter<InstalledAppsAdapter>(AdapterViewType.installed)
        OnDeviceSection.onModelLoaded(result, adapter)
    }

}