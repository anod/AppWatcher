package com.google.android.finsky.api;

import com.android.volley.Response;
import com.google.android.finsky.protos.nano.Messages;
import com.google.protobuf.nano.MessageNano;

class ProtoDfeRequest extends DfeRequest
{
    private final MessageNano mRequest;
    
    ProtoDfeRequest(final String s, final MessageNano mRequest, final DfeApiContext dfeApiContext, final Response.Listener<Messages.Response.ResponseWrapper> listener, final Response.ErrorListener errorListener) {
        super(1, s, dfeApiContext, listener, errorListener);
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