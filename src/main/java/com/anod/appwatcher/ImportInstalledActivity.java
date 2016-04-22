package com.anod.appwatcher;

import android.accounts.Account;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.SimpleArrayMap;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.anod.appwatcher.accounts.AccountChooserHelper;
import com.anod.appwatcher.adapters.AppViewHolder;
import com.anod.appwatcher.adapters.AppViewHolderDataProvider;
import com.anod.appwatcher.adapters.InstalledAppsAdapter;
import com.anod.appwatcher.fragments.AccountChooserFragment;
import com.anod.appwatcher.market.BulkDetailsEndpoint;
import com.anod.appwatcher.market.PlayStoreEndpoint;
import com.anod.appwatcher.model.AppInfo;
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
public class ImportInstalledActivity extends ToolbarActivity implements LoaderManager.LoaderCallbacks<List<PackageInfo>>, AccountChooserHelper.OnAccountSelectionListener, PlayStoreEndpoint.Listener {

    @Bind(android.R.id.list)
    RecyclerView mList;

    private PackageManagerUtils mPMUtils;
    private boolean mAllSelected;
    private ImportDataProvider mDataProvider;
    private BulkDetailsEndpoint mEndpoint;
    private AccountChooserHelper mAccountChooserHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_installed);
        ButterKnife.bind(this);
        setupToolbar();

        mPMUtils = new PackageManagerUtils(getPackageManager());

        mDataProvider = new ImportDataProvider(this, mPMUtils);
        mEndpoint = new BulkDetailsEndpoint(this);

        mList.setLayoutManager(new LinearLayoutManager(this));
        mList.setAdapter(new ImportAdapter(this, mPMUtils, mDataProvider));
        getSupportLoaderManager().initLoader(0, null, this).forceLoad();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mEndpoint.setListener(this);
        mAccountChooserHelper = new AccountChooserHelper(this, new Preferences(this), this);
        mAccountChooserHelper.init();
    }

    @OnClick(android.R.id.button3)
    public void onAllButtonClick()
    {
        ImportAdapter importAdapter = (ImportAdapter)mList.getAdapter();
        mAllSelected = !mAllSelected;
        mDataProvider.selectAllPackages(mAllSelected);
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
        ButterKnife.findById(this, android.R.id.button3).setVisibility(View.GONE);
        ButterKnife.findById(this, android.R.id.button1).setVisibility(View.GONE);
        mDataProvider.getSelectedPackages();
    }


    @Override
    public Loader<List<PackageInfo>> onCreateLoader(int id, Bundle args) {
        return new LocalPackageLoader(this, mPMUtils);
    }

    @Override
    public void onLoadFinished(Loader<List<PackageInfo>> loader, List<PackageInfo> data) {
        ImportAdapter downloadedAdapter = (ImportAdapter)mList.getAdapter();
        downloadedAdapter.clear();
        downloadedAdapter.addAll(data);
    }

    @Override
    public void onLoaderReset(Loader<List<PackageInfo>> loader) {
        ImportAdapter downloadedAdapter = (ImportAdapter)mList.getAdapter();
        downloadedAdapter.clear();
    }

    @Override
    public void onHelperAccountSelected(Account account, String authSubToken) {
        if (authSubToken == null) {
            Toast.makeText(this, R.string.failed_gain_access, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        mEndpoint.setAccount(account, authSubToken);
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
    public void onDataChanged() {

    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    static class ImportAdapter extends InstalledAppsAdapter {
        private final ImportDataProvider mDataProvider;

        public ImportAdapter(Context context, PackageManagerUtils pmutils, ImportDataProvider dataProvider) {
            super(context, pmutils, dataProvider, null);
            mDataProvider = dataProvider;
        }

        @Override
        public AppViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(mContext).inflate(R.layout.list_item_import_app, parent, false);
            v.setClickable(true);
            v.setFocusable(true);

            return new ImportAppViewHolder(v, mDataProvider);
        }
    }

    static class ImportDataProvider extends AppViewHolderDataProvider
    {
        private SimpleArrayMap<String, Boolean> mSelectedPackages = new SimpleArrayMap<>();
        private boolean mDefaultSelected;

        public ImportDataProvider(Context context, PackageManagerUtils pmutils) {
            super(context, pmutils);
        }

        public void selectAllPackages(boolean select) {
            mSelectedPackages.clear();
            mDefaultSelected = select;
        }

        public void selectPackage(String packageName, boolean select)
        {
            mSelectedPackages.put(packageName, select);
        }

        public boolean isPackageSelected(String packageName)
        {
            if (mSelectedPackages.containsKey(packageName)) {
                return mSelectedPackages.get(packageName);
            }
            return mDefaultSelected;
        }

        public SimpleArrayMap<String, Boolean> getSelectedPackages() {
            return mSelectedPackages;
        }
    }

    static class ImportAppViewHolder extends AppViewHolder
    {
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
            ((CheckedTextView)title).setChecked(checked);
        }

        @Override
        protected void bindPriceView(AppInfo app) { }

        @Override
        protected void bindSectionView() { }

        @Override
        public void onClick(View v) {
            ((CheckedTextView)title).toggle();
            mDataProvider.selectPackage(this.app.getPackageName(), ((CheckedTextView)title).isChecked());
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
            Map<String, Integer> watchingPackages = cr.queryPackagesMap();
            cr.release();

            return mPMUtils.getDownloadedApps(watchingPackages);
        }
    }

}
