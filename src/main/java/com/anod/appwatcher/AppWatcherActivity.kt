package com.anod.appwatcher

import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.v7.widget.SearchView
import android.widget.TextView

import com.anod.appwatcher.fragments.AppWatcherListFragment
import com.anod.appwatcher.installed.InstalledSectionProvider
import com.anod.appwatcher.model.Filters
import com.anod.appwatcher.sync.SyncScheduler
import com.anod.appwatcher.ui.AppWatcherBaseActivity

import info.anodsplace.android.log.AppLog

class AppWatcherActivity : AppWatcherBaseActivity(), TextView.OnEditorActionListener, SearchView.OnQueryTextListener {

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
        get() = R.menu.main

    override fun createViewPagerAdapter(): AppWatcherBaseActivity.Adapter {
        val adapter = AppWatcherBaseActivity.Adapter(supportFragmentManager)
        adapter.addFragment(AppWatcherListFragment.newInstance(
                Filters.TAB_ALL,
                prefs.sortIndex,
                AppWatcherListFragment.DefaultSection(),
                null), getString(R.string.tab_all))
        adapter.addFragment(AppWatcherListFragment.newInstance(
                Filters.TAB_INSTALLED,
                prefs.sortIndex,
                InstalledSectionProvider(), null), getString(R.string.tab_installed))
        adapter.addFragment(AppWatcherListFragment.newInstance(
                Filters.TAB_UNINSTALLED,
                prefs.sortIndex,
                AppWatcherListFragment.DefaultSection(), null), getString(R.string.tab_not_installed))
        return adapter
    }
}
