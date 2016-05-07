package com.anod.appwatcher.installed;

import android.content.Context;
import android.support.v4.util.SimpleArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.anod.appwatcher.R;
import com.anod.appwatcher.adapters.AppViewHolder;
import com.anod.appwatcher.utils.PackageManagerUtils;

class ImportAdapter extends InstalledAppsAdapter {
    private final ImportDataProvider mDataProvider;
    private SimpleArrayMap<String, Integer> mPackageIndex;

    public ImportAdapter(Context context, PackageManagerUtils pmUtils, ImportDataProvider dataProvider) {
        super(context, pmUtils, dataProvider, null);
        mDataProvider = dataProvider;
    }

    @Override
    public AppViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.list_item_import_app, parent, false);
        v.setClickable(true);
        v.setFocusable(true);

        return new ImportAppViewHolder(v, mDataProvider, mIconLoader);
    }

    public void clearPackageIndex() {
        mPackageIndex = new SimpleArrayMap<>();
    }

    public void storePackageIndex(String packageName, int idx) {
        mPackageIndex.put(packageName, idx);
    }

    public void notifyPackageStatusChanged(String packageName) {
        notifyItemChanged(mPackageIndex.get(packageName));
    }
}