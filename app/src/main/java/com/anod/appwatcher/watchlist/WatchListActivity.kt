package com.anod.appwatcher.watchlist

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.anod.appwatcher.BuildConfig
import com.anod.appwatcher.MarketSearchActivity
import com.anod.appwatcher.SettingsActivity
import com.anod.appwatcher.compose.AppTheme
import com.anod.appwatcher.compose.BaseComposeActivity
import com.anod.appwatcher.compose.MainDetailScreen
import com.anod.appwatcher.details.DetailsDialog
import com.anod.appwatcher.installed.InstalledActivity
import com.anod.appwatcher.sync.SyncScheduler
import com.anod.appwatcher.tags.TagWatchListComposeActivity
import com.anod.appwatcher.upgrade.Upgrade15500
import com.anod.appwatcher.upgrade.UpgradeCheck
import com.anod.appwatcher.utils.account
import com.anod.appwatcher.utils.prefs
import com.anod.appwatcher.wishlist.WishListActivity
import info.anodsplace.applog.AppLog
import info.anodsplace.framework.content.startActivitySafely
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

abstract class WatchListActivity : BaseComposeActivity(), KoinComponent {
    private val mainViewModel: MainViewModel by viewModels()
    private val listViewModel: WatchListStateViewModel by viewModels(factoryProducer = {
        WatchListStateViewModel.Factory(
            defaultFilterId = prefs.defaultMainFilterId,
            wideLayout = hingeDevice.layout.value
        )
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent?.extras?.containsKey("open_recently_installed") == true) {
            intent!!.extras!!.remove("open_recently_installed")
            startActivity(InstalledActivity.intent(importMode = false, context = this))
            return
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)

//            stateViewModel.viewStates.map { it.listState }.distinctUntilChanged().collect {
//                when (it) {
//                    is ListState.SyncStarted -> {
//                        Toast.makeText(this@WatchListActivity, R.string.refresh_scheduled, Toast.LENGTH_SHORT).show()
//                    }
//                    is ListState.SyncStopped -> {
//                        if (it.updatesCount == 0) {
//                            Toast.makeText(
//                                    this@WatchListActivity,
//                                    R.string.no_updates_found,
//                                    Toast.LENGTH_SHORT
//                            ).show()
//                        }
//                        ViewModelProvider(this@WatchListActivity).get(DrawerViewModel::class.java)
//                            .refreshLastUpdateTime()
//                        ListState.SyncStopped
//                    }
//                    is ListState.NoNetwork -> {
//                        Toast.makeText(this@WatchListActivity, R.string.check_connection, Toast.LENGTH_SHORT).show()
//                    }
//                    is ListState.ShowAuthDialog -> {
//                        this@WatchListActivity.showAccountsDialogWithCheck()
//                    }
//                }
//            }

        setContent {
            AppTheme(
                theme = listViewModel.prefs.theme,
                transparentSystemUi = true
            ) {
                val mainState by mainViewModel.viewStates.collectAsState(initial = mainViewModel.viewState)
                val listState by listViewModel.viewStates.collectAsState(initial = listViewModel.viewState)

                val pagingSourceConfig = WatchListPagingSource.Config(
                    filterId = listState.filterId,
                    tagId = null,
                    showRecentlyUpdated = listViewModel.prefs.showRecentlyUpdated,
                    showOnDevice = listViewModel.prefs.showOnDevice,
                    showRecentlyInstalled = listViewModel.prefs.showRecent
                )

                if (listState.wideLayout.isWideLayout) {
                    MainDetailScreen(
                        wideLayout = listState.wideLayout,
                        main = {
                            MainScreen(
                                mainState = mainState,
                                onMainEvent = { mainViewModel.handleEvent(it) },
                                listState = listState,
                                pagingSourceConfig = pagingSourceConfig,
                                onListEvent = { listViewModel.handleEvent(it) }
                            )
                        },
                        detail = {
                            DetailContent(app = listState.selectedApp)
                        }
                    )
                } else {
                    MainScreen(
                        mainState = mainState,
                        onMainEvent = { mainViewModel.handleEvent(it) },
                        listState = listState,
                        pagingSourceConfig = pagingSourceConfig,
                        onListEvent = { listViewModel.handleEvent(it) }
                    )
                }
            }
            

        }

        lifecycleScope.launch {
            listViewModel.viewActions.collect { onListAction(it) }
        }

        lifecycleScope.launch {
            mainViewModel.viewActions.collect { onMainAction(it) }
        }

        lifecycleScope.launchWhenCreated {
            hingeDevice.layout.collect {
                listViewModel.handleEvent(WatchListSharedStateEvent.SetWideLayout(it))
            }
        }
    }

