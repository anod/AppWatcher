package com.anod.appwatcher.utils


import android.content.Context
import android.os.AsyncTask

/**
 * @author algavris
 * *
 * @date 14/04/2017.
 */

object BackgroundTask {

    abstract class Worker<Param, Result> protected constructor(internal val param: Param) {
        abstract fun run(param: Param): Result
        abstract fun finished(result: Result)
    }

    fun <P, R> execute(worker: Worker<P, R>) {
        AsyncTaskRunner(worker).execute()
    }

    internal class AsyncTaskRunner<P, R>(private val worker: Worker<P, R>) : AsyncTask<Void, Void, R>() {

        override fun doInBackground(vararg params: Void): R {
            return this.worker.run(this.worker.param)
        }

        override fun onPostExecute(result: R) {
            this.worker.finished(result)
        }
    }

}