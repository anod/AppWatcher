package com.anod.appwatcher.installed;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.CheckedTextView;

import com.anod.appwatcher.R;
import com.anod.appwatcher.adapters.AppViewHolder;
import com.anod.appwatcher.model.AppInfo;

/**
 * @author alex
 * @date 2015-08-31
 */
class ImportAppViewHolder extends AppViewHolder {
    private final ImportDataProvider mDataProvider;

    public ImportAppViewHolder(View itemView, ImportDataProvider dataProvider) {
        super(itemView, dataProvider, null);
        mDataProvider = dataProvider;
    }

    @Override
    public void bindView(int position, AppInfo app) {
        this.app = app;
        title.setText(app.getTitle());
        boolean checked = mDataProvider.isPackageSelected(app.getPackageName());
        ((CheckedTextView) title).setChecked(checked);
        title.setEnabled(!mDataProvider.isImportStarted());
    }

    public int status()
    {
       return mDataProvider.getPackageStatus(app.getPackageName());
    }

    @Override
    protected void bindPriceView(AppInfo app) {
    }

    @Override
    protected void bindSectionView() {
    }

    @Override
    public void onClick(View v) {
        ((CheckedTextView) title).toggle();
        mDataProvider.selectPackage(this.app.getPackageName(), ((CheckedTextView) title).isChecked());
    }
}