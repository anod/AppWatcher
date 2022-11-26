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
import com.anod.appwatcher.R
import com.anod.appwatcher.compose.AppTheme
import com.anod.appwatcher.compose.BaseComposeActivity
import com.anod.appwatcher.tags.TagsListFragment
import com.anod.appwatcher.utils.prefs
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
                authToken = intent.extras?.getString(EXTRA_AUTH_TOKEN) ?: ""
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
                WishListScreen(
                    screenState = screenState,
                    onEvent = { viewModel.handleEvent(it) },
                    installedApps = viewModel.installedApps,
                    pagingDataFlow = viewModel.pagingData,
                    viewActions = viewModel.viewActions,
                    onActivityAction = { onActivityAction(it) }
                )
            }
        }
    }

    private fun onActivityAction(action: WishListActivityAction) {
        when (action) {
            WishListActivityAction.OnBackPress -> onBackPressed()
            is WishListActivityAction.ShowTagList -> startActivity(TagsListFragment.intent(this, viewModel.prefs, action.info))
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