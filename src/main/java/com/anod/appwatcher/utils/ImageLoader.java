package com.anod.appwatcher.utils;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.graphics.Bitmap;
import android.widget.ImageView;

abstract public class ImageLoader {
	private MemoryCache mMemoryCache=new MemoryCache();
	private Map<ImageView, String> mImageViews=Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
	private ExecutorService mExecutorService; 
	
	public ImageLoader(){
		mExecutorService=Executors.newFixedThreadPool(5);
	}

	abstract protected Bitmap loadBitmap(String imgUID);
	
	public Bitmap getCachedImage(String imgUID) {
		return mMemoryCache.get(imgUID);
	}
	
	protected void cacheImage(String imgUID, Bitmap bmp) {
        mMemoryCache.put(imgUID, bmp);
	}
	
	public void loadImage(String imgUID, ImageView imageView)
	{
		mImageViews.put(imageView, imgUID);
		Bitmap bitmap=mMemoryCache.get(imgUID);
		if(bitmap!=null) {
			imageView.setImageBitmap(bitmap);
		} else {
			queuePhoto(imgUID, imageView);
	    }
	}
	
    private void queuePhoto(String imgUID, ImageView imageView)
    {
        PhotoToLoad p=new PhotoToLoad(imgUID, imageView);
        mExecutorService.submit(new PhotosLoader(p));
    }

    //Task for the queue
    private class PhotoToLoad
    {
        public String imgUID;
        public ImageView imageView;
        public PhotoToLoad(String u, ImageView i){
            imgUID=u; 
            imageView=i;
        }
    }
	    
    class PhotosLoader implements Runnable {
        PhotoToLoad mPhotoToLoad;
        PhotosLoader(PhotoToLoad photoToLoad){
            mPhotoToLoad=photoToLoad;
        }
        
        @Override
        public void run() {
            if(imageViewReused(mPhotoToLoad)) {
                return;
            }
            Bitmap bmp=loadBitmap(mPhotoToLoad.imgUID);
            cacheImage(mPhotoToLoad.imgUID, bmp);
            if(imageViewReused(mPhotoToLoad)) {
                return;
            }
            Activity a=(Activity)mPhotoToLoad.imageView.getContext();
            if (bmp == null) {
            	mImageViews.remove(mPhotoToLoad.imgUID);
            	return;
            }
            BitmapDisplayer bd=new BitmapDisplayer(bmp, mPhotoToLoad);
            a.runOnUiThread(bd);
        }
    }
	    
    boolean imageViewReused(PhotoToLoad photoToLoad){
        String tag=mImageViews.get(photoToLoad.imageView);
        if ( tag==null || !tag.equals(photoToLoad.imgUID) ) {
            return true;
        }
        return false;
    }
	    
    //Used to display mBitmap in the UI thread
    class BitmapDisplayer implements Runnable
    {
        Bitmap mBitmap;
        PhotoToLoad mPhotoToLoad;
        public BitmapDisplayer(Bitmap b, PhotoToLoad p){
        	mBitmap=b;
        	mPhotoToLoad=p;
        }
        
        public void run()
        {
            if(imageViewReused(mPhotoToLoad))
                return;
            if(mBitmap!=null) {
                mPhotoToLoad.imageView.setImageBitmap(mBitmap);
            }
        }
    }

    public void clearCache() {
        mMemoryCache.clear();
    }

}
