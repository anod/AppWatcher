package com.anod.appwatcher.userLog

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.anod.appwatcher.compose.BaseComposeActivity
import info.anodsplace.framework.content.startActivitySafely
import kotlinx.coroutines.launch

/**
 * @author Alex Gavrishev
 * @date 04/01/2018
 */
class UserLogActivity : BaseComposeActivity() {
    private val viewModel: UserLogViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val screenState by viewModel.viewStates.collectAsState(initial = viewModel.viewState)

            UserLogScreen(
                    screenState = screenState,
                    onEvent = { viewModel.handleEvent(it) }
            )
        }

        lifecycleScope.launch {
            viewModel.viewActions.collect { action ->
                when (action) {
                    UserLogAction.OnBackNav -> {
                        onBackPressedDispatcher.onBackPressed()
                    }
                    is UserLogAction.StartActivity -> {
                        startActivitySafely(action.intent)
                    }
                }
            }
        }
    }
}