package com.anod.appwatcher

import android.os.Bundle
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.SearchView
import com.anod.appwatcher.model.Filters
import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.sync.SyncScheduler
import com.anod.appwatcher.watchlist.*
import info.anodsplace.framework.AppLog


class AppWatcherActivity : WatchListActivity(), TextView.OnEditorActionListener, SearchView.OnQueryTextListener {

    override val isHomeAsMenu: Boolean
        get() = true

    override val defaultFilterId: Int
        get() = prefs.defaultMainFilterId

    override val layoutResource: Int
        @LayoutRes get() = R.layout.activity_main

    override val menuResource: Int
        get() = R.menu.watchlist

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp)

        if (prefs.useAutoSync) {
            SyncScheduler(this).schedule(prefs.isRequiresCharging, prefs.isWifiOnly, prefs.updatesFrequency.toLong())
        }
    }

    override fun onResume() {
        super.onResume()

        AppLog.d("mark updates as viewed.")
        prefs.isLastUpdatesViewed = true
    }

    override fun createViewPagerAdapter(): Adapter {
        val adapter = Adapter(supportFragmentManager)
        val title = resources.getStringArray(R.array.filter_titles)

        adapter.addFragment(WatchListFragment.newInstance(
                Filters.TAB_ALL,
                prefs.sortIndex,
                sectionForAll(prefs),
                null), title[Filters.TAB_ALL])
        adapter.addFragment(WatchListFragment.newInstance(
                Filters.INSTALLED,
                prefs.sortIndex,
                WatchListFragment.DefaultSection(), null), title[Filters.INSTALLED])
        adapter.addFragment(WatchListFragment.newInstance(
                Filters.UNINSTALLED,
                prefs.sortIndex,
                WatchListFragment.DefaultSection(), null), title[Filters.UNINSTALLED])
        adapter.addFragment(WatchListFragment.newInstance(
                Filters.UPDATABLE,
                prefs.sortIndex,
                WatchListFragment.DefaultSection(), null), title[Filters.UPDATABLE])
        return adapter
    }

    private fun sectionForAll(prefs: Preferences): WatchListFragment.Section {
        if (prefs.showRecent && prefs.showOnDevice) {
            return RecentAndOnDeviceSection()
        }
        if (prefs.showRecent) {
            return RecentSection()
        }
        if (prefs.showOnDevice) {
            return OnDeviceSection()
        }
        return WatchListFragment.DefaultSection()
    }
}
