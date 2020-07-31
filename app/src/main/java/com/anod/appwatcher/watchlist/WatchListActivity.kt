package com.anod.appwatcher.watchlist

import android.accounts.Account
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.LayoutRes
import androidx.annotation.MenuRes
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.viewpager.widget.ViewPager
import com.anod.appwatcher.Application
import com.anod.appwatcher.ChangelogActivity
import com.anod.appwatcher.MarketSearchActivity
import com.anod.appwatcher.R
import com.anod.appwatcher.details.DetailsActivity
import com.anod.appwatcher.details.DetailsEmptyView
import com.anod.appwatcher.details.DetailsFragment
import com.anod.appwatcher.model.Filters
import com.anod.appwatcher.navigation.DrawerActivity
import com.anod.appwatcher.navigation.DrawerViewModel
import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.search.SearchActivity
import com.anod.appwatcher.upgrade.UpgradeCheck
import com.anod.appwatcher.upgrade.UpgradeRefresh
import com.anod.appwatcher.utils.HingeDevice
import com.anod.appwatcher.utils.Theme
import info.anodsplace.framework.AppLog
import info.anodsplace.framework.app.CustomThemeColors
import info.anodsplace.framework.app.DialogSingleChoice
import info.anodsplace.framework.app.FragmentFactory
import kotlinx.android.synthetic.main.activity_main.*

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
    override val layoutResource: Int
        @LayoutRes get() = R.layout.activity_main

    private lateinit var duoDevice: HingeDevice

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

        duoDevice = HingeDevice.create(this)
        updateWideLayout()

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

        viewPager.offscreenPageLimit = 0
        viewPager.adapter = createViewPagerAdapter()

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

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateWideLayout()
    }

    private fun updateWideLayout() {
        stateViewModel.isWideLayout = resources.getBoolean(R.bool.wide_layout)
        details.isVisible = stateViewModel.isWideLayout
        hinge.isVisible = stateViewModel.isWideLayout && duoDevice.hinge.width() > 0
        hinge.layoutParams.width = duoDevice.hinge.width()
        if (stateViewModel.isWideLayout) {
            if (supportFragmentManager.findFragmentByTag(DetailsEmptyView.tag) == null) {
                supportFragmentManager.commit {
                    replace(R.id.details, DetailsEmptyView(), DetailsEmptyView.tag)
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
            supportActionBar?.subtitle = (viewPager.adapter as? Adapter)?.getPageTitle(filterId)
                    ?: ""
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
        UpgradeRefresh(prefs, this, this).onUpgrade(upgrade)
        // SettingsUpgrade(prefs, this).onUpgrade(upgrade)
    }

    override fun onEditorAction(textView: TextView, i: Int, keyEvent: KeyEvent): Boolean {
        return false
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        startActivity(Intent(this, MarketSearchActivity::class.java).apply {
            putExtra(SearchActivity.EXTRA_KEYWORD, query)
            putExtra(SearchActivity.EXTRA_EXACT, true)
        })
        return true
    }

    override fun onQueryTextChange(newText: String): Boolean {

        if (newText != stateViewModel.titleFilter.value) {
            stateViewModel.titleFilter.value = newText
        }

        return true
    }

    fun openAppDetails(appId: String, rowId: Int, detailsUrl: String?) {
        if (stateViewModel.isWideLayout) {
            supportFragmentManager.commit {
                add(R.id.details, DetailsFragment.newInstance(appId, detailsUrl
                        ?: "", rowId), DetailsFragment.tag)
                addToBackStack(DetailsFragment.tag)
            }
        } else {
            val intent = Intent(this, ChangelogActivity::class.java).apply {
                putExtra(DetailsActivity.EXTRA_APP_ID, appId)
                putExtra(DetailsActivity.EXTRA_ROW_ID, rowId)
                putExtra(DetailsActivity.EXTRA_DETAILS_URL, detailsUrl)
            }
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        if (stateViewModel.isWideLayout && supportFragmentManager.findFragmentByTag(DetailsFragment.tag) != null) {
            supportFragmentManager.popBackStack(DetailsFragment.tag, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        } else {
            super.onBackPressed()
        }
    }

    class Adapter(activity: WatchListActivity) : FragmentPagerAdapter(activity.supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        private val fragments = mutableListOf<Fragment>()
        private val fragmentTitles = mutableListOf<String>()

        fun addFragment(fragmentFactory: FragmentFactory, title: String) {
            fragments.add(fragmentFactory.create()!!)
            fragmentTitles.add(title)
        }

        override fun getItem(position: Int) = fragments[position]

        override fun getCount() = fragments.size

        override fun getPageTitle(position: Int): CharSequence {
            return fragmentTitles[position]
        }
    }

    companion object {
        const val EXTRA_FROM_NOTIFICATION = "extra_noti"
        const val EXTRA_EXPAND_SEARCH = "expand_search"
    }
}