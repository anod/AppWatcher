package com.anod.appwatcher.watchlist

import android.accounts.Account
import com.anod.appwatcher.utils.BaseFlowViewModel

data class MainViewState(
    val account: Account? = null
)

sealed interface MainViewEvent

sealed interface MainViewAction

class MainViewModel : BaseFlowViewModel<MainViewState, MainViewEvent, MainViewAction>() {

    init {
        viewState = MainViewState()
    }

    override fun handleEvent(event: MainViewEvent) {
        when (event) {

            else -> {}
        }
    }

}