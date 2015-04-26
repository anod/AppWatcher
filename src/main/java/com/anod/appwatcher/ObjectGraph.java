package com.anod.appwatcher;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NoCache;
import com.anod.appwatcher.accounts.AccountManager;
import com.anod.appwatcher.market.DeviceIdHelper;
import com.anod.appwatcher.volley.LruBitmapCache;
import com.anod.appwatcher.volley.Network;

/**
 * @author alex
 * @date 2015-02-22
 */
public class ObjectGraph {

    private final AppWatcherApplication app;
    private RequestQueue mRequestQueue;
    private LruBitmapCache mCache;
    private ImageLoader mImageLoader;
    private String mDeviceId;
    private AccountManager mAccountManager;

    public ObjectGraph(AppWatcherApplication application)  {
        this.app = application;
    }

    public LruBitmapCache bitmapCache() {
        if (mCache == null) {
            mCache = new LruBitmapCache(this.app);
        }
        return mCache;
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
            mRequestQueue = new RequestQueue(new NoCache(), new Network(this.app), 2);
            mRequestQueue.start();
        }
        return mRequestQueue;
    }

    public ImageLoader imageLoader() {
        if (mImageLoader == null) {
            mImageLoader = new ImageLoader(requestQueue(), bitmapCache());
        }
        return mImageLoader;
    }

    public void reset() {
        if (mCache != null) {
            mCache.evictAll();
        }
        mCache = null;
        mImageLoader = null;
    }
}
