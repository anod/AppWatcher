package com.anod.appwatcher.search;

import android.content.Context;

import com.anod.appwatcher.market.DetailsEndpoint;
import com.anod.appwatcher.model.WatchAppList;
import com.google.android.finsky.api.model.Document;

/**
 * @author algavris
 * @date 26/08/2016.
 */

public class ResultsAdapterDetails extends ResultsAdapter {
    private final DetailsEndpoint mDetailsEndpoint;

    public ResultsAdapterDetails(Context context, DetailsEndpoint detailsEndpoint, WatchAppList newAppHandler) {
        super(context, newAppHandler);
        mDetailsEndpoint = detailsEndpoint;
    }

    @Override
    public Document getDocument(int position) {
        return mDetailsEndpoint.getDocument();
    }

    @Override
    public int getItemCount() {
        return mDetailsEndpoint.getDocument() != null ? 1 : 0;
    }
}
