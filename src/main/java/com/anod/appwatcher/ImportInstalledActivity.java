package com.anod.appwatcher;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.support.v4.util.SimpleArrayMap;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

import com.anod.appwatcher.adapters.AppViewHolder;
import com.anod.appwatcher.adapters.InstalledAppViewHolder;
import com.anod.appwatcher.adapters.InstalledAppsAdapter;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.model.AppListContentProviderClient;
import com.anod.appwatcher.ui.ToolbarActivity;
import com.anod.appwatcher.utils.PackageManagerUtils;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * @author algavris
 * @date 19/04/2016.
 */
public class ImportInstalledActivity extends ToolbarActivity implements AppViewHolder.OnClickListener {


    @Bind(android.R.id.list)
    RecyclerView mList;

    private PackageManagerUtils mPMUtils;
    private boolean mAllSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_installed);
        ButterKnife.bind(this);
        setupToolbar();

        mPMUtils = new PackageManagerUtils(getPackageManager());
        mList.setLayoutManager(new LinearLayoutManager(this));
        mList.setAdapter(new ImportAdapter(this, mPMUtils, this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshInstalledList();
    }

    @Override
    public void onItemClick(AppInfo app) {

    }

    @OnClick(android.R.id.button3)
    public void onAllButtonClick()
    {
        ImportAdapter importAdapter = (ImportAdapter)mList.getAdapter();
        mAllSelected = !mAllSelected;
        importAdapter.selectAll(mAllSelected);
        importAdapter.notifyDataSetChanged();
    }

    @OnClick(android.R.id.button2)
    public void onCancelButtonClick()
    {
        finish();
    }

    @OnClick(android.R.id.button1)
    public void onImportButtonClick()
    {
        finish();
    }


    protected void refreshInstalledList() {
        // TODO: Background?
        AppListContentProviderClient cr = new AppListContentProviderClient(this);
        Map<String, Integer> watchingPackages = cr.queryPackagesMap();
        cr.release();

        ImportAdapter downloadedAdapter = (ImportAdapter)mList.getAdapter();
        downloadedAdapter.clear();
        downloadedAdapter.addAll(mPMUtils.getDownloadedApps(watchingPackages));

    }

    static class ImportAdapter extends InstalledAppsAdapter {
        private SimpleArrayMap<String, Boolean> mSelectedPackages;

        public ImportAdapter(Context context, PackageManagerUtils pmutils, AppViewHolder.OnClickListener listener) {
            super(context, pmutils, listener);
            mSelectedPackages = new SimpleArrayMap<>();
        }

        @Override
        public AppViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(mContext).inflate(R.layout.list_item_import_app, parent, false);
            v.setClickable(true);
            v.setFocusable(true);

            return new ImportAppViewHolder(v, mDataProvider, mListener);
        }

        public void selectAll(boolean select) {
            // TDOD:
        }
    }


    static class ImportAppViewHolder extends AppViewHolder
    {
        public ImportAppViewHolder(View itemView, DataProvider dataProvider, OnClickListener listener) {
            super(itemView, dataProvider, listener);
        }

        @Override
        public void bindView(int position, AppInfo app) {
            title.setText(app.getTitle());
        }

        @Override
        protected void bindPriceView(AppInfo app) { }

        @Override
        protected void bindSectionView() { }

        @Override
        public void onClick(View v) {
            super.onClick(v);
            ((CheckedTextView)title).toggle();
        }
    }



}
