package com.google.android.volley;

import android.os.*;
import org.apache.http.conn.*;
import org.apache.http.conn.routing.*;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.protocol.*;
import org.apache.http.client.methods.*;
import android.content.*;
import org.apache.http.client.params.*;
import org.apache.http.params.*;
import org.apache.http.conn.scheme.*;
import android.net.*;
import org.apache.http.conn.params.*;
import com.google.android.volley.elegant.*;
import org.apache.http.impl.client.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import org.apache.http.client.*;
import org.apache.http.*;
import android.util.*;

public final class AndroidHttpClient implements HttpClient
{
    public static long DEFAULT_SYNC_MIN_GZIP_BYTES;
    private static final HttpRequestInterceptor sThreadCheckInterceptor;
    private static String[] textContentTypes;
    private volatile LoggingConfiguration curlConfiguration;
    private final HttpClient delegate;
    private RuntimeException mLeakedException;
    
    static {
        AndroidHttpClient.DEFAULT_SYNC_MIN_GZIP_BYTES = 256L;
        AndroidHttpClient.textContentTypes = new String[] { "text/", "application/xml", "application/json" };
        sThreadCheckInterceptor = (HttpRequestInterceptor)new HttpRequestInterceptor() {
            public void process(final HttpRequest httpRequest, final HttpContext httpContext) {
                if (Looper.myLooper() != null && Looper.myLooper() == Looper.getMainLooper()) {
                    throw new RuntimeException("This thread forbids HTTP requests");
                }
            }
        };
    }
    
    private AndroidHttpClient(final ClientConnectionManager clientConnectionManager, final HttpParams httpParams) {
        super();
        this.mLeakedException = new IllegalStateException("AndroidHttpClient created and never closed");
        this.delegate = (HttpClient)new DefaultHttpClient(clientConnectionManager, httpParams) {
            protected RequestDirector createClientRequestDirector(final HttpRequestExecutor httpRequestExecutor, final ClientConnectionManager clientConnectionManager, final ConnectionReuseStrategy connectionReuseStrategy, final ConnectionKeepAliveStrategy connectionKeepAliveStrategy, final HttpRoutePlanner httpRoutePlanner, final HttpProcessor httpProcessor, final HttpRequestRetryHandler httpRequestRetryHandler, final RedirectHandler redirectHandler, final AuthenticationHandler authenticationHandler, final AuthenticationHandler authenticationHandler2, final UserTokenHandler userTokenHandler, final HttpParams httpParams) {
                return (RequestDirector)new ElegantRequestDirector(httpRequestExecutor, clientConnectionManager, connectionReuseStrategy, connectionKeepAliveStrategy, httpRoutePlanner, httpProcessor, httpRequestRetryHandler, redirectHandler, authenticationHandler, authenticationHandler2, userTokenHandler, httpParams);
            }
            
            protected HttpContext createHttpContext() {
                final BasicHttpContext basicHttpContext = new BasicHttpContext();
                ((HttpContext)basicHttpContext).setAttribute("http.authscheme-registry", (Object)this.getAuthSchemes());
                ((HttpContext)basicHttpContext).setAttribute("http.cookiespec-registry", (Object)this.getCookieSpecs());
                ((HttpContext)basicHttpContext).setAttribute("http.auth.credentials-provider", (Object)this.getCredentialsProvider());
                return (HttpContext)basicHttpContext;
            }
            
            protected BasicHttpProcessor createHttpProcessor() {
                final BasicHttpProcessor httpProcessor = super.createHttpProcessor();
                httpProcessor.addRequestInterceptor(AndroidHttpClient.sThreadCheckInterceptor);
                httpProcessor.addRequestInterceptor((HttpRequestInterceptor)new CurlLogger());
                return httpProcessor;
            }
        };
    }
    
