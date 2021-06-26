package com.anod.appwatcher.watchlist

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.work.Operation
import com.anod.appwatcher.AppWatcherApplication
import com.anod.appwatcher.Application
import com.anod.appwatcher.sync.SyncScheduler
import com.anod.appwatcher.sync.UpdateCheck
import info.anodsplace.applog.AppLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * @author Alex Gavrishev
 * *
 * @date 18/03/2017.
 */
class WatchListStateViewModel(application: android.app.Application) : AndroidViewModel(application) {
    val titleFilter = MutableLiveData<String>()
    val sortId = MutableLiveData<Int>()
    val listState = MutableLiveData<ListState>()
    var isAuthenticated = false
    var isWideLayout = false

    /**
     * Receive notifications from UpdateCheck
     */
    private val syncFinishedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                UpdateCheck.syncProgress -> listState.value = SyncStarted
                UpdateCheck.syncStop -> {
                    val updatesCount = intent.getIntExtra(UpdateCheck.extrasUpdatesCount, 0)
                    listState.value = SyncStopped(updatesCount)
                }
                listChangedEvent -> listState.value = Updated
            }
        }
    }

    init {
        val filter = IntentFilter().apply {
            addAction(UpdateCheck.syncProgress)
            addAction(UpdateCheck.syncStop)
            addAction(listChangedEvent)
        }
        application.registerReceiver(syncFinishedReceiver, filter)
    }

    override fun onCleared() {
        app.unregisterReceiver(syncFinishedReceiver)
    }

    private val app: AppWatcherApplication
        get() = getApplication()

    fun requestRefresh(): Flow<Operation.State> {
        AppLog.d("Refresh requested")
        if (!isAuthenticated) {
            if (Application.provide(app).networkConnection.isNetworkAvailable) {
                this.listState.value = ShowAuthDialog
            } else {
                this.listState.value = NoNetwork
            }
            return flowOf()
        }

        this.listState.value = SyncStarted
        return SyncScheduler(app).execute()
    }

    companion object {
        const val listChangedEvent = "com.anod.appwatcher.list.changed"
    }
}