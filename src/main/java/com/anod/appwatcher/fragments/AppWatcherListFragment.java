package com.anod.appwatcher.fragments;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.anod.appwatcher.BuildConfig;
import com.anod.appwatcher.ChangelogActivity;
import com.anod.appwatcher.MarketSearchActivity;
import com.anod.appwatcher.R;
import com.anod.appwatcher.adapters.AppListCursorAdapterWrapper;
import com.anod.appwatcher.adapters.AppViewHolder;
import com.anod.appwatcher.content.AppListCursor;
import com.anod.appwatcher.installed.ImportInstalledActivity;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.model.AppListCursorLoader;
import com.anod.appwatcher.model.Filters;
import com.anod.appwatcher.model.InstalledFilter;
import com.anod.appwatcher.model.Tag;
import com.anod.appwatcher.ui.AppWatcherBaseActivity;
import com.anod.appwatcher.utils.InstalledAppsProvider;
import com.anod.appwatcher.utils.IntentUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;
import info.anodsplace.android.log.AppLog;
import info.anodsplace.android.widget.recyclerview.MergeRecyclerAdapter;

public class AppWatcherListFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        AppWatcherBaseActivity.EventListener,
        AppViewHolder.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    static final String ARG_FILTER = "filter";
    static final String ARG_SORT = "sort";
    static final String ARG_TAG = "tag";
    static final String ARG_SECTION_PROVIDER = "section";

    private static final int REQUEST_APP_INFO = 1;

    protected String mTitleFilter = "";
    protected InstalledAppsProvider mInstalledApps;
    protected MergeRecyclerAdapter mAdapter;
    protected int mSortId;
    protected int mFilterId;
    protected Tag mTag;

    private int mListenerIndex;

    @BindView(android.R.id.list)
    RecyclerView mListView;
    @BindView(R.id.progress)
    View mProgressContainer;
    @BindView(android.R.id.empty)
    View mEmptyView;
    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout mSwipeLayout;

    private SectionProvider mSection;

    public static AppWatcherListFragment newInstance(int filterId, int sortId, SectionProvider sectionProvider, @Nullable Tag tag) {
        AppWatcherListFragment frag = new AppWatcherListFragment();
        frag.setArguments(createArguments(filterId, sortId, sectionProvider, tag));
        return frag;
    }

    public interface SectionProvider {
        void fillAdapters(MergeRecyclerAdapter adapter, Context context, InstalledAppsProvider installedApps, AppViewHolder.OnClickListener clickListener);
        Loader<Cursor> createLoader(Context context, String titleFilter, int sortId, InstalledFilter filter, Tag tag);
        void loadFinished(MergeRecyclerAdapter adapter, Loader<Cursor> loader, Cursor data);
        void loaderReset(MergeRecyclerAdapter adapter);
    }

    public static class DefaultSection implements SectionProvider {
        private static final int ADAPTER_WATCHLIST = 0;

        @Override
        public void fillAdapters(MergeRecyclerAdapter adapter, Context context, InstalledAppsProvider installedApps, AppViewHolder.OnClickListener clickListener) {
            adapter.addAdapter(ADAPTER_WATCHLIST, new AppListCursorAdapterWrapper(context, installedApps, clickListener));
        }

        @Override
        public Loader<Cursor> createLoader(Context context, String titleFilter, int sortId, InstalledFilter filter, Tag tag) {
            return new AppListCursorLoader(context, titleFilter, sortId, filter, tag);
        }

        @Override
        public void loadFinished(MergeRecyclerAdapter adapter, Loader<Cursor> loader, Cursor data) {
            AppListCursorAdapterWrapper watchlistAdapter = ((AppListCursorAdapterWrapper) adapter.getAdapter(ADAPTER_WATCHLIST));
            watchlistAdapter.swapData((AppListCursor) data);

            int newCount = ((AppListCursorLoader) loader).getNewCountFiltered();
            int updatableCount = ((AppListCursorLoader) loader).getUpdatableCountFiltered();

            watchlistAdapter.setNewAppsCount(newCount, updatableCount);
        }

        @Override
        public void loaderReset(MergeRecyclerAdapter adapter) {
            // This is called when the last Cursor provided to onLoadFinished()
            // above is about to be closed.  We need to make sure we are no
            // longer using it.
            AppListCursorAdapterWrapper watchlistAdapter = ((AppListCursorAdapterWrapper) adapter.getAdapter(ADAPTER_WATCHLIST));
            watchlistAdapter.swapData(null);
        }
    }

    protected static Bundle createArguments(int filterId, int sortId,@NonNull SectionProvider sectionProvider, @Nullable Tag tag)
    {
        Bundle args = new Bundle();
        args.putInt(ARG_FILTER, filterId);
        args.putInt(ARG_SORT, sortId);
        args.putString(ARG_SECTION_PROVIDER, sectionProvider.getClass().getName());
        if (tag != null)
        {
            args.putParcelable(ARG_TAG, tag);
        }
        return args;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        AppWatcherBaseActivity act = (AppWatcherBaseActivity) context;
        mListenerIndex = act.addQueryChangeListener(this);
        AppLog.d("addQueryChangeListener with index: %d", mListenerIndex);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        AppWatcherBaseActivity act = (AppWatcherBaseActivity) getActivity();
        act.removeQueryChangeListener(mListenerIndex);
    }

    public void setListVisible(boolean visible) {
        if (visible) {
            mProgressContainer.setVisibility(View.GONE);
            if (mAdapter.getItemCount() == 0){
                mEmptyView.setVisibility(View.VISIBLE);
                mListView.setVisibility(View.INVISIBLE);
            } else {
                mEmptyView.setVisibility(View.GONE);
                mListView.setVisibility(View.VISIBLE);
            }
        } else {
            mProgressContainer.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.INVISIBLE);
            mEmptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflateView(inflater, container, savedInstanceState);
        ButterKnife.bind(this, root);
        mEmptyView.setVisibility(View.GONE);
        mSwipeLayout.setOnRefreshListener(this);
        return root;
    }

    protected View inflateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_applist, container, false);
    }

    public SectionProvider sectionForClassName(String sectionClassName)
    {
        try {
            Class<SectionProvider> sectionClass = (Class<SectionProvider>) Class.forName(sectionClassName);
            return sectionClass.newInstance();
        } catch (Exception e) {
            AppLog.e(e);
        }
        return null;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mInstalledApps = new InstalledAppsProvider.PackageManager(getActivity().getPackageManager());

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mListView.setLayoutManager(layoutManager);

        mSection = sectionForClassName(getArguments().getString(ARG_SECTION_PROVIDER));

        // Create an empty adapter we will use to display the loaded data.
        mAdapter = new MergeRecyclerAdapter();
        mSection.fillAdapters(mAdapter, getActivity(), mInstalledApps, this);
        mListView.setAdapter(mAdapter);

        // Start out with a progress indicator.
        setListVisible(false);

        mSortId = getArguments().getInt(ARG_SORT);
        mFilterId = getArguments().getInt(ARG_FILTER);
        mTag = getArguments().getParcelable(ARG_TAG);
        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return mSection.createLoader(getActivity(), mTitleFilter, mSortId, createFilter(mFilterId), mTag);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mSection.loadFinished(mAdapter, loader, data);
        setListVisible(true);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mSection.loaderReset(mAdapter);
    }

    protected InstalledFilter createFilter(int filterId) {
        if (filterId == Filters.TAB_INSTALLED) {
            return new InstalledFilter(true, mInstalledApps);
        } else if (filterId == Filters.TAB_UNINSTALLED) {
            return new InstalledFilter(false, mInstalledApps);
        }
        return null;
    }

    @Override
    public void onSortChanged(int sortIndex)
    {
        mSortId = sortIndex;
        restartLoader();
    }

    @Override
    public void onQueryTextChanged(String newQuery) {
        String newFilter = !TextUtils.isEmpty(newQuery) ? newQuery : "";
        if (!TextUtils.equals(newFilter, mTitleFilter)) {
            mTitleFilter = newFilter;
            restartLoader();
        }
    }

    @Override
    public void onSyncStart() {
        if (mSwipeLayout!= null){
            mSwipeLayout.setRefreshing(true);
        }
    }

    @Override
    public void onSyncFinish() {
        if (mSwipeLayout!=null) {
            mSwipeLayout.setRefreshing(false);
        }
    }

    @Override
    public void onItemClick(AppInfo app) {
        Intent intent = new Intent(getActivity(), ChangelogActivity.class);
        intent.putExtra(ChangelogActivity.EXTRA_APP_ID, app.getAppId());
        intent.putExtra(ChangelogActivity.EXTRA_ROW_ID, app.getRowId());
        intent.putExtra(ChangelogActivity.EXTRA_DETAILS_URL, app.getDetailsUrl());
        startActivityForResult(intent, REQUEST_APP_INFO);

        if (BuildConfig.DEBUG) {
            AppLog.d(app.packageName);
            Toast.makeText(getActivity(), app.packageName, Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onActionButton() {
        IntentUtils.startActivitySafely(getContext(), IntentUtils.createMyAppsIntent(true));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_APP_INFO && resultCode == Activity.RESULT_OK) {
            if (data.getExtras() != null) {
                restartLoader();
            }
        }
    }

    @OnClick(android.R.id.button1)
    @Optional
    public void onSearchButton() {
        Intent searchIntent = new Intent(getActivity(), MarketSearchActivity.class);
        searchIntent.putExtra(MarketSearchActivity.EXTRA_KEYWORD, "");
        searchIntent.putExtra(MarketSearchActivity.EXTRA_FOCUS, true);
        startActivity(searchIntent);
    }

    @OnClick(android.R.id.button2)
    @Optional
    public void onImportButton() {
        startActivity(new Intent(getActivity(), ImportInstalledActivity.class));
    }

    @OnClick(android.R.id.button3)
    @Optional
    public void onShareButton() {
        Intent intent = Intent.makeMainActivity(new ComponentName("com.android.vending", "com.android.vending.AssetBrowserActivity"));
        IntentUtils.startActivitySafely(getActivity(), intent);
    }

    @Override
    public void onRefresh() {
        ((AppWatcherBaseActivity)getActivity()).requestRefresh();
    }

    private void restartLoader() {
        if (isResumed()) {
            getLoaderManager().restartLoader(0, null, this);
        }
    }
}
