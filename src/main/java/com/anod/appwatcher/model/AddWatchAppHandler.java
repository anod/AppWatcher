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


    int addSync(AppInfo info) {
        if (mAddedApps.containsKey(info.packageName)) {
            return 0;
        }

        mAddedApps.put(info.packageName, true);
        AppInfo existingApp = mContentProvider.queryAppId(info.packageName);
        if (existingApp != null) {
            if (existingApp.getStatus() == AppInfoMetadata.STATUS_DELETED) {
                int success = mContentProvider.updateStatus(existingApp.getRowId(), AppInfoMetadata.STATUS_NORMAL);
                if (success > 0) {
                    return RESULT_OK;
                } else {
                    return ERROR_INSERT;
                }
            }
            return ERROR_ALREADY_ADDED;
        }

        Uri uri = mContentProvider.insert(info);
        if (uri == null) {
            return ERROR_INSERT;
        }
        return RESULT_OK;
    }

    public void add(final AppInfo info) {
        if (mAddedApps.containsKey(info.packageName)) {
            return;
        }

        mAddedApps.put(info.packageName, true);
        AppInfo existingApp = mContentProvider.queryAppId(info.packageName);
        if (existingApp != null) {
            if (existingApp.getStatus() == AppInfoMetadata.STATUS_DELETED) {
                int success = mContentProvider.updateStatus(existingApp.getRowId(), AppInfoMetadata.STATUS_NORMAL);
                if (success > 0) {
                    mListener.onAppAddSuccess(info);
                } else {
                    mListener.onAppAddError(info, ERROR_INSERT);
                }
                return;
            }
            mListener.onAppAddError(info, ERROR_ALREADY_ADDED);
            return;
        }

        insertApp(info);
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
