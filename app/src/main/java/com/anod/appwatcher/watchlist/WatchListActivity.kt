package com.anod.appwatcher.watchlist

import android.accounts.Account
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.MenuRes
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.viewpager.widget.ViewPager
import com.anod.appwatcher.Application
import com.anod.appwatcher.MarketSearchActivity
import com.anod.appwatcher.R
import com.anod.appwatcher.model.Filters
import com.anod.appwatcher.navigation.DrawerActivity
import com.anod.appwatcher.navigation.DrawerViewModel
import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.search.SearchActivity
import com.anod.appwatcher.upgrade.UpgradeCheck
import com.anod.appwatcher.upgrade.UpgradeRefresh
import com.anod.appwatcher.utils.Theme
import info.anodsplace.framework.AppLog
import info.anodsplace.framework.app.CustomThemeColors
import info.anodsplace.framework.app.DialogSingleChoice

sealed class ListState
object SyncStarted : ListState()
class SyncStopped(val updatesCount: Int) : ListState()
object Updated : ListState()
object NoNetwork : ListState()
object ShowAuthDialog : ListState()

abstract class WatchListActivity : DrawerActivity(), TextView.OnEditorActionListener, SearchView.OnQueryTextListener {

    override val themeRes: Int
        get() = Theme(this).theme
    override val themeColors: CustomThemeColors
        get() = Theme(this).colors

    private lateinit var viewPager: ViewPager

    val prefs: Preferences
        get() = Application.provide(this).prefs

    open val defaultFilterId = Filters.TAB_ALL

    private val actionMenu by lazy { WatchListMenu(this, this) }
    private val stateViewModel: WatchListStateViewModel by viewModels()

    @get:MenuRes
    protected abstract val menuResource: Int

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("tab_id", viewPager.currentItem)
        outState.putString("filter", actionMenu.searchQuery)
        super.onSaveInstanceState(outState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val filterId: Int
        if (savedInstanceState != null) {
            filterId = savedInstanceState.getInt("tab_id", defaultFilterId)
            actionMenu.searchQuery = savedInstanceState.getString("filter") ?: ""
            AppLog.d("Restore tab: $filterId")
        } else {
            val fromNotification = intentExtras.getBoolean(EXTRA_FROM_NOTIFICATION, false)
            val expandSearch = intentExtras.getBoolean(EXTRA_EXPAND_SEARCH)
            filterId = if (fromNotification || expandSearch) defaultFilterId else intentExtras.getInt("tab_id", defaultFilterId)
            actionMenu.expandSearch = expandSearch
        }

        viewPager = findViewById(R.id.viewpager)
        viewPager.adapter = createViewPagerAdapter()
        viewPager.offscreenPageLimit = 0

        viewPager.currentItem = filterId
        actionMenu.filterId = filterId
        updateSubtitle(filterId)
        viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                onFilterSelected(position)
            }
        })

        stateViewModel.listState.observe(this) {
            when (it) {
                is SyncStarted -> {
                    actionMenu.startRefresh()
                    Toast.makeText(this, R.string.refresh_scheduled, Toast.LENGTH_SHORT).show()
                }
                is SyncStopped -> {
                    actionMenu.stopRefresh()
                    if (it.updatesCount == 0) {
                        Toast.makeText(this@WatchListActivity, R.string.no_updates_found, Toast.LENGTH_SHORT).show()
                    }
                    ViewModelProvider(this@WatchListActivity).get(DrawerViewModel::class.java).refreshLastUpdateTime()
                }
                is NoNetwork -> {
                    Toast.makeText(this, R.string.check_connection, Toast.LENGTH_SHORT).show()
                    actionMenu.stopRefresh()
                }
                is ShowAuthDialog -> {
                    this.showAccountsDialogWithCheck()
                    actionMenu.stopRefresh()
                }
            }
        }
    }

    override fun onAuthToken(authToken: String) {
        super.onAuthToken(authToken)
        stateViewModel.isAuthenticated = authToken.isNotEmpty()
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (actionMenu.onItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    fun showSortOptions() {
        val selected = prefs.sortIndex
        DialogSingleChoice(this, R.style.AlertDialog, R.array.sort_titles, selected) { dialog, index ->
            prefs.sortIndex = index
            stateViewModel.sortId.value = index
            dialog.dismiss()
        }.show()
    }

    override fun onAccountSelected(account: Account) {
        super.onAccountSelected(account)

        val upgrade = UpgradeCheck(prefs).result
        if (!upgrade.isNewVersion) {
            return
        }

        // SetupInterfaceUpgrade(prefs, this).onUpgrade(upgrade)
        UpgradeRefresh(prefs, this).onUpgrade(upgrade)
        // SettingsUpgrade(prefs, this).onUpgrade(upgrade)
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

        if (newText != stateViewModel.titleFilter.value) {
            stateViewModel.titleFilter.value = newText
        }

        return true
    }

    class Adapter(fm: androidx.fragment.app.FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        private val fragments = mutableListOf<androidx.fragment.app.Fragment>()
        private val fragmentTitles = mutableListOf<String>()

        fun addFragment(fragment: androidx.fragment.app.Fragment, title: String) {
            fragments.add(fragment)
            fragmentTitles.add(title)
        }

        override fun getItem(position: Int): androidx.fragment.app.Fragment {
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