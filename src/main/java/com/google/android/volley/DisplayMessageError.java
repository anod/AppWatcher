package com.google.android.volley;

import com.android.volley.VolleyError;

public abstract class DisplayMessageError extends VolleyError
{
    String mDisplayErrorHtml;
    
    public DisplayMessageError() {
        super();
    }
    
    public DisplayMessageError(final String mDisplayErrorHtml) {
        super();
        this.mDisplayErrorHtml = mDisplayErrorHtml;
    }
    
    public String getDisplayErrorHtml() {
        return this.mDisplayErrorHtml;
    }
    
    @Override
    public String toString() {
        return "DisplayErrorMessage[" + this.mDisplayErrorHtml + "]";
    }
}
