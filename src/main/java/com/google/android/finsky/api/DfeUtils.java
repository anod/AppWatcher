package com.google.android.finsky.api;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.ParseError;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;

public class DfeUtils
{
  public static String createDetailsUrlFromId(String paramString)
  {
    return "details?doc=" + paramString;
  }

  private static Uri.Builder createSearchUrlBuilder(String query, int backendId)
  {
    if (backendId == 9) {
        backendId = 0;
    }
    return DfeApi.SEARCH_CHANNEL_URI.buildUpon().appendQueryParameter("c", Integer.toString(backendId)).appendQueryParameter("q", query);
  }

  public static String formSearchUrl(String query, int backendId)
  {
    return createSearchUrlBuilder(query, backendId).build().toString();
  }

  public static String formSearchUrlWithFprDisabled(String query, int backendId)
  {
    Uri.Builder localBuilder = createSearchUrlBuilder(query, backendId);
    localBuilder.appendQueryParameter("fpr", "0");
    return localBuilder.build().toString();
  }

  public static String getLegacyErrorCode(VolleyError paramVolleyError)
  {
    if ((paramVolleyError instanceof ServerError))
      return "SERVER";
    if ((paramVolleyError instanceof AuthFailureError))
      return "AUTH";
    if ((paramVolleyError instanceof NetworkError))
      return "NETWORK";
    if ((paramVolleyError instanceof TimeoutError))
      return "TIMEOUT";
    if ((paramVolleyError instanceof ParseError))
      return "SERVER";
    return "SERVER";
  }

  public static boolean isSameDocumentDetailsUrl(String paramString1, String paramString2)
  {
    if ((paramString1 == null) || (paramString2 == null))
      return false;
    return TextUtils.equals(Uri.parse(paramString1).getQueryParameter("doc"), Uri.parse(paramString2).getQueryParameter("doc"));
  }

  public static String base64Encode(byte[] input) {
    return Base64.encodeToString(input, Base64.NO_WRAP | Base64.URL_SAFE);
  }
}