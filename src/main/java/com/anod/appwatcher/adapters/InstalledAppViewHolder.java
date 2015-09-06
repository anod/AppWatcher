package com.anod.appwatcher.adapters;

import android.text.TextUtils;
import android.view.View;

import com.anod.appwatcher.R;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.utils.PackageManagerUtils;

/**
 * @author alex
 * @date 2015-08-31
 */
public class InstalledAppViewHolder extends AppViewHolder {

    public InstalledAppViewHolder(View itemView, DataProvider dataProvider, OnClickListener listener) {
        super(itemView, dataProvider, listener);
    }

    @Override
    protected void bindPriceView(AppInfo app) {
        price.setVisibility(View.GONE);
    }

    @Override
    protected void bindSectionView() {
        if (position == 0) {
            sectionText.setText(R.string.downloaded);
            sectionCount.setText(String.valueOf(mDataProvider.getTotalAppsCount()));
            section.setVisibility(View.VISIBLE);
        } else {
            section.setVisibility(View.GONE);
        }
    }
}
