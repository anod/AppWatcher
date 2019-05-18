package info.anodsplace.framework.os

import android.os.AsyncTask

/**
 * @author Alex Gavrishev
 * *
 * @date 14/04/2017.
 */

class BackgroundTask<P, R>(private val worker: Worker<P, R>) : AsyncTask<Void, Void, R>() {

    abstract class Worker<Param, Result> protected constructor(internal val param: Param) {
        abstract fun run(param: Param): Result
        abstract fun finished(result: Result)
    }

    override fun doInBackground(vararg params: Void): R {
        return this.worker.run(this.worker.param)
    }

    override fun onPostExecute(result: R) {
        this.worker.finished(result)
    }
}
