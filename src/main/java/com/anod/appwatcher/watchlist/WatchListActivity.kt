package com.anod.appwatcher.watchlist

import android.accounts.Account
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.annotation.MenuRes
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
import com.anod.appwatcher.model.Filters
import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.sync.ManualSyncService
import com.anod.appwatcher.sync.UpdateCheck
import com.anod.appwatcher.search.SearchActivity
import com.anod.appwatcher.upgrade.SetupInterfaceUpgrade
import com.anod.appwatcher.upgrade.UpgradeRefresh
import com.anod.appwatcher.utils.Theme
import com.anod.appwatcher.upgrade.UpgradeCheck
import info.anodsplace.framework.AppLog
import java.util.*

/**
 * @author algavris
 * *
 * @date 18/03/2017.
 */

abstract class WatchListActivity : DrawerActivity(), TextView.OnEditorActionListener, SearchView.OnQueryTextListener {

    override val themeRes: Int
        get() = Theme(this).theme

    private var syncFinishedReceiverRegistered: Boolean = false

    private lateinit var viewPager: ViewPager

    val prefs: Preferences
        get() = App.provide(this).prefs

    open val defaultFilterId = Filters.TAB_ALL

    interface EventListener {
        fun onSortChanged(sortIndex: Int)
        fun onQueryTextChanged(newQuery: String)
        fun onSyncStart()
        fun onSyncFinish()
    }

    private val actionMenu by lazy { WatchListMenu(this, this) }

    private val eventListeners = ArrayList<EventListener>(3)

    @get:LayoutRes protected abstract val contentLayout: Int
    @get:MenuRes protected abstract val menuResource: Int

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("tab_id", viewPager.currentItem)
        outState.putString("filter", actionMenu.searchQuery)
        super.onSaveInstanceState(outState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(contentLayout)
        setupDrawer()

        val filterId: Int
        if (savedInstanceState != null) {
            filterId = savedInstanceState.getInt("tab_id", defaultFilterId)
            actionMenu.searchQuery = savedInstanceState.getString("filter") ?: ""
            AppLog.d("Restore tab: " + filterId)
        } else {
            val fromNotification = intentExtras.getBoolean(EXTRA_FROM_NOTIFICATION, false)
            val expandSearch = intentExtras.getBoolean(EXTRA_EXPAND_SEARCH)
            filterId = if (fromNotification || expandSearch) defaultFilterId else intentExtras.getInt("tab_id", defaultFilterId)
            actionMenu.expandSearch = expandSearch
        }

        viewPager = findViewById<View>(R.id.viewpager) as ViewPager
        viewPager.adapter = createViewPagerAdapter()
        viewPager.offscreenPageLimit = 0

        viewPager.currentItem = filterId
        actionMenu.filterId = filterId
        updateSubtitle(filterId)
        viewPager.addOnPageChangeListener(object: ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                onFilterSelected(position)
            }
        })
    }

    protected abstract fun createViewPagerAdapter(): Adapter

    fun applyFilter(filterId: Int) {
        viewPager.currentItem = filterId
    }

    open fun onFilterSelected(filterId: Int) {
        actionMenu.filterId = filterId
        updateSubtitle(filterId)
    }

    private fun updateSubtitle(filterId: Int) {
        if (filterId == 0) {
            supportActionBar?.subtitle = ""
        } else {
            supportActionBar?.subtitle = viewPager.adapter?.getPageTitle(filterId) ?: ""
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu
        menuInflater.inflate(menuResource, menu)

        actionMenu.init(menu)

        return super.onCreateOptionsMenu(menu)
    }

    /**
     * Receive notifications from UpdateCheck
     */
    private val syncFinishedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (UpdateCheck.syncProgress == action) {
                actionMenu.startRefresh()
                notifySyncStart()
            } else if (UpdateCheck.syncStop == action) {
                val updatesCount = intent.getIntExtra(UpdateCheck.extrasUpdatesCount, 0)
                actionMenu.stopRefresh()
                notifySyncStop()
                if (updatesCount == 0) {
                    Toast.makeText(this@WatchListActivity, R.string.no_updates_found, Toast.LENGTH_SHORT).show()
                }
                refreshLastUpdateTime()
            }
        }
    }

    override fun onResume() {
        val filter = IntentFilter()
        filter.addAction(UpdateCheck.syncProgress)
        filter.addAction(UpdateCheck.syncStop)
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
        if (actionMenu.onItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    fun showSortOptions() {
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
            if (App.provide(this).networkConnection.isNetworkAvailable) {
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

    override fun onAccountSelected(account: Account) {
        super.onAccountSelected(account)

        val upgrade = UpgradeCheck(prefs).result;
        if (!upgrade.isNewVersion) {
            return
        }

        SetupInterfaceUpgrade(prefs, this).onUpgrade(upgrade)
        UpgradeRefresh(prefs, this).onUpgrade(upgrade)

    }

    override fun onEditorAction(textView: TextView, i: Int, keyEvent: KeyEvent): Boolean {
        return false
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        val searchIntent = Intent(this, MarketSearchActivity::class.java)
        searchIntent.putExtra(SearchActivity.EXTRA_KEYWORD, query)
        searchIntent.putExtra(SearchActivity.EXTRA_EXACT, true)
        startActivity(searchIntent)
        return true
    }

    override fun onQueryTextChange(newText: String): Boolean {

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

    fun addQueryChangeListener(listener: EventListener): Int {
        eventListeners.add(listener)

        if (!TextUtils.isEmpty(actionMenu.searchQuery)) {
            notifyQueryChange(actionMenu.searchQuery)
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