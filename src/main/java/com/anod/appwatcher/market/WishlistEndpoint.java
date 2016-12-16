package com.anod.appwatcher.market;

import android.content.Context;

import com.google.android.finsky.api.DfeApi;
import com.google.android.finsky.api.model.DfeList;
import com.google.android.finsky.api.model.DfeModel;

/**
 * @author algavris
 * @date 16/12/2016.
 */

public class WishlistEndpoint extends PlayStoreEndpointBase {
    private static String LIBRARY_ID = "u-wl";
    private static int BACKEND_ID = 0;

    public WishlistEndpoint(Context context) {
        super(context);
    }

    public DfeList getData() {
        return (DfeList)mDfeModel;
    }

    @Override
    public void reset() {
        if (getData() != null) {
            getData().resetItems();
            super.reset();
        }
    }

    public int getCount() {
        if (mDfeModel == null) {
            return 0;
        }
        return getData().getCount();
    }


    @Override
    protected void executeAsync() {
        getData().startLoadItems();
    }

    @Override
    protected void executeSync() {
        throw new UnsupportedOperationException("Not implemented");
    }


    @Override
    protected DfeModel createDfeModel() {
        return new DfeList(mDfeApi, mDfeApi.getLibraryUrl(BACKEND_ID, LIBRARY_ID, 7, null), true);
    }
}
