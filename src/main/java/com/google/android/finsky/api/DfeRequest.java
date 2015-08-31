package com.google.android.finsky.api;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.android.finsky.protos.Response;
import com.google.android.finsky.protos.ResponseMessages;
import com.google.android.finsky.utils.Utils;
import com.google.android.play.dfe.utils.NanoProtoHelper;
import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.google.protobuf.nano.MessageNano;
import com.google.protobuf.nano.MessageNanoPrinter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class DfeRequest<T extends MessageNano> extends Request<Response.ResponseWrapper>
{    private static final boolean SKIP_ALL_CACHES = false;

    private static final boolean PROTO_DEBUG;
    private boolean mAllowMultipleResponses;
    private final DfeApiContext mApiContext;
    private boolean mAvoidBulkCancel;
    private Map<String, String> mExtraHeaders;
    private com.android.volley.Response.Listener<T> mListener;
    private final Class<T> mResponseClass;
    private boolean mResponseDelivered;
    private DfeResponseVerifier mResponseVerifier;
    private long mServerLatencyMs;
    
    static {
        PROTO_DEBUG = Log.isLoggable("AppWatcher.DfeProto", 2);
    }
    
    public DfeRequest(final int method, final String s, final DfeApiContext mApiContext, final Class<T> mResponseClass, final com.android.volley.Response.Listener<T> mListener, final com.android.volley.Response.ErrorListener errorListener) {
        super(method, Uri.withAppendedPath(DfeApi.BASE_URI, s).toString(), errorListener);
        this.mAllowMultipleResponses = false;
        this.mServerLatencyMs = -1L;
        this.mAvoidBulkCancel = false;
        if (TextUtils.isEmpty((CharSequence)s)) {
            AppLog.e("Empty DFE URL");
        }
        this.setShouldCache(!SKIP_ALL_CACHES);
        this.setRetryPolicy(new DfeRetryPolicy(mApiContext));
        this.mApiContext = mApiContext;
        this.mListener = mListener;
        this.mResponseClass = mResponseClass;
    }
    
    public DfeRequest(final String url, final DfeApiContext dfeApiContext, final Class<T> clazz, final com.android.volley.Response.Listener<T> listener, final com.android.volley.Response.ErrorListener errorListener) {
        this(Method.GET, url, dfeApiContext, clazz, listener, errorListener);
    }
    
    private String getSignatureResponse(final NetworkResponse networkResponse) {
        return networkResponse.headers.get("X-DFE-Signature-Response");
    }
    
    private com.android.volley.Response<Response.ResponseWrapper> handleServerCommands(final Response.ResponseWrapper responseWrapper) {
        if (responseWrapper.commands != null) {
            final ResponseMessages.ServerCommands commands = responseWrapper.commands;
            if (commands.hasLogErrorStacktrace) {
                AppLog.d("%s", commands.logErrorStacktrace);
            }
            if (commands.clearCache) {
              //  this.mApiContext.getCache().clear();
            }
            if (commands.hasDisplayErrorMessage) {
                return com.android.volley.Response.error(new DfeServerError(commands.displayErrorMessage));
            }
        }
        return null;
    }
    
    private void logProtoResponse(final Response.ResponseWrapper responseWrapper) {
        final String s = ".*";
        if (this.getUrl().matches(s)) {
            synchronized (MessageNanoPrinter.class) {
                Log.v("DfeProto", "{ response: \"" + this.getUrl()+"\".\n");
                final String[] split = MessageNanoPrinter.print(responseWrapper).split("\n");
                for (int length = split.length, i = 0; i < length; ++i) {
                    Log.v("DfeProto", split[i]);
                }
                Log.v("DfeProto", "}");
                return;
            }
        }
        Log.v("DfeProto", "Url does not match regexp: url=" + this.getUrl() + " / regexp=" + s);
    }
    
    private String makeCacheKey(final String s) {
        return new StringBuilder(256).append(s).append("/account=").append(this.mApiContext.getAccountName()).toString();
    }
    
    public static Cache.Entry parseCacheHeaders(final NetworkResponse networkResponse) {
        final Cache.Entry cacheHeaders = HttpHeaderParser.parseCacheHeaders(networkResponse);
        if (cacheHeaders == null) {
            return null;
        }
        final long currentTimeMillis = System.currentTimeMillis();
        try {
            final String s = networkResponse.headers.get("X-DFE-Soft-TTL");
            if (s != null) {
                cacheHeaders.softTtl = currentTimeMillis + Long.parseLong(s);
            }
            final String s2 = networkResponse.headers.get("X-DFE-Hard-TTL");
            if (s2 != null) {
                cacheHeaders.ttl = currentTimeMillis + Long.parseLong(s2);
            }
            cacheHeaders.ttl = Math.max(cacheHeaders.ttl, cacheHeaders.softTtl);
        }
        catch (NumberFormatException ex) {
            AppLog.d("Invalid TTL: %s", networkResponse.headers);
            cacheHeaders.softTtl = 0L;
            cacheHeaders.ttl = 0L;
        }
        return cacheHeaders;
    }
    
    private Response.ResponseWrapper parseWrapperAndVerifyFromBytes(final NetworkResponse networkResponse, final String s) throws InvalidProtocolBufferNanoException, DfeResponseVerifier.DfeResponseVerifierException {
        final Response.ResponseWrapper from = Response.ResponseWrapper.parseFrom(networkResponse.data);
        if (this.mResponseVerifier != null) {
            this.mResponseVerifier.verify(networkResponse.data, s);
            this.addMarker("signature-verification-succeeded");
        }
        return from;
    }
    
    private Response.ResponseWrapper parseWrapperAndVerifySignature(final NetworkResponse networkResponse, final boolean gzip) {
        try {
            final String signatureResponse = this.getSignatureResponse(networkResponse);
            if (gzip) {
                return this.parseWrapperAndVerifySignatureFromIs(new GZIPInputStream(new ByteArrayInputStream(networkResponse.data)), signatureResponse);
            }
            return this.parseWrapperAndVerifyFromBytes(networkResponse, signatureResponse);
        }
        catch (InvalidProtocolBufferNanoException ex2) {
//            if (!gzip) {
//                return this.parseWrapperAndVerifySignature(networkResponse, true);
//            }
            AppLog.d("Cannot parse response as ResponseWrapper proto.");
        }
        catch (IOException ex3) {
            AppLog.w("IOException while manually unzipping request.");
        }
        catch (DfeResponseVerifier.DfeResponseVerifierException ex) {
            this.addMarker("signature-verification-failed");
            AppLog.e("Could not verify request: %s, exception %s", this, ex);

        }
        return null;
    }
    
    private Response.ResponseWrapper parseWrapperAndVerifySignatureFromIs(final InputStream inputStream, final String s) throws IOException, DfeResponseVerifier.DfeResponseVerifierException {
        final byte[] bytes = Utils.readBytes(inputStream);
        final Response.ResponseWrapper from = Response.ResponseWrapper.parseFrom(bytes);
        if (this.mResponseVerifier != null) {
            this.mResponseVerifier.verify(bytes, s);
        }
        return from;
    }
    
    public void addExtraHeader(final String s, final String s2) {
        if (this.mExtraHeaders == null) {
            this.mExtraHeaders = new HashMap<String, String>();
        }
        this.mExtraHeaders.put(s, s2);
    }
    
    @Override
    public void deliverError(final VolleyError volleyError) {
        if (!this.mResponseDelivered) {
            super.deliverError(volleyError);
            return;
        }
        AppLog.d("Not delivering error response for request=[%s], error=[%s] because response already delivered.", this, volleyError);
    }
    
    public void deliverResponse(final Response.ResponseWrapper responseWrapper) {
        MessageNano parsedResponseFromWrapper;
        try {
            parsedResponseFromWrapper = NanoProtoHelper.getParsedResponseFromWrapper(responseWrapper.payload, Response.Payload.class, this.mResponseClass);
        }
        catch (Exception ex) {
            AppLog.e("Null wrapper parsed for request=[%s]", this);
            this.deliverError(new ParseError(ex));
            return;
        }

        if (parsedResponseFromWrapper == null) {
            AppLog.e("Null parsed response for request=[%s]", this);
            this.deliverError(new VolleyError());
            return;
        }
        if (this.mAllowMultipleResponses || !this.mResponseDelivered) {
            this.mListener.onResponse((T)parsedResponseFromWrapper);
            this.mResponseDelivered = true;
        } else {
            AppLog.d("Not delivering second response for request=[%s]", this);
        }
    }
    
    public boolean getAvoidBulkCancel() {
        return this.mAvoidBulkCancel;
    }
    
    @Override
    public String getCacheKey() {
        return this.makeCacheKey(super.getUrl());
    }
    
    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        final Map<String, String> headers = this.mApiContext.getHeaders();
        if (this.mExtraHeaders != null) {
            headers.putAll(this.mExtraHeaders);
        }
        if (this.mResponseVerifier == null) {
            return headers;
        }
        try {
            headers.put("X-DFE-Signature-Request", this.mResponseVerifier.getSignatureRequest());
            final RetryPolicy retryPolicy = this.getRetryPolicy();
            String s = "timeoutMs=" + retryPolicy.getCurrentTimeout();
            final int currentRetryCount = retryPolicy.getCurrentRetryCount();
            if (currentRetryCount > 0) {
                s = s + "; retryAttempt=" + currentRetryCount;
            }
            headers.put("X-DFE-Request-Params", s);
        }
        catch (DfeResponseVerifier.DfeResponseVerifierException ex) {
            AppLog.d("Couldn't create signature request: %s", ex);
            this.cancel();
        }
        return headers;
    }
    
    public long getServerLatencyMs() {
        return this.mServerLatencyMs;
    }
    
    @Override
    public String getUrl() {
        char c = '&';
        String s = super.getUrl();
//        final String s2 = DfeApiConfig.ipCountryOverride.get();
//        if (!TextUtils.isEmpty((CharSequence)s2)) {
//            final StringBuilder append = new StringBuilder().append(s);
//            char c2;
//            if (s.indexOf(63) != -1) {
//                c2 = c;
//            }
//            else {
//                c2 = '?';
//            }
//            s = append.append(c2).toString() + "ipCountryOverride=" + s2;
//        }
//        final String s3 = DfeApiConfig.mccMncOverride.get();
//        if (!TextUtils.isEmpty((CharSequence)s3)) {
//            final StringBuilder append2 = new StringBuilder().append(s);
//            char c3;
//            if (s.indexOf(63) != -1) {
//                c3 = c;
//            }
//            else {
//                c3 = '?';
//            }
//            s = append2.append(c3).toString() + "mccmncOverride=" + s3;
//        }
        if (SKIP_ALL_CACHES) {
            final StringBuilder append3 = new StringBuilder().append(s);
            char c4;
            if (s.indexOf(63) != -1) {
                c4 = c;
            }
            else {
                c4 = '?';
            }
            s = append3.append(c4).toString() + "skipCache=true";
        }
//        if (DfeApiConfig.showStagingData.get()) {
//            final StringBuilder append4 = new StringBuilder().append(s);
//            char c5;
//            if (s.indexOf(63) != -1) {
//                c5 = c;
//            }
//            else {
//                c5 = '?';
//            }
//            s = append4.append(c5).toString() + "showStagingData=true";
//        }
//        if (DfeApiConfig.prexDisabled.get()) {
//            final StringBuilder append5 = new StringBuilder().append(s);
//            if (s.indexOf(63) == -1) {
//                c = '?';
//            }
//            s = append5.append(c).toString() + "p13n=false";
//        }
        return s;
    }
    
    public void handleNotifications(final Response.ResponseWrapper responseWrapper) {
//        if (this.mApiContext.getNotificationManager() != null && responseWrapper.notification.length != 0) {
//            final Notifications.Notification[] notification = responseWrapper.notification;
//            for (int length = notification.length, i = 0; i < length; ++i) {
//                this.mApiContext.getNotificationManager().processNotification(notification[i]);
//            }
//        }
    }
    
    @Override
    protected VolleyError parseNetworkError(VolleyError error) {
        if (error instanceof ServerError && error.networkResponse != null) {
            final Response.ResponseWrapper wrapperAndVerifySignature = this.parseWrapperAndVerifySignature(error.networkResponse, false);
            if (wrapperAndVerifySignature != null) {
                error = this.handleServerCommands(wrapperAndVerifySignature).error;
            }
        }
        return error;
    }
    
    public com.android.volley.Response<Response.ResponseWrapper> parseNetworkResponse(final NetworkResponse networkResponse) {
        if (AppLog.DEBUG) {
            final Map<String, String> headers = networkResponse.headers;
            int n = 0;
            if (headers != null) {
                final boolean containsKey = networkResponse.headers.containsKey("X-DFE-Content-Length");
                n = 0;
                if (containsKey) {
                    n = Integer.parseInt(networkResponse.headers.get("X-DFE-Content-Length")) / 1024;
                }
            }
            AppLog.v("Parsed response for url=[%s] contentLength=[%d KB]", this.getUrl(), n);
        }
        final Response.ResponseWrapper wrapperAndVerifySignature = this.parseWrapperAndVerifySignature(networkResponse, false);
        com.android.volley.Response<Response.ResponseWrapper> response;
        if (wrapperAndVerifySignature == null) {
            response = com.android.volley.Response.error(new ParseError(networkResponse));
        }
        else {
            if (DfeRequest.PROTO_DEBUG) {
                this.logProtoResponse(wrapperAndVerifySignature);
            }
            response = this.handleServerCommands(wrapperAndVerifySignature);
            if (response == null) {
                if (wrapperAndVerifySignature.serverMetadata != null) {
                    final ResponseMessages.ServerMetadata serverMetadata = wrapperAndVerifySignature.serverMetadata;
                    if (serverMetadata.hasLatencyMillis) {
                        this.mServerLatencyMs = serverMetadata.latencyMillis;
                    }
                }
                this.handleNotifications(wrapperAndVerifySignature);
                Cache.Entry cacheHeaders;
                if (this.mResponseVerifier != null) {
                    cacheHeaders = null;
                }
                else {
                    cacheHeaders = parseCacheHeaders(networkResponse);
                }
                if (cacheHeaders != null) {
                    this.stripForCache(wrapperAndVerifySignature, cacheHeaders);
                }
                final com.android.volley.Response<Response.ResponseWrapper> success = com.android.volley.Response.success(wrapperAndVerifySignature, cacheHeaders);
                AppLog.d("DFE response %s", this.getUrl());
                return success;
            }
        }
        return response;
    }
    
    public void setAllowMultipleResponses(final boolean mAllowMultipleResponses) {
        this.mAllowMultipleResponses = mAllowMultipleResponses;
    }
    
    public void setAvoidBulkCancel() {
        this.mAvoidBulkCancel = true;
    }
    
    public void setRequireAuthenticatedResponse(final DfeResponseVerifier mResponseVerifier) {
        this.mResponseVerifier = mResponseVerifier;
    }
    
    void stripForCache(final Response.ResponseWrapper responseWrapper, final Cache.Entry entry) {
//        if (responseWrapper.preFetch.length < 1 && responseWrapper.commands == null && responseWrapper.notification.length < 1) {
//            return;
//        }
//        //final Cache cache = this.mApiContext.getCache();
//        final long currentTimeMillis = System.currentTimeMillis();
//        for (final ResponseMessages.PreFetch preFetch2 : responseWrapper.preFetch) {
//            final Cache.Entry entry2 = new Cache.Entry();
//            entry2.data = preFetch2.response;
//            entry2.etag = preFetch2.etag;
//            entry2.serverDate = entry.serverDate;
//            entry2.ttl = currentTimeMillis + preFetch2.ttl;
//            entry2.softTtl = currentTimeMillis + preFetch2.softTtl;
//            //cache.put(this.makeCacheKey(Uri.withAppendedPath(DfeApi.BASE_URI, preFetch2.url).toString()), entry2);
//        }
//        responseWrapper.preFetch = ResponseMessages.PreFetch.emptyArray();
//        responseWrapper.commands = null;
//        //responseWrapper.notification = Notifications.Notification.emptyArray();
//        entry.data = MessageNano.toByteArray(responseWrapper);
    }
}
