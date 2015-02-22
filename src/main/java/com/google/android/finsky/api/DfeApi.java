package com.google.android.finsky.api;

import android.net.Uri;

import com.android.volley.Request;
import com.android.volley.Response;
import com.google.android.finsky.protos.Details;
import com.google.android.finsky.protos.Search;

import java.util.Collection;

public interface DfeApi
{
    public static final Uri BASE_URI = Uri.parse("https://android.clients.google.com/fdfe/");
    public static final Uri SEARCH_CHANNEL_URI = Uri.parse("search");
    public static final Uri BULK_DETAILS_URI = Uri.parse("bulkDetails");

    Request<?> search(String url, Response.Listener<Search.SearchResponse> responseListener, Response.ErrorListener errorListener);

    Request<?> getDetails(String url, boolean noPrefetch, boolean noBulkCancel, Response.Listener<Details.DetailsResponse> responseListener, Response.ErrorListener errorListener);

    Request<?> getDetails(final Collection<String> collection, final boolean includeDetails, final Response.Listener<Details.BulkDetailsResponse> listener, final Response.ErrorListener errorListener);

}

