package com.anod.appwatcher.watchlist

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.anod.appwatcher.MarketSearchActivity
import com.anod.appwatcher.R
import com.anod.appwatcher.SettingsActivity
import com.anod.appwatcher.accounts.AccountSelectionDialog
import com.anod.appwatcher.compose.AppTheme
import com.anod.appwatcher.compose.BaseComposeActivity
import com.anod.appwatcher.compose.MainDetailScreen
import com.anod.appwatcher.compose.onCommonActivityAction
import com.anod.appwatcher.details.DetailsDialog
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

abstract class WatchListActivity : BaseComposeActivity(), KoinComponent {
    private val mainViewModel: MainViewModel by viewModels()
    private val listViewModel: WatchListStateViewModel by viewModels(factoryProducer = {
        WatchListStateViewModel.Factory(
            defaultFilterId = prefs.defaultMainFilterId,
            wideLayout = hingeDevice.layout.value
        )
    })
    private lateinit var accountSelectionDialog: AccountSelectionDialog
    private lateinit var notificationPermissionRequest: ActivityResultLauncher<AppPermissions.Request.Input>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent?.extras?.containsKey("open_recently_installed") == true) {
            intent!!.extras!!.remove("open_recently_installed")
            startActivity(InstalledActivity.intent(importMode = false, context = this))
            return
        }

        accountSelectionDialog = AccountSelectionDialog(this, prefs)
        notificationPermissionRequest = registerForActivityResult(AppPermissions.Request()) {
            val enabled = it[AppPermission.PostNotification.value] ?: false
            mainViewModel.handleEvent(MainViewEvent.NotificationPermissionResult(enabled = enabled))
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)

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
                    if (listState.selectedApp != null) {
                        DetailsDialog(
                            appId = listState.selectedApp!!.appId,
                            rowId = listState.selectedApp!!.rowId,
                            detailsUrl = listState.selectedApp!!.detailsUrl ?: "",
                            onDismissRequest = { listViewModel.handleEvent(WatchListSharedStateEvent.SelectApp(app = null)) },
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
            mainViewModel.viewActions.collect { onMainAction(it) }
        }

        lifecycleScope.launchWhenCreated {
            hingeDevice.layout.collect {
                listViewModel.handleEvent(WatchListSharedStateEvent.SetWideLayout(it))
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
                }
            }
            is MainViewAction.NavigateToTag -> startActivity(TagWatchListComposeActivity.createTagIntent(action.tag, this))
            MainViewAction.RequestNotificationPermission -> notificationPermissionRequest.launch(AppPermission.PostNotification.toRequestInput())
            MainViewAction.ChooseAccount -> accountSelectionDialog.show()
            is MainViewAction.ActivityAction -> onCommonActivityAction(action.action)
        }
    }

    override fun onBackPressed() {
        if (listViewModel.viewState.wideLayout.isWideLayout) {
            if (listViewModel.viewState.selectedApp != null) {
                listViewModel.handleEvent(WatchListSharedStateEvent.SelectApp(app = null))
            } else {
                super.onBackPressed()
            }
        } else super.onBackPressed()
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