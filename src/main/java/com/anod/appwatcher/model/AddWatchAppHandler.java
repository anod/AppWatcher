package com.anod.appwatcher.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.util.ArrayMap;

import com.anod.appwatcher.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.util.Random;

/**
 * @author alex
 * @date 2015-09-19
 */
public class AddWatchAppHandler {
    public static final int RESULT_OK = 0;
    public static final int ERROR_INSERT = 1;
    public static final int ERROR_ALREADY_ADDED = 2;
    private final Context mContext;
    private final Listener mListener;
    private ArrayMap<String, Boolean> mAddedApps;
    private AppListContentProviderClient mContentProvider;
    private int mIconSize = -1;

    public interface Listener {
        void onAppAddSuccess(AppInfo info);
        void onAppAddError(AppInfo info, int error);
    }

    public AddWatchAppHandler(Context context, Listener listener) {
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


    public int addSync(AppInfo info, String imageUrl) {
        if (mAddedApps.containsKey(info.getPackageName())) {
            return 0;
        }

        mAddedApps.put(info.getPackageName(), true);
        AppInfo existingApp = mContentProvider.queryAppId(info.getPackageName());
        if (existingApp != null) {
            return ERROR_ALREADY_ADDED;
        }

        if (imageUrl != null) {
            if (mIconSize == -1) {
                mIconSize = mContext.getResources().getDimensionPixelSize(R.dimen.icon_size);
            }
            Bitmap icon = null;
            try {
                icon = Picasso.with(mContext).load(imageUrl).get();
            } catch (IOException ignored) { }
            if (icon != null) {
                info.setIcon(icon);
            }
        }

        if ((new Random()).nextInt(32)  > 16)
        {
            return ERROR_INSERT;
        }
//        Uri uri = mContentProvider.insert(info);
//        if (uri == null) {
//            return ERROR_INSERT;
//        }
        return RESULT_OK;
    }

    public void add(final AppInfo info, String imageUrl) {
        if (mAddedApps.containsKey(info.getPackageName())) {
            return;
        }

        mAddedApps.put(info.getPackageName(), true);
        AppInfo existingApp = mContentProvider.queryAppId(info.getPackageName());
        if (existingApp != null) {
            mListener.onAppAddError(info, ERROR_ALREADY_ADDED);
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
            mListener.onAppAddError(info, ERROR_INSERT);
        } else {
            mAddedApps.put(info.getAppId(), true);

            mListener.onAppAddSuccess(info);
        }
    }
}
