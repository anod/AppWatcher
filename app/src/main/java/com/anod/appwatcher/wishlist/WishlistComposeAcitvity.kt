package com.anod.appwatcher.wishlist

import android.accounts.Account
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.anod.appwatcher.R
import com.anod.appwatcher.compose.AppTheme
import com.anod.appwatcher.compose.BaseComposeActivity
import com.anod.appwatcher.compose.MainDetailScreen
import info.anodsplace.framework.content.onCommonActivityAction
import com.anod.appwatcher.details.DetailsDialog
import com.anod.appwatcher.utils.prefs
import com.anod.appwatcher.watchlist.DetailContent
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

/**
 * @author Alex Gavrishev
 * *
 * @date 16/12/2016.
 */
class WishListActivity : BaseComposeActivity(), KoinComponent {

    private val viewModel: WishListViewModel by viewModels(factoryProducer = {
        WishListViewModel.Factory(
            account = intent.extras?.getParcelable(EXTRA_ACCOUNT),
            authToken = intent.extras?.getString(EXTRA_AUTH_TOKEN) ?: "",
            wideLayout = hingeDevice.layout.value,
        )
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (viewModel.viewState.account == null || viewModel.viewState.authToken.isEmpty()) {
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
                            WishListScreen(
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
                                onDismissRequest = { viewModel.handleEvent(WishListEvent.SelectApp(app = null)) },
                                onCommonActivityAction = { onCommonActivityAction(it) }
                            )
                        }
                    )
                } else {
                    WishListScreen(
                        screenState = screenState,
                        onEvent = viewModel::handleEvent,
                        pagingDataFlow = viewModel.pagingData,
                        viewActions = viewModel.viewActions,
                        onActivityAction = { onCommonActivityAction(it) }
                    )
                    if (screenState.selectedApp != null) {
                        DetailsDialog(
                            app = screenState.selectedApp!!,
                            onDismissRequest = { viewModel.handleEvent(WishListEvent.SelectApp(app = null)) },
                            onCommonActivityAction = { onCommonActivityAction(it) }
                        )
                    }
                }
            }
        }

        lifecycleScope.launch {
            hingeDevice.layout.collect {
                viewModel.handleEvent(WishListEvent.SetWideLayout(it))
            }
        }
    }

    companion object {
        const val EXTRA_ACCOUNT = "extra_account"
        const val EXTRA_AUTH_TOKEN = "extra_auth_token"

        fun intent(context: Context, account: Account?, authToken: String?): Intent = Intent(context, WishListActivity::class.java).apply {
            putExtra(EXTRA_ACCOUNT, account)
            putExtra(EXTRA_AUTH_TOKEN, authToken)
        }
    }
}