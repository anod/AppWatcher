package com.anod.appwatcher.watchlist

import android.accounts.Account
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import com.anod.appwatcher.R
import com.anod.appwatcher.accounts.AccountSelectionResult
import com.anod.appwatcher.accounts.AuthTokenBlocking
import com.anod.appwatcher.accounts.AuthTokenStartIntent
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.sync.SyncScheduler
import com.anod.appwatcher.upgrade.Upgrade15500
import com.anod.appwatcher.upgrade.UpgradeCheck
import com.anod.appwatcher.utils.BaseFlowViewModel
import com.anod.appwatcher.utils.account
import com.anod.appwatcher.utils.networkConnection
import com.anod.appwatcher.utils.prefs
import com.google.firebase.crashlytics.FirebaseCrashlytics
import info.anodsplace.applog.AppLog
import info.anodsplace.framework.util.Hash
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

typealias TagCountList = List<Pair<Tag, Int>>

data class MainViewState(
    val drawerItems: List<DrawerItem> = com.anod.appwatcher.watchlist.drawerItems,
    val account: Account? = null,
    val lastUpdate: Long = 0L,
    val tags: TagCountList = emptyList(),
    val showNewTagDialog: Boolean = false
)

sealed interface MainViewEvent {
    object ChooseAccount : MainViewEvent
    class AddNewTagDialog(val show: Boolean) : MainViewEvent
    class DrawerItemClick(val id: DrawerItem.Id) : MainViewEvent
    class SetAccount(val result: AccountSelectionResult) : MainViewEvent
    class NavigateToTag(val tag: Tag) : MainViewEvent
    class NotificationPermissionResult(val enabled: Boolean) : MainViewEvent
}

sealed interface MainViewAction {
    object ChooseAccount : MainViewAction
    class NavigateTo(val id: DrawerItem.Id) : MainViewAction
    class ShowToast(@StringRes val resId: Int = 0, val text: String = "", val length: Int = Toast.LENGTH_SHORT) : MainViewAction
    object RequestNotificationPermission : MainViewAction
    class StartActivity(val intent: Intent) : MainViewAction
    class NavigateToTag(val tag: Tag) : MainViewAction
}

class MainViewModel : BaseFlowViewModel<MainViewState, MainViewEvent, MainViewAction>(), KoinComponent {
    private val database: AppsDatabase by inject()
    private val context: Context by inject()

    val authToken: AuthTokenBlocking by inject()

    init {
        viewState = MainViewState(
            account = account,
            lastUpdate = prefs.lastUpdateTime
        )

        viewModelScope.launch {
            database.appTags().queryCounts()
                .combine(database.tags().observe()) { counts, tags ->
                    val tagCounts: Map<Int, Int> = counts.associate { Pair(it.tagId, it.count) }
                    val result: TagCountList = tags.map { Pair(it, tagCounts[it.id] ?: 0) }
                    result
                }.collect { tags ->
                    viewState = viewState.copy(tags = tags)
                }
        }
    }

    override fun handleEvent(event: MainViewEvent) {
        when (event) {
            is MainViewEvent.DrawerItemClick -> emitAction(MainViewAction.NavigateTo(event.id))
            is MainViewEvent.SetAccount -> {
                when (event.result) {
                    AccountSelectionResult.Canceled -> onAccountNotFound("")
                    is AccountSelectionResult.Error -> onAccountNotFound(event.result.errorMessage)
                    is AccountSelectionResult.Success -> {
                        viewState = viewState.copy(account = event.result.account)
                        onAccountSelect(event.result.account)
                    }
                }
            }

            is MainViewEvent.AddNewTagDialog -> {
                viewState = viewState.copy(showNewTagDialog = event.show)
            }

            is MainViewEvent.NavigateToTag -> emitAction(MainViewAction.NavigateToTag(tag = event.tag))
            MainViewEvent.ChooseAccount -> emitAction(MainViewAction.ChooseAccount)
            is MainViewEvent.NotificationPermissionResult -> onNotificationResult(event.enabled)
        }
    }

    private fun onNotificationResult(enabled: Boolean) {
        if (!enabled && prefs.notificationDisabledToastCount < 3) {
            emitAction(MainViewAction.ShowToast(
                resId = R.string.notifications_not_enabled,
                length = Toast.LENGTH_LONG
            ))
            prefs.notificationDisabledToastCount = prefs.notificationDisabledToastCount + 1
        }
        if (enabled) {
            if (prefs.useAutoSync) {
                viewModelScope.launch {
                    SyncScheduler(context)
                        .schedule(prefs.isRequiresCharging, prefs.isWifiOnly, prefs.updatesFrequency.toLong(), false)
                        .collect { }
                }
            }
        }
    }

    private fun onAccountSelect(account: Account) {
        val collectReports = prefs.collectCrashReports
        viewModelScope.launch {
            try {
                if (authToken.refreshToken(account)) {
                    if (collectReports) {
                        FirebaseCrashlytics.getInstance().setUserId(Hash.sha256(account.name).encoded)
                    }
                    if (!prefs.areNotificationsEnabled && prefs.updatesFrequency > 0) {
                        emitAction(MainViewAction.RequestNotificationPermission)
                    }
                    upgradeCheck()
                } else {
                    AppLog.e("Error retrieving authentication token")
                    onAccountNotFound("")
                }
            } catch (e: AuthTokenStartIntent) {
                emitAction(MainViewAction.StartActivity(e.intent))
            }
        }
    }

    private fun onAccountNotFound(errorMessage: String) {
        if (networkConnection.isNetworkAvailable) {
            if (errorMessage.isNotBlank()) {
                emitAction(MainViewAction.ShowToast(
                    text = errorMessage,
                    length = Toast.LENGTH_LONG
                ))
            } else {
                emitAction(MainViewAction.ShowToast(
                    resId = R.string.failed_gain_access,
                    length = Toast.LENGTH_LONG
                ))
            }
        } else {
            emitAction(MainViewAction.ShowToast(
                resId = R.string.check_connection,
                length = Toast.LENGTH_SHORT
            ))
        }
    }


    private fun upgradeCheck() {
        val upgrade = UpgradeCheck(prefs).result
        if (!upgrade.isNewVersion) {
            return
        }

        Upgrade15500().onUpgrade(upgrade)
    }

}