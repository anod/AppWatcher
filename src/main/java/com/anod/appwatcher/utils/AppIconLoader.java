package com.anod.appwatcher.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;
import com.squareup.picasso.RequestHandler;

import java.io.IOException;

import info.anodsplace.android.log.AppLog;

import static com.squareup.picasso.Picasso.LoadedFrom.DISK;


public class AppIconLoader {
    private final Context mContext;
    private Picasso mPicasso;
    public static final String SCHEME = "application.icon";

    public AppIconLoader(Context context) {
        mContext = context.getApplicationContext();
    }


    static class PackageIconRequestHandler extends RequestHandler {
        private final PackageManager mPackageManager;
        private final Context mContext;

        public PackageIconRequestHandler(Context context) {
            mContext = context;
            mPackageManager = context.getPackageManager();
        }

        @Override
        public boolean canHandleRequest(Request data) {
            return SCHEME.equals(data.uri.getScheme());
        }

        @Override
        public Result load(Request request, int networkPolicy) throws IOException {
            Drawable d = null;
            Bitmap icon;

            String part = request.uri.getSchemeSpecificPart();
            AppLog.d("Get Activity Info: " + part);
            ComponentName cmp = ComponentName.unflattenFromString(part);
            try {
                d = mPackageManager.getActivityIcon(cmp);
            } catch (PackageManager.NameNotFoundException ignored) {
            }

            if (d == null) {
                try {
                    d = mPackageManager.getApplicationIcon(cmp.getPackageName());
                } catch (PackageManager.NameNotFoundException e1) {
                    AppLog.e(e1);
                    return null;
                }
            }

            if (d instanceof BitmapDrawable) {
                // Ensure the bitmap has a density.
                BitmapDrawable bitmapDrawable = (BitmapDrawable) d;
                icon = bitmapDrawable.getBitmap();
                if (icon.getDensity() == Bitmap.DENSITY_NONE) {
                    bitmapDrawable.setTargetDensity(mContext.getResources().getDisplayMetrics());
                }
                return new Result(icon, DISK);
            }
            return null;
        }

    }

    public Picasso picasso() {
        if (mPicasso == null) {
            mPicasso = new Picasso.Builder(mContext)
                    .addRequestHandler(new PackageIconRequestHandler(mContext))
                    .build();
        }
        return mPicasso;
    }

    public void shutdown() {
        if (mPicasso != null) {
            mPicasso.shutdown();
            mPicasso = null;
        }
    }
}