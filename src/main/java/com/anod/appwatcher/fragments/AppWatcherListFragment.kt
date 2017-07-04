package com.anod.appwatcher.fragments

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.support.annotation.Nullable
import android.support.v4.app.Fragment
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Optional
import com.anod.appwatcher.BuildConfig
import com.anod.appwatcher.ChangelogActivity
import com.anod.appwatcher.MarketSearchActivity
import com.anod.appwatcher.R
import com.anod.appwatcher.adapters.AppListCursorAdapterWrapper
import com.anod.appwatcher.adapters.AppViewHolder
import com.anod.appwatcher.content.AppListCursor
import com.anod.appwatcher.installed.ImportInstalledActivity
import com.anod.appwatcher.model.*
import com.anod.appwatcher.ui.AppWatcherBaseActivity
import com.anod.appwatcher.utils.InstalledAppsProvider
import com.anod.appwatcher.utils.IntentUtils
import info.anodsplace.android.log.AppLog
import info.anodsplace.android.widget.recyclerview.MergeRecyclerAdapter

open class AppWatcherListFragment : Fragment(), LoaderManager.LoaderCallbacks<Cursor>, AppWatcherBaseActivity.EventListener, AppViewHolder.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    protected var mTitleFilter = ""
    protected lateinit var mInstalledApps: InstalledAppsProvider
    protected lateinit var mAdapter: MergeRecyclerAdapter
    protected var mSortId: Int = 0
    protected var mFilterId: Int = 0
    protected var mTag: Tag? = null

    private var mListenerIndex: Int = 0

    @BindView(android.R.id.list)
    lateinit var mListView: RecyclerView
    @BindView(R.id.progress)
    lateinit var mProgressContainer: View
    @BindView(android.R.id.empty)
    lateinit var mEmptyView: View
    @BindView(R.id.swipe_layout)
    @Nullable @JvmField var mSwipeLayout: SwipeRefreshLayout? = null

    private lateinit var mSection: SectionProvider

    interface SectionProvider {
        fun fillAdapters(adapter: MergeRecyclerAdapter, context: Context, installedApps: InstalledAppsProvider, clickListener: AppViewHolder.OnClickListener)
        fun createLoader(context: Context, titleFilter: String, sortId: Int, filter: InstalledFilter?, tag: Tag?): Loader<Cursor>
        fun loadFinished(adapter: MergeRecyclerAdapter, loader: Loader<Cursor>, data: Cursor)
        fun loaderReset(adapter: MergeRecyclerAdapter)
    }

    open class DefaultSection : SectionProvider {

        override fun fillAdapters(adapter: MergeRecyclerAdapter, context: Context, installedApps: InstalledAppsProvider, clickListener: AppViewHolder.OnClickListener) {
            adapter.addAdapter(ADAPTER_WATCHLIST, AppListCursorAdapterWrapper(context, installedApps, clickListener))
        }

        override fun createLoader(context: Context, titleFilter: String, sortId: Int, filter: InstalledFilter?, tag: Tag?): Loader<Cursor> {
            return AppListCursorLoader(context, titleFilter, sortId, filter, tag)
        }

        override fun loadFinished(adapter: MergeRecyclerAdapter, loader: Loader<Cursor>, data: Cursor) {
            val watchlistAdapter = adapter.getAdapter(ADAPTER_WATCHLIST) as AppListCursorAdapterWrapper
            watchlistAdapter.swapData(data as AppListCursor)

            val newCount = (loader as AppListCursorLoader).newCountFiltered
            val updatableCount = loader.updatableCountFiltered

            watchlistAdapter.setNewAppsCount(newCount, updatableCount)
        }

        override fun loaderReset(adapter: MergeRecyclerAdapter) {
            // This is called when the last Cursor provided to onLoadFinished()
            // above is about to be closed.  We need to make sure we are no
            // longer using it.
            val watchlistAdapter = adapter.getAdapter(ADAPTER_WATCHLIST) as AppListCursorAdapterWrapper
            watchlistAdapter.swapData(null)
        }

        companion object {
            private const val ADAPTER_WATCHLIST = 0
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mListenerIndex = (context as AppWatcherBaseActivity).addQueryChangeListener(this)
        AppLog.d("addQueryChangeListener with index: %d", mListenerIndex)
    }

    override fun onDetach() {
        super.onDetach()
        (activity as AppWatcherBaseActivity).removeQueryChangeListener(mListenerIndex)
    }

    fun setListVisible(visible: Boolean) {
        if (visible) {
            mProgressContainer.visibility = View.GONE
            if (mAdapter.itemCount == 0) {
                mEmptyView.visibility = View.VISIBLE
                mListView.visibility = View.INVISIBLE
            } else {
                mEmptyView.visibility = View.GONE
                mListView.visibility = View.VISIBLE
            }
        } else {
            mProgressContainer.visibility = View.VISIBLE
            mListView.visibility = View.INVISIBLE
            mEmptyView.visibility = View.GONE
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflateView(inflater, container, savedInstanceState)
        ButterKnife.bind(this, root)
        mEmptyView.visibility = View.GONE
        mSwipeLayout?.setOnRefreshListener(this)
        return root
    }

    protected open fun inflateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_applist, container, false)
    }

    fun sectionForClassName(sectionClassName: String): SectionProvider {
        val sectionClass = Class.forName(sectionClassName)
        return sectionClass.newInstance() as SectionProvider
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mInstalledApps = InstalledAppsProvider.PackageManager(activity.packageManager)

        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        mListView.layoutManager = layoutManager

        mSection = sectionForClassName(arguments.getString(ARG_SECTION_PROVIDER))

        // Create an empty adapter we will use to display the loaded data.
        mAdapter = MergeRecyclerAdapter()
        mSection.fillAdapters(mAdapter, activity, mInstalledApps, this)
        mListView.adapter = mAdapter

        // Start out with a progress indicator.
        setListVisible(false)

        mSortId = arguments.getInt(ARG_SORT)
        mFilterId = arguments.getInt(ARG_FILTER)
        mTag = arguments.getParcelable<Tag>(ARG_TAG)
        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        loaderManager.initLoader(0, null, this)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        return mSection.createLoader(activity, mTitleFilter, mSortId, createFilter(mFilterId), mTag)
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor) {
        mSection.loadFinished(mAdapter, loader, data)
        setListVisible(true)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        mSection.loaderReset(mAdapter)
    }

    protected fun createFilter(filterId: Int): InstalledFilter? {
        if (filterId == Filters.TAB_INSTALLED) {
            return InstalledFilter(true, mInstalledApps)
        } else if (filterId == Filters.TAB_UNINSTALLED) {
            return InstalledFilter(false, mInstalledApps)
        }
        return null
    }

    override fun onSortChanged(sortIndex: Int) {
        mSortId = sortIndex
        restartLoader()
    }

    override fun onQueryTextChanged(newQuery: String) {
        val newFilter = if (!TextUtils.isEmpty(newQuery)) newQuery else ""
        if (!TextUtils.equals(newFilter, mTitleFilter)) {
            mTitleFilter = newFilter
            restartLoader()
        }
    }

    override fun onSyncStart() {
        mSwipeLayout?.isRefreshing = true
    }

    override fun onSyncFinish() {
        mSwipeLayout?.isRefreshing = false
    }

    override fun onItemClick(app: AppInfo) {
        val intent = Intent(activity, ChangelogActivity::class.java)
        intent.putExtra(ChangelogActivity.EXTRA_APP_ID, app.appId)
        intent.putExtra(ChangelogActivity.EXTRA_ROW_ID, app.rowId)
        intent.putExtra(ChangelogActivity.EXTRA_DETAILS_URL, app.detailsUrl)
        startActivityForResult(intent, REQUEST_APP_INFO)

        if (BuildConfig.DEBUG) {
            AppLog.d(app.packageName)
            Toast.makeText(activity, app.packageName, Toast.LENGTH_SHORT).show()
        }

    }

    override fun onActionButton() {
        IntentUtils.startActivitySafely(context, IntentUtils.createMyAppsIntent(true))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_APP_INFO && resultCode == Activity.RESULT_OK) {
            if (data!!.extras != null) {
                restartLoader()
            }
        }
    }

    @OnClick(android.R.id.button1)
    @Optional
    open fun onSearchButton() {
        val searchIntent = Intent(activity, MarketSearchActivity::class.java)
        searchIntent.putExtra(MarketSearchActivity.EXTRA_KEYWORD, "")
        searchIntent.putExtra(MarketSearchActivity.EXTRA_FOCUS, true)
        startActivity(searchIntent)
    }

    @OnClick(android.R.id.button2)
    @Optional
    fun onImportButton() {
        startActivity(Intent(activity, ImportInstalledActivity::class.java))
    }

    @OnClick(android.R.id.button3)
    @Optional
    fun onShareButton() {
        val intent = Intent.makeMainActivity(ComponentName("com.android.vending", "com.android.vending.AssetBrowserActivity"))
        IntentUtils.startActivitySafely(activity, intent)
    }

    override fun onRefresh() {
       if (!(activity as AppWatcherBaseActivity).requestRefresh()) {
           mSwipeLayout?.isRefreshing = false
       }
    }

    private fun restartLoader() {
        if (isResumed) {
            loaderManager.restartLoader(0, null, this)
        }
    }

    companion object {
        internal const val ARG_FILTER = "filter"
        internal const val ARG_SORT = "sort"
        internal const val ARG_TAG = "tag"
        internal const val ARG_SECTION_PROVIDER = "section"

        private const val REQUEST_APP_INFO = 1

        fun newInstance(filterId: Int, sortId: Int, sectionProvider: SectionProvider, tag: Tag?): AppWatcherListFragment {
            val frag = AppWatcherListFragment()
            frag.arguments = createArguments(filterId, sortId, sectionProvider, tag)
            return frag
        }

        fun createArguments(filterId: Int, sortId: Int, sectionProvider: SectionProvider, tag: Tag?): Bundle {
            val args = Bundle()
            args.putInt(ARG_FILTER, filterId)
            args.putInt(ARG_SORT, sortId)
            args.putString(ARG_SECTION_PROVIDER, sectionProvider.javaClass.name)
            if (tag != null) {
                args.putParcelable(ARG_TAG, tag)
            }
            return args
        }
    }
}
