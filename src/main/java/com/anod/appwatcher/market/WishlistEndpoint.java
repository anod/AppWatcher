package com.anod.appwatcher.market;

import android.content.Context;

import com.anod.appwatcher.utils.CollectionsUtils;
import com.google.android.finsky.api.model.DfeList;
import com.google.android.finsky.api.model.DfeModel;
import com.google.android.finsky.api.model.Document;

/**
 * @author algavris
 * @date 16/12/2016.
 */

public class WishlistEndpoint extends PlayStoreEndpointBase {
    private static String LIBRARY_ID = "u-wl";
    private static int BACKEND_ID = 0;
    private final boolean mAutoloadNext;

    public WishlistEndpoint(Context context, boolean autoLoadNextPage) {
        super(context);
        mAutoloadNext = autoLoadNextPage;
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
        return new DfeList(mDfeApi, mDfeApi.getLibraryUrl(BACKEND_ID, LIBRARY_ID, 7, null), mAutoloadNext, new CollectionsUtils.Predicate<Document>() {
            @Override
            public boolean test(Document document) {
                return document.getAppDetails() == null;
            }
        });
    }
}
