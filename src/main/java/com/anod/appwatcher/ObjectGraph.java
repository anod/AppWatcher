package com.anod.appwatcher;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.NoCache;
import com.android.volley.toolbox.Volley;
import com.anod.appwatcher.accounts.AccountManager;
import com.anod.appwatcher.market.DeviceIdHelper;
import com.anod.appwatcher.utils.AppIconLoader;
import com.anod.appwatcher.market.Network;
import com.google.android.gms.gcm.GcmNetworkManager;

/**
 * @author alex
 * @date 2015-02-22
 */
public class ObjectGraph {

    private final AppWatcherApplication app;
    private RequestQueue mRequestQueue;
    private String mDeviceId;
    private AccountManager mAccountManager;
    private AppIconLoader mIconLoader;

    public ObjectGraph(AppWatcherApplication application)  {
        this.app = application;
    }

    public AccountManager accountManager() {
        if (mAccountManager == null) {
            mAccountManager = new AccountManager(this.app);
        }
        return mAccountManager;
    }

    public String deviceId() {
        if (mDeviceId == null) {
            final Preferences prefs = new Preferences(this.app);
            mDeviceId = DeviceIdHelper.getDeviceId(this.app, prefs);
        }
        return mDeviceId;
    }

    public RequestQueue requestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = new RequestQueue(new NoCache(), new Network(), 2);
            mRequestQueue.start();
        }
        return mRequestQueue;
    }

    public AppIconLoader iconLoader()
    {
        if (mIconLoader == null)
        {
            mIconLoader = new AppIconLoader(this.app);
        }
        return mIconLoader;
    }

    public GcmNetworkManager gcmNetworkManager() {
        return GcmNetworkManager.getInstance(this.app);
    }

}
