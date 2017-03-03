package com.anod.appwatcher.installed;

import android.content.Context;
import android.support.v4.util.SimpleArrayMap;

import com.anod.appwatcher.adapters.AppViewHolderDataProvider;
import com.anod.appwatcher.utils.InstalledAppsProvider;

class ImportDataProvider extends AppViewHolderDataProvider {

    static final int STATUS_DEFAULT = 0;
    static final int STATUS_IMPORTING = 1;
    static final int STATUS_DONE = 2;
    static final int STATUS_ERROR = 3;

    private SimpleArrayMap<String, Boolean> mSelectedPackages = new SimpleArrayMap<>();
    private boolean mDefaultSelected;
    private SimpleArrayMap<String, Integer> mProcessingPackages = new SimpleArrayMap<>();
    private boolean mImportStarted;

    ImportDataProvider(Context context, InstalledAppsProvider installedAppsProvider) {
        super(context, installedAppsProvider);
    }

    void selectAllPackages(boolean select) {
        mSelectedPackages.clear();
        mDefaultSelected = select;
    }

    void selectPackage(String packageName, boolean select) {
        mSelectedPackages.put(packageName, select);
    }

    boolean isPackageSelected(String packageName) {
        if (mSelectedPackages.containsKey(packageName)) {
            return mSelectedPackages.get(packageName);
        }
        return mDefaultSelected;
    }

    int getPackageStatus(String packageName) {
        if (mProcessingPackages.containsKey(packageName)) {
            return mProcessingPackages.get(packageName);
        }
        return STATUS_DEFAULT;
    }

    void setPackageStatus(String packageName, int status) {
        mProcessingPackages.put(packageName, status);
    }

    void setImportStarted(boolean started) {
        this.mImportStarted = started;
    }

    boolean isImportStarted() {
        return mImportStarted;
    }
}