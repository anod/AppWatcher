package com.anod.appwatcher.wishlist;

import android.content.Context;

import com.anod.appwatcher.market.WishlistEndpoint;
import com.anod.appwatcher.model.WatchAppList;
import com.anod.appwatcher.search.ResultsAdapter;
import com.google.android.finsky.api.model.Document;

/**
 * @author algavris
 * @date 16/12/2016.
 */

class ResultsAdapterWishlist extends ResultsAdapter {
    private final WishlistEndpoint mEngine;

    ResultsAdapterWishlist(Context context, WishlistEndpoint engine, WatchAppList newAppHandler) {
        super(context, newAppHandler);
        mEngine = engine;
    }

    @Override
    public Document getDocument(int position) {
        boolean isLastPosition = mEngine.getCount() - 1 == position;
        return mEngine.getData().getItem(position, isLastPosition);
    }

    @Override
    public int getItemCount() {
        return mEngine.getCount();
    }

}
