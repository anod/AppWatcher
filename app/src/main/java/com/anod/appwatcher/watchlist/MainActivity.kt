package com.anod.appwatcher.watchlist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.anod.appwatcher.MarketSearchActivity
import com.anod.appwatcher.R
import com.anod.appwatcher.SettingsActivity
import com.anod.appwatcher.accounts.AccountSelectionDialog
import com.anod.appwatcher.compose.AppTheme
import com.anod.appwatcher.compose.BaseComposeActivity
import com.anod.appwatcher.compose.MainDetailScreen
import com.anod.appwatcher.compose.onCommonActivityAction
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.details.DetailsDialog
import com.anod.appwatcher.history.HistoryListActivity
import com.anod.appwatcher.installed.InstalledActivity
import com.anod.appwatcher.tags.TagWatchListComposeActivity
import com.anod.appwatcher.utils.prefs
import com.anod.appwatcher.wishlist.WishListActivity
import info.anodsplace.applog.AppLog
import info.anodsplace.permissions.AppPermission
import info.anodsplace.permissions.AppPermissions
import info.anodsplace.permissions.toRequestInput
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

abstract class MainActivity : BaseComposeActivity(), KoinComponent {
    private val mainViewModel: MainViewModel by viewModels()
    private val listViewModel: WatchListStateViewModel by viewModels(factoryProducer = {
        WatchListStateViewModel.Factory(
            defaultFilterId = prefs.defaultMainFilterId,
            wideLayout = hingeDevice.layout.value,
            collectRecentlyInstalledApps = prefs.showRecent
        )
    })
    private lateinit var accountSelectionDialog: AccountSelectionDialog
    private lateinit var notificationPermissionRequest: ActivityResultLauncher<AppPermissions.Request.Input>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val extras = intent?.extras ?: bundleOf()
        if (extras.containsKey("open_recently_installed")) {
            intent!!.extras!!.remove("open_recently_installed")
            startActivity(InstalledActivity.intent(importMode = false, context = this))
            finish()
            return
        }

        if (extras.containsKey(WatchListStateViewModel.EXTRA_TAG_ID)) {
            val extraTagId = extras.getInt(WatchListStateViewModel.EXTRA_TAG_ID)
            intent!!.extras!!.remove(WatchListStateViewModel.EXTRA_TAG_ID)
            startActivity(TagWatchListComposeActivity.createTagIntent(
                tag = Tag(
                    id = extraTagId,
                    name = "",
                    color = extras.getInt(WatchListStateViewModel.EXTRA_TAG_COLOR)
                ),
                context = this
            ))
            // Do not finish and return to support back action
        }

