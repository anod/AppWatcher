package com.anod.appwatcher.market;

import com.android.volley.VolleyError;

/**
 * @author algavris
 * @date 27/08/2016.
 */

public class CompositeStateEndpoint extends CompositeEndpoint implements PlayStoreEndpoint.Listener {
    private int mActiveId = -1;
    protected final CompositeStateEndpoint.Listener mListener;

    public CompositeStateEndpoint(Listener listener) {
        mListener = listener;
    }

    @Override
    public void add(int id, PlayStoreEndpoint endpoint) {
        super.add(id, endpoint);
        if (mActiveId == -1)
        {
            mActiveId = id;
        }
        endpoint.setListener(this);
    }

    public PlayStoreEndpointBase active()
    {
        return (PlayStoreEndpointBase) get(mActiveId);
    }

    public CompositeStateEndpoint setActive(int id) {
        mActiveId = id;
        return this;
    }

    @Override
    public void setListener(PlayStoreEndpoint.Listener listener) {
        super.setListener(this);
    }

    @Override
    public void onDataChanged() {
        mListener.onDataChanged(mActiveId, active());
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        mListener.onErrorResponse(mActiveId, active(), error);
    }

    @Override
    public void startAsync() {
        active().startAsync();
    }

    @Override
    public void startSync() {
        active().startSync();
    }

    public interface Listener
    {
        void onDataChanged(int id, PlayStoreEndpointBase endpoint);
        void onErrorResponse(int id, PlayStoreEndpointBase endpoint, VolleyError error);
    }
}