    private static boolean isBinaryContent(final HttpUriRequest httpUriRequest) {
        final Header[] headers = httpUriRequest.getHeaders("content-encoding");
        if (headers != null) {
            for (int length = headers.length, i = 0; i < length; ++i) {
                if ("gzip".equalsIgnoreCase(headers[i].getValue())) {
                    return true;
                }
            }
        }
        Label_0052: {
            break Label_0052;
        }
        final Header[] headers2 = httpUriRequest.getHeaders("content-type");
        if (headers2 != null) {
            for (final Header header : headers2) {
                final String[] textContentTypes = AndroidHttpClient.textContentTypes;
                for (int length3 = textContentTypes.length, k = 0; k < length3; ++k) {
                    if (header.getValue().startsWith(textContentTypes[k])) {
                        return false;
                    }
                }
            }
            return true;
        }
        return true;
    }

    public static AndroidHttpClient newInstance(final String s, final Context context) {
        final BasicHttpParams basicHttpParams = new BasicHttpParams();
        HttpConnectionParams.setStaleCheckingEnabled((HttpParams)basicHttpParams, false);
        HttpConnectionParams.setConnectionTimeout((HttpParams)basicHttpParams, 20000);
        HttpConnectionParams.setSoTimeout((HttpParams)basicHttpParams, 20000);
        HttpConnectionParams.setSocketBufferSize((HttpParams)basicHttpParams, 8192);
        HttpClientParams.setRedirecting((HttpParams)basicHttpParams, false);
        SSLSessionCache sslSessionCache;
        if (context == null) {
            sslSessionCache = null;
        }
        else {
            sslSessionCache = new SSLSessionCache(context);
        }
        HttpProtocolParams.setUserAgent((HttpParams)basicHttpParams, s);
        final SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", (SocketFactory)ElegantPlainSocketFactory.getSocketFactory(), 80));
        schemeRegistry.register(new Scheme("https", (SocketFactory)getSocketFactory(sslSessionCache), 443));
        final ConnManagerParamBean connManagerParamBean = new ConnManagerParamBean((HttpParams)basicHttpParams);
        connManagerParamBean.setConnectionsPerRoute(new ConnPerRouteBean(4));
        connManagerParamBean.setMaxTotalConnections(8);
        return new AndroidHttpClient((ClientConnectionManager)new ElegantThreadSafeConnManager((HttpParams)basicHttpParams, schemeRegistry), (HttpParams)basicHttpParams);
    }

    private static SSLSocketFactory getSocketFactory(final SSLSessionCache sslSessionCache) {
        final javax.net.ssl.SSLSocketFactory default1 = SSLCertificateSocketFactory.getDefault(5000, sslSessionCache);
        try {
            return SSLSocketFactory.class.getConstructor(javax.net.ssl.SSLSocketFactory.class).newInstance(default1);
        }
        catch (NoSuchMethodException ex) {
            throw new IllegalStateException(ex);
        }
        catch (InstantiationException ex2) {
            throw new IllegalStateException(ex2);
        }
        catch (IllegalAccessException ex3) {
            throw new IllegalStateException(ex3);
        }
        catch (InvocationTargetException ex4) {
            throw new IllegalStateException(ex4);
        }
    }
    
    private static String toCurl(final HttpUriRequest httpUriRequest, final boolean b) throws IOException {
        final StringBuilder sb = new StringBuilder();
        sb.append("curl ");
        sb.append("-X ");
        sb.append(httpUriRequest.getMethod());
        sb.append(" ");
        for (final Header header : httpUriRequest.getAllHeaders()) {
            if (b || (!header.getName().equals("Authorization") && !header.getName().equals("Cookie"))) {
                sb.append("--header \"");
                sb.append(header.toString().trim());
                sb.append("\" ");
            }
        }
        URI uri = httpUriRequest.getURI();
        if (httpUriRequest instanceof RequestWrapper) {
            final HttpRequest original = ((RequestWrapper)httpUriRequest).getOriginal();
            if (original instanceof HttpUriRequest) {
                uri = ((HttpUriRequest)original).getURI();
            }
        }
        sb.append("\"");
        sb.append(uri);
        sb.append("\"");
        if (httpUriRequest instanceof HttpEntityEnclosingRequest) {
            final HttpEntity entity = ((HttpEntityEnclosingRequest)httpUriRequest).getEntity();
            if (entity != null && entity.isRepeatable()) {
                if (entity.getContentLength() < 1024L) {
                    final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    entity.writeTo((OutputStream)byteArrayOutputStream);
                    if (isBinaryContent(httpUriRequest)) {
                        sb.insert(0, "echo '" + Base64.encodeToString(byteArrayOutputStream.toByteArray(), 2) + "' | base64 -d > /tmp/$$.bin; ");
                        sb.append(" --data-binary @/tmp/$$.bin");
                    }
                    else {
                        sb.append(" --data-ascii \"").append(byteArrayOutputStream.toString()).append("\"");
                    }
                }
                else {
                    sb.append(" [TOO MUCH DATA TO INCLUDE]");
                }
            }
        }
        return sb.toString();
    }
    
