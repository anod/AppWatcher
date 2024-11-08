package com.anod.appwatcher.utils

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
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
                UpdateCheck.SYNC_PROGRESS -> trySendBlocking(SyncProgress(true, 0))
                UpdateCheck.SYNC_STOP -> {
                    val updatesCount = intent.getIntExtra(UpdateCheck.EXTRA_UPDATES_COUNT, 0)
                    trySendBlocking(SyncProgress(false, updatesCount))
                }
            }
        }
    }
    val filter = IntentFilter().apply {
        addAction(UpdateCheck.SYNC_PROGRESS)
        addAction(UpdateCheck.SYNC_STOP)
    }

    ContextCompat.registerReceiver(
        application,
        syncFinishedReceiver,
        filter,
        ContextCompat.RECEIVER_NOT_EXPORTED
    )

    awaitClose {
        application.unregisterReceiver(syncFinishedReceiver)
    }
}