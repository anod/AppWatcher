package com.anod.appwatcher.watchlist

import android.app.Application
import android.graphics.Rect
import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import androidx.work.Operation
import com.anod.appwatcher.accounts.AuthTokenBlocking
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.AppTag
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.model.Filters
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.skip
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

sealed class ListState {
    object SyncStarted : ListState()
    class SyncStopped(val updatesCount: Int) : ListState()
    object Updated : ListState()
    object NoNetwork : ListState()
    object ShowAuthDialog : ListState()
}

data class WatchListSharedState(
        val tag: Tag,
        val sortId: Int,
        val filterId: Int,
        val titleFilter: String = "",
        val listState: ListState? = null,
        val wideLayout: HingeDeviceLayout = HingeDeviceLayout(isWideLayout = false, hinge = Rect()),
        val selectedApp: App? = null,
        val showAppTagDialog: Boolean = false,
        val showEditTagDialog: Boolean = false,
        val tagAppsChange: Int = 0,
        val expandSearch: Boolean = false
)

sealed interface WatchListSharedStateEvent {
    object OnBackPressed : WatchListSharedStateEvent
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
    object ImportInstalled : WatchListSharedStateAction
    object ShareFromStore : WatchListSharedStateAction
    object Dismiss : WatchListSharedStateAction
    class ExpandSection(val type: SectionHeader) : WatchListSharedStateAction
    class OpenApp(val app: App, val index: Int) : WatchListSharedStateAction
    class OnSearch(val query: String) : WatchListSharedStateAction
}

class WatchListStateViewModel(state: SavedStateHandle, defaultFilterId: Int, wideLayout: HingeDeviceLayout) : BaseFlowViewModel<WatchListSharedState, WatchListSharedStateEvent, WatchListSharedStateAction>(), KoinComponent {
    private val authToken: AuthTokenBlocking by inject()
    private val application: Application by inject()
    private val db: AppsDatabase by inject()

    class Factory(
        private val defaultFilterId: Int,
        private val wideLayout: HingeDeviceLayout,
        owner: SavedStateRegistryOwner,
        defaultArgs: Bundle?
    ) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
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
                viewState = if (event.syncProgress.isRefreshing) {
                    viewState.copy(listState = ListState.SyncStarted)
                } else {
                    viewState.copy(listState = ListState.SyncStopped(updatesCount = event.syncProgress.updatesCount))
                }
            }
        }
    }

    private fun handleListEvent(listEvent: WatchListEvent) {
        when (listEvent) {
            is WatchListEvent.ItemClick -> {
                val app = when (val item = listEvent.item) {
                    is SectionItem.Header -> {
                        emitAction(WatchListSharedStateAction.ExpandSection(item.type))
                        null
                    }
                    is SectionItem.App -> item.appListItem.app
                    is SectionItem.OnDevice -> item.appListItem.app
                    else -> null
                }
                if (app != null) {
                    if (viewState.wideLayout.isWideLayout) {
                        viewState = viewState.copy(selectedApp = app)
                    } else {
                        emitAction(WatchListSharedStateAction.OpenApp(app, listEvent.index))
                    }
                }
            }
            is WatchListEvent.EmptyButton -> {
                when (listEvent.idx) {
                    1 -> emitAction(WatchListSharedStateAction.SearchInStore)
                    2 -> emitAction(WatchListSharedStateAction.ImportInstalled)
                    3 -> emitAction(WatchListSharedStateAction.ShareFromStore)
                }
            }
            WatchListEvent.Refresh -> {
                val isRefreshing = (viewState.listState is ListState.SyncStarted)
                if (!isRefreshing) {
                    val schedule = requestRefresh()
                    appScope.launch {
                        schedule.collect { }
                    }
                }
            }
            is WatchListEvent.FilterByTitle -> {}
            WatchListEvent.Reload -> {}
            is WatchListEvent.ItemLongClick -> {}
        }
    }

    fun requestRefresh(): Flow<Operation.State> {
        AppLog.d("Refresh requested")
        if (!authToken.isFresh) {
            viewState = if (networkConnection.isNetworkAvailable) {
                viewState.copy(listState = ListState.ShowAuthDialog)
            } else {
                viewState.copy(listState = ListState.NoNetwork)
            }
            return flowOf()
        }

        viewState = viewState.copy(listState = ListState.SyncStarted)
        return SyncScheduler(application).execute()
    }
}