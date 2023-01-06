package com.anod.appwatcher.search

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.anod.appwatcher.accounts.AccountSelectionDialog
import com.anod.appwatcher.accounts.AccountSelectionResult
import com.anod.appwatcher.compose.AppTheme
import com.anod.appwatcher.compose.BaseComposeActivity
import com.anod.appwatcher.compose.onCommonActivityAction
import info.anodsplace.framework.app.HingeDeviceLayout
import kotlinx.coroutines.launch

open class SearchComposeActivity : BaseComposeActivity() {
    val viewModel: SearchViewModel by viewModels(factoryProducer = {
        SearchViewModel.Factory(
            initialState = intentToState(intent, hingeDevice.layout.value),
        )
    })

    private lateinit var accountSelectionDialog: AccountSelectionDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        accountSelectionDialog = AccountSelectionDialog(this, viewModel.prefs)

        setContent {
            AppTheme(
                    theme = viewModel.prefs.theme
            ) {
                val screenState by viewModel.viewStates.collectAsState(initial = viewModel.viewState)
                SearchResultsScreen(
                    screenState = screenState,
                    onEvent = { viewModel.handleEvent(it) },
                    installedApps = viewModel.installedApps,
                    pagingDataFlow = { viewModel.pagingData },
                    viewActions = viewModel.viewActions,
                    onActivityAction = { onCommonActivityAction(it) }
                )
            }
        }

        lifecycleScope.launch {
            accountSelectionDialog.accountSelected.collect { result ->
                when (result) {
                    AccountSelectionResult.Canceled -> viewModel.handleEvent(SearchViewEvent.AccountSelectError(errorMessage = ""))
                    is AccountSelectionResult.Error -> viewModel.handleEvent(SearchViewEvent.AccountSelectError(errorMessage = result.errorMessage))
                    is AccountSelectionResult.Success -> viewModel.handleEvent(SearchViewEvent.AccountSelected(account = result.account))
                }
            }
        }

        lifecycleScope.launch {
            hingeDevice.layout.collect {
                viewModel.handleEvent(SearchViewEvent.SetWideLayout(it))
            }
        }
    }

    private fun intentToState(intent: Intent?, wideLayout: HingeDeviceLayout) = SearchViewState(
            wideLayout = wideLayout,
            searchQuery = intent?.getStringExtra(EXTRA_KEYWORD) ?: "",
            isPackageSearch = intent?.getBooleanExtra(EXTRA_PACKAGE, false) ?: false,
            initiateSearch = intent?.getBooleanExtra(EXTRA_EXACT, false) ?: false,
            isShareSource = intent?.getBooleanExtra(EXTRA_SHARE, false) ?: false,
            hasFocus = intent?.getBooleanExtra(EXTRA_FOCUS, false) ?: false
    )

    companion object {
        const val EXTRA_KEYWORD = "keyword"
        const val EXTRA_EXACT = "exact"
        const val EXTRA_SHARE = "share"
        const val EXTRA_FOCUS = "focus"
        const val EXTRA_PACKAGE = "package"
    }
}