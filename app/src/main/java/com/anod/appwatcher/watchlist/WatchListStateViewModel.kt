package com.anod.appwatcher.watchlist

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Rect
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.work.Operation
import com.anod.appwatcher.accounts.AuthTokenBlocking
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.model.Filters
import com.anod.appwatcher.sync.SyncScheduler
import com.anod.appwatcher.sync.UpdateCheck
import com.anod.appwatcher.tags.AppsTagViewModel
import com.anod.appwatcher.utils.BaseFlowViewModel
import com.anod.appwatcher.utils.appScope
import com.anod.appwatcher.utils.networkConnection
import com.anod.appwatcher.utils.prefs
import info.anodsplace.applog.AppLog
import info.anodsplace.framework.app.HingeDeviceLayout
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class WatchListSharedState(
        val tag: Tag,
        val sortId: Int,
        val filterId: Int,
        val titleFilter: String = "",
        val listState: ListState? = null,
        val wideLayout: HingeDeviceLayout = HingeDeviceLayout(isWideLayout = false, hinge = Rect()),
        val selectedApp: App? = null
)

sealed interface WatchListSharedStateEvent {
    object OnBackPressed : WatchListSharedStateEvent
    class ChangeSort(val sortId: Int) : WatchListSharedStateEvent
    class FilterByTitle(val query: String) : WatchListSharedStateEvent
    class SetWideLayout(val layout: HingeDeviceLayout) : WatchListSharedStateEvent
    class ListEvent(val event: WatchListEvent) : WatchListSharedStateEvent
    class FilterById(val filterId: Int) : WatchListSharedStateEvent
    class AddAppToTag(val tag: Tag) : WatchListSharedStateEvent
    class EditTag(val tag: Tag) : WatchListSharedStateEvent
    class OnSearch(val query: String) : WatchListSharedStateEvent
    class SelectApp(val app: App?) : WatchListSharedStateEvent
}

sealed interface WatchListSharedStateAction {
    object OnBackPressed : WatchListSharedStateAction
    object SearchInStore : WatchListSharedStateAction
    object ImportInstalled : WatchListSharedStateAction
    object ShareFromStore : WatchListSharedStateAction
    class ExpandSection(val type: SectionHeader) : WatchListSharedStateAction
    class OpenApp(val app: App, val index: Int) : WatchListSharedStateAction
    class AddAppToTag(val tag: Tag) : WatchListSharedStateAction
    class EditTag(val tag: Tag) : WatchListSharedStateAction
    class OnSearch(val query: String) : WatchListSharedStateAction
}

class WatchListStateViewModel(state: SavedStateHandle) : BaseFlowViewModel<WatchListSharedState, WatchListSharedStateEvent, WatchListSharedStateAction>(), KoinComponent {
    private val authToken: AuthTokenBlocking by inject()
    private val application: Application by inject()
    private val db: AppsDatabase by inject()

    /**
     * Receive notifications from UpdateCheck
     */
    private val syncFinishedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                UpdateCheck.syncProgress -> viewState = viewState.copy(listState = ListState.SyncStarted)
                UpdateCheck.syncStop -> {
                    val updatesCount = intent.getIntExtra(UpdateCheck.extrasUpdatesCount, 0)
                    viewState = viewState.copy(listState = ListState.SyncStopped(updatesCount))
                }
                listChangedEvent -> viewState = viewState.copy(listState = ListState.Updated)
            }
        }
    }

    init {
        viewState = WatchListSharedState(
                tag = state[AppsTagViewModel.EXTRA_TAG] ?: Tag.empty,
                sortId = prefs.sortIndex,
                filterId = state.get<Any?>("tab_id")?.let { tabId ->
                    (tabId as? Int) ?: (tabId as? String)?.toIntOrNull()
                } ?: Filters.TAB_ALL,
        )
        val filter = IntentFilter().apply {
            addAction(UpdateCheck.syncProgress)
            addAction(UpdateCheck.syncStop)
            addAction(listChangedEvent)
        }
        application.registerReceiver(syncFinishedReceiver, filter)

        viewModelScope.launch {
            if (!viewState.tag.isEmpty) {
                db.tags()
                        .observe()
                        .mapNotNull { list -> list.first { tag -> tag.id == viewState.tag.id } }
                        .collect { tag ->
                            viewState = viewState.copy(tag = tag)
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
                when (val listEvent = event.event) {
                    is WatchListEvent.ItemClick -> {
                        when (val item = listEvent.item) {
                            is SectionItem.Header -> emitAction(WatchListSharedStateAction.ExpandSection(item.type))
                            is SectionItem.App -> {
                                if (viewState.wideLayout.isWideLayout) {
                                    viewState = viewState.copy(selectedApp = item.appListItem.app)
                                } else {
                                    emitAction(WatchListSharedStateAction.OpenApp(item.appListItem.app, listEvent.index))
                                }
                            }
                            is SectionItem.OnDevice -> emitAction(WatchListSharedStateAction.OpenApp(item.appListItem.app, listEvent.index))
                            else -> {}
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
                }
            }
            is WatchListSharedStateEvent.AddAppToTag -> emitAction(WatchListSharedStateAction.AddAppToTag(event.tag))
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
            is WatchListSharedStateEvent.EditTag -> emitAction(WatchListSharedStateAction.EditTag(event.tag))
            is WatchListSharedStateEvent.OnSearch -> emitAction(WatchListSharedStateAction.OnSearch(event.query))
            is WatchListSharedStateEvent.SelectApp -> {
                viewState = viewState.copy(selectedApp = event.app)
            }
        }
    }

    override fun onCleared() {
        application.unregisterReceiver(syncFinishedReceiver)
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

    companion object {
        const val listChangedEvent = "com.anod.appwatcher.list.changed"
    }
}