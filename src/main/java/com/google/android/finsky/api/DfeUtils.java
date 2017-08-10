package com.google.android.finsky.api;

import android.annotation.TargetApi;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.ParseError;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.google.android.finsky.protos.nano.Messages;

public class DfeUtils {

    private static Uri.Builder createSearchUrlBuilder(String query, int backendId) {
        if (backendId == 9) {
            backendId = 0;
        }
        return DfeApi.SEARCH_CHANNEL_URI.buildUpon().appendQueryParameter("c", Integer.toString(backendId)).appendQueryParameter("q", query);
    }

    public static String formSearchUrl(String query, int backendId) {
        return createSearchUrlBuilder(query, backendId).build().toString();
    }

    public static String base64Encode(byte[] input) {
        return Base64.encodeToString(input, Base64.NO_WRAP | Base64.URL_SAFE);
    }

    @TargetApi(21)
    public static String[] supportedAbis() {

        if (Build.VERSION.SDK_INT >= 21) {
            return Build.SUPPORTED_ABIS;
        } else {
            String[] arrayOfString;
            if (Build.CPU_ABI2.equals("unknown")) {
                arrayOfString = new String[1];
                arrayOfString[0] = Build.CPU_ABI;
            } else {
                arrayOfString = new String[2];
                arrayOfString[0] = Build.CPU_ABI;
                arrayOfString[1] = Build.CPU_ABI2;
            }
            return arrayOfString;
        }
    }

    public static Messages.DocV2 getRootDoc(Messages.Response.Payload payload) {
        if (null == payload) {
            return null;
        }
        if (payload.searchResponse != null && payload.searchResponse.doc.length > 0) {
            return getRootDoc(payload.searchResponse.doc[0]);
        } else if (payload.listResponse != null && payload.listResponse.doc.length > 0) {
            return getRootDoc(payload.listResponse.doc[0]);
        }
        return null;
    }

    private static Messages.DocV2 getRootDoc(Messages.DocV2 doc) {
        if (isRootDoc(doc)) {
            return doc;
        }
        for (Messages.DocV2 child: doc.child) {
            Messages.DocV2 root = getRootDoc(child);
            if (null != root) {
                return root;
            }
        }
        return null;
    }

    private static boolean isRootDoc(Messages.DocV2 doc) {
        return doc.child.length > 0 && doc.child[0].backendId == 3 && doc.child[0].docType == 1;
    }
}