        accountSelectionDialog = AccountSelectionDialog(this, prefs)
        notificationPermissionRequest = registerForActivityResult(AppPermissions.Request()) {
            val enabled = it[AppPermission.PostNotification.value] ?: false
            mainViewModel.handleEvent(MainViewEvent.NotificationPermissionResult(enabled = enabled))
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (mainViewModel.viewState.isDrawerOpen) {
                    mainViewModel.handleEvent(MainViewEvent.DrawerState(isOpen = false))
                } else {
                    listViewModel.handleEvent(WatchListEvent.NavigationButton)
                }
            }
        })

        setContent {
            AppTheme(
                theme = prefs.theme,
                transparentSystemUi = true
            ) {
                val mainState by mainViewModel.viewStates.collectAsState(initial = mainViewModel.viewState)
                val listState by listViewModel.viewStates.collectAsState(initial = listViewModel.viewState)

                val pagingSourceConfig = WatchListPagingSource.Config(
                    filterId = listState.filterId,
                    tagId = null,
                    showRecentlyDiscovered = prefs.showRecentlyDiscovered,
                    showOnDevice = prefs.showOnDevice,
                    showRecentlyInstalled = prefs.showRecent
                )

                val drawerValue = if (mainState.isDrawerOpen) DrawerValue.Open else DrawerValue.Closed
                val drawerState = rememberDrawerState(initialValue = drawerValue)
                LaunchedEffect(true) {
                    repeatOnLifecycle(state = Lifecycle.State.RESUMED) {
                        mainViewModel.viewActions.collect { action ->
                            if (action is MainViewAction.DrawerState) {
                                if (action.isOpen) {
                                    drawerState.open()
                                } else {
                                    drawerState.close()
                                }
                            } else onMainAction(action)
                        }
                    }
                }

                if (listState.wideLayout.isWideLayout) {
                    MainDetailScreen(
                        wideLayout = listState.wideLayout,
                        main = {
                            MainScreen(
                                mainState = mainState,
                                drawerState = drawerState,
                                onMainEvent = mainViewModel::handleEvent,
                                listState = listState,
                                pagingSourceConfig = pagingSourceConfig,
                                onListEvent = listViewModel::handleEvent,
                                installedApps = listViewModel.installedApps
                            )
                        },
                        detail = {
                            DetailContent(
                                app = listState.selectedApp,
                                onDismissRequest = { listViewModel.handleEvent(WatchListEvent.SelectApp(app = null)) },
                                onCommonActivityAction = { onCommonActivityAction(it) }
                            )
                        }
                    )
                } else {
                    MainScreen(
                        mainState = mainState,
                        drawerState = drawerState,
                        onMainEvent = mainViewModel::handleEvent,
                        listState = listState,
                        pagingSourceConfig = pagingSourceConfig,
                        onListEvent = listViewModel::handleEvent,
                        installedApps = listViewModel.installedApps
                    )
                    if (listState.selectedApp != null) {
                        DetailsDialog(
                            app = listState.selectedApp!!,
                            onDismissRequest = { listViewModel.handleEvent(WatchListEvent.SelectApp(app = null)) },
                            onCommonActivityAction = { onCommonActivityAction(it) }
                        )
                    }
                }
            }
        }

        lifecycleScope.launch {
            listViewModel.viewActions.collect { onCommonActivityAction(it) }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                hingeDevice.layout.collect {
                    listViewModel.handleEvent(WatchListEvent.SetWideLayout(it))
                }
            }
        }

        lifecycleScope.launch {
            accountSelectionDialog.accountSelected.collect { result ->
                mainViewModel.handleEvent(MainViewEvent.SetAccount(result))
            }
        }

        if (prefs.account == null) {
            Toast.makeText(this, R.string.failed_gain_access, Toast.LENGTH_LONG).show()
            accountSelectionDialog.show()
        } else {
            mainViewModel.handleEvent(MainViewEvent.InitAccount(prefs.account!!))
        }
    }

    override fun onResume() {
        super.onResume()

        AppLog.d("mark updates as viewed.")
        prefs.isLastUpdatesViewed = true
    }

    private fun onMainAction(action: MainViewAction) {
        when (action) {
            is MainViewAction.NavigateTo -> {
                when (action.id) {
                    DrawerItem.Id.Add -> startActivity(Intent(this, MarketSearchActivity::class.java))
                    DrawerItem.Id.Installed -> startActivity(InstalledActivity.intent(false, this))
                    DrawerItem.Id.Refresh -> { }
                    DrawerItem.Id.Settings ->  startActivity( Intent(this, SettingsActivity::class.java))
                    DrawerItem.Id.Wishlist -> startActivity(WishListActivity.intent(this, prefs.account, mainViewModel.authToken.token))
                    DrawerItem.Id.Purchases -> startActivity(HistoryListActivity.intent(this, prefs.account, mainViewModel.authToken.token))
                }
            }
            is MainViewAction.NavigateToTag -> startActivity(TagWatchListComposeActivity.createTagIntent(action.tag, this))
            MainViewAction.RequestNotificationPermission -> notificationPermissionRequest.launch(AppPermission.PostNotification.toRequestInput())
            MainViewAction.ChooseAccount -> accountSelectionDialog.show()
            is MainViewAction.ActivityAction -> onCommonActivityAction(action.action)
            is MainViewAction.DrawerState -> { }
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