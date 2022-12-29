package com.anod.appwatcher.watchlist

import android.accounts.Account
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.annotation.MenuRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.anod.appwatcher.MarketSearchActivity
import com.anod.appwatcher.R
import com.anod.appwatcher.databinding.ActivityMainBinding
import com.anod.appwatcher.details.DetailsDialog
import com.anod.appwatcher.details.DetailsEmptyView
import com.anod.appwatcher.details.DetailsFragment
import com.anod.appwatcher.model.Filters
import com.anod.appwatcher.navigation.DrawerActivity
import com.anod.appwatcher.navigation.DrawerViewModel
import com.anod.appwatcher.search.SearchComposeActivity
import com.anod.appwatcher.upgrade.Upgrade15500
import com.anod.appwatcher.upgrade.UpgradeCheck
import com.anod.appwatcher.utils.EventFlow
import com.anod.appwatcher.utils.Theme
import com.anod.appwatcher.utils.appScope
import com.anod.appwatcher.utils.prefs
import info.anodsplace.applog.AppLog
import info.anodsplace.framework.app.CustomThemeColors
import info.anodsplace.framework.app.FragmentContainerFactory
import info.anodsplace.framework.app.HingeDeviceLayout
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

sealed class ListState {
    object SyncStarted : ListState()
    class SyncStopped(val updatesCount: Int) : ListState()
    object Updated : ListState()
    object NoNetwork : ListState()
    object ShowAuthDialog : ListState()
}

interface AppDetailsRouter {
    fun openAppDetails(appId: String, rowId: Int, detailsUrl: String?)
}

abstract class WatchListActivity : DrawerActivity(), TextView.OnEditorActionListener, AppDetailsRouter, KoinComponent {

    internal lateinit var binding: ActivityMainBinding

    val theme: Theme
        get() = Theme(this, prefs)

    override val themeRes: Int
        get() = theme.theme
    override val themeColors: CustomThemeColors
        get() = theme.colors

    open val defaultFilterId = Filters.ALL

    override val layoutView: View
        get() {
            binding = ActivityMainBinding.inflate(layoutInflater)
            return binding.root
        }

    @get:IdRes
    override val detailsLayoutId = R.id.details

    @get:IdRes
    override val hingeLayoutId = R.id.hinge

    private val menuAction = EventFlow<MenuAction>()
    private val actionMenu by lazy { WatchListMenu(menuAction, this, appScope, prefs) }
    private val stateViewModel: WatchListStateViewModel by viewModels()

    @get:MenuRes
    protected abstract val menuResource: Int

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("tab_id", binding.viewPager.currentItem)
        outState.putString("filter", actionMenu.search.query)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val filterId: Int
        if (savedInstanceState != null) {
            filterId = savedInstanceState.getInt("tab_id", defaultFilterId)
            actionMenu.search.query = savedInstanceState.getString("filter") ?: ""
            AppLog.d("Restore tab: $filterId")
        } else {
            val fromNotification = intentExtras.getBoolean(EXTRA_FROM_NOTIFICATION, false)
            val expandSearch = intentExtras.getBoolean(EXTRA_EXPAND_SEARCH)
            filterId = if (fromNotification || expandSearch) defaultFilterId else intentExtras.getInt("tab_id", defaultFilterId)
            actionMenu.search.expand = expandSearch
        }

