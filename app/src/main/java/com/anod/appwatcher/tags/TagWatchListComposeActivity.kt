package com.anod.appwatcher.tags

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.anod.appwatcher.compose.AppTheme
import com.anod.appwatcher.compose.BaseComposeActivity
import com.anod.appwatcher.compose.MainDetailScreen
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.details.DetailsDialog
import com.anod.appwatcher.model.Filters
import com.anod.appwatcher.utils.prefs
import com.anod.appwatcher.watchlist.DetailContent
import com.anod.appwatcher.watchlist.WatchListEvent
import com.anod.appwatcher.watchlist.WatchListPagingSource
import com.anod.appwatcher.watchlist.WatchListStateViewModel
import info.anodsplace.framework.app.addMultiWindowFlags
import info.anodsplace.framework.content.onCommonActivityAction
import kotlinx.coroutines.launch

class TagWatchListComposeActivity : BaseComposeActivity() {
    private val viewModel: WatchListStateViewModel by viewModels(factoryProducer = {
        WatchListStateViewModel.Factory(
            defaultFilterId = Filters.ALL,
            wideLayout = foldableDevice.layout.value,
            collectRecentlyInstalledApps = false
        )
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val screenState by viewModel.viewStates.collectAsState(initial = viewModel.viewState)
            val customPrimaryColor by remember(screenState) {
                derivedStateOf { Color(screenState.tag.color) }
            }
            AppTheme(
                customPrimaryColor = customPrimaryColor,
                theme = viewModel.prefs.theme
            ) {
                val pagingSourceConfig = WatchListPagingSource.Config(
                    filterId = screenState.filterId,
                    tagId = screenState.tag.id,
                    showRecentlyDiscovered = viewModel.prefs.showRecentlyDiscovered,
                    showOnDevice = false,
                    showRecentlyInstalled = false
                )

                if (screenState.wideLayout.isWideLayout) {
                    MainDetailScreen(
                        wideLayout = screenState.wideLayout,
                        main = {
                            TagWatchListScreen(
                                screenState = screenState,
                                pagingSourceConfig = pagingSourceConfig,
                                onEvent = viewModel::handleEvent,
                                installedApps = viewModel.installedApps
                            )
                        },
                        detail = {
                            DetailContent(
                                app = screenState.selectedApp,
                                onDismissRequest = { viewModel.handleEvent(WatchListEvent.SelectApp(app = null)) },
                                onCommonActivityAction = { onCommonActivityAction(it) }
                            )
                        }
                    )
                } else {
                    TagWatchListScreen(
                        screenState = screenState,
                        pagingSourceConfig = pagingSourceConfig,
                        onEvent = viewModel::handleEvent,
                        installedApps = viewModel.installedApps
                    )
                    if (screenState.selectedApp != null) {
                        DetailsDialog(
                            app = screenState.selectedApp!!,
                            onDismissRequest = { viewModel.handleEvent(WatchListEvent.SelectApp(app = null)) },
                            onCommonActivityAction = { onCommonActivityAction(it) }
                        )
                    }
                }

                if (screenState.showAppTagDialog) {
                    AppsTagDialog(
                        tag = screenState.tag,
                        onDismissRequest = { viewModel.handleEvent(WatchListEvent.AddAppToTag(show = false)) }
                    )
                }

                if (screenState.showEditTagDialog) {
                    EditTagDialog(
                        tag = screenState.tag,
                        onDismissRequest = { viewModel.handleEvent(WatchListEvent.EditTag(show = false)) }
                    )
                }
            }
        }

        lifecycleScope.launch {
            viewModel.viewActions.collect { onCommonActivityAction(it) }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                foldableDevice.layout.collect {
                    viewModel.handleEvent(WatchListEvent.SetWideLayout(it))
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (viewModel.viewState.wideLayout.isWideLayout) {
            if (viewModel.viewState.selectedApp != null) {
                viewModel.handleEvent(WatchListEvent.SelectApp(app = null))
            } else {
                super.onBackPressed()
            }
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        fun createTagIntent(tag: Tag, context: Context) = Intent(context, TagWatchListComposeActivity::class.java).apply {
            putExtra(WatchListStateViewModel.EXTRA_TAG, tag)
            addMultiWindowFlags(context)
        }
    }
}