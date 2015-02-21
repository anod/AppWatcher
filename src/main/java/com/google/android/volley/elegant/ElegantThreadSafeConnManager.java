package com.google.android.volley.elegant;

import org.apache.http.params.*;
import org.apache.http.conn.scheme.*;
import java.util.concurrent.*;
import java.io.*;
import org.apache.http.conn.routing.*;
import org.apache.http.impl.conn.*;
import org.apache.http.conn.*;
import java.util.concurrent.locks.*;
import java.util.*;
import com.android.volley.*;
import org.apache.http.impl.conn.tsccm.*;

public class ElegantThreadSafeConnManager extends ThreadSafeClientConnManager
{
    public ElegantThreadSafeConnManager(final HttpParams httpParams, final SchemeRegistry schemeRegistry) {
        super(httpParams, schemeRegistry);
    }
}
