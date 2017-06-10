package com.anod.appwatcher.utils


import android.content.Context
import android.os.AsyncTask

/**
 * @author algavris
 * *
 * @date 14/04/2017.
 */

object BackgroundTask {

    abstract class Worker<Param, Result> protected constructor(internal val param: Param, internal val context: Context) {

        abstract fun run(param: Param, context: Context): Result
        abstract fun finished(result: Result, context: Context)
    }

    fun <P, R> execute(worker: Worker<P, R>) {
        AsyncTaskRunner(worker, worker.context).execute()
    }

    internal class AsyncTaskRunner<P, R>(private val worker: Worker<P, R>, private val context: Context) : AsyncTask<Void, Void, R>() {

        override fun doInBackground(vararg params: Void): R {
            return this.worker.run(this.worker.param, this.context)
        }

        override fun onPostExecute(result: R) {
            this.worker.finished(result, this.context)
        }
    }

}