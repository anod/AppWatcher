package com.anod.appwatcher.installed;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.CheckedTextView;

import com.anod.appwatcher.R;
import com.anod.appwatcher.adapters.AppViewHolder;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.utils.AppIconLoader;

/**
 * @author alex
 * @date 2015-08-31
 */
class ImportAppViewHolder extends AppViewHolder {
    private final ImportDataProvider mDataProvider;

    public ImportAppViewHolder(View itemView, ImportDataProvider dataProvider, AppIconLoader iconLoader) {
        super(itemView, dataProvider, iconLoader, null);
        mDataProvider = dataProvider;
    }

    @Override
    public void bindView(int position, AppInfo app) {
        this.app = app;
        title.setText(app.title);
        boolean checked = mDataProvider.isPackageSelected(app.packageName);
        ((CheckedTextView) title).setChecked(checked);
        title.setEnabled(!mDataProvider.isImportStarted());

        this.bindIcon(app);
    }

    public int status()
    {
       return mDataProvider.getPackageStatus(app.packageName);
    }

    @Override
    public void onClick(View v) {
        ((CheckedTextView) title).toggle();
        mDataProvider.selectPackage(this.app.packageName, ((CheckedTextView) title).isChecked());
    }
}