package com.google.android.finsky.api.model;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.RequestFuture;

import java.util.concurrent.ExecutionException;

import info.anodsplace.android.log.AppLog;

/**
 * @author alex
 * @date 2015-02-23
 */
public abstract class DfeBaseModel<DocType> extends DfeModel implements Response.Listener<DocType> {

    public void startAsync() {
        execute(this, this);
    }

    public void startSync() {
        RequestFuture<DocType> future = RequestFuture.newFuture();

        execute(future, future);
        DocType response = null;
        try {
            response = future.get();
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause == null) {
                AppLog.e(e);
                onErrorResponse(new VolleyError("Response exception: " + e.getMessage(), e));
            } else {
                AppLog.e(cause);
                onErrorResponse(new VolleyError("Response exception: " + cause.getMessage(), cause));
            }
        } catch (InterruptedException e) {
            AppLog.e(e);
            onErrorResponse(new VolleyError("Response exception: "+e.getMessage(), e));
        }
        onResponse(response);
    }

    abstract protected void execute(Response.Listener<DocType> responseListener, Response.ErrorListener errorListener);

}
