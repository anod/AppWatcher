package com.google.android.finsky.api;

import com.android.volley.Response;
import com.google.protobuf.nano.MessageNano;

class ProtoDfeRequest<T extends MessageNano> extends DfeRequest<T>
{
    private final MessageNano mRequest;
    
    ProtoDfeRequest(final String s, final MessageNano mRequest, final DfeApiContext dfeApiContext, final Class<T> clazz, final Response.Listener<T> listener, final Response.ErrorListener errorListener) {
        super(1, s, dfeApiContext, clazz, listener, errorListener);
        this.mRequest = mRequest;
        this.setShouldCache(false);
    }
    
    @Override
    public byte[] getBody() {
        return MessageNano.toByteArray(this.mRequest);
    }
    
    @Override
    public String getBodyContentType() {
        return "application/x-protobuf";
    }
}