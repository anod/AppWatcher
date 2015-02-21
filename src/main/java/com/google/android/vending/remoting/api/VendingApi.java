package com.google.android.vending.remoting.api;

import com.android.volley.RequestQueue;

public class VendingApi {
    private final VendingApiContext mApiContext;
    private final RequestQueue mRequestQueue;

    public VendingApi(RequestQueue paramRequestQueue, VendingApiContext paramVendingApiContext) {
        this.mRequestQueue = paramRequestQueue;
        this.mApiContext = paramVendingApiContext;
    }

    public VendingApiContext getApiContext() {
        return this.mApiContext;
    }


}

/* Location:           /Users/alex/Documents/android-tools/com.android.vending-5.0.31-80300031-minAPI9-dex2jar.jar
 * Qualified Name:     com.google.android.vending.remoting.api.VendingApi
 * JD-Core Version:    0.6.2
 */