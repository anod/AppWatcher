package com.anod.appwatcher.watchlist

import android.accounts.Account
import com.anod.appwatcher.accounts.AuthTokenBlocking
import com.anod.appwatcher.utils.BaseFlowViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class MainViewState(
    val navigationItems: List<DrawerNavigationItem> = drawerNavigationItems,
    val account: Account? = null,
    val lastUpdate: Long = 0L
)

sealed interface MainViewEvent {
    class NavigateTo(val id: DrawerNavigationItem.Id) : MainViewEvent
}

sealed interface MainViewAction {
    class NavigateTo(val id: DrawerNavigationItem.Id) : MainViewAction
}

class MainViewModel : BaseFlowViewModel<MainViewState, MainViewEvent, MainViewAction>(), KoinComponent {
    val authToken: AuthTokenBlocking by inject()

    init {
        viewState = MainViewState()
    }

    override fun handleEvent(event: MainViewEvent) {
        when (event) {
            is MainViewEvent.NavigateTo -> emitAction(MainViewAction.NavigateTo(event.id))
        }
    }

}