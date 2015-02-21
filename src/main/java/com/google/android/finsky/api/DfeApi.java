package com.google.android.finsky.api;

import android.net.Uri;

import com.android.volley.Request;
import com.android.volley.Response;
import com.google.android.finsky.protos.Search;

public interface DfeApi
{
    public static final Uri BASE_URI = Uri.parse("https://android.clients.google.com/fdfe/");
    public static final Uri SEARCH_CHANNEL_URI = Uri.parse("search");

    Request<?> search(String p0, Response.Listener<Search.SearchResponse> p1, Response.ErrorListener p2);
}

