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
import android.support.v4.view.MenuItemCompat
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
import com.anod.appwatcher.sync.SyncAdapter
import com.anod.appwatcher.tags.TagsListActivity
import com.anod.appwatcher.utils.MenuItemAnimation
import com.anod.appwatcher.utils.UpgradeCheck
import com.google.android.gms.gcm.GcmTaskService
import info.anodsplace.android.log.AppLog
import java.util.*

/**
 * @author algavris
 * *
 * @date 18/03/2017.
 */

abstract class AppWatcherBaseActivity : DrawerActivity(), TextView.OnEditorActionListener, SearchView.OnQueryTextListener {

    private var mSyncFinishedReceiverRegistered: Boolean = false

    private lateinit var mViewPager: ViewPager
    private val mRefreshAnim: MenuItemAnimation = MenuItemAnimation(this, R.anim.rotate)
    private var mFilterQuery = ""

    val prefs: Preferences
        get() = App.provide(this).prefs

    interface EventListener {
        fun onSortChanged(sortIndex: Int)
        fun onQueryTextChanged(newQuery: String)
        fun onSyncStart()
        fun onSyncFinish()
    }

    private var mSearchMenuItem: MenuItem? = null
    private val mEventListener = ArrayList<AppWatcherBaseActivity.EventListener>(3)

    @get:LayoutRes protected abstract val contentLayout: Int
    @get:MenuRes protected abstract val menuResource: Int

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("tab_id", mViewPager.currentItem)
        outState.putString("filter", mFilterQuery)
        super.onSaveInstanceState(outState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(contentLayout)
        setupDrawer()

        var filterId = Filters.TAB_ALL
        if (savedInstanceState != null) {
            filterId = savedInstanceState.getInt("tab_id", Filters.TAB_ALL)
            mFilterQuery = savedInstanceState.getString("filter")
            AppLog.d("Restore tab: " + filterId)
        }

        mViewPager = findViewById<View>(R.id.viewpager) as ViewPager
        mViewPager.adapter = createViewPagerAdapter()
        mViewPager.currentItem = filterId

        val tabLayout = findViewById<View>(R.id.tabs) as TabLayout
        tabLayout.setupWithViewPager(mViewPager)
    }

    protected abstract fun createViewPagerAdapter(): AppWatcherBaseActivity.Adapter

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu
        menuInflater.inflate(menuResource, menu)

        mSearchMenuItem = menu.findItem(R.id.menu_act_filter)
        MenuItemCompat.setOnActionExpandListener(mSearchMenuItem, object : MenuItemCompat.OnActionExpandListener {
            override fun onMenuItemActionExpand(menuItem: MenuItem): Boolean {
                return true
            }

            override fun onMenuItemActionCollapse(menuItem: MenuItem): Boolean {
                notifyQueryChange("")
                return true
            }
        })

        val searchView = MenuItemCompat.getActionView(mSearchMenuItem) as SearchView
        searchView.setOnQueryTextListener(this)
        searchView.isSubmitButtonEnabled = true
        searchView.queryHint = getString(R.string.search)

        val filterQuery = mFilterQuery
        if (!TextUtils.isEmpty(filterQuery)) {
            MenuItemCompat.expandActionView(mSearchMenuItem)
            searchView.setQuery(filterQuery, false)
        }

