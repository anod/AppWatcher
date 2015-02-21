package com.google.android.vending.remoting.api;

import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.anod.appwatcher.utils.AppLog;
import com.google.android.finsky.protos.VendingProtos;
import com.google.android.finsky.utils.Maps;
import com.google.android.finsky.utils.Utils;
import com.google.android.play.dfe.utils.NanoProtoHelper;
import com.google.protobuf.nano.MessageNano;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class VendingRequest<T extends MessageNano, U extends MessageNano> extends Request<VendingProtos.ResponseProto> {
    protected final VendingApiContext mApiContext;
    private boolean mAvoidBulkCancel = false;
    private Map<String, String> mExtraHeaders;
    private final Response.Listener<U> mListener;
    private final T mRequest;
    private final Class<T> mRequestClass;
    private final Class<U> mResponseClass;
    private final boolean mUseSecureAuthToken;

    protected VendingRequest(String paramString, Class<T> paramClass, T paramT, Class<U> paramClass1, Response.Listener<U> paramListener, VendingApiContext paramVendingApiContext, Response.ErrorListener paramErrorListener) {
        super(1, paramString, paramErrorListener);
        this.mUseSecureAuthToken = paramString.startsWith("https");
        this.mRequest = paramT;
        this.mRequestClass = paramClass;
        this.mResponseClass = paramClass1;
        this.mListener = paramListener;
        this.mApiContext = paramVendingApiContext;
        setRetryPolicy(new VendingRetryPolicy(this.mApiContext, this.mUseSecureAuthToken));
    }

    public static <T extends MessageNano, U extends MessageNano> VendingRequest<T, U> make(String paramString, Class<T> paramClass, T paramT, Class<U> paramClass1, Response.Listener<U> paramListener, VendingApiContext paramVendingApiContext, Response.ErrorListener paramErrorListener) {
        return new VendingRequest(paramString, paramClass, paramT, paramClass1, paramListener, paramVendingApiContext, paramErrorListener);
    }

    public void addExtraHeader(String paramString1, String paramString2) {
        if (this.mExtraHeaders == null) {
            this.mExtraHeaders = Maps.newHashMap();
        }
        this.mExtraHeaders.put(paramString1, paramString2);
    }

    public void deliverError(VolleyError paramVolleyError) {
        if ((paramVolleyError instanceof AuthFailureError)) {
            this.mApiContext.invalidateAuthToken(this.mUseSecureAuthToken);
        }
        super.deliverError(paramVolleyError);
    }

    protected void deliverResponse(VendingProtos.ResponseProto paramResponseProto) {
        U localMessageNano = NanoProtoHelper.getParsedResponseFromWrapper(paramResponseProto.response[0], VendingProtos.ResponseProto.Response.class, this.mResponseClass);
        this.mListener.onResponse(localMessageNano);
    }

    public boolean getAvoidBulkCancel() {
        return this.mAvoidBulkCancel;
    }

    public Map<String, String> getHeaders() {
        Map<String, String> localObject = this.mApiContext.getHeaders();
        if ((this.mExtraHeaders != null) && (!this.mExtraHeaders.isEmpty())) {
            HashMap<String, String> localHashMap = Maps.newHashMap();
            localHashMap.putAll((Map) localObject);
            localHashMap.putAll(this.mExtraHeaders);
            localObject = localHashMap;
        }
        return localObject;
    }

    public Map<String, String> getParams()
            throws AuthFailureError {
        HashMap localHashMap = Maps.newHashMap();
        localHashMap.put("request", serializeRequestProto(this.mRequest));
        localHashMap.put("version", String.valueOf(2));
        return localHashMap;
    }

    protected Response<VendingProtos.ResponseProto> parseNetworkResponse(NetworkResponse paramNetworkResponse) {
        try {
            VendingProtos.ResponseProto localResponseProto = VendingProtos.ResponseProto.parseFrom(Utils.readBytes(new GZIPInputStream(new ByteArrayInputStream(paramNetworkResponse.data), paramNetworkResponse.data.length)));
            if (localResponseProto.response.length != 1)
                return Response.error(new ServerError());
            if (localResponseProto.response[0].responseProperties.result != 0)
                return Response.error(new ServerError());
//      handlePendingNotifications(localResponseProto, true);
            Response localResponse = Response.success(localResponseProto, null);
            return localResponse;
        } catch (IOException localIOException) {
            AppLog.e("Cannot parse Vending ResponseProto: " + localIOException, localIOException);
        }
        return Response.error(new VolleyError());
    }

    String serializeRequestProto(T paramT)
            throws AuthFailureError {
        VendingProtos.RequestProto.Request localRequest = new VendingProtos.RequestProto.Request();
        NanoProtoHelper.setRequestInWrapper(localRequest, VendingProtos.RequestProto.Request.class, paramT, this.mRequestClass);
        VendingProtos.RequestProto localRequestProto = new VendingProtos.RequestProto();
        VendingProtos.RequestPropertiesProto requestProperties = this.mApiContext.getRequestProperties(this.mUseSecureAuthToken);

        localRequestProto.requestProperties = requestProperties;
        localRequestProto.request = new VendingProtos.RequestProto.Request[]{localRequest};
        return Base64.encodeToString(MessageNano.toByteArray(localRequestProto), 11);
    }

    public void setAvoidBulkCancel() {
        this.mAvoidBulkCancel = true;
    }

    public String toString() {
        return super.toString() + " " + this.mRequestClass.getSimpleName();
    }
}

/* Location:           /Users/alex/Documents/android-tools/com.android.vending-5.0.31-80300031-minAPI9-dex2jar.jar
 * Qualified Name:     com.google.android.vending.remoting.api.VendingRequest
 * JD-Core Version:    0.6.2
 */