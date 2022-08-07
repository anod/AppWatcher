package com.anod.appwatcher.watchlist

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.SavedStateHandle
import androidx.work.Operation
import com.anod.appwatcher.accounts.AuthTokenBlocking
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class WatchListSharedState(
        val tag: Tag,
        val sortId: Int,
        val filterId: Int,
        val titleFilter: String = "",
        val listState: ListState? = null,
        val isWideLayout: Boolean = false
)

sealed interface WatchListSharedStateEvent {
    object OnBackPressed : WatchListSharedStateEvent
    class ChangeSort(val sortId: Int) : WatchListSharedStateEvent
    class FilterByTitle(val query: String) : WatchListSharedStateEvent
    class SetWideLayout(val wideLayout: Boolean) : WatchListSharedStateEvent
    class ListEvent(val event: WatchListEvent) : WatchListSharedStateEvent
    class FilterById(val filterId: Int) : WatchListSharedStateEvent
    class AddAppToTag(val tag: Tag) : WatchListSharedStateEvent
    class EditTag(val tag: Tag) : WatchListSharedStateEvent
    class OnSearch(val query: String) : WatchListSharedStateEvent
}

sealed interface WatchListSharedStateAction {
    object OnBackPressed : WatchListSharedStateAction
    class ListAction(val action: WatchListAction) : WatchListSharedStateAction
    class AddAppToTag(val tag: Tag) : WatchListSharedStateAction
    class EditTag(val tag: Tag) : WatchListSharedStateAction
    class OnSearch(val query: String) : WatchListSharedStateAction
}

class WatchListStateViewModel(state: SavedStateHandle) : BaseFlowViewModel<WatchListSharedState, WatchListSharedStateEvent, WatchListSharedStateAction>(), KoinComponent {
    private val authToken: AuthTokenBlocking by inject()
    private val application: Application by inject()

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
                tag = state[AppsTagViewModel.EXTRA_TAG] ?: Tag(0, "", 0),
                sortId = prefs.sortIndex,
                filterId = state["tab_id"] ?: Filters.TAB_ALL
        )
        val filter = IntentFilter().apply {
            addAction(UpdateCheck.syncProgress)
            addAction(UpdateCheck.syncStop)
            addAction(listChangedEvent)
        }
        application.registerReceiver(syncFinishedReceiver, filter)
    }

    override fun handleEvent(event: WatchListSharedStateEvent) {
        when (event) {
            is WatchListSharedStateEvent.ChangeSort -> {
                prefs.sortIndex = event.sortId
                viewState = viewState.copy(sortId = event.sortId)
            }
            is WatchListSharedStateEvent.FilterByTitle -> viewState = viewState.copy(titleFilter = event.query)
            is WatchListSharedStateEvent.SetWideLayout -> viewState = viewState.copy(isWideLayout = event.wideLayout)
            is WatchListSharedStateEvent.ListEvent -> {
                when (val listEvent = event.event) {
                    is WatchListEvent.ItemClick -> {
                        when (val item = listEvent.item) {
                            is SectionItem.Header -> emitAction(WatchListSharedStateAction.ListAction(WatchListAction.SectionHeaderClick(item.type)))
                            is SectionItem.App -> emitAction(WatchListSharedStateAction.ListAction(WatchListAction.ItemClick(item.appListItem.app, listEvent.index)))
                            is SectionItem.OnDevice -> emitAction(WatchListSharedStateAction.ListAction(WatchListAction.ItemClick(item.appListItem.app, listEvent.index)))
                            SectionItem.Empty -> {}
                            SectionItem.Recent -> {}
                        }
                    }
                    is WatchListEvent.ChangeSort -> {}
                    is WatchListEvent.EmptyButton -> {}
                    is WatchListEvent.FilterByTitle -> {}
                    WatchListEvent.Refresh -> {
                        val isRefreshing = (viewState.listState is ListState.SyncStarted)
                        if (!isRefreshing) {
                            val schedule = requestRefresh()
                            appScope.launch {
                                schedule.collect { }
                            }
                        }
                    }
                    is WatchListEvent.SetFilter -> {}
                }
            }
            is WatchListSharedStateEvent.AddAppToTag -> emitAction(WatchListSharedStateAction.AddAppToTag(event.tag))
            WatchListSharedStateEvent.OnBackPressed -> emitAction(WatchListSharedStateAction.OnBackPressed)
            is WatchListSharedStateEvent.FilterById -> {
                viewState = viewState.copy(filterId = event.filterId)
            }
            is WatchListSharedStateEvent.EditTag -> emitAction(WatchListSharedStateAction.EditTag(event.tag))
            is WatchListSharedStateEvent.OnSearch -> emitAction(WatchListSharedStateAction.OnSearch(event.query))
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