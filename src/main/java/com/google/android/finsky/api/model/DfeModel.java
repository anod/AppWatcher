package com.google.android.finsky.api.model;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.HashSet;

public abstract class DfeModel implements Response.ErrorListener
{
    private HashSet<Response.ErrorListener> mErrorListeners;
    private HashSet<OnDataChangedListener> mListeners;
    private VolleyError mVolleyError;
    
    public DfeModel() {
        super();
        this.mListeners = new HashSet<OnDataChangedListener>();
        this.mErrorListeners = new HashSet<Response.ErrorListener>();
    }
    
    public final void addDataChangedListener(final OnDataChangedListener onDataChangedListener) {
        this.mListeners.add(onDataChangedListener);
    }
    
    public final void addErrorListener(final Response.ErrorListener errorListener) {
        this.mErrorListeners.add(errorListener);
    }
    
    protected void clearErrors() {
        this.mVolleyError = null;
    }
    
    public VolleyError getVolleyError() {
        return this.mVolleyError;
    }
    
    public boolean inErrorState() {
        return this.mVolleyError != null;
    }
    
    public abstract boolean isReady();
    
    protected void notifyDataSetChanged() {
        final OnDataChangedListener[] array = this.mListeners.toArray(new OnDataChangedListener[this.mListeners.size()]);
        for (int i = 0; i < array.length; ++i) {
            array[i].onDataChanged();
        }
    }
    
    protected void notifyErrorOccured(final VolleyError volleyError) {
        final Response.ErrorListener[] array = this.mErrorListeners.toArray(new Response.ErrorListener[this.mErrorListeners.size()]);
        for (int i = 0; i < array.length; ++i) {
            array[i].onErrorResponse(volleyError);
        }
    }
    
    @Override
    public void onErrorResponse(final VolleyError mVolleyError) {
        this.notifyErrorOccured(this.mVolleyError = mVolleyError);
    }
    
    public final void removeDataChangedListener(final OnDataChangedListener onDataChangedListener) {
        this.mListeners.remove(onDataChangedListener);
    }
    
    public final void removeErrorListener(final Response.ErrorListener errorListener) {
        this.mErrorListeners.remove(errorListener);
    }
    
    public final void unregisterAll() {
        this.mListeners.clear();
        this.mErrorListeners.clear();
    }
}
