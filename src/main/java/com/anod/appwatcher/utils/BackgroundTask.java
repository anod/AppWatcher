package com.anod.appwatcher.utils;


import android.content.Context;
import android.os.AsyncTask;

/**
 * @author algavris
 * @date 14/04/2017.
 */

public class BackgroundTask {

    public abstract static class Worker<Param, Result>
    {
        final Param param;
        final Context context;

        protected Worker(Param param, Context context) {
            this.param = param;
            this.context = context;
        }

        public abstract Result run(Param param, Context context);
        public abstract void finished(Result result, Context context);
    }

    public static <P,R> void execute(Worker<P,R> worker)
    {
        (new AsyncTaskRunner<>(worker, worker.context)).execute();
    }

    static class AsyncTaskRunner<P,R> extends AsyncTask<Void, Void, R>
    {
        private final Worker<P,R> worker;
        private final Context context;
        AsyncTaskRunner(Worker<P, R> worker, Context context) {
            this.worker = worker;
            this.context = context;
        }

        @Override
        protected R doInBackground(Void... params) {
            return this.worker.run(this.worker.param, this.context);
        }

        @Override
        protected void onPostExecute(R result) {
            this.worker.finished(result, this.context);
        }
    }

}