package com.anod.appwatcher.watchlist

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.anod.appwatcher.AppWatcherApplication
import com.anod.appwatcher.Application
import com.anod.appwatcher.sync.SyncScheduler
import com.anod.appwatcher.sync.UpdateCheck
import info.anodsplace.framework.AppLog

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
        val filter = IntentFilter()
        filter.addAction(UpdateCheck.syncProgress)
        filter.addAction(UpdateCheck.syncStop)
        filter.addAction(listChangedEvent)
        application.registerReceiver(syncFinishedReceiver, filter)
    }

    override fun onCleared() {
        app.unregisterReceiver(syncFinishedReceiver)
    }

    private val app: AppWatcherApplication
        get() = getApplication()

    fun requestRefresh() {
        AppLog.d("Refresh requested")
        if (!isAuthenticated) {
            if (Application.provide(app).networkConnection.isNetworkAvailable) {
                this.listState.value = ShowAuthDialog
            } else {
                this.listState.value = NoNetwork
            }
            return
        }

        SyncScheduler(app).execute()
        this.listState.value = SyncStarted
    }

    companion object {
        const val listChangedEvent = "com.anod.appwatcher.list.changed"
    }
}