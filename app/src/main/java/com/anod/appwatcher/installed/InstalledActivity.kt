// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.installed

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.Keep
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.anod.appwatcher.compose.AppTheme
import com.anod.appwatcher.compose.BaseComposeActivity
import com.anod.appwatcher.compose.MainDetailScreen
import com.anod.appwatcher.compose.onCommonActivityAction
import com.anod.appwatcher.details.DetailsDialog
import com.anod.appwatcher.model.Filters
import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.utils.prefs
import com.anod.appwatcher.watchlist.DetailContent
import com.anod.appwatcher.watchlist.MainActivity
import com.anod.appwatcher.watchlist.WatchListPagingSource
import kotlinx.coroutines.launch

@Keep
class InstalledActivity : BaseComposeActivity() {
    private val viewModel: InstalledListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.handleEvent(InstalledListEvent.SetWideLayout(hingeDevice.layout.value))

        setContent {
            AppTheme(
                    theme = viewModel.prefs.theme
            ) {
                val screenState by viewModel.viewStates.collectAsState(initial = viewModel.viewState)

                val pagingSourceConfig = WatchListPagingSource.Config(
                        filterId = Filters.ALL,
                        tagId = null,
                        showRecentlyUpdated = false,
                        showOnDevice = true,
                        showRecentlyInstalled = false
                )

                if (screenState.wideLayout.isWideLayout) {
                    MainDetailScreen(
                            wideLayout = screenState.wideLayout,
                            main = {
                                InstalledListScreen(
                                    screenState = screenState,
                                    pagingSourceConfig = pagingSourceConfig,
                                    onEvent = { viewModel.handleEvent(it) },
                                    installedApps = viewModel.installedApps
                                )
                            },
                            detail = {
                                DetailContent(app = screenState.selectedApp)
                            }
                    )
                } else {
                    InstalledListScreen(
                        screenState = screenState,
                        pagingSourceConfig = pagingSourceConfig,
                        onEvent = { viewModel.handleEvent(it) },
                        installedApps = viewModel.installedApps
                    )
                    if (screenState.selectedApp != null) {
                        DetailsDialog(
                            app = screenState.selectedApp!!,
                            onDismissRequest = { viewModel.handleEvent(InstalledListEvent.SelectApp(app = null)) },
                            onCommonActivityAction = { onCommonActivityAction(it) }
                        )
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.viewActions.collect { onCommonActivityAction(it) }
        }
    }

    companion object {
        private fun intent(sortId: Int, showImportAction: Boolean, context: Context): Intent {
            return Intent(context, InstalledActivity::class.java).apply {
                putExtra(MainActivity.ARG_SORT, sortId)
                putExtra(MainActivity.ARG_SHOW_ACTION, showImportAction)
            }
        }

        fun intent(importMode: Boolean, context: Context) = intent(
                if (importMode) Preferences.SORT_NAME_ASC else Preferences.SORT_DATE_DESC,
                importMode,
                context
        )
    }
}