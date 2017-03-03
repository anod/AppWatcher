package com.anod.appwatcher.installed;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.util.SimpleArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.anod.appwatcher.R;
import com.anod.appwatcher.adapters.AppViewHolderBase;

class ImportAdapter extends InstalledAppsAdapter {
    private final ImportDataProvider mDataProvider;
    private SimpleArrayMap<String, Integer> mPackageIndex;

    ImportAdapter(Context context, PackageManager pm, ImportDataProvider dataProvider) {
        super(context, pm, dataProvider, null);
        mDataProvider = dataProvider;
    }

    @Override
    public AppViewHolderBase onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.list_item_import_app, parent, false);
        v.setClickable(true);
        v.setFocusable(true);

        return new ImportAppViewHolder(v, mDataProvider, mIconLoader);
    }

    void clearPackageIndex() {
        mPackageIndex = new SimpleArrayMap<>();
    }

    void storePackageIndex(String packageName, int idx) {
        mPackageIndex.put(packageName, idx);
    }

    void notifyPackageStatusChanged(String packageName) {
        notifyItemChanged(mPackageIndex.get(packageName));
    }
}