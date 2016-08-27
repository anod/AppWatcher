package com.anod.appwatcher.market;

import android.accounts.Account;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.finsky.api.model.DfeModel;
import com.google.android.finsky.api.model.OnDataChangedListener;

/**
 * @author algavris
 * @date 27/08/2016.
 */
public interface PlayStoreEndpoint {
    void setListener(Listener listener);

    String getAuthSubToken();

    PlayStoreEndpoint setAccount(Account account, String authSubToken);

    void startAsync();

    void startSync();

    void reset();

    interface Listener {
        void onDataChanged();
        void onErrorResponse(VolleyError error);
    }
}
