package com.google.android.volley.elegant;

import org.apache.http.conn.scheme.*;
import org.apache.http.params.*;
import com.android.volley.*;
import org.apache.http.conn.*;
import java.net.*;
import java.io.*;

public final class ElegantPlainSocketFactory implements SocketFactory
{
    private static final ElegantPlainSocketFactory DEFAULT_FACTORY;
    private final HostNameResolver nameResolver;
    
    static {
        DEFAULT_FACTORY = new ElegantPlainSocketFactory();
    }
    
    public ElegantPlainSocketFactory() {
        this(null);
    }
    
    public ElegantPlainSocketFactory(final HostNameResolver nameResolver) {
        super();
        this.nameResolver = nameResolver;
    }
    
    public static ElegantPlainSocketFactory getSocketFactory() {
        return ElegantPlainSocketFactory.DEFAULT_FACTORY;
    }
    
    public Socket connectSocket(Socket socket, final String s, final int n, final InetAddress inetAddress, int n2, final HttpParams httpParams) throws IOException {
        if (s == null) {
            throw new IllegalArgumentException("Target host may not be null.");
        }
        if (httpParams == null) {
            throw new IllegalArgumentException("Parameters may not be null.");
        }
        if (socket == null) {
            socket = this.createSocket();
        }
        if (inetAddress != null || n2 > 0) {
            if (n2 < 0) {
                n2 = 0;
            }
            socket.bind(new InetSocketAddress(inetAddress, n2));
        }
        final int connectionTimeout = HttpConnectionParams.getConnectionTimeout(httpParams);
        InetSocketAddress inetSocketAddress;
        if (this.nameResolver == null) {
            inetSocketAddress = new InetSocketAddress(s, n);
        } else {
            inetSocketAddress = new InetSocketAddress(this.nameResolver.resolve(s), n);
        }
        try {
            final long currentTimeMillis = System.currentTimeMillis();
            socket.connect(inetSocketAddress, connectionTimeout);
            VolleyLog.v("Established connection to [host=%s] in [%s ms]", s, System.currentTimeMillis() - currentTimeMillis);
        }
        catch (SocketTimeoutException ex) {
            throw new ConnectTimeoutException("Connect to " + inetSocketAddress + " timed out");
        }
        return socket;
    }
    
    public Socket createSocket() {
        return new Socket();
    }
    
    @Override
    public boolean equals(final Object o) {
        return o == this;
    }
    
    @Override
    public int hashCode() {
        return ElegantPlainSocketFactory.class.hashCode();
    }
    
    public final boolean isSecure(final Socket socket) throws IllegalArgumentException {
        if (socket == null) {
            throw new IllegalArgumentException("Socket may not be null.");
        }
        if (socket.getClass() != Socket.class) {
            throw new IllegalArgumentException("Socket not created by this factory.");
        }
        if (socket.isClosed()) {
            throw new IllegalArgumentException("Socket is closed.");
        }
        return false;
    }
}
