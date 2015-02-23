package com.anod.appwatcher.volley;

import android.content.Context;

import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.ByteArrayPool;
import com.google.android.volley.GoogleHttpClientStack;

/**
 * @author alex
 * @date 2015-02-23
 */
public class Network extends BasicNetwork {
    public Network(Context context) {
        super(new GoogleHttpClientStack(context, false), new ByteArrayPool(1024 * 256));
    }
}
