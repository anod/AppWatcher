package com.google.android.volley;

import android.content.Context;
import android.util.Log;

import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpClientStack;

public class GoogleHttpClientStack extends HttpClientStack
{
  private final GoogleHttpClient mGoogleHttpClient;

  public GoogleHttpClientStack(Context paramContext)
  {
    this(paramContext, false);
  }

  public GoogleHttpClientStack(Context paramContext, boolean enableCurlLogging)
  {
    this(new GoogleHttpClient(paramContext, "unused/0", true), enableCurlLogging);
  }

  private GoogleHttpClientStack(GoogleHttpClient paramGoogleHttpClient, boolean enableCurlLogging)
  {
    super(paramGoogleHttpClient);
    this.mGoogleHttpClient = paramGoogleHttpClient;
    if (enableCurlLogging) {
        paramGoogleHttpClient.enableCurlLogging(VolleyLog.TAG, Log.VERBOSE);
    }
  }
}