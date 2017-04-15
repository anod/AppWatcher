package com.anod.appwatcher.installed;

import android.graphics.Color;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.ImageView;

import com.anod.appwatcher.R;
import com.anod.appwatcher.adapters.AppViewHolderBase;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.utils.AppIconLoader;

import butterknife.BindColor;
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

    @BindColor(R.color.theme_accent)
    int themeAccent;
    @BindColor(R.color.material_red_800)
    int materialRed;

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

        mIconLoader.loadAppIntoImageView(app, this.icon, R.drawable.ic_android_black_24dp);

        if (status() == ImportDataProvider.STATUS_DONE) {
            itemView.setBackgroundColor(themeAccent);
        } else if (status() == ImportDataProvider.STATUS_ERROR) {
            itemView.setBackgroundColor(materialRed);
        } else {
            itemView.setBackgroundColor(Color.TRANSPARENT);
        }
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