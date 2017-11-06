package com.anod.appwatcher.ui

import android.accounts.Account
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.annotation.MenuRes
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AlertDialog
import android.support.v7.widget.SearchView
import android.text.TextUtils
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.anod.appwatcher.*
import com.anod.appwatcher.installed.ImportInstalledActivity
import com.anod.appwatcher.model.Filters
import com.anod.appwatcher.sync.ManualSyncService
import com.anod.appwatcher.sync.VersionsCheck
import com.anod.appwatcher.tags.TagsListActivity
import com.anod.appwatcher.utils.MenuItemAnimation
import com.anod.appwatcher.utils.UpgradeCheck
import info.anodsplace.android.log.AppLog
import java.util.*

/**
 * @author algavris
 * *
 * @date 18/03/2017.
 */

abstract class AppWatcherBaseActivity : DrawerActivity(), TextView.OnEditorActionListener, SearchView.OnQueryTextListener {

    private var syncFinishedReceiverRegistered: Boolean = false

    private lateinit var viewPager: ViewPager
    private val refreshMenuAnimation: MenuItemAnimation = MenuItemAnimation(this, R.anim.rotate)
    private var filterQuery = ""
    private var expandSearch = false

    val prefs: Preferences
        get() = App.provide(this).prefs

    interface EventListener {
        fun onSortChanged(sortIndex: Int)
        fun onQueryTextChanged(newQuery: String)
        fun onSyncStart()
        fun onSyncFinish()
    }

    private var searchMenuItem: MenuItem? = null
    private val eventListeners = ArrayList<AppWatcherBaseActivity.EventListener>(3)

    @get:LayoutRes protected abstract val contentLayout: Int
    @get:MenuRes protected abstract val menuResource: Int

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("tab_id", viewPager.currentItem)
        outState.putString("filter", filterQuery)
        super.onSaveInstanceState(outState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(contentLayout)
        setupDrawer()

        val filterId: Int
        if (savedInstanceState != null) {
            filterId = savedInstanceState.getInt("tab_id", Filters.TAB_ALL)
            filterQuery = savedInstanceState.getString("filter") ?: ""
            expandSearch = filterQuery.isNotEmpty()
            AppLog.d("Restore tab: " + filterId)
        } else {
            filterId = intentExtras.getInt("tab_id", Filters.TAB_ALL)
            expandSearch = intentExtras.getBoolean(EXTRA_EXPAND_SEARCH)
        }

        viewPager = findViewById<View>(R.id.viewpager) as ViewPager
        viewPager.adapter = createViewPagerAdapter()
        viewPager.currentItem = filterId

        val tabLayout = findViewById<View>(R.id.tabs) as TabLayout
        tabLayout.setupWithViewPager(viewPager)
    }

    protected abstract fun createViewPagerAdapter(): AppWatcherBaseActivity.Adapter

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu
        menuInflater.inflate(menuResource, menu)

        searchMenuItem = menu.findItem(R.id.menu_act_filter)
        searchMenuItem?.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(menuItem: MenuItem): Boolean {
                return true
            }

