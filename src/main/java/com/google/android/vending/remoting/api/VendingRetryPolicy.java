package com.google.android.vending.remoting.api;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.VolleyError;

public class VendingRetryPolicy extends DefaultRetryPolicy
{
  private static final int VENDING_TIMEOUT_MS = DEFAULT_TIMEOUT_MS;
  private boolean mHadAuthException;
  private boolean mUseSecureToken;
  private final VendingApiContext mVendingApiContext;

  public VendingRetryPolicy(VendingApiContext paramVendingApiContext, boolean paramBoolean)
  {
    super(VENDING_TIMEOUT_MS, 1, 0.0F);
    this.mVendingApiContext = paramVendingApiContext;
    this.mUseSecureToken = paramBoolean;
  }

  public void retry(VolleyError paramVolleyError)
    throws VolleyError
  {
    if ((paramVolleyError instanceof AuthFailureError))
    {
      if (this.mHadAuthException)
        throw paramVolleyError;
      this.mHadAuthException = true;
      this.mVendingApiContext.invalidateAuthToken(this.mUseSecureToken);
    }
    super.retry(paramVolleyError);
  }
}

/* Location:           /Users/alex/Documents/android-tools/com.android.vending-5.0.31-80300031-minAPI9-dex2jar.jar
 * Qualified Name:     com.google.android.vending.remoting.api.VendingRetryPolicy
 * JD-Core Version:    0.6.2
 */