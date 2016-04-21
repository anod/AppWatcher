package com.anod.appwatcher.fragments;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
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
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.anod.appwatcher.AppWatcherActivity;
import com.anod.appwatcher.BuildConfig;
import com.anod.appwatcher.ChangelogActivity;
import com.anod.appwatcher.R;
import com.anod.appwatcher.adapters.AppViewHolder;
import com.anod.appwatcher.adapters.ListCursorAdapterWrapper;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.model.AppListCursorLoader;
import com.anod.appwatcher.model.Filters;
import com.anod.appwatcher.model.InstalledFilter;
import com.anod.appwatcher.utils.IntentUtils;
import com.anod.appwatcher.utils.PackageManagerUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import info.anodsplace.android.log.AppLog;
import info.anodsplace.android.widget.recyclerview.MergeRecyclerAdapter;

public class AppWatcherListFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        AppWatcherActivity.EventListener,
        AppViewHolder.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private static final int ADAPTER_WATCHLIST = 0;
    public static final String ARG_FILTER = "filter";
    private static final int REQUEST_APP_INFO = 1;
    protected String mTitleFilter = "";

    @Bind(android.R.id.list)
    RecyclerView mListView;
    @Bind(R.id.progress)
    View mProgressContainer;
    @Bind(android.R.id.empty)
    View mEmptyView;
    @Bind(R.id.swipe_layout)
    SwipeRefreshLayout mSwipeLayout;


    protected InstalledFilter mInstalledFilter;

    protected PackageManagerUtils mPMUtils;

    protected MergeRecyclerAdapter mAdapter;
    private int mListenerIndex;

    public static AppWatcherListFragment newInstance(int filterId) {
        AppWatcherListFragment frag = new AppWatcherListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_FILTER, filterId);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        AppWatcherActivity act = (AppWatcherActivity) context;
        mListenerIndex = act.addQueryChangeListener(this);
        AppLog.d("addQueryChangeListener with index: %d", mListenerIndex);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        AppWatcherActivity act = (AppWatcherActivity) getActivity();
        act.removeQueryChangeListener(mListenerIndex);
    }

    public void setListVisible(boolean visible, boolean animate) {
        if (visible) {
            if (animate) {
                mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_out));
                if (mAdapter.getItemCount() == 0){
                    mEmptyView.startAnimation(AnimationUtils.loadAnimation(
                            getActivity(), android.R.anim.fade_in));
                } else {
                    mListView.startAnimation(AnimationUtils.loadAnimation(
                            getActivity(), android.R.anim.fade_in));
                }
            }
            mProgressContainer.setVisibility(View.GONE);
            if (mAdapter.getItemCount() == 0){
                mEmptyView.setVisibility(View.VISIBLE);
                mListView.setVisibility(View.INVISIBLE);
            } else {
                mEmptyView.setVisibility(View.GONE);
                mListView.setVisibility(View.VISIBLE);
            }
        } else {
            if (animate) {
                mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_in));
                mListView.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_out));
            }
            mProgressContainer.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.INVISIBLE);
            mEmptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_applist, container, false);
        ButterKnife.bind(this, root);
        mEmptyView.setVisibility(View.GONE);
        mSwipeLayout.setOnRefreshListener(this);
        return root;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // We have a menu item to show in action bar.
        setHasOptionsMenu(true);

        mPMUtils = new PackageManagerUtils(getActivity().getPackageManager());

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mListView.setLayoutManager(layoutManager);

        // Create an empty adapter we will use to display the loaded data.
        mAdapter = new MergeRecyclerAdapter();
        mAdapter.addAdapter(ADAPTER_WATCHLIST, new ListCursorAdapterWrapper(getActivity(), mPMUtils, this));
        mListView.setAdapter(mAdapter);

        // Start out with a progress indicator.
        setListVisible(false, true);

        setupFilter(getArguments().getInt(ARG_FILTER));
        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(0, null, this);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new AppListCursorLoader(getActivity(), mTitleFilter, mInstalledFilter);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        ListCursorAdapterWrapper watchlistAdapter = ((ListCursorAdapterWrapper) mAdapter.getAdapter(ADAPTER_WATCHLIST));
        watchlistAdapter.swapData(data);
        if (mInstalledFilter == null) {
            int newCount = ((AppListCursorLoader) loader).getNewCount();
            watchlistAdapter.setNewAppsCount(newCount);
        } else {
            int newCount = mInstalledFilter.getNewCount();
            watchlistAdapter.setNewAppsCount(newCount);
        }

        // The list should now be shown.
        if (isResumed()) {
            setListVisible(true, true);
        } else {
            setListVisible(true, false);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        ListCursorAdapterWrapper watchlistAdapter = ((ListCursorAdapterWrapper) mAdapter.getAdapter(ADAPTER_WATCHLIST));
        watchlistAdapter.swapData(null);
    }

    private void setupFilter(int filterId) {
        if (filterId == Filters.TAB_INSTALLED) {
            mInstalledFilter = new InstalledFilter(true, mPMUtils);
        } else if (filterId == Filters.TAB_UNINSTALLED) {
            mInstalledFilter = new InstalledFilter(false, mPMUtils);
        } else {
            mInstalledFilter = null;
        }
    }

    @Override
    public void onQueryTextChanged(String newQuery) {
        String newFilter = !TextUtils.isEmpty(newQuery) ? newQuery : "";
        if (!TextUtils.equals(newFilter, mTitleFilter)) {
            mTitleFilter = newFilter;
            getLoaderManager().restartLoader(0, null, this);
        }
    }

    @Override
    public void onSyncStart() {
        mSwipeLayout.setRefreshing(true);
    }

    @Override
    public void onSyncFinish() {
        mSwipeLayout.setRefreshing(false);
    }

    @Override
    public void onItemClick(AppInfo app) {
        Intent intent = new Intent(getActivity(), ChangelogActivity.class);
        intent.putExtra(ChangelogActivity.EXTRA_APP_ID, app.getAppId());
        intent.putExtra(ChangelogActivity.EXTRA_ROW_ID, app.getRowId());
        intent.putExtra(ChangelogActivity.EXTRA_DETAILS_URL, app.getDetailsUrl());
        startActivityForResult(intent, REQUEST_APP_INFO);

        if (BuildConfig.DEBUG) {
            AppLog.d(app.getPackageName());
            Toast.makeText(getActivity(), app.getPackageName(), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_APP_INFO && resultCode == Activity.RESULT_OK) {
            if (data.getExtras() != null) {
                getLoaderManager().restartLoader(0, null, this);
            }
        }
    }

    @OnClick(android.R.id.button1)
    public void onSearchButton() {
        AppWatcherActivity activity = (AppWatcherActivity) getActivity();
        activity.openSearch();
    }

    @OnClick(android.R.id.button2)
    public void onImportButton() {
        AppWatcherActivity activity = (AppWatcherActivity) getActivity();
        activity.switchTab(Filters.TAB_INSTALLED);
    }

    @OnClick(android.R.id.button3)
    public void onShareButton() {
        Intent intent = Intent.makeMainActivity(new ComponentName("com.android.vending", "com.android.vending.AssetBrowserActivity"));
        IntentUtils.startActivitySafely(getActivity(), intent);
    }

    @Override
    public void onRefresh() {
        ((AppWatcherActivity)getActivity()).requestRefresh();
    }
}
