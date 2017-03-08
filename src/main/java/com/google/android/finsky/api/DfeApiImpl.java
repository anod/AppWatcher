package com.google.android.finsky.api;

import android.net.Uri;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.google.android.finsky.protos.nano.Messages.Details;
import com.google.android.finsky.protos.nano.Messages.ListResponse;
import com.google.android.finsky.protos.nano.Messages.Search;
import com.google.android.finsky.utils.NetworkType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * @author alex
 * @date 2015-02-15
 */
public class DfeApiImpl implements DfeApi{
    private static final float BULK_DETAILS_BACKOFF_MULT = 1.0f;
    private static final int BULK_DETAILS_MAX_RETRIES = 1;
    private static final int BULK_DETAILS_TIMEOUT_MS = 30000;

    private final DfeApiContext mApiContext;
    private final RequestQueue mQueue;

    public DfeApiImpl(final RequestQueue mQueue, final DfeApiContext mApiContext) {
        super();
        this.mQueue = mQueue;
        this.mApiContext = mApiContext;
    }

    @Override
    public Request<?> search(final String url, final Response.Listener<Search.SearchResponse> responseListener, final Response.ErrorListener errorListener) {
        return this.mQueue.add((Request<?>)new DfeRequest<>(url, this.mApiContext, Search.SearchResponse.class, responseListener, errorListener));
    }

    @Override
    public Request<?> getDetails(String url, boolean noPrefetch, boolean noBulkCancel, Response.Listener<Details.DetailsResponse> responseListener, Response.ErrorListener errorListener) {
        final DfeRequest dfeRequest = new DfeRequest<>(url, this.mApiContext, Details.DetailsResponse.class, responseListener, errorListener);
        if (noPrefetch) {
            dfeRequest.addExtraHeader("X-DFE-No-Prefetch", "true");
        }
        if (noBulkCancel) {
            dfeRequest.setAvoidBulkCancel();
        }
        return this.mQueue.add(dfeRequest);
    }

    @Override
    public Request<?> getDetails(final Collection<String> collection, final boolean includeDetails, final Response.Listener<Details.BulkDetailsResponse> listener, final Response.ErrorListener errorListener) {
        final Details.BulkDetailsRequest bulkDetailsRequest = new Details.BulkDetailsRequest();
        final ArrayList<String> list = new ArrayList<String>(collection);
        Collections.sort(list);
        bulkDetailsRequest.docid = list.toArray(new String[list.size()]);
        bulkDetailsRequest.includeDetails = includeDetails;
        final ProtoDfeRequest<Details.BulkDetailsResponse> dfeRequest = new ProtoDfeRequest<Details.BulkDetailsResponse>(
                DfeApiImpl.BULK_DETAILS_URI.toString(), bulkDetailsRequest, mApiContext, Details.BulkDetailsResponse.class, listener, errorListener)
        {
            private String computeDocumentIdHash() {
                long n = 0L;
                for(String item: list) {
                    n = 31L * n + item.hashCode();
                }
                return Long.toString(n);
            }

            @Override
            public String getCacheKey() {
                return super.getCacheKey() + "/docidhash=" + this.computeDocumentIdHash();
            }
        };
        dfeRequest.setShouldCache(true);
        dfeRequest.setRetryPolicy(new DfeRetryPolicy(DfeApiImpl.BULK_DETAILS_TIMEOUT_MS, DfeApiImpl.BULK_DETAILS_MAX_RETRIES, DfeApiImpl.BULK_DETAILS_BACKOFF_MULT, mApiContext));
        return this.mQueue.add((Request<?>)dfeRequest);
    }

    @Override
    public String getLibraryUrl(int c, String libraryId, int dt, byte[] serverToken) {
        final Uri.Builder appendQueryParameter = LIBRARY_URI.buildUpon()
                .appendQueryParameter("c", Integer.toString(c))
                .appendQueryParameter("dt", Integer.toString(dt))
                .appendQueryParameter("libid", libraryId);

        if (serverToken != null) {
            appendQueryParameter.appendQueryParameter("st", DfeUtils.base64Encode(serverToken));
        }
        return appendQueryParameter.toString();
    }

    @Override
    public Request<?> getList(String url, Response.Listener<ListResponse> listener, Response.ErrorListener errorListener) {
        final DfeRequest dfeRequest = new DfeRequest<>(url, this.mApiContext, ListResponse.class, listener, errorListener);
        addNetworkTypeToRequest(dfeRequest);
        return this.mQueue.add(dfeRequest);
    }

    private void addNetworkTypeToRequest(final DfeRequest<?> dfeRequest) {
        dfeRequest.addExtraHeader("X-DFE-Network-Type", Integer.toString(NetworkType.get(mApiContext.mContext)));
    }

}
