package com.anod.appwatcher.utils

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.anod.appwatcher.sync.UpdateCheck
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

data class SyncProgress(val isRefreshing: Boolean, val updatesCount: Int)

fun syncProgressFlow(application: Application): Flow<SyncProgress> = callbackFlow {
    val syncFinishedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                UpdateCheck.syncProgress -> trySendBlocking(SyncProgress(true, 0))
                UpdateCheck.syncStop ->  {
                    val updatesCount = intent.getIntExtra(UpdateCheck.extrasUpdatesCount, 0)
                    trySendBlocking(SyncProgress(false, updatesCount))
                }
            }
        }
    }
    val filter = IntentFilter().apply {
        addAction(UpdateCheck.syncProgress)
        addAction(UpdateCheck.syncStop)
    }
    application.registerReceiver(syncFinishedReceiver, filter)

    awaitClose {
        application.unregisterReceiver(syncFinishedReceiver)
    }
}