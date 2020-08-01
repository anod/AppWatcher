// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.watchlist

import android.content.Context
import android.util.SparseIntArray
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.anod.appwatcher.utils.SingleLiveEvent
import info.anodsplace.framework.content.InstalledApps
import info.anodsplace.framework.widget.recyclerview.MergeRecyclerAdapter

interface Section {
    val emptyAdapter: EmptyAdapter
    val adapter: MergeRecyclerAdapter
    var adapterIndexMap: SparseIntArray
    fun viewModel(fragment: WatchListFragment): WatchListViewModel
    fun attach(fragment: WatchListFragment, installedApps: InstalledApps, clickListener: AppViewHolder.OnClickListener)
    fun onModelLoaded(result: LoadResult)
    fun addEmptySection(context: Context, action: SingleLiveEvent<WishListAction>, configure: (emptyView: View, action: SingleLiveEvent<WishListAction>) -> Unit)

    val isEmpty: Boolean
}

// Must have empty constructor
open class DefaultSection : Section {

    override var adapterIndexMap = SparseIntArray()
    override val emptyAdapter: EmptyAdapter
        get() = getInnerAdapter(AdapterViewType.empty)
    override val adapter: MergeRecyclerAdapter by lazy { MergeRecyclerAdapter() }

    override fun attach(fragment: WatchListFragment, installedApps: InstalledApps, clickListener: AppViewHolder.OnClickListener) {
        val context = fragment.requireContext()
        val index = adapter.add(AppInfoAdapter(AdapterViewType.apps, context, installedApps, clickListener))
        adapterIndexMap.put(AdapterViewType.apps, index)
    }

    override fun addEmptySection(context: Context, action: SingleLiveEvent<WishListAction>, configure: (emptyView: View, action: SingleLiveEvent<WishListAction>) -> Unit) {
        val index = adapter.add(EmptyAdapter(AdapterViewType.empty, action, configure, context))
        adapterIndexMap.put(AdapterViewType.empty, index)
    }

    override fun viewModel(fragment: WatchListFragment): WatchListViewModel {
        return ViewModelProvider(fragment).get(WatchListViewModel::class.java)
    }

    override fun onModelLoaded(result: LoadResult) {
        getInnerAdapter<AppInfoAdapter>(AdapterViewType.apps).updateList(result.appsList)
    }

    fun <T : RecyclerView.Adapter<*>> getInnerAdapter(id: Int): T {
        val index = adapterIndexMap.get(id)
        return adapter[index] as T
    }

    override val isEmpty: Boolean
        get() = getInnerAdapter<AppInfoAdapter>(AdapterViewType.apps).itemCount == 0

}