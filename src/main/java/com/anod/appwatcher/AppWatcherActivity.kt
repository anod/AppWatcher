package com.anod.appwatcher

import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.v7.widget.SearchView
import android.widget.TextView

import com.anod.appwatcher.watchlist.WatchListFragment
import com.anod.appwatcher.model.Filters
import com.anod.appwatcher.sync.SyncScheduler
import com.anod.appwatcher.watchlist.WatchListActivity
import com.anod.appwatcher.watchlist.WatchListSection

import info.anodsplace.android.log.AppLog

class AppWatcherActivity : WatchListActivity(), TextView.OnEditorActionListener, SearchView.OnQueryTextListener {

    override val isHomeAsMenu: Boolean
        get() = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp)
        App.provide(this).fireBase

        if (prefs.useAutoSync) {
            SyncScheduler.schedule(this, prefs.isRequiresCharging, prefs.isWifiOnly, prefs.updatesFrequency)
        }
    }

    override fun onResume() {
        super.onResume()

        AppLog.d("mark updates as viewed.")
        prefs.isLastUpdatesViewed = true
    }

    override val contentLayout: Int
        @LayoutRes get() = R.layout.activity_main

    override val menuResource: Int
        get() = R.menu.watchlist

    override fun createViewPagerAdapter(): WatchListActivity.Adapter {
        val adapter = WatchListActivity.Adapter(supportFragmentManager)
        adapter.addFragment(WatchListFragment.newInstance(
                Filters.TAB_ALL,
                prefs.sortIndex,
                WatchListSection(),
                null), getString(R.string.tab_all))
        adapter.addFragment(WatchListFragment.newInstance(
                Filters.TAB_INSTALLED,
                prefs.sortIndex,
                WatchListFragment.DefaultSection(), null), getString(R.string.tab_installed))
        adapter.addFragment(WatchListFragment.newInstance(
                Filters.TAB_UNINSTALLED,
                prefs.sortIndex,
                WatchListFragment.DefaultSection(), null), getString(R.string.tab_not_installed))
        adapter.addFragment(WatchListFragment.newInstance(
                Filters.TAB_UPDATABLE,
                prefs.sortIndex,
                WatchListFragment.DefaultSection(), null), getString(R.string.tab_updatable))
        return adapter
    }
}
