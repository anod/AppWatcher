package com.anod.appwatcher;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.ByteArrayPool;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NoCache;
import com.anod.appwatcher.utils.LruBitmapCache;
import com.google.android.volley.GoogleHttpClientStack;

import java.io.File;

/**
 * @author alex
 * @date 2015-02-22
 */
public class ObjectGraph {
    /** Default on-disk cache directory. */
    private static final String DEFAULT_CACHE_DIR = "volley";

    private final AppWatcherApplication app;
    private RequestQueue mRequestQueue;
    private LruBitmapCache mCache;
    private ImageLoader mImageLoader;

    public ObjectGraph(AppWatcherApplication application)  {
        this.app = application;
    }

    public LruBitmapCache bitmapCache() {
        if (mCache == null) {
            mCache = new LruBitmapCache(this.app);
        }
        return mCache;
    }

    public RequestQueue requestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = new RequestQueue(new NoCache(), createNetwork(), 2);
            mRequestQueue.start();
        }
        return mRequestQueue;
    }

    private Network createNetwork()
    {
        return new BasicNetwork(new GoogleHttpClientStack(this.app, false), new ByteArrayPool(1024 * 256));
    }

    public ImageLoader imageLoader() {
        if (mImageLoader == null) {
            mImageLoader = new ImageLoader(requestQueue(), bitmapCache());
        }
        return mImageLoader;
    }
}
