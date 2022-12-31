package com.anod.appwatcher.tags

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import com.anod.appwatcher.BuildConfig
import com.anod.appwatcher.MarketSearchActivity
import com.anod.appwatcher.compose.AppTheme
import com.anod.appwatcher.compose.BaseComposeActivity
import com.anod.appwatcher.compose.MainDetailScreen
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.details.DetailsDialog
import com.anod.appwatcher.installed.InstalledActivity
import com.anod.appwatcher.model.Filters
import com.anod.appwatcher.utils.forMyApps
import com.anod.appwatcher.utils.prefs
import com.anod.appwatcher.watchlist.DetailContent
import com.anod.appwatcher.watchlist.WatchListPagingSource
import com.anod.appwatcher.watchlist.WatchListSharedStateAction
import com.anod.appwatcher.watchlist.WatchListSharedStateEvent
import com.anod.appwatcher.watchlist.WatchListStateViewModel
import info.anodsplace.applog.AppLog
import info.anodsplace.framework.app.addMultiWindowFlags
import info.anodsplace.framework.content.startActivitySafely
import kotlinx.coroutines.launch

class TagWatchListComposeActivity : BaseComposeActivity() {
    val viewModel: WatchListStateViewModel by viewModels(factoryProducer = {
        WatchListStateViewModel.Factory(
            defaultFilterId = Filters.ALL,
            wideLayout = hingeDevice.layout.value
        )
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme(
                    customPrimaryColor = Color(viewModel.viewState.tag.color),
                    theme = viewModel.prefs.theme
            ) {
                val screenState by viewModel.viewStates.collectAsState(initial = viewModel.viewState)
                
                val pagingSourceConfig = WatchListPagingSource.Config(
                        filterId = screenState.filterId,
                        tagId = if (screenState.tag.isEmpty) null else screenState.tag.id,
                        showRecentlyUpdated = viewModel.prefs.showRecentlyUpdated,
                        showOnDevice = false,
                        showRecentlyInstalled = false
                )

                if (screenState.wideLayout.isWideLayout) {
                    MainDetailScreen(
                            wideLayout = screenState.wideLayout,
                            main = {
                                TagWatchListScreen(screenState = screenState, pagingSourceConfig = pagingSourceConfig, onEvent = { viewModel.handleEvent(it) })
                            },
                            detail = {
                                DetailContent(app = screenState.selectedApp)
                            }
                    )
                } else {
                    TagWatchListScreen(screenState = screenState, pagingSourceConfig = pagingSourceConfig, onEvent = { viewModel.handleEvent(it) })
                }
            }
        }

        lifecycleScope.launch {
            viewModel.viewActions.collect { onViewAction(it) }
        }

        lifecycleScope.launchWhenCreated {
            hingeDevice.layout.collect {
                viewModel.handleEvent(WatchListSharedStateEvent.SetWideLayout(it))
            }
        }
    }

    private fun onViewAction(action: WatchListSharedStateAction) {
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
            is WatchListSharedStateAction.ShareFromStore -> startActivitySafely(Intent.makeMainActivity(ComponentName("com.android.vending", "com.android.vending.AssetBrowserActivity")))
            is WatchListSharedStateAction.OnSearch -> startActivity(MarketSearchActivity.intent(this, action.query, true))
            WatchListSharedStateAction.Dismiss -> finish()
            WatchListSharedStateAction.PlayStoreMyApps -> startActivitySafely(Intent().forMyApps(true, this))
            WatchListSharedStateAction.ShowAccountsDialog -> { }
            is WatchListSharedStateAction.ShowToast -> {
                if (action.resId == 0) {
                    Toast.makeText(this, action.text, action.length).show()
                } else {
                    Toast.makeText(this, action.resId, action.length).show()
                }
            }
        }
    }

    override fun onBackPressed() {
        if (viewModel.viewState.wideLayout.isWideLayout) {
            if (viewModel.viewState.selectedApp != null) {
                viewModel.handleEvent(WatchListSharedStateEvent.SelectApp(app = null))
            } else {
                super.onBackPressed()
            }
        } else if (!DetailsDialog.dismiss(supportFragmentManager)) {
            super.onBackPressed()
        }
    }

    companion object {
        fun createTagIntent(tag: Tag, context: Context) = Intent(context, TagWatchListComposeActivity::class.java).apply {
            putExtra(AppsTagViewModel.EXTRA_TAG, tag)
            addMultiWindowFlags(context)
        }
    }
}