    override fun onResume() {
        super.onResume()

        AppLog.d("mark updates as viewed.")
        prefs.isLastUpdatesViewed = true
    }

    private fun upgradeCheck() {
        val upgrade = UpgradeCheck(prefs).result
        if (!upgrade.isNewVersion) {
            return
        }

        Upgrade15500().onUpgrade(upgrade)
    }

    private fun onNotificationEnabled() {
        if (prefs.useAutoSync) {
            lifecycleScope.launchWhenCreated {
                SyncScheduler(applicationContext)
                    .schedule(prefs.isRequiresCharging, prefs.isWifiOnly, prefs.updatesFrequency.toLong(), false)
                    .collect { }
            }
        }
    }

    private fun onListAction(action: WatchListSharedStateAction) {
        when (action) {
            WatchListSharedStateAction.OnBackPressed -> onBackPressed()
            is WatchListSharedStateAction.OpenApp -> {
                val app = action.app
                if (BuildConfig.DEBUG) {
                    AppLog.d(app.packageName)
                }
                DetailsDialog.show(app.appId, app.rowId, app.detailsUrl, supportFragmentManager)
            }
            is WatchListSharedStateAction.SearchInStore -> startActivity(MarketSearchActivity.intent(this, "", true))
            is WatchListSharedStateAction.Installed -> startActivity(InstalledActivity.intent(action.importMode, this))
            is WatchListSharedStateAction.ShareFromStore -> startActivitySafely(Intent.makeMainActivity(
                ComponentName("com.android.vending", "com.android.vending.AssetBrowserActivity")
            ))
            is WatchListSharedStateAction.OnSearch -> startActivity(MarketSearchActivity.intent(this, action.query, true))
            WatchListSharedStateAction.Dismiss -> finish()
        }
    }


    private fun onMainAction(action: MainViewAction) {
        when (action) {
            is MainViewAction.NavigateTo -> {
                when (action.id) {
                    DrawerNavigationItem.Id.Add -> startActivity(Intent(this, MarketSearchActivity::class.java))
                    DrawerNavigationItem.Id.Installed -> startActivity(InstalledActivity.intent(false, this))
                    DrawerNavigationItem.Id.Refresh -> { }
                    DrawerNavigationItem.Id.Settings ->  startActivity( Intent(this, SettingsActivity::class.java))
                    DrawerNavigationItem.Id.Wishlist -> startActivity(WishListActivity.intent(this, account, mainViewModel.authToken.token))
                }
            }
            is MainViewAction.NavigateToTag -> startActivity(TagWatchListComposeActivity.createTagIntent(action.tag, this))
            MainViewAction.RequestNotificationPermission -> { }
            is MainViewAction.ShowToast -> {
                if (action.resId == 0) {
                    Toast.makeText(this, action.text, action.length).show()
                } else {
                    Toast.makeText(this, action.resId, action.length).show()
                }
            }
            is MainViewAction.StartActivity -> startActivitySafely(action.intent)
        }
    }

    override fun onBackPressed() {
        if (listViewModel.viewState.wideLayout.isWideLayout) {
            if (listViewModel.viewState.selectedApp != null) {
                listViewModel.handleEvent(WatchListSharedStateEvent.SelectApp(app = null))
            } else {
                super.onBackPressed()
            }
        } else if (!DetailsDialog.dismiss(supportFragmentManager)) {
            super.onBackPressed()
        }
    }

    companion object {
        const val EXTRA_FROM_NOTIFICATION = "extra_noti"
        const val EXTRA_EXPAND_SEARCH = "expand_search"

        const val ARG_FILTER = "filter"
        const val ARG_SORT = "sort"
        const val ARG_TAG = "tag"
        const val ARG_SHOW_ACTION = "showAction"
    }
}