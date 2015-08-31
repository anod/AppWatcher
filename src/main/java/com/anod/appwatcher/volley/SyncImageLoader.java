package com.anod.appwatcher.volley;

import android.graphics.Bitmap;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.RequestFuture;

import java.util.concurrent.ExecutionException;

import info.anodsplace.android.log.AppLog;

/**
 * @author alex
 * @date 2015-02-26
 */
public class SyncImageLoader extends ImageLoader {

    private final RequestQueue mRequestQueue;

    /**
     * Constructs a new ImageLoader.
     *
     * @param queue      The RequestQueue to use for making image requests.
     */
    public SyncImageLoader(RequestQueue queue) {
        super(queue, new NoImageCache());
        mRequestQueue = queue;
    }

    @Override
    public ImageContainer get(String requestUrl, ImageListener imageListener, int maxWidth, int maxHeight) {
        RequestFuture<Bitmap> future = RequestFuture.newFuture();
        Request<Bitmap> newRequest = makeImageRequest(requestUrl, maxWidth, maxHeight, future);

        mRequestQueue.add(newRequest);

        Bitmap bitmap = null;
        try {
            bitmap = future.get();
        } catch (InterruptedException | ExecutionException e) {
            AppLog.e(e);
        }
        ImageContainer imageContainer =  new ImageContainer(bitmap, requestUrl, null, null);

        return imageContainer;
    }

    protected Request<Bitmap> makeImageRequest(String requestUrl, int maxWidth, int maxHeight, RequestFuture future) {
        return new ImageRequest(requestUrl, future, maxWidth, maxHeight, Bitmap.Config.RGB_565, future);
    }

}