        mRefreshAnim.menuItem = menu.findItem(R.id.menu_act_refresh)
        return super.onCreateOptionsMenu(menu)
    }

    /**
     * Receive notifications from SyncAdapter
     */
    private val mSyncFinishedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (GcmTaskService.SERVICE_ACTION_EXECUTE_TASK == action || SyncAdapter.SYNC_PROGRESS == action) {
                mRefreshAnim.start()
                notifySyncStart()
            } else if (SyncAdapter.SYNC_STOP == action) {
                val updatesCount = intent.getIntExtra(SyncAdapter.EXTRA_UPDATES_COUNT, 0)
                mRefreshAnim.stop()
                notifySyncStop()
                if (updatesCount == 0) {
                    Toast.makeText(this@AppWatcherBaseActivity, R.string.no_updates_found, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
        val filter = IntentFilter()
        filter.addAction(SyncAdapter.SYNC_PROGRESS)
        filter.addAction(SyncAdapter.SYNC_STOP)
        filter.addAction(GcmTaskService.SERVICE_ACTION_EXECUTE_TASK)
        registerReceiver(mSyncFinishedReceiver, filter)
        mSyncFinishedReceiverRegistered = true
        super.onResume()

        notifySyncStop()
    }

    override fun onPause() {
        super.onPause()
        if (mSyncFinishedReceiverRegistered) {
            unregisterReceiver(mSyncFinishedReceiver)
            mSyncFinishedReceiverRegistered = false
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
            showAccountsDialogWithCheck()
            return false
        }

        Toast.makeText(this, "Refresh scheduled", Toast.LENGTH_SHORT).show()
        ManualSyncService.startActionSync(this)
        return false
    }

    override fun onEditorAction(textView: TextView, i: Int, keyEvent: KeyEvent): Boolean {
        return false
    }

    override fun onAccountSelected(account: Account, authSubToken: String?) {
        super.onAccountSelected(account, authSubToken)
        if (UpgradeCheck.isNewVersion(prefs)) {
            requestRefresh()
        }
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        mFilterQuery = ""
        if (TextUtils.isEmpty(query)) {
            MenuItemCompat.collapseActionView(mSearchMenuItem)
        } else {
            val searchIntent = Intent(this, MarketSearchActivity::class.java)
            searchIntent.putExtra(MarketSearchActivity.EXTRA_KEYWORD, query)
            searchIntent.putExtra(MarketSearchActivity.EXTRA_EXACT, true)
            startActivity(searchIntent)
            notifyQueryChange("")
            if (mSearchMenuItem != null) {
                MenuItemCompat.collapseActionView(mSearchMenuItem)
            }
        }
        return true
    }

    override fun onQueryTextChange(newText: String): Boolean {
        mFilterQuery = newText
        notifyQueryChange(newText)
        return true
    }

    private fun notifySortChange(sortIndex: Int) {
        for (idx in mEventListener.indices) {
            mEventListener[idx].onSortChanged(sortIndex)
        }
    }

    private fun notifyQueryChange(newTexr: String) {
        for (idx in mEventListener.indices) {
            mEventListener[idx].onQueryTextChanged(newTexr)
        }
    }

    fun addQueryChangeListener(listener: AppWatcherBaseActivity.EventListener): Int {
        mEventListener.add(listener)

        if (!TextUtils.isEmpty(mFilterQuery)) {
            notifyQueryChange(mFilterQuery)
        }

        return mEventListener.size - 1
    }

    fun removeQueryChangeListener(index: Int) {
        if (index < mEventListener.size) {
            mEventListener.removeAt(index)
        }
    }

    private fun notifySyncStart() {
        for (idx in mEventListener.indices) {
            mEventListener[idx].onSyncStart()
        }
    }

    private fun notifySyncStop() {
        for (idx in mEventListener.indices) {
            mEventListener[idx].onSyncFinish()
        }
    }

    class Adapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        private val mFragments = ArrayList<Fragment>()
        private val mFragmentTitles = ArrayList<String>()

        fun addFragment(fragment: Fragment, title: String) {
            mFragments.add(fragment)
            mFragmentTitles.add(title)
        }

        override fun getItem(position: Int): Fragment {
            return mFragments[position]
        }

        override fun getCount(): Int {
            return mFragments.size
        }

        override fun getPageTitle(position: Int): CharSequence {
            return mFragmentTitles[position]
        }
    }

    companion object {

        val EXTRA_FROM_NOTIFICATION = "extra_noti"
    }
}