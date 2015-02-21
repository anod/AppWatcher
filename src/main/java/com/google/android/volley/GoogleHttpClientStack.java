package com.google.android.volley;

import android.content.Context;

import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpClientStack;

public class GoogleHttpClientStack extends HttpClientStack
{
  private final GoogleHttpClient mGoogleHttpClient;

  public GoogleHttpClientStack(Context paramContext)
  {
    this(paramContext, false);
  }

  public GoogleHttpClientStack(Context paramContext, boolean paramBoolean)
  {
    this(new GoogleHttpClient(paramContext, "unused/0", true), paramBoolean);
  }

  private GoogleHttpClientStack(GoogleHttpClient paramGoogleHttpClient, boolean paramBoolean)
  {
    super(paramGoogleHttpClient);
    this.mGoogleHttpClient = paramGoogleHttpClient;
    if (paramBoolean) {
        paramGoogleHttpClient.enableCurlLogging(VolleyLog.TAG, 2);
    }
  }
}