    public void enableCurlLogging(final String s, final int n) {
        if (s == null) {
            throw new NullPointerException("name");
        }
        if (n < 2 || n > 7) {
            throw new IllegalArgumentException("Level is out of range [2..7]");
        }
        this.curlConfiguration = new LoggingConfiguration(s, n);
    }
    
    public <T> T execute(final HttpHost httpHost, final HttpRequest httpRequest, final ResponseHandler<? extends T> responseHandler) throws IOException, ClientProtocolException {
        return (T)this.delegate.execute(httpHost, httpRequest, (ResponseHandler)responseHandler);
    }
    
    public <T> T execute(final HttpHost httpHost, final HttpRequest httpRequest, final ResponseHandler<? extends T> responseHandler, final HttpContext httpContext) throws IOException, ClientProtocolException {
        return (T)this.delegate.execute(httpHost, httpRequest, (ResponseHandler)responseHandler, httpContext);
    }
    
    public <T> T execute(final HttpUriRequest httpUriRequest, final ResponseHandler<? extends T> responseHandler) throws IOException, ClientProtocolException {
        return (T)this.delegate.execute(httpUriRequest, (ResponseHandler)responseHandler);
    }
    
    public <T> T execute(final HttpUriRequest httpUriRequest, final ResponseHandler<? extends T> responseHandler, final HttpContext httpContext) throws IOException, ClientProtocolException {
        return (T)this.delegate.execute(httpUriRequest, (ResponseHandler)responseHandler, httpContext);
    }
    
    public HttpResponse execute(final HttpHost httpHost, final HttpRequest httpRequest) throws IOException {
        return this.delegate.execute(httpHost, httpRequest);
    }
    
    public HttpResponse execute(final HttpHost httpHost, final HttpRequest httpRequest, final HttpContext httpContext) throws IOException {
        return this.delegate.execute(httpHost, httpRequest, httpContext);
    }
    
    public HttpResponse execute(final HttpUriRequest httpUriRequest) throws IOException {
        return this.delegate.execute(httpUriRequest);
    }
    
    public HttpResponse execute(final HttpUriRequest httpUriRequest, final HttpContext httpContext) throws IOException {
        return this.delegate.execute(httpUriRequest, httpContext);
    }
    
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (this.mLeakedException != null) {
            Log.e("AndroidHttpClient", "Leak found", (Throwable)this.mLeakedException);
            this.mLeakedException = null;
        }
    }
    
    public ClientConnectionManager getConnectionManager() {
        return this.delegate.getConnectionManager();
    }
    
    public HttpParams getParams() {
        return this.delegate.getParams();
    }
    
    private class CurlLogger implements HttpRequestInterceptor
    {
        public void process(final HttpRequest httpRequest, final HttpContext httpContext) throws IOException {
            final LoggingConfiguration access$300 = AndroidHttpClient.this.curlConfiguration;
            if (access$300 != null && access$300.isLoggable() && httpRequest instanceof HttpUriRequest) {
                access$300.println(toCurl((HttpUriRequest)httpRequest, true));
            }
        }
    }
    
    private static class LoggingConfiguration
    {
        private final int level;
        private final String tag;
        
        private LoggingConfiguration(final String tag, final int level) {
            super();
            this.tag = tag;
            this.level = level;
        }
        
        private boolean isLoggable() {
            return Log.isLoggable(this.tag, this.level);
        }
        
        private void println(final String s) {
            Log.println(this.level, this.tag, s);
        }
    }
}
