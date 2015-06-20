package com.anod.appwatcher.watchlist;

import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.app.ShareCompat.IntentBuilder;
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
import com.anod.appwatcher.fragments.RemoveDialogFragment;
import com.anod.appwatcher.market.MarketInfo;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.model.AppListCursorLoader;
import com.anod.appwatcher.model.Filters;
import com.anod.appwatcher.model.InstalledFilter;
import com.anod.appwatcher.utils.AppLog;
import com.anod.appwatcher.utils.IntentUtils;
import com.anod.appwatcher.utils.PackageManagerUtils;

public class AppWatcherListFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        AppWatcherActivity.QueryChangeListener,
        AppWatcherActivity.RefreshListener,
        SwipeRefreshLayout.OnRefreshListener {

    private ListCursorAdapterWrapper mAdapter;
    private String mTitleFilter = "";

    public RecyclerView mList;
    private boolean mListShown;
    private View mProgressContainer;
    private View mListContainer;
    private SwipeRefreshLayout mSwipeLayout;
    private InstalledFilter mInstalledFilter;

    private PackageManagerUtils mPMUtils;

    public static AppWatcherListFragment newInstance() {
        AppWatcherListFragment frag = new AppWatcherListFragment();
        Bundle args = new Bundle();
        frag.setArguments(args);
        return frag;
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        AppLog.d("Register listeners");
        AppWatcherActivity act = (AppWatcherActivity) getActivity();

        act.setQueryChangeListener(this);
        act.setRefreshListener(this);

    }

    public void setListShown(boolean shown, boolean animate) {
        if (mListShown == shown) {
            return;
        }
        mListShown = shown;
        if (shown) {
            if (animate) {
                mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_out));
                mListContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_in));
            }
            mProgressContainer.setVisibility(View.GONE);
            mListContainer.setVisibility(View.VISIBLE);
        } else {
            if (animate) {
                mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_in));
                mListContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_out));
            }
            mProgressContainer.setVisibility(View.VISIBLE);
            mListContainer.setVisibility(View.INVISIBLE);
        }
    }

    public void setListShown(boolean shown) {
        setListShown(shown, true);
    }

    public void setListShownNoAnimation(boolean shown) {
        setListShown(shown, false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_applist, container, false);
        mList = (RecyclerView) root.findViewById(android.R.id.list);

        mListContainer = root.findViewById(R.id.list_container);
        mProgressContainer = root.findViewById(R.id.progress);
        mListShown = true;

        mSwipeLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipe_container);
        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        return root;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // We have a menu item to show in action bar.
        setHasOptionsMenu(true);

        mPMUtils = new PackageManagerUtils(getActivity().getPackageManager());

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mList.setLayoutManager(layoutManager);

        Resources r = getResources();

        // Create an empty adapter we will use to display the loaded data.
        mAdapter = new ListCursorAdapterWrapper(getActivity(), mPMUtils);
        mList.setAdapter(mAdapter);

        // Start out with a progress indicator.
        setListShown(false);

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(0, null, this);
    }

    private void onRemoveClick(View v) {
        AppInfo app = (AppInfo) v.getTag();
        RemoveDialogFragment removeDialog = RemoveDialogFragment.newInstance(
                app.getTitle(), app.getRowId()
        );
        removeDialog.show(getFragmentManager(), "removeDialog");
    }

    private void onPlayStoreClick(View v) {
        String pkg = (String) v.getTag();
        Intent intent = IntentUtils.createPlayStoreIntent(pkg);
        startActivity(intent);
    }

    private void onShareClick(View v) {
        AppInfo app = (AppInfo) v.getTag();
        IntentBuilder builder = ShareCompat.IntentBuilder.from(getActivity());
        if (app.getStatus() == AppInfo.STATUS_UPDATED) {
            builder.setSubject(getString(R.string.share_subject_updated, app.getTitle()));
        } else {
            builder.setSubject(getString(R.string.share_subject_normal, app.getTitle()));
        }
        builder.setText(String.format(MarketInfo.URL_WEB_PLAY_STORE, app.getPackageName()));
        builder.setType("text/plain");
        builder.startChooser();
    }

    private void onChangelogClick(final String appId, final String detailsUrl) {
        Intent intent = new Intent(getActivity(), ChangelogActivity.class);
        intent.putExtra(ChangelogActivity.EXTRA_APP_ID, appId);
        intent.putExtra(ChangelogActivity.EXTRA_DETAILS_URL, detailsUrl);
        startActivity(intent);
    }

    /**
     * @param v
     */
    private void onIconClick(View v) {
        AppViewHolder holder = (AppViewHolder) v.getTag();
        String pkgName = holder.app.getPackageName();
        boolean isInstalled = mPMUtils.isAppInstalled(holder.app.getPackageName());
        if (isInstalled) {
            Intent appInfo = IntentUtils.createApplicationDetailsIntent(holder.app.getPackageName());
            startActivity(appInfo);
        }
        if (BuildConfig.DEBUG) {
            AppLog.d(pkgName);
            Toast.makeText(getActivity(), pkgName, Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new AppListCursorLoader(getActivity(), mTitleFilter, mInstalledFilter);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mAdapter.swapCursor(data);
        if (mInstalledFilter == null) {
            int newCount = ((AppListCursorLoader) loader).getNewCount();
            mAdapter.setNewAppsCount(newCount);
        } else {
            int newCount = mInstalledFilter.getNewCount();
            mAdapter.setNewAppsCount(newCount);
        }

        // The list should now be shown.
        if (isResumed()) {
            setListShown(true);
        } else {
            setListShownNoAnimation(true);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mAdapter.swapCursor(null);
    }

    public void onFilterChanged(int navId) {
        if (navId == Filters.NAVDRAWER_ITEM_INSTALLED) {
            mInstalledFilter = new InstalledFilter(true, mPMUtils);
        } else if (navId == Filters.NAVDRAWER_ITEM_UNINSTALLED) {
            mInstalledFilter = new InstalledFilter(false, mPMUtils);
        } else {
            mInstalledFilter = null;
        }
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void onQueryTextChanged(String newQuery) {
        String newFilter = !TextUtils.isEmpty(newQuery) ? newQuery : "";
        if (!TextUtils.equals(newFilter, mTitleFilter)) {
            mTitleFilter = newFilter;
            getLoaderManager().restartLoader(0, null, this);
        }
    }


    public void onRefreshFinish() {
        mSwipeLayout.setRefreshing(false);
    }


    @Override
    public void onRefresh() {
        if (!((AppWatcherActivity) getActivity()).requestRefresh()) {
            mSwipeLayout.setRefreshing(false);
        }
    }
}
