package com.google.android.finsky.api;

import com.android.volley.VolleyError;

class DfeServerError extends VolleyError {

    String mDisplayErrorHtml;

    DfeServerError(String displayErrorHtml) {
        super();

        mDisplayErrorHtml = displayErrorHtml;
    }

    public String getDisplayErrorHtml() {
        return this.mDisplayErrorHtml;
    }

    @Override
    public String toString() {
        return "DisplayErrorMessage[" + this.mDisplayErrorHtml + "]";
    }
}