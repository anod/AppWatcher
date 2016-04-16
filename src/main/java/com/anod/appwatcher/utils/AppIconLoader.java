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
        private final PackageManagerUtils mPackageManager;
        private final Context mContext;

        public PackageIconRequestHandler(Context context) {
            mContext = context;
            mPackageManager = new PackageManagerUtils(context.getPackageManager());
        }

        @Override
        public boolean canHandleRequest(Request data) {
            return SCHEME.equals(data.uri.getScheme());
        }

        @Override
        public Result load(Request request, int networkPolicy) throws IOException {

            String part = request.uri.getSchemeSpecificPart();
            AppLog.d("Get Activity Info: " + part);
            ComponentName cmp = ComponentName.unflattenFromString(part);

            Bitmap icon = mPackageManager.loadIcon(cmp, mContext.getResources().getDisplayMetrics());
            if (icon == null){
                return null;
            }
            return new Result(icon, DISK);
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