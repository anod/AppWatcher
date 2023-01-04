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
import androidx.lifecycle.lifecycleScope
import com.anod.appwatcher.compose.AppTheme
import com.anod.appwatcher.compose.BaseComposeActivity
import com.anod.appwatcher.compose.MainDetailScreen
import com.anod.appwatcher.compose.onCommonActivityAction
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.details.DetailsDialog
import com.anod.appwatcher.model.Filters
import com.anod.appwatcher.utils.prefs
import com.anod.appwatcher.watchlist.DetailContent
import com.anod.appwatcher.watchlist.WatchListPagingSource
import com.anod.appwatcher.watchlist.WatchListSharedStateEvent
import com.anod.appwatcher.watchlist.WatchListStateViewModel
import info.anodsplace.framework.app.addMultiWindowFlags
import kotlinx.coroutines.launch

class TagWatchListComposeActivity : BaseComposeActivity() {
    private val viewModel: WatchListStateViewModel by viewModels(factoryProducer = {
        WatchListStateViewModel.Factory(
            defaultFilterId = Filters.ALL,
            wideLayout = hingeDevice.layout.value
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
                    if (screenState.selectedApp != null) {
                        DetailsDialog(
                            appId = screenState.selectedApp!!.appId,
                            rowId = screenState.selectedApp!!.rowId,
                            detailsUrl = screenState.selectedApp!!.detailsUrl ?: "",
                            onDismissRequest = { viewModel.handleEvent(WatchListSharedStateEvent.SelectApp(app = null)) },
                            onCommonActivityAction = { onCommonActivityAction(it) }
                        )
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.viewActions.collect { onCommonActivityAction(it) }
        }

        lifecycleScope.launchWhenCreated {
            hingeDevice.layout.collect {
                viewModel.handleEvent(WatchListSharedStateEvent.SetWideLayout(it))
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
        } else super.onBackPressed()
    }

    companion object {
        fun createTagIntent(tag: Tag, context: Context) = Intent(context, TagWatchListComposeActivity::class.java).apply {
            putExtra(AppsTagViewModel.EXTRA_TAG, tag)
            addMultiWindowFlags(context)
        }
    }
}
