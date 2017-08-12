package com.google.android.finsky.api.model;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.RequestFuture;
import com.google.android.finsky.protos.nano.Messages;

import java.util.concurrent.ExecutionException;

import info.anodsplace.android.log.AppLog;

/**
 * @author alex
 * @date 2015-02-23
 */
public abstract class DfeBaseModel extends DfeModel implements Response.Listener<Messages.Response.ResponseWrapper> {

    public void startAsync() {
        execute(this, this);
    }

    public void startSync() {
        RequestFuture<Messages.Response.ResponseWrapper> future = RequestFuture.newFuture();

        execute(future, future);
        Messages.Response.ResponseWrapper response;
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
            return;
        } catch (InterruptedException e) {
            AppLog.e(e);
            onErrorResponse(new VolleyError("Response exception: "+e.getMessage(), e));
            return;
        }
        if (response == null) {
            onErrorResponse(new VolleyError("Response exception: Response is null"));
            return;
        }
        if (response.payload == null) {
            onErrorResponse(new VolleyError("Response exception: Payload is null"));
            return;
        }
        onResponse(response);
    }

    abstract protected void execute(Response.Listener<Messages.Response.ResponseWrapper> responseListener, Response.ErrorListener errorListener);

}
