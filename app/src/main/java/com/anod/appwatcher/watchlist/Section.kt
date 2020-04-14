// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.watchlist

import android.util.SparseIntArray
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import info.anodsplace.framework.content.InstalledApps
import info.anodsplace.framework.widget.recyclerview.MergeRecyclerAdapter


interface Section {
    val adapter: MergeRecyclerAdapter
    var adapterIndexMap: SparseIntArray
    fun viewModel(fragment: WatchListFragment): WatchListViewModel
    fun attach(fragment: WatchListFragment, installedApps: InstalledApps, clickListener: AppViewHolder.OnClickListener)
    fun onModelLoaded(result: LoadResult)
    val isEmpty: Boolean
}

// Must have empty constructor
open class DefaultSection : Section {

    override var adapterIndexMap = SparseIntArray()
    override val adapter: MergeRecyclerAdapter by lazy { MergeRecyclerAdapter() }

    override fun attach(fragment: WatchListFragment, installedApps: InstalledApps, clickListener: AppViewHolder.OnClickListener) {
        val context = fragment.requireContext()
        val index = adapter.add(AppInfoAdapter(context, installedApps, clickListener))
        adapterIndexMap.put(ADAPTER_WATCHLIST, index)
    }

    override fun viewModel(fragment: WatchListFragment): WatchListViewModel {
        return ViewModelProvider(fragment).get(WatchListViewModel::class.java)
    }

    override fun onModelLoaded(result: LoadResult) {
        getInnerAdapter<AppInfoAdapter>(ADAPTER_WATCHLIST).updateList(result.appsList)
    }

    fun <T : RecyclerView.Adapter<*>> getInnerAdapter(id: Int): T {
        val index = adapterIndexMap.get(id)
        return adapter[index] as T
    }

    override val isEmpty: Boolean
        get() = getInnerAdapter<AppInfoAdapter>(ADAPTER_WATCHLIST).itemCount == 0

    companion object {
        const val ADAPTER_WATCHLIST = 0
    }
}