package com.google.android.finsky.api.model;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.HashSet;

public abstract class DfeModel implements Response.ErrorListener
{
    private HashSet<Response.ErrorListener> errorListeners = new HashSet<>();
    private HashSet<OnDataChangedListener> listeners = new HashSet<>();
    private VolleyError volleyError;
    
    public DfeModel() {
        super();
    }
    
    public final void addDataChangedListener(final OnDataChangedListener onDataChangedListener) {
        this.listeners.add(onDataChangedListener);
    }
    
    public final void addErrorListener(final Response.ErrorListener errorListener) {
        this.errorListeners.add(errorListener);
    }
    
    protected void clearErrors() {
        this.volleyError = null;
    }
    
    public boolean inErrorState() {
        return this.volleyError != null;
    }
    
    public abstract boolean isReady();
    
    protected void notifyDataSetChanged() {
        final OnDataChangedListener[] array = this.listeners.toArray(new OnDataChangedListener[this.listeners.size()]);
        for (int i = 0; i < array.length; ++i) {
            array[i].onDataChanged();
        }
    }
    
    protected void notifyErrorOccured(final VolleyError volleyError) {
        final Response.ErrorListener[] array = this.errorListeners.toArray(new Response.ErrorListener[this.errorListeners.size()]);
        for (int i = 0; i < array.length; ++i) {
            array[i].onErrorResponse(volleyError);
        }
    }
    
    @Override
    public void onErrorResponse(final VolleyError mVolleyError) {
        this.notifyErrorOccured(this.volleyError = mVolleyError);
    }
    
    public final void removeDataChangedListener(final OnDataChangedListener onDataChangedListener) {
        this.listeners.remove(onDataChangedListener);
    }
    
    public final void removeErrorListener(final Response.ErrorListener errorListener) {
        this.errorListeners.remove(errorListener);
    }
    
    public final void unregisterAll() {
        this.listeners.clear();
        this.errorListeners.clear();
    }
}
