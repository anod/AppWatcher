package com.anod.appwatcher

import android.os.Bundle
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.anod.appwatcher.installed.InstalledFragment
import com.anod.appwatcher.model.Filters
import com.anod.appwatcher.sync.SyncScheduler
import com.anod.appwatcher.watchlist.WatchListActivity
import com.anod.appwatcher.watchlist.WatchListFragment
import info.anodsplace.applog.AppLog
import kotlinx.coroutines.flow.collect


class AppWatcherActivity : WatchListActivity(), TextView.OnEditorActionListener {

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
            lifecycleScope.launchWhenCreated {
                SyncScheduler(applicationContext)
                    .schedule(prefs.isRequiresCharging, prefs.isWifiOnly, prefs.updatesFrequency.toLong(), false)
                    .collect { }
            }
        }

        if (intentExtras.containsKey("open_recently_installed")) {
            intent!!.extras!!.remove("open_recently_installed")
            startActivity(InstalledFragment.intent(
                    false,
                    this,
                    themeRes,
                    themeColors
            ))
        }
    }

    override fun onResume() {
        super.onResume()

        AppLog.d("mark updates as viewed.")
        prefs.isLastUpdatesViewed = true
    }

    override fun createViewPagerAdapter(): Adapter {
        val factories = listOf(
                WatchListFragment.Factory(Filters.TAB_ALL, prefs.sortIndex, null),
                WatchListFragment.Factory(Filters.INSTALLED, prefs.sortIndex, null),
                WatchListFragment.Factory(Filters.UNINSTALLED, prefs.sortIndex, null),
                WatchListFragment.Factory(Filters.UPDATABLE, prefs.sortIndex, null)
        )
        return Adapter(factories, this)
    }
}
