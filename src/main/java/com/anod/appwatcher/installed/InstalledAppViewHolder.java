package com.anod.appwatcher.installed;

import android.view.View;

import com.anod.appwatcher.R;
import com.anod.appwatcher.adapters.AppViewHolder;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.utils.AppIconLoader;

/**
 * @author alex
 * @date 2015-08-31
 */
class InstalledAppViewHolder extends AppViewHolder {

    InstalledAppViewHolder(View itemView, DataProvider dataProvider, AppIconLoader iconLoader, OnClickListener listener) {
        super(itemView, dataProvider, iconLoader, listener);
        mIsLocalApp = true;
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
