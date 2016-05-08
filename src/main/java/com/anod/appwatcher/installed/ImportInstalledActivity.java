package com.anod.appwatcher.installed;

import android.accounts.Account;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.SimpleArrayMap;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.anod.appwatcher.Preferences;
import com.anod.appwatcher.R;
import com.anod.appwatcher.accounts.AccountChooserHelper;
import com.anod.appwatcher.fragments.AccountChooserFragment;
import com.anod.appwatcher.model.AddWatchAppHandler;
import com.anod.appwatcher.model.AppListContentProviderClient;
import com.anod.appwatcher.ui.ToolbarActivity;
import com.anod.appwatcher.utils.PackageManagerUtils;

import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * @author algavris
 * @date 19/04/2016.
 */
public class ImportInstalledActivity extends ToolbarActivity implements LoaderManager.LoaderCallbacks<List<PackageInfo>>, AccountChooserHelper.OnAccountSelectionListener, ImportBulkManager.Listener {
    @Bind(android.R.id.list)
    RecyclerView mList;
    @Bind(android.R.id.progress)
    ProgressBar mProgress;

    private PackageManagerUtils mPMUtils;
    private boolean mAllSelected;
    private ImportDataProvider mDataProvider;
    private AccountChooserHelper mAccountChooserHelper;
    private ImportBulkManager mImportManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_installed);
        ButterKnife.bind(this);
        setupToolbar();

        mPMUtils = new PackageManagerUtils(getPackageManager());

        mImportManager = new ImportBulkManager(this, this);
        mDataProvider = new ImportDataProvider(this, mPMUtils);

        mList.setLayoutManager(new LinearLayoutManager(this));
        mList.setAdapter(new ImportAdapter(this, mPMUtils, mDataProvider));
        mList.setItemAnimator(new ImportItemAnimator(this));
        getSupportLoaderManager().initLoader(0, null, this).forceLoad();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mAccountChooserHelper = new AccountChooserHelper(this, new Preferences(this), this);
        mAccountChooserHelper.init();
    }

    @OnClick(android.R.id.button3)
    public void onAllButtonClick() {
        ImportAdapter importAdapter = (ImportAdapter) mList.getAdapter();
        mAllSelected = !mAllSelected;
        mDataProvider.selectAllPackages(mAllSelected);
        importAdapter.notifyDataSetChanged();
    }

    @OnClick(android.R.id.button2)
    public void onCancelButtonClick() {
        if (mDataProvider.isImportStarted()) {
            mImportManager.stop();
        }
        finish();
    }

    @OnClick(android.R.id.button1)
    public void onImportButtonClick() {
        ButterKnife.findById(this, android.R.id.button3).setVisibility(View.GONE);
        ButterKnife.findById(this, android.R.id.button1).setVisibility(View.GONE);
        ImportAdapter adapter = (ImportAdapter) mList.getAdapter();

        mImportManager.init();
        adapter.clearPackageIndex();
        for (int idx = 0; idx < adapter.getItemCount(); idx++) {
            PackageInfo packageInfo = adapter.getItem(idx);
            if (mDataProvider.isPackageSelected(packageInfo.packageName)) {
                mImportManager.addPackage(packageInfo.packageName);
                adapter.storePackageIndex(packageInfo.packageName, idx);
            }
        }
        if (mImportManager.isEmpty()) {
            finish();
            return;
        }
        mImportManager.start();
    }

    @Override
    public Loader<List<PackageInfo>> onCreateLoader(int id, Bundle args) {
        return new LocalPackageLoader(this, mPMUtils);
    }

    @Override
    public void onLoadFinished(Loader<List<PackageInfo>> loader, List<PackageInfo> data) {
        mProgress.setVisibility(View.GONE);
        ImportAdapter downloadedAdapter = (ImportAdapter) mList.getAdapter();
        downloadedAdapter.clear();
        downloadedAdapter.addAll(data);
    }

    @Override
    public void onLoaderReset(Loader<List<PackageInfo>> loader) {
        ImportAdapter downloadedAdapter = (ImportAdapter) mList.getAdapter();
        downloadedAdapter.clear();
    }

    @Override
    public void onHelperAccountSelected(Account account, String authSubToken) {
        if (authSubToken == null) {
            Toast.makeText(this, R.string.failed_gain_access, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        mImportManager.setAccount(account, authSubToken);
    }

    @Override
    public void onHelperAccountNotFound() {
        Toast.makeText(this, R.string.failed_gain_access, Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public AccountChooserFragment.OnAccountSelectionListener getAccountSelectionListener() {
        return mAccountChooserHelper;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mAccountChooserHelper.onRequestPermissionResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onImportProgress(List<String> docIds, SimpleArrayMap<String, Integer> result) {
        ImportAdapter adapter = (ImportAdapter) mList.getAdapter();
        for (String packageName : docIds) {
            Integer resultCode = result.get(packageName);
            int status;
            if (resultCode == null) {
                status = ImportDataProvider.STATUS_ERROR;
            } else {
                status = resultCode == AddWatchAppHandler.RESULT_OK ? ImportDataProvider.STATUS_DONE : ImportDataProvider.STATUS_ERROR;
            }
            mDataProvider.setPackageStatus(packageName, status);
            adapter.notifyPackageStatusChanged(packageName);
        }
    }

    @Override
    public void onImportFinish() {
        ((Button)ButterKnife.findById(this, android.R.id.button2)).setText(android.R.string.ok);
    }

    @Override
    public void onImportStart(List<String> docIds) {
        ImportAdapter adapter = (ImportAdapter) mList.getAdapter();
        mDataProvider.setImportStarted(true);
        for (String packageName : docIds) {
            mDataProvider.setPackageStatus(packageName, ImportDataProvider.STATUS_IMPORTING);
            adapter.notifyPackageStatusChanged(packageName);
        }
    }


    private static class LocalPackageLoader extends AsyncTaskLoader<List<PackageInfo>> {
        private final PackageManagerUtils mPMUtils;

        public LocalPackageLoader(Context context, PackageManagerUtils pmUtils) {
            super(context);
            mPMUtils = pmUtils;
        }

        @Override
        public List<PackageInfo> loadInBackground() {
            AppListContentProviderClient cr = new AppListContentProviderClient(getContext());
            Map<String, Integer> watchingPackages = cr.queryPackagesMap(false);
            cr.release();

            return mPMUtils.getDownloadedApps(watchingPackages);
        }
    }

}
