package com.anod.appwatcher.installed;

import android.view.View;

import com.anod.appwatcher.R;
import com.anod.appwatcher.adapters.AppViewHolder;
import com.anod.appwatcher.model.AppInfo;

/**
 * @author alex
 * @date 2015-08-31
 */
public class InstalledAppViewHolder extends AppViewHolder {

    private final DataProvider mDataProvider;

    public InstalledAppViewHolder(View itemView, DataProvider dataProvider, OnClickListener listener) {
        super(itemView, dataProvider, listener);
        mDataProvider = dataProvider;
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
