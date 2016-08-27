package com.anod.appwatcher.market;

import android.accounts.Account;
import android.support.v4.util.SparseArrayCompat;

/**
 * @author algavris
 * @date 27/08/2016.
 */

public class CompositeEndpoint implements PlayStoreEndpoint {
    private SparseArrayCompat<PlayStoreEndpoint> mEndpoints = new SparseArrayCompat<>();

    public PlayStoreEndpoint get(int id) {
        return mEndpoints.get(id);
    }

    public void add(int id, PlayStoreEndpoint endpoint)
    {
        mEndpoints.put(id, endpoint);
    }

    public void clear()
    {
        mEndpoints = new SparseArrayCompat<>();
    }

    @Override
    public void setListener(Listener listener) {
        for (int i = 0; i < mEndpoints.size(); i++) {
            mEndpoints.valueAt(i).setListener(listener);
        }
    }

    @Override
    public String getAuthSubToken() {
        return mEndpoints.size() > 0 ? mEndpoints.valueAt(0).getAuthSubToken() : null;
    }

    @Override
    public PlayStoreEndpoint setAccount(Account account, String authSubToken) {
        for (int i = 0; i < mEndpoints.size(); i++) {
            mEndpoints.valueAt(i).setAccount(account, authSubToken);
        }
        return this;
    }

    @Override
    public void startAsync() {
        for (int i = 0; i < mEndpoints.size(); i++) {
            mEndpoints.valueAt(i).startAsync();
        }
    }

    @Override
    public void startSync() {
        for (int i = 0; i < mEndpoints.size(); i++) {
            mEndpoints.valueAt(i).startSync();
        }
    }

    @Override
    public void reset() {
        for (int i = 0; i < mEndpoints.size(); i++) {
            PlayStoreEndpoint endpoint = mEndpoints.valueAt(i);
            endpoint.reset();
        }
    }

}
