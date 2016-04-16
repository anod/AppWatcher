package com.google.android.volley;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Build;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.LayeredSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.impl.client.EntityEnclosingRequestWrapper;
import org.apache.http.impl.client.RequestWrapper;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class GoogleHttpClient implements HttpClient
{
    private final String mAppName;
    private final AndroidHttpClient mClient;
    private final ThreadLocal<Boolean> mConnectionAllocated;
    private final CookieSourceApplier mCookieSourceApplier;
    private final ContentResolver mResolver;
    
    public GoogleHttpClient(final Context context, final String s, final boolean b) {
        this(context, s, b, null);
    }
    
    public GoogleHttpClient(final Context context, final String mAppName, final boolean compressed, final PseudonymousCookieSource pseudonymousCookieSource) {
        super();
        this.mConnectionAllocated = new ThreadLocal<Boolean>();
        String s = mAppName + " (" + Build.DEVICE + " " + Build.ID + ")";
        if (compressed) {
            s += "; gzip";
        }
        this.mClient = AndroidHttpClient.newInstance(s, context);
        this.mCookieSourceApplier = new CookieSourceApplier(this.mClient, pseudonymousCookieSource);
        this.mResolver = context.getContentResolver();
        this.mAppName = mAppName;
        final SchemeRegistry schemeRegistry = this.getConnectionManager().getSchemeRegistry();
        for (final String s2 : schemeRegistry.getSchemeNames()) {
            final Scheme unregister = schemeRegistry.unregister(s2);
            final SocketFactory socketFactory = unregister.getSocketFactory();
            Object o;
            if (socketFactory instanceof LayeredSocketFactory) {
                o = new WrappedLayeredSocketFactory((LayeredSocketFactory)socketFactory);
            }
            else {
                o = new WrappedSocketFactory(socketFactory);
            }
            schemeRegistry.register(new Scheme(s2, (SocketFactory)o, unregister.getDefaultPort()));
        }
    }
    
    private static RequestWrapper wrapRequest(final HttpRequest httpRequest) throws IOException {
        try {
            RequestWrapper o;
            if (httpRequest instanceof HttpEntityEnclosingRequest) {
                o = new EntityEnclosingRequestWrapper((HttpEntityEnclosingRequest)httpRequest);
            }
            else {
                o = new RequestWrapper(httpRequest);
            }
            ((RequestWrapper)o).resetHeaders();
            return (RequestWrapper)o;
        }
        catch (ProtocolException ex) {
            throw new ClientProtocolException((Throwable)ex);
        }
    }
    
    private static RequestWrapper wrapRequest(final HttpUriRequest httpUriRequest) throws IOException {
        try {
            Object o;
            if (httpUriRequest instanceof HttpEntityEnclosingRequest) {
                o = new EntityEnclosingRequestWrapper((HttpEntityEnclosingRequest)httpUriRequest);
            }
            else {
                o = new RequestWrapper((HttpRequest)httpUriRequest);
            }
            ((RequestWrapper)o).resetHeaders();
            return (RequestWrapper)o;
        }
        catch (ProtocolException ex) {
            throw new ClientProtocolException((Throwable)ex);
        }
    }
    
    public void enableCurlLogging(final String tag, final int logLevel) {
        this.mClient.enableCurlLogging(tag, logLevel);
    }
    
    public <T> T execute(final HttpHost httpHost, final HttpRequest httpRequest, final ResponseHandler<? extends T> responseHandler) throws IOException, ClientProtocolException {
        return (T)this.mCookieSourceApplier.execute(httpHost, httpRequest, responseHandler);
    }
    
    public <T> T execute(final HttpHost httpHost, final HttpRequest httpRequest, final ResponseHandler<? extends T> responseHandler, final HttpContext httpContext) throws IOException, ClientProtocolException {
        return (T)this.mCookieSourceApplier.execute(httpHost, httpRequest, responseHandler, httpContext);
    }
    
    public <T> T execute(final HttpUriRequest httpUriRequest, final ResponseHandler<? extends T> responseHandler) throws IOException, ClientProtocolException {
        return (T)this.mCookieSourceApplier.execute(httpUriRequest, responseHandler);
    }
    
    public <T> T execute(final HttpUriRequest httpUriRequest, final ResponseHandler<? extends T> responseHandler, final HttpContext httpContext) throws IOException, ClientProtocolException {
        return (T)this.mCookieSourceApplier.execute(httpUriRequest, responseHandler, httpContext);
    }
    
    public HttpResponse execute(final HttpHost httpHost, final HttpRequest httpRequest) throws IOException {
        return this.mCookieSourceApplier.execute(httpHost, httpRequest);
    }
    
    public HttpResponse execute(final HttpHost httpHost, final HttpRequest httpRequest, final HttpContext httpContext) throws IOException {
        return this.mCookieSourceApplier.execute(httpHost, httpRequest, httpContext);
    }
    
    public HttpResponse execute(final HttpUriRequest httpUriRequest) throws IOException {
        return this.execute(httpUriRequest, (HttpContext)null);
    }
    
    public HttpResponse execute(final HttpUriRequest httpUriRequest, final HttpContext httpContext) throws IOException {
        final String string = httpUriRequest.getURI().toString();
//        final UrlRules.Rule matchRule = UrlRules.getRules(this.mResolver).matchRule(string);
//        final String apply = matchRule.apply(string);
//        if (apply == null) {
//            Log.w("GoogleHttpClient", "Blocked by " + matchRule.mName + ": " + string);
//            throw new BlockedRequestException(matchRule);
//        }
//        if (apply == string) {
            return this.executeWithoutRewriting(httpUriRequest, httpContext);
//        }
//        try {
//            final URI uri = new URI(apply);
//            final RequestWrapper wrapRequest = wrapRequest(httpUriRequest);
//            wrapRequest.setURI(uri);
//            return this.executeWithoutRewriting((HttpUriRequest)wrapRequest, httpContext);
//        }
//        catch (URISyntaxException ex) {
//            throw new RuntimeException("Bad URL from rule: " + matchRule.mName, ex);
//        }
    }
    
    public HttpResponse executeWithoutRewriting(final HttpUriRequest httpUriRequest, final HttpContext httpContext) throws IOException {
        return this.mCookieSourceApplier.execute(httpUriRequest, httpContext);
    }
    
    public ClientConnectionManager getConnectionManager() {
        return this.mClient.getConnectionManager();
    }
    
    public HttpParams getParams() {
        return this.mClient.getParams();
    }
    

    private final class CookieSourceApplier
    {
        private final AndroidHttpClient mClient;
        private final PseudonymousCookieSource mCookieSource;
        
        private CookieSourceApplier(final AndroidHttpClient mClient, final PseudonymousCookieSource mCookieSource) {
            super();
            this.mClient = mClient;
            this.mCookieSource = mCookieSource;
        }
        
        private <T> T execute(final HttpHost httpHost, final HttpRequest httpRequest, final ResponseHandler<T> responseHandler) throws IOException, ClientProtocolException {
            final RequestWrapper access$1100 = wrapRequest(httpRequest);
            return (T)this.mClient.execute(httpHost, PseudonymousCookieSource.Helper.setRequestCookie(access$1100, this.mCookieSource), (org.apache.http.client.ResponseHandler<?>)new SetCookie((ResponseHandler)responseHandler, (HttpUriRequest)access$1100, this.mCookieSource));
        }
        
        private <T> T execute(final HttpHost httpHost, final HttpRequest httpRequest, final ResponseHandler<T> responseHandler, final HttpContext httpContext) throws IOException, ClientProtocolException {
            final RequestWrapper access$1100 = wrapRequest(httpRequest);
            return (T)this.mClient.execute(httpHost, (HttpRequest)PseudonymousCookieSource.Helper.setRequestCookie((HttpUriRequest)access$1100, this.mCookieSource), (org.apache.http.client.ResponseHandler<?>)new SetCookie((ResponseHandler)responseHandler, (HttpUriRequest)access$1100, this.mCookieSource), httpContext);
        }
        
        private <T> T execute(final HttpUriRequest httpUriRequest, final ResponseHandler<T> responseHandler) throws IOException {
            return this.mClient.execute(PseudonymousCookieSource.Helper.setRequestCookie(httpUriRequest, this.mCookieSource), (org.apache.http.client.ResponseHandler<? extends T>)new SetCookie((ResponseHandler)responseHandler, httpUriRequest, this.mCookieSource));
        }
        
        private <T> T execute(final HttpUriRequest httpUriRequest, final ResponseHandler<T> responseHandler, final HttpContext httpContext) throws IOException, ClientProtocolException {
            return this.mClient.execute(httpUriRequest, (org.apache.http.client.ResponseHandler<? extends T>)new SetCookie((ResponseHandler)responseHandler, httpUriRequest, this.mCookieSource), httpContext);
        }
        
        private HttpResponse execute(final HttpHost httpHost, final HttpRequest httpRequest) throws IOException {
            return PseudonymousCookieSource.Helper.updateFromResponseCookie((HttpUriRequest)wrapRequest(httpRequest), this.mClient.execute(httpHost, (HttpRequest)PseudonymousCookieSource.Helper.setRequestCookie((HttpUriRequest)wrapRequest(httpRequest), this.mCookieSource)), this.mCookieSource);
        }
        
        private HttpResponse execute(final HttpHost httpHost, final HttpRequest httpRequest, final HttpContext httpContext) throws IOException {
            return PseudonymousCookieSource.Helper.updateFromResponseCookie((HttpUriRequest)wrapRequest(httpRequest), this.mClient.execute(httpHost, (HttpRequest)PseudonymousCookieSource.Helper.setRequestCookie((HttpUriRequest)wrapRequest(httpRequest), this.mCookieSource), httpContext), this.mCookieSource);
        }
        
        private HttpResponse execute(final HttpUriRequest httpUriRequest, final HttpContext httpContext) throws IOException {
            return PseudonymousCookieSource.Helper.updateFromResponseCookie(httpUriRequest, this.mClient.execute(PseudonymousCookieSource.Helper.setRequestCookie(httpUriRequest, this.mCookieSource)), this.mCookieSource);
        }
    }
    
    private final class SetCookie<T> implements ResponseHandler<T>
    {
        private final PseudonymousCookieSource mCookieSource;
        private final ResponseHandler<T> mHandler;
        private final HttpUriRequest mRequest;
        
        private SetCookie(final ResponseHandler<T> mHandler, final HttpUriRequest mRequest, final PseudonymousCookieSource mCookieSource) {
            super();
            this.mHandler = mHandler;
            this.mRequest = mRequest;
            this.mCookieSource = mCookieSource;
        }
        
        public T handleResponse(final HttpResponse httpResponse) throws ClientProtocolException, IOException {
            return (T)this.mHandler.handleResponse(PseudonymousCookieSource.Helper.updateFromResponseCookie(this.mRequest, httpResponse, this.mCookieSource));
        }
    }
    
    private class WrappedLayeredSocketFactory extends WrappedSocketFactory implements LayeredSocketFactory
    {
        private LayeredSocketFactory mDelegate;
        
        private WrappedLayeredSocketFactory(final LayeredSocketFactory mDelegate) {
            super((SocketFactory)mDelegate);
            this.mDelegate = mDelegate;
        }
        
        public final Socket createSocket(final Socket socket, final String s, final int n, final boolean b) throws IOException {
            return this.mDelegate.createSocket(socket, s, n, b);
        }
    }
    
    private class WrappedSocketFactory implements SocketFactory
    {
        private SocketFactory mDelegate;
        
        private WrappedSocketFactory(final SocketFactory mDelegate) {
            super();
            this.mDelegate = mDelegate;
        }
        
        public final Socket connectSocket(final Socket socket, final String s, final int n, final InetAddress inetAddress, final int n2, final HttpParams httpParams) throws IOException {
            GoogleHttpClient.this.mConnectionAllocated.set(Boolean.TRUE);
            return this.mDelegate.connectSocket(socket, s, n, inetAddress, n2, httpParams);
        }
        
        public final Socket createSocket() throws IOException {
            return this.mDelegate.createSocket();
        }
        
        public final boolean isSecure(final Socket socket) {
            return this.mDelegate.isSecure(socket);
        }
    }
}
