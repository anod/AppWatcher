// Copyright 2013 Google Inc. All Rights Reserved.

package com.anod.appwatcher.backup.gdrive

import android.os.AsyncTask
import android.os.Bundle
import android.util.Log

import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks

import java.util.concurrent.CountDownLatch

/**
 * An AsyncTask that maintains a connected client.
 */
abstract class ApiClientAsyncTask(protected val googleApiClient: GoogleApiClient)
    : AsyncTask<Void, Void, ApiClientAsyncTask.Result>() {

    class Result(var status: Boolean, var ex: Exception?)

    override fun doInBackground(vararg params: Void?): Result? {
        Log.d("TAG", "in background")
        val latch = CountDownLatch(1)
        googleApiClient.registerConnectionCallbacks(object : ConnectionCallbacks {
            override fun onConnectionSuspended(cause: Int) {}

            override fun onConnected(arg0: Bundle?) {
                latch.countDown()
            }
        })
        googleApiClient.registerConnectionFailedListener { latch.countDown() }
        googleApiClient.connect()
        try {
            latch.await()
        } catch (e: InterruptedException) {
            return Result(false, e)
        }

        if (!googleApiClient.isConnected) {
            return Result(false, null)
        }
        try {
            return doInBackgroundConnected()
        } finally {
            googleApiClient.disconnect()
        }
    }

    /**
     * Override this method to perform a computation on a background thread, while the client is
     * connected.
     */
    abstract fun doInBackgroundConnected(): ApiClientAsyncTask.Result
}