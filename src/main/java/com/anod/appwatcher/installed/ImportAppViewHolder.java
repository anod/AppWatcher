package com.anod.appwatcher.installed;

import android.view.View;
import android.widget.CheckedTextView;
import android.widget.ImageView;

import com.anod.appwatcher.adapters.AppViewHolderBase;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.utils.AppIconLoader;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author alex
 * @date 2015-08-31
 */
class ImportAppViewHolder extends AppViewHolderBase implements View.OnClickListener {
    private final ImportDataProvider mDataProvider;

    public AppInfo app;
    @BindView(android.R.id.title)
    public CheckedTextView title;
    @BindView(android.R.id.icon)
    public ImageView icon;

    ImportAppViewHolder(View itemView, ImportDataProvider dataProvider, AppIconLoader iconLoader) {
        super(itemView, dataProvider, iconLoader);
        mDataProvider = dataProvider;
        ButterKnife.bind(this, itemView);
    }

    public void bindView(int position, AppInfo app) {
        this.app = app;
        this.title.setText(app.title);
        boolean checked = mDataProvider.isPackageSelected(app.packageName);
        this.title.setChecked(checked);
        title.setEnabled(!mDataProvider.isImportStarted());

        itemView.findViewById(android.R.id.content).setOnClickListener(this);

        this.bindIcon(app, this.icon);
    }

    public int status() {
        return mDataProvider.getPackageStatus(app.packageName);
    }

    @Override
    public void onClick(View v) {
        this.title.toggle();
        mDataProvider.selectPackage(this.app.packageName, title.isChecked());
    }
}