package com.anod.appwatcher.utils;

import android.content.ComponentName;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.RemoteException;

import com.anod.appwatcher.AppListContentProvider;
import com.anod.appwatcher.BuildConfig;
import com.anod.appwatcher.R;
import com.anod.appwatcher.model.AppListContentProviderClient;
import com.anod.appwatcher.model.schema.AppListTable;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.RequestHandler;

import java.io.IOException;
import java.util.Map;

import info.anodsplace.android.log.AppLog;

import static com.squareup.picasso.Picasso.LoadedFrom.DISK;


public class AppIconLoader {
    private final Context mContext;
    private Picasso mPicasso;
    public static final String SCHEME = "application.icon";
    private int mIconSize = -1;

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

    static class IconDbRequestHandler extends RequestHandler {

        private final Context mContext;

        public IconDbRequestHandler(Context context) {
            mContext = context;
        }

        @Override
        public boolean canHandleRequest(Request data) {
            return AppListContentProvider.matchIconUri(data.uri);
        }

        @Override
        public Result load(Request request, int networkPolicy) throws IOException {
            AppListContentProviderClient client = new AppListContentProviderClient(mContext);
            Bitmap icon = client.queryAppIcon(request.uri);
            client.release();
            if (icon == null)
            {
                return null;
            }
            return new Result(icon, DISK);
        }
    }

    private Picasso picasso() {
        if (mPicasso == null) {
            mPicasso = new Picasso.Builder(mContext)
                    .addRequestHandler(new PackageIconRequestHandler(mContext))
                    .addRequestHandler(new IconDbRequestHandler(mContext))
                    .indicatorsEnabled(BuildConfig.DEBUG)
                    .build();
        }
        return mPicasso;
    }

    public RequestCreator retrieve(Uri uri) {
        if (mIconSize == -1) {
            mIconSize = mContext.getResources().getDimensionPixelSize(R.dimen.icon_size);
        }
        return picasso().load(uri)
                .resize(mIconSize, mIconSize)
                .centerInside()
                .onlyScaleDown();
    }


    public RequestCreator retrieve(String imageUrl) {
        if (mIconSize == -1) {
            mIconSize = mContext.getResources().getDimensionPixelSize(R.dimen.icon_size);
        }
        return picasso().load(imageUrl)
                .resize(mIconSize, mIconSize)
                .centerInside()
                .onlyScaleDown();
    }


    public void shutdown() {
        if (mPicasso != null) {
            mPicasso.shutdown();
            mPicasso = null;
        }
    }
}