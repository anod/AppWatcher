package com.google.android.finsky.api;

import android.net.Uri;

import com.android.volley.Request;
import com.android.volley.Response;
import com.google.android.finsky.protos.nano.Messages;
import com.google.android.finsky.protos.nano.Messages.Details;
import com.google.android.finsky.protos.nano.Messages.ListResponse;
import com.google.android.finsky.protos.nano.Messages.Search;

import java.util.Collection;

public interface DfeApi
{
    Uri BASE_URI = Uri.parse("https://android.clients.google.com/fdfe/");
    Uri SEARCH_CHANNEL_URI = Uri.parse("search");
    Uri BULK_DETAILS_URI = Uri.parse("bulkDetails");
    Uri LIBRARY_URI = Uri.parse("library");

    Request search(String url, Response.Listener<Messages.Response.ResponseWrapper> responseListener, Response.ErrorListener errorListener);

    Request details(String url, boolean noPrefetch, boolean noBulkCancel, Response.Listener<Messages.Response.ResponseWrapper> responseListener, Response.ErrorListener errorListener);

    Request details(final Collection<String> collection, final boolean includeDetails, final Response.Listener<Messages.Response.ResponseWrapper> listener, final Response.ErrorListener errorListener);

    String createLibraryUrl(int c, String libraryId, int dt, byte[] serverToken);

    Request list(final String url, final Response.Listener<Messages.Response.ResponseWrapper> listener, final Response.ErrorListener errorListener);
}

