package com.anod.appwatcher.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import info.anodsplace.android.image.ImageLoader;
import info.anodsplace.android.log.AppLog;


public class AppIconLoader extends ImageLoader {


    private final Context mContext;

    private final PackageManager mPackageManager;

    public AppIconLoader(Context context) {
        super();
        mContext = context.getApplicationContext();
        mPackageManager = context.getPackageManager();
    }

    public void precacheIcon(String appId) {
        Bitmap bmp = loadBitmap(appId);
        if (bmp != null) {
            cacheImage(appId, bmp);
        }
    }

    public Bitmap loadImageUncached(String imgUID) {
        return loadBitmap(imgUID);
    }

    public void loadImage(ApplicationInfo info, ImageView imageView){
        super.loadImage(info.packageName, imageView);
    }

    @Override
    protected Bitmap loadBitmap(String imgUID) {
        Drawable d = null;
        Bitmap icon = null;

        try {
            d = mPackageManager.getApplicationIcon(imgUID);
        } catch (PackageManager.NameNotFoundException e1) {
            AppLog.e(e1);
            return null;
        }
        if (d instanceof BitmapDrawable) {
            // Ensure the bitmap has a density.
            BitmapDrawable bitmapDrawable = (BitmapDrawable) d;
            icon = bitmapDrawable.getBitmap();
            if (icon.getDensity() == Bitmap.DENSITY_NONE) {
                bitmapDrawable.setTargetDensity(mContext.getResources().getDisplayMetrics());
            }
        }

        return icon;
    }
}
