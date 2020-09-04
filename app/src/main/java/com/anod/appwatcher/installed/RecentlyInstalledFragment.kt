// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.installed

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import com.anod.appwatcher.AppComponent
import com.anod.appwatcher.AppWatcherApplication
import com.anod.appwatcher.watchlist.WatchListFragment
import info.anodsplace.framework.app.FragmentFactory

class RecentlyInstalledViewModel(application: android.app.Application) : AndroidViewModel(application) {
    private val appComponent: AppComponent
        get() = getApplication<AppWatcherApplication>().appComponent

}

class RecentlyInstalledFragment : WatchListFragment() {

    
    class Factory(
            private val filterId: Int,
            private val sortId: Int
    ) : FragmentFactory("recently-installed-$filterId-$sortId") {

        override fun create(): Fragment? = RecentlyInstalledFragment().also {
            it.arguments = Bundle().apply {
                putInt(ARG_FILTER, filterId)
                putInt(ARG_SORT, sortId)
            }
        }
    }
}