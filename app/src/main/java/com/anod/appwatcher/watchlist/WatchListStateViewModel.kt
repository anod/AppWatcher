package com.anod.appwatcher.watchlist

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.graphics.Rect
import android.widget.Toast
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anod.appwatcher.MarketSearchActivity
import com.anod.appwatcher.R
import com.anod.appwatcher.accounts.AuthTokenBlocking
import com.anod.appwatcher.compose.CommonActivityAction
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.installed.InstalledActivity
import com.anod.appwatcher.sync.SyncScheduler
import com.anod.appwatcher.tags.AppsTagViewModel
import com.anod.appwatcher.utils.BaseFlowViewModel
import com.anod.appwatcher.utils.SyncProgress
import com.anod.appwatcher.utils.appScope
import com.anod.appwatcher.utils.forMyApps
import com.anod.appwatcher.utils.getInt
import com.anod.appwatcher.utils.networkConnection
import com.anod.appwatcher.utils.prefs
import com.anod.appwatcher.utils.syncProgressFlow
import info.anodsplace.applog.AppLog
import info.anodsplace.framework.app.HingeDeviceLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class WatchListSharedState(
        val tag: Tag,
        val sortId: Int,
        val filterId: Int,
        val titleFilter: String = "",
        val syncProgress: SyncProgress? = null,
        val wideLayout: HingeDeviceLayout = HingeDeviceLayout(isWideLayout = false, hinge = Rect()),
        val selectedApp: App? = null,
        val showAppTagDialog: Boolean = false,
        val showEditTagDialog: Boolean = false,
        val tagAppsChange: Int = 0,
        val expandSearch: Boolean = false,
        val dbAppsChange: Int = 0
)

sealed interface WatchListSharedStateEvent {
    object OnBackPressed : WatchListSharedStateEvent
    object PlayStoreMyApps : WatchListSharedStateEvent
    object Refresh : WatchListSharedStateEvent
    class ChangeSort(val sortId: Int) : WatchListSharedStateEvent
    class FilterByTitle(val query: String) : WatchListSharedStateEvent
    class SetWideLayout(val layout: HingeDeviceLayout) : WatchListSharedStateEvent
    class ListEvent(val event: WatchListEvent) : WatchListSharedStateEvent
    class FilterById(val filterId: Int) : WatchListSharedStateEvent
    class AddAppToTag(val show: Boolean) : WatchListSharedStateEvent
    class EditTag(val show: Boolean) : WatchListSharedStateEvent
    class OnSearch(val query: String) : WatchListSharedStateEvent
    class SelectApp(val app: App?) : WatchListSharedStateEvent
    class UpdateSyncProgress(val syncProgress: SyncProgress) : WatchListSharedStateEvent
}

private fun startActivityAction(intent: Intent, addMultiWindowFlags: Boolean = false) : CommonActivityAction.StartActivity {
    return CommonActivityAction.StartActivity(
        intent = intent,
        addMultiWindowFlags = addMultiWindowFlags
    )
}

private fun showToastAction(resId: Int = 0, text: String = "", length: Int = Toast.LENGTH_SHORT) : CommonActivityAction.ShowToast {
    return CommonActivityAction.ShowToast(
        resId = resId,
        text = text,
        length = length
    )
}

class WatchListStateViewModel(state: SavedStateHandle, defaultFilterId: Int, wideLayout: HingeDeviceLayout) : BaseFlowViewModel<WatchListSharedState, WatchListSharedStateEvent, CommonActivityAction>(), KoinComponent {
    private val authToken: AuthTokenBlocking by inject()
    private val application: Application by inject()
    private val db: AppsDatabase by inject()

