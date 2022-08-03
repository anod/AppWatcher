package com.anod.appwatcher.watchlist

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.work.Operation
import com.anod.appwatcher.accounts.AuthTokenBlocking
import com.anod.appwatcher.sync.SyncScheduler
import com.anod.appwatcher.sync.UpdateCheck
import com.anod.appwatcher.utils.BaseFlowViewModel
import com.anod.appwatcher.utils.networkConnection
import info.anodsplace.applog.AppLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class WatchListSharedState(
        val titleFilter: String = "",
        val sortId: Int = 0,
        val listState: ListState? = null,
        val isWideLayout: Boolean = false
)

sealed interface WatchListSharedStateEvent {
    class ChangeSort(val sortId: Int) : WatchListSharedStateEvent
    class FilterByTitle(val query: String) : WatchListSharedStateEvent
    class SetWideLayout(val wideLayout: Boolean) : WatchListSharedStateEvent
}

sealed interface WatchListSharedStateAction

class WatchListStateViewModel(private val application: Application) : BaseFlowViewModel<WatchListSharedState, WatchListSharedStateEvent, WatchListSharedStateAction>(), KoinComponent {
    private val authToken: AuthTokenBlocking by inject()

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return WatchListStateViewModel(application) as T
        }
    }

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
        viewState = WatchListSharedState()
        val filter = IntentFilter().apply {
            addAction(UpdateCheck.syncProgress)
            addAction(UpdateCheck.syncStop)
            addAction(listChangedEvent)
        }
        application.registerReceiver(syncFinishedReceiver, filter)
    }

    override fun handleEvent(event: WatchListSharedStateEvent) {
        when (event) {
            is WatchListSharedStateEvent.ChangeSort -> viewState = viewState.copy(sortId = event.sortId)
            is WatchListSharedStateEvent.FilterByTitle -> viewState = viewState.copy(titleFilter = event.query)
            is WatchListSharedStateEvent.SetWideLayout -> viewState = viewState.copy(isWideLayout = event.wideLayout)
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