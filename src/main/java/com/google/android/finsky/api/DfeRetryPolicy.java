package com.google.android.finsky.api;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.VolleyError;

public class DfeRetryPolicy extends DefaultRetryPolicy {
    private final DfeApiContext mDfeApiContext;
    private boolean mHadAuthException;


    public DfeRetryPolicy(int initialTimeoutMs, int maxNumRetries, float backoffMultiplier, final DfeApiContext mDfeApiContext) {
        super(initialTimeoutMs, maxNumRetries, backoffMultiplier);
        this.mDfeApiContext = mDfeApiContext;
    }

    public DfeRetryPolicy(final DfeApiContext mDfeApiContext) {
        super();
        this.mDfeApiContext = mDfeApiContext;
    }

    @Override
    public void retry(final VolleyError volleyError) throws VolleyError {
        if (volleyError instanceof AuthFailureError) {
            if (this.mHadAuthException) {
                throw volleyError;
            }
            this.mHadAuthException = true;
        }
        super.retry(volleyError);
    }
}