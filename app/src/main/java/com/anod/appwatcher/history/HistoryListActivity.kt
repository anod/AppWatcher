package com.anod.appwatcher.history

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.anod.appwatcher.R
import com.anod.appwatcher.compose.AppTheme
import com.anod.appwatcher.compose.BaseComposeActivity
import com.anod.appwatcher.compose.MainDetailScreen
import com.anod.appwatcher.details.DetailsDialog
import com.anod.appwatcher.utils.prefs
import com.anod.appwatcher.watchlist.DetailContent
import info.anodsplace.framework.content.onCommonActivityAction
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

/**
 * @author Alex Gavrishev
 * *
 * @date 16/12/2016.
 */
class HistoryListActivity : BaseComposeActivity(), KoinComponent {

    private val viewModel: HistoryListViewModel by viewModels(factoryProducer = {
        HistoryListViewModel.Factory(
            wideLayout = foldableDevice.layout.value,
        )
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!viewModel.authenticated) {
            Toast.makeText(this, R.string.choose_an_account, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setContent {
            AppTheme(
                theme = viewModel.prefs.theme
            ) {
                val screenState by viewModel.viewStates.collectAsState(initial = viewModel.viewState)
                if (screenState.wideLayout.isWideLayout) {
                    MainDetailScreen(
                        wideLayout = screenState.wideLayout,
                        main = {
                            HistoryListScreen(
                                screenState = screenState,
                                onEvent = viewModel::handleEvent,
                                pagingDataFlow = viewModel.pagingData,
                                viewActions = viewModel.viewActions,
                                onActivityAction = { onCommonActivityAction(it) }
                            )
                        },
                        detail = {
                            DetailContent(
                                app = screenState.selectedApp,
                                onDismissRequest = { viewModel.handleEvent(HistoryListEvent.SelectApp(app = null)) },
                                onCommonActivityAction = { onCommonActivityAction(it) }
                            )
                        }
                    )
                } else {
                    HistoryListScreen(
                        screenState = screenState,
                        onEvent = viewModel::handleEvent,
                        pagingDataFlow = viewModel.pagingData,
                        viewActions = viewModel.viewActions,
                        onActivityAction = { onCommonActivityAction(it) }
                    )
                    if (screenState.selectedApp != null) {
                        DetailsDialog(
                            app = screenState.selectedApp!!,
                            onDismissRequest = { viewModel.handleEvent(HistoryListEvent.SelectApp(app = null)) },
                            onCommonActivityAction = { onCommonActivityAction(it) }
                        )
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                foldableDevice.layout.collect {
                    viewModel.handleEvent(HistoryListEvent.SetWideLayout(it))
                }
            }
        }
    }

    companion object {
        fun intent(context: Context): Intent = Intent(context, HistoryListActivity::class.java)
    }
}