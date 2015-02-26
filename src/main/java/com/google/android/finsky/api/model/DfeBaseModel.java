package com.google.android.finsky.api.model;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.RequestFuture;
import com.anod.appwatcher.utils.AppLog;
import com.google.android.finsky.protos.Details;

import java.util.concurrent.ExecutionException;

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
        } catch (InterruptedException | ExecutionException e) {
            AppLog.ex(e);
            onErrorResponse(new VolleyError("Response exception: "+e.getMessage(), e));
        }
        onResponse(response);
    }

    abstract protected void execute(Response.Listener<DocType> responseListener, Response.ErrorListener errorListener);

}
