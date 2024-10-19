package com.anod.appwatcher.watchlist

import android.accounts.Account
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import com.anod.appwatcher.R
import com.anod.appwatcher.accounts.AccountSelectionResult
import com.anod.appwatcher.accounts.AuthAccount
import com.anod.appwatcher.accounts.AuthAccountInitializer
import com.anod.appwatcher.accounts.AuthTokenBlocking
import com.anod.appwatcher.accounts.AuthTokenStartIntent
import info.anodsplace.framework.content.CommonActivityAction
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.sync.SyncScheduler
import com.anod.appwatcher.upgrade.UpgradeCheck
import com.anod.appwatcher.utils.BaseFlowViewModel
import com.anod.appwatcher.utils.networkConnection
import com.anod.appwatcher.utils.prefs
import com.google.android.gms.auth.api.Auth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import info.anodsplace.applog.AppLog
import info.anodsplace.ktx.Hash
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

typealias TagCountList = List<Pair<Tag, Int>>

@Immutable
data class MainViewState(
    val drawerItems: List<DrawerItem> = com.anod.appwatcher.watchlist.drawerItems,
    val account: AuthAccount? = null,
    val lastUpdate: Long = 0L,
    val tags: TagCountList = emptyList(),
    val showNewTagDialog: Boolean = false,
    val isDrawerOpen: Boolean = false
)

sealed interface MainViewEvent {
    data object ChooseAccount : MainViewEvent
    class AddNewTagDialog(val show: Boolean) : MainViewEvent
    class DrawerItemClick(val id: DrawerItem.Id) : MainViewEvent
    class SetAccount(val result: AccountSelectionResult) : MainViewEvent
    class NavigateToTag(val tag: Tag) : MainViewEvent
    class NotificationPermissionResult(val enabled: Boolean) : MainViewEvent
    class InitAccount(val account: Account) : MainViewEvent
    class DrawerState(val isOpen: Boolean) : MainViewEvent
}

sealed interface MainViewAction {
    class ActivityAction(val action: CommonActivityAction) : MainViewAction
    data object ChooseAccount : MainViewAction
    class NavigateTo(val id: DrawerItem.Id) : MainViewAction
    data object RequestNotificationPermission : MainViewAction
    class NavigateToTag(val tag: Tag) : MainViewAction
    class DrawerState(val isOpen: Boolean) : MainViewAction
}

private fun startActivityAction(intent: Intent, addMultiWindowFlags: Boolean = false) : MainViewAction.ActivityAction {
    return MainViewAction.ActivityAction(
        action = CommonActivityAction.StartActivity(
            intent = intent,
            addMultiWindowFlags = addMultiWindowFlags
        )
    )
}

private fun showToastAction(@StringRes resId: Int = 0, text: String = "", length: Int = Toast.LENGTH_SHORT) : MainViewAction.ActivityAction {
    return MainViewAction.ActivityAction(
        action = CommonActivityAction.ShowToast(
            resId = resId,
            text = text,
            length = length
        )
    )
}

class MainViewModel : BaseFlowViewModel<MainViewState, MainViewEvent, MainViewAction>(), KoinComponent {
    private val database: AppsDatabase by inject()
    private val context: Context by inject()
    private val authAccountInitializer: AuthAccountInitializer by inject()
    val authToken: AuthTokenBlocking by inject()

    init {
        viewState = MainViewState(
            account = prefs.account,
            lastUpdate = prefs.lastUpdateTime
        )

        viewModelScope.launch {
            database.appTags().queryCounts()
                .combine(
                    flow = database.tags().observe()
                ) { counts, tags ->
                    val tagCounts: Map<Int, Int> = counts.associate { Pair(it.tagId, it.count) }
                    val emptyCount = tagCounts[Tag.empty.id] ?: 0
                    val result: TagCountList = tags.map { Pair(it, tagCounts[it.id] ?: 0) }
                    if (tags.isNotEmpty() && emptyCount > 0) {
                        result.toMutableList().also {
                            it.add(0, Pair(Tag.empty, emptyCount) )
                        }
                    } else result
                }.collect { tags ->
                    viewState = viewState.copy(tags = tags)
                }
        }

        viewModelScope.launch {
            prefs.changes
                .filter { it == Preferences.LAST_UPDATE_TIME }
                .distinctUntilChanged()
                .collect {
                    viewState = viewState.copy(lastUpdate = prefs.lastUpdateTime)
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
            is MainViewEvent.InitAccount -> onAccountSelect(event.account)
            is MainViewEvent.DrawerState -> {
                viewState = viewState.copy(isDrawerOpen = event.isOpen)
                emitAction(MainViewAction.DrawerState(isOpen = event.isOpen))
            }
        }
    }

    private fun onNotificationResult(enabled: Boolean) {
        if (!enabled && prefs.notificationDisabledToastCount < 3) {
            emitAction(showToastAction(
                resId = R.string.notifications_not_enabled,
                length = Toast.LENGTH_LONG
            ))
            prefs.notificationDisabledToastCount = prefs.notificationDisabledToastCount + 1
        }
        if (enabled) {
            if (prefs.useAutoSync) {
                viewModelScope.launch {
                    scheduleRefresh()
                }
            }
        }
    }

    private fun onAccountSelect(account: Account) {
        val collectReports = prefs.collectCrashReports
        val initializer = authAccountInitializer
        viewModelScope.launch {
            try {
                val authAccount = initializer.initialize(account)
                viewState = viewState.copy(account = authAccount)
                if (collectReports) {
                    FirebaseCrashlytics.getInstance().setUserId(Hash.sha256(account.name).encoded)
                }
                if (!prefs.areNotificationsEnabled && prefs.updatesFrequency > 0) {
                    emitAction(MainViewAction.RequestNotificationPermission)
                }
                if (prefs.useAutoSync) {
                    scheduleRefresh()
                }
                upgradeCheck()
            } catch (e: AuthTokenStartIntent) {
                emitAction(startActivityAction(e.intent))
            } catch (e: Exception) {
                AppLog.e("Error retrieving authentication token")
                onAccountNotFound("")
            }
        }
    }

    private suspend fun scheduleRefresh() {
        SyncScheduler(context)
            .schedule(prefs.isRequiresCharging, prefs.isWifiOnly, prefs.updatesFrequency.toLong(), false)
            .collect { }
    }

    private fun onAccountNotFound(errorMessage: String) {
        if (networkConnection.isNetworkAvailable) {
            if (errorMessage.isNotBlank()) {
                emitAction(showToastAction(
                    text = errorMessage,
                    length = Toast.LENGTH_LONG
                ))
            } else {
                emitAction(showToastAction(
                    resId = R.string.failed_gain_access,
                    length = Toast.LENGTH_LONG
                ))
            }
        } else {
            emitAction(showToastAction(resId = R.string.check_connection))
        }
    }


    private fun upgradeCheck() {
        val result = UpgradeCheck(prefs).result
        if (!result.isNewVersion) {
            return
        }

        UpgradeCheck.upgrades.forEach { upgrade ->
            upgrade.onUpgrade(result)
        }
    }

}