        binding.viewPager.offscreenPageLimit = 1
        binding.viewPager.adapter = createViewPagerAdapter()
        binding.viewPager.setCurrentItem(filterId, false)
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                onFilterSelected(position)
            }
        })

        actionMenu.filterId = filterId
        updateSubtitle(filterId)

        lifecycleScope.launchWhenResumed {
            menuAction.collectLatest {
                when (it) {
                    is FilterMenuAction -> binding.viewPager.currentItem = it.filterId
                    is SortMenuAction -> stateViewModel.handleEvent(WatchListSharedStateEvent.ChangeSort(it.sortId))
                    is SearchQueryAction -> {
                        if (it.submit) {
                            startActivity(Intent(this@WatchListActivity, MarketSearchActivity::class.java).apply {
                                putExtra(SearchComposeActivity.EXTRA_KEYWORD, it.query)
                                putExtra(SearchComposeActivity.EXTRA_EXACT, true)
                            })
                        } else {
                            stateViewModel.handleEvent(WatchListSharedStateEvent.FilterByTitle(it.query))
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            stateViewModel.viewStates.map { it.listState }.distinctUntilChanged().collect {
                when (it) {
                    is ListState.SyncStarted -> {
                        actionMenu.startRefresh()
                        Toast.makeText(this@WatchListActivity, R.string.refresh_scheduled, Toast.LENGTH_SHORT).show()
                    }
                    is ListState.SyncStopped -> {
                        actionMenu.stopRefresh()
                        if (it.updatesCount == 0) {
                            Toast.makeText(
                                    this@WatchListActivity,
                                    R.string.no_updates_found,
                                    Toast.LENGTH_SHORT
                            ).show()
                        }
                        ViewModelProvider(this@WatchListActivity).get(DrawerViewModel::class.java)
                                .refreshLastUpdateTime()
                    }
                    is ListState.NoNetwork -> {
                        Toast.makeText(this@WatchListActivity, R.string.check_connection, Toast.LENGTH_SHORT).show()
                        actionMenu.stopRefresh()
                    }
                    is ListState.ShowAuthDialog -> {
                        this@WatchListActivity.showAccountsDialogWithCheck()
                        actionMenu.stopRefresh()
                    }
                    else -> {
                    }
                }
            }
        }
    }

    override fun updateWideLayout(wideLayout: HingeDeviceLayout) {
        super.updateWideLayout(wideLayout)
        stateViewModel.handleEvent(WatchListSharedStateEvent.SetWideLayout(wideLayout))
        if (wideLayout.isWideLayout) {
            if (supportFragmentManager.findFragmentByTag(DetailsEmptyView.tag) == null) {
                supportFragmentManager.commit {
                    replace(R.id.details, DetailsEmptyView(), DetailsEmptyView.tag)
                }
            }
        }
    }

    protected abstract fun createViewPagerAdapter(): Adapter

    open fun onFilterSelected(filterId: Int) {
        actionMenu.filterId = filterId
        updateSubtitle(filterId)
    }

    private fun updateSubtitle(filterId: Int) {
        if (filterId == 0) {
            supportActionBar?.subtitle = ""
        } else {
            val titles = resources.getStringArray(R.array.filter_titles)
            supportActionBar?.subtitle = titles.getOrNull(filterId) ?: ""
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

    override fun onAccountSelected(account: Account) {
        super.onAccountSelected(account)

        val upgrade = UpgradeCheck(prefs).result
        if (!upgrade.isNewVersion) {
            return
        }

       Upgrade15500().onUpgrade(upgrade)
    }

    override fun onEditorAction(textView: TextView, i: Int, keyEvent: KeyEvent): Boolean {
        return false
    }

    override fun openAppDetails(appId: String, rowId: Int, detailsUrl: String?) {
        if (stateViewModel.viewState.wideLayout.isWideLayout) {
            supportFragmentManager.commit {
                add(R.id.details, DetailsFragment.newInstance(appId, detailsUrl ?: "", rowId), DetailsFragment.tag)
                addToBackStack(DetailsFragment.tag)
            }
        } else {
            DetailsDialog.show(appId, rowId, detailsUrl, supportFragmentManager)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (stateViewModel.viewState.wideLayout.isWideLayout && supportFragmentManager.findFragmentByTag(DetailsFragment.tag) != null) {
            supportFragmentManager.popBackStack(DetailsFragment.tag, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        } else {
            if (!DetailsDialog.dismiss(supportFragmentManager)) {
                super.onBackPressed()
            }
        }
    }

    class Adapter(private val factories: List<FragmentContainerFactory>, activity: FragmentActivity) : FragmentStateAdapter(activity) {

        override fun getItemCount(): Int = factories.size

        override fun createFragment(position: Int): Fragment {
            return factories[position].create()
        }
    }

    companion object {
        const val EXTRA_FROM_NOTIFICATION = "extra_noti"
        const val EXTRA_EXPAND_SEARCH = "expand_search"
    }
}