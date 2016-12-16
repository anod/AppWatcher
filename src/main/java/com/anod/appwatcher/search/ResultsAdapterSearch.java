package com.anod.appwatcher.search;

import android.content.Context;

import com.anod.appwatcher.market.SearchEndpoint;
import com.anod.appwatcher.model.WatchAppList;
import com.google.android.finsky.api.model.Document;

/**
 * @author algavris
 * @date 26/08/2016.
 */

public class ResultsAdapterSearch extends ResultsAdapter {
    private final SearchEndpoint mSearchEngine;

    public ResultsAdapterSearch(Context context, SearchEndpoint searchEngine, WatchAppList newAppHandler) {
        super(context, newAppHandler);
        mSearchEngine = searchEngine;
    }

    @Override
    public Document getDocument(int position) {
        boolean isLastPosition = mSearchEngine.getCount() - 1 == position;
        return mSearchEngine.getData().getItem(position, isLastPosition);
    }

    @Override
    public int getItemCount() {
        return mSearchEngine.getCount();
    }

}
