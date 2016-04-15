package com.anod.appwatcher.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.util.ArrayMap;
import android.view.View;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.anod.appwatcher.AppWatcherApplication;
import com.anod.appwatcher.R;
import com.anod.appwatcher.utils.DocUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import info.anodsplace.android.log.AppLog;

/**
 * @author alex
 * @date 2015-09-19
 */
public class NewWatchAppHandler {
    public static final int ERROR_INSERT = 1;
    public static final int ERROR_ALEREADY_ADDED = 2;
    private final Context mContext;
    private final Listener mListener;
    private ArrayMap<String, Boolean> mAddedApps;
    private AppListContentProviderClient mContentProvider;
    private int mIconSize = -1;

    public interface Listener {
        void onAppAddSuccess(AppInfo info);
        void onAppAddError(int error);
    }

    public NewWatchAppHandler(Context context, Listener listener) {
        mContext = context;
        mAddedApps = new ArrayMap<>();
        mListener = listener;
    }

    public void setContentProvider(AppListContentProviderClient contentProvider) {
        mContentProvider = contentProvider;
    }

    public boolean isAdded(String packageName) {
        return mAddedApps.containsKey(packageName);
    }

    public void add(final AppInfo info, String imageUrl) {
        if (mAddedApps.containsKey(info.getPackageName())) {
            return;
        }

        mAddedApps.put(info.getPackageName(), true);
        AppInfo existingApp = mContentProvider.queryAppId(info.getPackageName());
        if (existingApp != null) {
            mListener.onAppAddError(ERROR_ALEREADY_ADDED);
            return;
        }

        if (imageUrl != null) {
            if (mIconSize == -1) {
                mIconSize = mContext.getResources().getDimensionPixelSize(R.dimen.icon_size);
            }
            Picasso.with(mContext).load(imageUrl).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    info.setIcon(bitmap);
                    insertApp(info);
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                    insertApp(info);
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) { }
            });
        } else {
            insertApp(info);
        }

    }

    private void insertApp(final AppInfo info) {
        Uri uri = mContentProvider.insert(info);

        if (uri == null) {
            mListener.onAppAddError(ERROR_INSERT);
        } else {
            mAddedApps.put(info.getAppId(), true);

            mListener.onAppAddSuccess(info);
        }
    }
}