            override fun onMenuItemActionCollapse(menuItem: MenuItem): Boolean {
                notifyQueryChange("")
                return true
            }
        })

        val searchView = searchMenuItem?.actionView as SearchView
        searchView.setOnQueryTextListener(this)
        searchView.isSubmitButtonEnabled = true
        searchView.queryHint = getString(R.string.search)

        if (expandSearch) {
            searchMenuItem?.expandActionView()
            searchView.setQuery(filterQuery, false)
        }

        refreshMenuAnimation.menuItem = menu.findItem(R.id.menu_act_refresh)
        return super.onCreateOptionsMenu(menu)
    }

    /**
     * Receive notifications from VersionsCheck
     */
    private val syncFinishedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (VersionsCheck.SYNC_PROGRESS == action) {
                refreshMenuAnimation.start()
                notifySyncStart()
            } else if (VersionsCheck.SYNC_STOP == action) {
                val updatesCount = intent.getIntExtra(VersionsCheck.EXTRA_UPDATES_COUNT, 0)
                refreshMenuAnimation.stop()
                notifySyncStop()
                if (updatesCount == 0) {
                    Toast.makeText(this@AppWatcherBaseActivity, R.string.no_updates_found, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
        val filter = IntentFilter()
        filter.addAction(VersionsCheck.SYNC_PROGRESS)
        filter.addAction(VersionsCheck.SYNC_STOP)
        registerReceiver(syncFinishedReceiver, filter)
        syncFinishedReceiverRegistered = true
        super.onResume()

        notifySyncStop()
    }

    override fun onPause() {
        super.onPause()
        if (syncFinishedReceiverRegistered) {
            unregisterReceiver(syncFinishedReceiver)
            syncFinishedReceiverRegistered = false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_add -> {
                val addActivity = Intent(this, MarketSearchActivity::class.java)
                startActivity(addActivity)
                return true
            }
            R.id.menu_act_refresh -> {
                requestRefresh()
                return true
            }
            R.id.menu_settings -> {
                val settingsActivity = Intent(this, SettingsActivity::class.java)
                startActivity(settingsActivity)
                return true
            }
            R.id.menu_act_import -> {
                startActivity(Intent(this, ImportInstalledActivity::class.java))
                return true
            }
            R.id.menu_act_tags -> {
                startActivity(Intent(this, TagsListActivity::class.java))
                return true
            }
            R.id.menu_act_sort -> {
                showSortOptions()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showSortOptions() {
        val selected = prefs.sortIndex
        AlertDialog.Builder(this)
                .setSingleChoiceItems(R.array.sort_titles, selected) { dialog, index ->
                    prefs.sortIndex = index
                    notifySortChange(index)
                    dialog.dismiss()
                }
                .create()
                .show()
    }

    fun requestRefresh(): Boolean {
        AppLog.d("Refresh pressed")
        if (!isAuthenticated) {
            if (App.with(this).isNetworkAvailable) {
                this.showAccountsDialogWithCheck()
            } else {
                Toast.makeText(this, R.string.check_connection, Toast.LENGTH_SHORT).show()
            }
            return false
        }

        ManualSyncService.startActionSync(this)
        Toast.makeText(this, R.string.refresh_scheduled, Toast.LENGTH_SHORT).show()
        return false
    }

    override fun onEditorAction(textView: TextView, i: Int, keyEvent: KeyEvent): Boolean {
        return false
    }

    override fun onAccountSelected(account: Account) {
        super.onAccountSelected(account)
        if (UpgradeCheck(prefs).isNewVersion) {
            requestRefresh()
        }
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        filterQuery = ""
        if (TextUtils.isEmpty(query)) {
            searchMenuItem?.collapseActionView()
        } else {
            val searchIntent = Intent(this, MarketSearchActivity::class.java)
            searchIntent.putExtra(MarketSearchActivity.EXTRA_KEYWORD, query)
            searchIntent.putExtra(MarketSearchActivity.EXTRA_EXACT, true)
            startActivity(searchIntent)
            notifyQueryChange("")
            searchMenuItem?.collapseActionView()
        }
        return true
    }

    override fun onQueryTextChange(newText: String): Boolean {
        filterQuery = newText
        notifyQueryChange(newText)
        return true
    }

    private fun notifySortChange(sortIndex: Int) {
        for (idx in eventListeners.indices) {
            eventListeners[idx].onSortChanged(sortIndex)
        }
    }

    private fun notifyQueryChange(newTexr: String) {
        for (idx in eventListeners.indices) {
            eventListeners[idx].onQueryTextChanged(newTexr)
        }
    }

    fun addQueryChangeListener(listener: AppWatcherBaseActivity.EventListener): Int {
        eventListeners.add(listener)

        if (!TextUtils.isEmpty(filterQuery)) {
            notifyQueryChange(filterQuery)
        }

        return eventListeners.size - 1
    }

    fun removeQueryChangeListener(index: Int) {
        if (index < eventListeners.size) {
            eventListeners.removeAt(index)
        }
    }

    private fun notifySyncStart() {
        for (idx in eventListeners.indices) {
            eventListeners[idx].onSyncStart()
        }
    }

    private fun notifySyncStop() {
        for (idx in eventListeners.indices) {
            eventListeners[idx].onSyncFinish()
        }
    }

    class Adapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        private val fragments = mutableListOf<Fragment>()
        private val fragmentTitles = mutableListOf<String>()

        fun addFragment(fragment: Fragment, title: String) {
            fragments.add(fragment)
            fragmentTitles.add(title)
        }

        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        override fun getCount(): Int {
            return fragments.size
        }

        override fun getPageTitle(position: Int): CharSequence {
            return fragmentTitles[position]
        }
    }

    companion object {
        const val EXTRA_FROM_NOTIFICATION = "extra_noti"
        const val EXTRA_EXPAND_SEARCH = "expand_search"
    }
}