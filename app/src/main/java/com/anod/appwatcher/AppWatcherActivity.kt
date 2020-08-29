package com.anod.appwatcher

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import com.anod.appwatcher.model.Filters
import com.anod.appwatcher.sync.SyncScheduler
import com.anod.appwatcher.watchlist.WatchListActivity
import com.anod.appwatcher.watchlist.WatchListFragment
import info.anodsplace.framework.AppLog


class AppWatcherActivity : WatchListActivity(), TextView.OnEditorActionListener, SearchView.OnQueryTextListener {

    override val isHomeAsMenu: Boolean
        get() = true

    override val defaultFilterId: Int
        get() = prefs.defaultMainFilterId


    override val menuResource: Int
        get() = R.menu.watchlist

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp)

        if (prefs.useAutoSync) {
            SyncScheduler(this)
                    .schedule(prefs.isRequiresCharging, prefs.isWifiOnly, prefs.updatesFrequency.toLong(), false)
                    .observe(this, Observer { })
        }
    }

    override fun onResume() {
        super.onResume()

        AppLog.d("mark updates as viewed.")
        prefs.isLastUpdatesViewed = true
    }

    override fun createViewPagerAdapter(): Adapter {
        val adapter = Adapter(this)
        val title = resources.getStringArray(R.array.filter_titles)

        adapter.addFragment(WatchListFragment.Factory(
                Filters.TAB_ALL,
                prefs.sortIndex,
                null), title[Filters.TAB_ALL])
        adapter.addFragment(WatchListFragment.Factory(
                Filters.INSTALLED,
                prefs.sortIndex,
                null), title[Filters.INSTALLED])
        adapter.addFragment(WatchListFragment.Factory(
                Filters.UNINSTALLED,
                prefs.sortIndex,
                null), title[Filters.UNINSTALLED])
        adapter.addFragment(WatchListFragment.Factory(
                Filters.UPDATABLE,
                prefs.sortIndex,
                null), title[Filters.UPDATABLE])
        return adapter
    }
}