    class Factory(
        private val defaultFilterId: Int,
        private val wideLayout: HingeDeviceLayout
    ) : AbstractSavedStateViewModelFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            key: String,
            modelClass: Class<T>,
            handle: SavedStateHandle
        ): T {
            return WatchListStateViewModel(
                state = handle,
                defaultFilterId = defaultFilterId,
                wideLayout = wideLayout
            ) as T
        }
    }

    init {
        val expandSearch = state.remove("expand_search") ?: false
        val fromNotification = state.remove("extra_noti") ?: false
        val filterId = if (fromNotification || expandSearch) defaultFilterId else state.getInt("tab_id", defaultFilterId)

        viewState = WatchListSharedState(
            tag = state[AppsTagViewModel.EXTRA_TAG] ?: Tag.empty,
            sortId = prefs.sortIndex,
            filterId = filterId,
            expandSearch = expandSearch,
            wideLayout = wideLayout
        )
        viewModelScope.launch {
            syncProgressFlow(application).collect {
                handleEvent(WatchListSharedStateEvent.UpdateSyncProgress(syncProgress = it))
            }
        }

        if (!viewState.tag.isEmpty) {
             viewModelScope.launch {
                db.tags()
                        .observeTag(viewState.tag.id)
                        .collect { tag ->
                            if (tag == null) {
                                emitAction(CommonActivityAction.Finish)
                            } else {
                                viewState = viewState.copy(tag = tag)
                            }
                        }
            }

            viewModelScope.launch {
                db.appTags()
                    .forTag(viewState.tag.id)
                    .drop(1) // skip initial load
                    .collect {
                        viewState = viewState.copy(tagAppsChange = viewState.tagAppsChange + 1)
                    }
            }
        }

        viewModelScope.launch {
            AppListTable.Queries.changes(db.apps())
                .drop(1)
                .map { (System.currentTimeMillis() / 1000).toInt() }
                .filter { it > 0 }
                .onEach { delay(600) }
                .flowOn(Dispatchers.Default)
                .collect {
                    viewState = viewState.copy(dbAppsChange = viewState.dbAppsChange + 1)
                }
        }
    }

    override fun handleEvent(event: WatchListSharedStateEvent) {
        when (event) {
            is WatchListSharedStateEvent.ChangeSort -> {
                prefs.sortIndex = event.sortId
                viewState = viewState.copy(sortId = event.sortId)
            }
            is WatchListSharedStateEvent.FilterByTitle -> viewState = viewState.copy(titleFilter = event.query)
            is WatchListSharedStateEvent.SetWideLayout -> viewState = viewState.copy(wideLayout = event.layout)
            is WatchListSharedStateEvent.ListEvent -> {
                handleListEvent(event.event)
            }
            is WatchListSharedStateEvent.AddAppToTag -> viewState = viewState.copy(showAppTagDialog = event.show)
            WatchListSharedStateEvent.OnBackPressed -> {
                if (viewState.wideLayout.isWideLayout && viewState.selectedApp != null) {
                    viewState = viewState.copy(selectedApp = null)
                } else {
                    emitAction(CommonActivityAction.OnBackPressed)
                }
            }
            is WatchListSharedStateEvent.FilterById -> {
                viewState = viewState.copy(filterId = event.filterId)
            }
            is WatchListSharedStateEvent.EditTag -> viewState = viewState.copy(showEditTagDialog = event.show)
            is WatchListSharedStateEvent.OnSearch -> emitAction(startActivityAction(
                intent = MarketSearchActivity.intent(application, event.query, true)
            ))
            is WatchListSharedStateEvent.SelectApp -> {
                viewState = viewState.copy(selectedApp = event.app)
            }
            is WatchListSharedStateEvent.UpdateSyncProgress -> {
                viewState = viewState.copy(syncProgress = event.syncProgress)
                if (event.syncProgress.isRefreshing) {
                    emitAction(showToastAction(resId = R.string.refresh_scheduled))
                } else {
                    if (event.syncProgress.updatesCount == 0) {
                        emitAction(showToastAction(resId = R.string.no_updates_found))
                    }
                }
            }
            WatchListSharedStateEvent.PlayStoreMyApps -> emitAction(startActivityAction(
                intent = Intent().forMyApps(true),
                addMultiWindowFlags = true
            ))
            WatchListSharedStateEvent.Refresh -> refresh()
        }
    }

    private fun handleListEvent(listEvent: WatchListEvent) {
        when (listEvent) {
            is WatchListEvent.AppClick -> {
                viewState = viewState.copy(selectedApp = listEvent.app)
            }
            is WatchListEvent.EmptyButton -> {
                when (listEvent.idx) {
                    1 -> emitAction(startActivityAction(
                        intent = MarketSearchActivity.intent(application, "", true)
                    ))
                    2 -> emitAction(startActivityAction(
                        intent = InstalledActivity.intent(importMode = true, application)
                    ))
                    3 -> emitAction(startActivityAction(
                        intent = Intent.makeMainActivity(ComponentName("com.android.vending", "com.android.vending.AssetBrowserActivity"))
                    ))
                }
            }
            WatchListEvent.Refresh -> refresh()
            is WatchListEvent.FilterByTitle -> {}
            is WatchListEvent.AppLongClick -> {}
            is WatchListEvent.SectionHeaderClick -> {
                when (listEvent.type) {
                    SectionHeader.RecentlyInstalled -> emitAction(startActivityAction(
                        intent = InstalledActivity.intent(importMode = false, application)
                    ))
                    else -> { }
                }
            }
        }
    }

    private fun refresh() {
        val isRefreshing = viewState.syncProgress?.isRefreshing == true
        if (!isRefreshing) {
            appScope.launch {
                try {
                    requestRefresh()
                } catch (e: Exception) {
                    if (networkConnection.isNetworkAvailable) {
                        emitAction(CommonActivityAction.ShowToast(
                            resId = R.string.failed_gain_access,
                            length = Toast.LENGTH_SHORT
                        ))
                    } else {
                        emitAction(CommonActivityAction.ShowToast(
                            resId = R.string.check_connection,
                            length = Toast.LENGTH_SHORT
                        ))
                    }
                    viewState = viewState.copy(syncProgress = null)
                }
            }
        }
    }

    private suspend fun requestRefresh() {
        AppLog.d("Refresh requested")
        if (!authToken.isFresh) {
            val account = prefs.account ?: throw IllegalStateException("account is null")
            authToken.refreshToken(account)
        }

        viewState = viewState.copy(syncProgress = SyncProgress(true, 0))
        SyncScheduler(application).execute().first()
    }
}