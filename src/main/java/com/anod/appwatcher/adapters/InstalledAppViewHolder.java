package com.anod.appwatcher.adapters;

import android.view.View;

import com.anod.appwatcher.R;

/**
 * @author alex
 * @date 2015-08-31
 */
public class InstalledAppViewHolder extends AppViewHolder {

    public InstalledAppViewHolder(View itemView, DataProvider dataProvider, OnClickListener listener) {
        super(itemView, dataProvider, listener);
    }

    @Override
    protected void bindSectionView() {
        if (position == 0) {
            sectionText.setText(R.string.on_device);
            sectionCount.setText(mDataProvider.getTotalAppsCount());
            section.setVisibility(View.VISIBLE);
        } else {
            section.setVisibility(View.GONE);
        }
    }
}
