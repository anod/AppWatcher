package com.google.android.finsky.api;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.google.android.finsky.protos.Search;

/**
 * @author alex
 * @date 2015-02-15
 */
public class DfeApiImpl implements DfeApi{
    private final DfeApiContext mApiContext;
    private final RequestQueue mQueue;

    public DfeApiImpl(final RequestQueue mQueue, final DfeApiContext mApiContext) {
        super();
        this.mQueue = mQueue;
        this.mApiContext = mApiContext;
    }

    @Override
    public Request<?> search(final String s, final Response.Listener<Search.SearchResponse> listener, final Response.ErrorListener errorListener) {
        return this.mQueue.add((Request<?>)new DfeRequest<Search.SearchResponse>(s, this.mApiContext, Search.SearchResponse.class, listener, errorListener));
    }
}
