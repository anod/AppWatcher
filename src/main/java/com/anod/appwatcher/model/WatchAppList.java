package com.anod.appwatcher.model;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.util.SimpleArrayMap;


/**
 * @author alex
 * @date 2015-09-19
 */
public class WatchAppList {
    public static final int RESULT_OK = 0;
    public static final int ERROR_INSERT = 1;
    public static final int ERROR_ALREADY_ADDED = 2;
    public static final int ERROR_DELETE = 3;

    private final Listener mListener;
    private SimpleArrayMap<String, Integer> mAddedApps;
    private AppListContentProviderClient mContentProvider;

    public interface Listener {
        void onWatchListChangeSuccess(AppInfo info, int newStatus);
        void onWatchListChangeError(AppInfo info, int error);
    }

    public WatchAppList(Listener listener) {
        mAddedApps = new SimpleArrayMap<>();
        mListener = listener;
    }

    public void attach(Context context) {
        attach(new AppListContentProviderClient(context));
    }

    private void attach(@NonNull AppListContentProviderClient contentProvider) {
        mContentProvider = contentProvider;
        mAddedApps = mContentProvider.queryPackagesMap(false);
    }

    public void detach()
    {
        if (mContentProvider != null){
            mContentProvider.release();
        }
        mContentProvider = null;
    }

    public boolean contains(String packageName) {
        return mAddedApps.containsKey(packageName);
    }


    int addSync(AppInfo info) {
        if (mAddedApps.containsKey(info.packageName)) {
            return 0;
        }

        mAddedApps.put(info.packageName, -1);
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

        mAddedApps.put(info.packageName, -1);
        AppInfo existingApp = mContentProvider.queryAppId(info.packageName);
        if (existingApp != null) {
            if (existingApp.getStatus() == AppInfoMetadata.STATUS_DELETED) {
                int success = mContentProvider.updateStatus(existingApp.getRowId(), AppInfoMetadata.STATUS_NORMAL);
                if (success > 0) {
                    mListener.onWatchListChangeSuccess(info, AppInfoMetadata.STATUS_NORMAL);
                } else {
                    mListener.onWatchListChangeError(info, ERROR_INSERT);
                }
                return;
            }
            mListener.onWatchListChangeError(info, ERROR_ALREADY_ADDED);
            return;
        }

        insertApp(info);
    }

    public void delete(AppInfo info)
    {
        if (!mAddedApps.containsKey(info.packageName)) {
            return;
        }

        AppInfo existingApp = mContentProvider.queryAppId(info.packageName);
        if (existingApp != null) {
            int success = mContentProvider.updateStatus(existingApp.getRowId(), AppInfoMetadata.STATUS_DELETED);
            if (success > 0) {
                mAddedApps.remove(info.packageName);
                mListener.onWatchListChangeSuccess(info, AppInfoMetadata.STATUS_DELETED);
            } else {
                mListener.onWatchListChangeError(info, ERROR_DELETE);
            }
        }
    }

    private void insertApp(final AppInfo info) {
        Uri uri = mContentProvider.insert(info);

        if (uri == null) {
            mListener.onWatchListChangeError(info, ERROR_INSERT);
        } else {
            mAddedApps.put(info.getAppId(), -1);
            mListener.onWatchListChangeSuccess(info, AppInfoMetadata.STATUS_NORMAL);
        }
    }

}
