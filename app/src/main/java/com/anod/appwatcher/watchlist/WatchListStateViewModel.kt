package com.anod.appwatcher.watchlist

import android.app.Application
import android.graphics.Rect
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Operation
import com.anod.appwatcher.R
import com.anod.appwatcher.accounts.AuthTokenBlocking
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.sync.SyncScheduler
import com.anod.appwatcher.tags.AppsTagViewModel
import com.anod.appwatcher.utils.BaseFlowViewModel
import com.anod.appwatcher.utils.SyncProgress
import com.anod.appwatcher.utils.appScope
import com.anod.appwatcher.utils.getInt
import com.anod.appwatcher.utils.networkConnection
import com.anod.appwatcher.utils.prefs
import com.anod.appwatcher.utils.syncProgressFlow
import info.anodsplace.applog.AppLog
import info.anodsplace.framework.app.HingeDeviceLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOf
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

sealed interface WatchListSharedStateAction {
    object OnBackPressed : WatchListSharedStateAction
    object SearchInStore : WatchListSharedStateAction
    class Installed(val importMode: Boolean) : WatchListSharedStateAction
    object ShareFromStore : WatchListSharedStateAction
    object Dismiss : WatchListSharedStateAction
    object PlayStoreMyApps : WatchListSharedStateAction
    object ShowAccountsDialog : WatchListSharedStateAction
    class OpenApp(val app: App, val index: Int) : WatchListSharedStateAction
    class OnSearch(val query: String) : WatchListSharedStateAction
    class ShowToast(@StringRes val resId: Int = 0, val text: String = "", val length: Int = Toast.LENGTH_SHORT) : WatchListSharedStateAction
}

class WatchListStateViewModel(state: SavedStateHandle, defaultFilterId: Int, wideLayout: HingeDeviceLayout) : BaseFlowViewModel<WatchListSharedState, WatchListSharedStateEvent, WatchListSharedStateAction>(), KoinComponent {
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
                                emitAction(WatchListSharedStateAction.Dismiss)
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

//                        ViewModelProvider(this@WatchListActivity).get(DrawerViewModel::class.java)
//                            .refreshLastUpdateTime()
//                        ListState.SyncStopped

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
                    emitAction(WatchListSharedStateAction.OnBackPressed)
                }
            }
            is WatchListSharedStateEvent.FilterById -> {
                viewState = viewState.copy(filterId = event.filterId)
            }
            is WatchListSharedStateEvent.EditTag -> viewState = viewState.copy(showEditTagDialog = event.show)
            is WatchListSharedStateEvent.OnSearch -> emitAction(WatchListSharedStateAction.OnSearch(event.query))
            is WatchListSharedStateEvent.SelectApp -> {
                viewState = viewState.copy(selectedApp = event.app)
            }
            is WatchListSharedStateEvent.UpdateSyncProgress -> {
                viewState = viewState.copy(syncProgress = event.syncProgress)
                if (event.syncProgress.isRefreshing) {
                    emitAction(WatchListSharedStateAction.ShowToast(
                        resId = R.string.refresh_scheduled,
                        length = Toast.LENGTH_SHORT
                    ))
                } else {
                    if (event.syncProgress.updatesCount == 0) {
                        emitAction(
                            WatchListSharedStateAction.ShowToast(
                                resId = R.string.no_updates_found,
                                length = Toast.LENGTH_SHORT
                            )
                        )
                    }
                }
            }
            WatchListSharedStateEvent.PlayStoreMyApps -> emitAction(WatchListSharedStateAction.PlayStoreMyApps)
            WatchListSharedStateEvent.Refresh -> refresh()
        }
    }

    private fun handleListEvent(listEvent: WatchListEvent) {
        when (listEvent) {
            is WatchListEvent.AppClick -> {
                if (viewState.wideLayout.isWideLayout) {
                    viewState = viewState.copy(selectedApp = listEvent.app)
                } else {
                    emitAction(WatchListSharedStateAction.OpenApp(listEvent.app, listEvent.index))
                }
            }
            is WatchListEvent.EmptyButton -> {
                when (listEvent.idx) {
                    1 -> emitAction(WatchListSharedStateAction.SearchInStore)
                    2 -> emitAction(WatchListSharedStateAction.Installed(importMode = true))
                    3 -> emitAction(WatchListSharedStateAction.ShareFromStore)
                }
            }
            WatchListEvent.Refresh -> refresh()
            is WatchListEvent.FilterByTitle -> {}
            is WatchListEvent.AppLongClick -> {}
            is WatchListEvent.SectionHeaderClick -> {
                when (listEvent.type) {
                    SectionHeader.RecentlyInstalled -> emitAction(WatchListSharedStateAction.Installed(importMode = false))
                    else -> { }
                }
            }
        }
    }

    private fun refresh() {
        val isRefreshing = viewState.syncProgress?.isRefreshing == true
        if (!isRefreshing) {
            val schedule = requestRefresh()
            appScope.launch {
                schedule.collect { }
            }
        }
    }

    private fun requestRefresh(): Flow<Operation.State> {
        AppLog.d("Refresh requested")
        if (!authToken.isFresh) {
            if (networkConnection.isNetworkAvailable) {
                emitAction(WatchListSharedStateAction.ShowAccountsDialog)
            } else {
                emitAction(WatchListSharedStateAction.ShowToast(
                    resId = R.string.check_connection,
                    length = Toast.LENGTH_SHORT
                ))
            }
            viewState = viewState.copy(syncProgress = null)
            return flowOf()
        }

        viewState = viewState.copy(syncProgress = SyncProgress(true, 0))
        return SyncScheduler(application).execute()
    }
}