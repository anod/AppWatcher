package com.anod.appwatcher.watchlist

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.anod.appwatcher.BuildConfig
import com.anod.appwatcher.ChangelogActivity
import com.anod.appwatcher.MarketSearchActivity
import com.anod.appwatcher.R
import com.anod.appwatcher.content.AppListCursor
import com.anod.appwatcher.details.DetailsActivity
import com.anod.appwatcher.installed.ImportInstalledActivity
import com.anod.appwatcher.model.*
import com.anod.appwatcher.search.SearchActivity
import info.anodsplace.android.log.AppLog
import info.anodsplace.android.widget.recyclerview.MergeRecyclerAdapter
import info.anodsplace.appwatcher.framework.InstalledApps
import info.anodsplace.appwatcher.framework.forMyApps
import info.anodsplace.appwatcher.framework.startActivitySafely

open class WatchListFragment : Fragment(), LoaderManager.LoaderCallbacks<Cursor>, WatchListActivity.EventListener, AppViewHolder.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    protected var titleFilter = ""
    protected lateinit var installedApps: InstalledApps
    protected lateinit var adapter: MergeRecyclerAdapter
    protected var sortId: Int = 0
    protected var filterId: Int = 0
    protected var tag: Tag? = null

    private var listenerIndex: Int = 0

    lateinit var listView: RecyclerView
    lateinit var emptyView: View
    var swipeLayout: SwipeRefreshLayout? = null

    private lateinit var section: Section

    interface Section {
        var adapterIndexMap: SparseIntArray
        fun fillAdapters(adapter: MergeRecyclerAdapter, context: Context, installedApps: InstalledApps, clickListener: AppViewHolder.OnClickListener)
        fun createLoader(context: Context, titleFilter: String, sortId: Int, filter: AppListFilter?, tag: Tag?): Loader<Cursor>
        fun loadFinished(adapter: MergeRecyclerAdapter, loader: Loader<Cursor>, data: Cursor)
        fun loaderReset(adapter: MergeRecyclerAdapter)
    }

    open class DefaultSection : Section {
        override var adapterIndexMap = SparseIntArray()

        override fun fillAdapters(adapter: MergeRecyclerAdapter, context: Context, installedApps: InstalledApps, clickListener: AppViewHolder.OnClickListener) {
            val index = adapter.addAdapter(AppListCursorAdapter(context, installedApps, clickListener))
            adapterIndexMap.put(ADAPTER_WATCHLIST, index)
        }

        override fun createLoader(context: Context, titleFilter: String, sortId: Int, filter: AppListFilter?, tag: Tag?): Loader<Cursor> {
            return AppListCursorLoader(context, titleFilter, sortId, filter, tag)
        }

        override fun loadFinished(adapter: MergeRecyclerAdapter, loader: Loader<Cursor>, data: Cursor) {
            val watchlistAdapter = getAdapter<AppListCursorAdapter>(ADAPTER_WATCHLIST, adapter)
            watchlistAdapter.swapData(data as AppListCursor)

            val newCount = (loader as AppListCursorLoader).newCountFiltered
            val updatableCount = loader.updatableCountFiltered

            watchlistAdapter.setNewAppsCount(newCount, updatableCount)
        }

        override fun loaderReset(adapter: MergeRecyclerAdapter) {
            // This is called when the last Cursor provided to onLoadFinished()
            // above is about to be closed.  We need to make sure we are no
            // longer using it.
            val watchlistAdapter = getAdapter<AppListCursorAdapter>(ADAPTER_WATCHLIST, adapter)
            watchlistAdapter.swapData(null)
        }

        fun <T : RecyclerView.Adapter<*>> getAdapter(id: Int, adapter: MergeRecyclerAdapter): T {
            val index = adapterIndexMap.get(id)
            return adapter.getAdapter(index) as T
        }

        companion object {
            private const val ADAPTER_WATCHLIST = 0
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        listenerIndex = (context as WatchListActivity).addQueryChangeListener(this)
        AppLog.d("addQueryChangeListener with index: %d", listenerIndex)
    }

    override fun onDetach() {
        super.onDetach()
        (activity as WatchListActivity).removeQueryChangeListener(listenerIndex)
    }

    private var isListVisible: Boolean
        get() = listView.visibility == View.VISIBLE
        set(visible) {
            if (visible) {
                if (adapter.itemCount == 0) {
                    emptyView.visibility = View.VISIBLE
                    listView.visibility = View.INVISIBLE
                } else {
                    emptyView.visibility = View.GONE
                    listView.visibility = View.VISIBLE
                }
            } else {
                listView.visibility = View.INVISIBLE
                emptyView.visibility = View.GONE
            }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_applist, container, false)
    }

    fun sectionForClassName(sectionClassName: String): Section {
        val sectionClass = Class.forName(sectionClassName)
        return sectionClass.newInstance() as Section
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listView = view.findViewById(android.R.id.list)
        emptyView = view.findViewById(android.R.id.empty)
        swipeLayout = view.findViewById(R.id.swipe_layout)

        emptyView.visibility = View.GONE
        swipeLayout?.setOnRefreshListener(this)

        installedApps = InstalledApps.PackageManager(activity!!.packageManager)

        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        listView.layoutManager = layoutManager

        section = sectionForClassName(arguments!!.getString(ARG_SECTION_PROVIDER))

        // Create an empty adapter we will use to display the loaded data.
        adapter = MergeRecyclerAdapter()
        section.fillAdapters(adapter, activity!!, installedApps, this)
        listView.adapter = adapter

        // Start out with a progress indicator.
        this.isListVisible = false

        sortId = arguments!!.getInt(ARG_SORT)
        filterId = arguments!!.getInt(ARG_FILTER)
        tag = arguments!!.getParcelable(ARG_TAG)
        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        loaderManager.initLoader(0, null, this)

        view.findViewById<View>(android.R.id.button1)?.setOnClickListener {
            val searchIntent = Intent(activity, MarketSearchActivity::class.java)
            searchIntent.putExtra(SearchActivity.EXTRA_KEYWORD, "")
            searchIntent.putExtra(SearchActivity.EXTRA_FOCUS, true)
            startActivity(searchIntent)
        }

        view.findViewById<View>(android.R.id.button2)?.setOnClickListener {
            startActivity(Intent(activity, ImportInstalledActivity::class.java))
        }

        view.findViewById<View>(android.R.id.button3)?.setOnClickListener {
            val intent = Intent.makeMainActivity(ComponentName("com.android.vending", "com.android.vending.AssetBrowserActivity"))
            activity?.startActivitySafely(intent)
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        return section.createLoader(activity!!, titleFilter, sortId, createFilter(filterId), tag)
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor) {
        section.loadFinished(adapter, loader, data)
        this.isListVisible = true
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        section.loaderReset(adapter)
    }

    protected fun createFilter(filterId: Int): AppListFilter? {
        when (filterId) {
            Filters.TAB_INSTALLED -> return AppListFilter(AppListFilterInclusionInstalled(), installedApps)
            Filters.TAB_UNINSTALLED -> return AppListFilter(AppListFilterInclusionUninstalled(), installedApps)
            Filters.TAB_UPDATABLE -> return AppListFilter(AppListFilterInclusionUpdatable(), installedApps)
            else -> return null
        }
    }

    override fun onSortChanged(sortIndex: Int) {
        sortId = sortIndex
        restartLoader()
    }

    override fun onQueryTextChanged(newQuery: String) {
        val newFilter = if (!TextUtils.isEmpty(newQuery)) newQuery else ""
        if (!TextUtils.equals(newFilter, titleFilter)) {
            titleFilter = newFilter
            restartLoader()
        }
    }

    override fun onSyncStart() {
        swipeLayout?.isRefreshing = true
    }

    override fun onSyncFinish() {
        swipeLayout?.isRefreshing = false
    }

    override fun onItemClick(app: AppInfo) {
        val intent = Intent(activity, ChangelogActivity::class.java)
        intent.putExtra(DetailsActivity.EXTRA_APP_ID, app.appId)
        intent.putExtra(DetailsActivity.EXTRA_ROW_ID, app.rowId)
        intent.putExtra(DetailsActivity.EXTRA_DETAILS_URL, app.detailsUrl)
        startActivityForResult(intent, REQUEST_APP_INFO)

        if (BuildConfig.DEBUG) {
            AppLog.d(app.packageName)
            Toast.makeText(activity, app.packageName, Toast.LENGTH_SHORT).show()
        }

    }

    override fun onActionButton() {
        context?.startActivitySafely(Intent().forMyApps(true))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_APP_INFO && resultCode == Activity.RESULT_OK) {
            if (data!!.extras != null) {
                restartLoader()
            }
        }
    }

    override fun onRefresh() {
        if (!(activity as WatchListActivity).requestRefresh()) {
            swipeLayout?.isRefreshing = false
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

        fun newInstance(filterId: Int, sortId: Int, section: Section, tag: Tag?): WatchListFragment {
            val frag = WatchListFragment()
            frag.arguments = createArguments(filterId, sortId, section, tag)
            return frag
        }

        fun createArguments(filterId: Int, sortId: Int, section: Section, tag: Tag?): Bundle {
            val args = Bundle()
            args.putInt(ARG_FILTER, filterId)
            args.putInt(ARG_SORT, sortId)
            args.putString(ARG_SECTION_PROVIDER, section.javaClass.name)
            if (tag != null) {
                args.putParcelable(ARG_TAG, tag)
            }
            return args
        }
    }
}
