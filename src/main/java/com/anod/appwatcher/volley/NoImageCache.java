package com.anod.appwatcher.volley;

import android.graphics.Bitmap;

import com.android.volley.toolbox.ImageLoader;

/**
 * @author alex
 * @date 2015-02-23
 */
public class NoImageCache implements ImageLoader.ImageCache {
    @Override
    public Bitmap getBitmap(String url) {
        return null;
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {

    }
}
