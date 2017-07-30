package com.anod.appwatcher.fragments

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
import butterknife.bindOptionalView
import butterknife.bindView
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

    protected var titleFilter = ""
    protected lateinit var installedApps: InstalledAppsProvider
    protected lateinit var adapter: MergeRecyclerAdapter
    protected var sortId: Int = 0
    protected var filterId: Int = 0
    protected var tag: Tag? = null

    private var listenerIndex: Int = 0

    val listView: RecyclerView by bindView(android.R.id.list)
    val progressContainer: View by bindView(R.id.progress)
    val emptyView: View by bindView(android.R.id.empty)
    val swipeLayout: SwipeRefreshLayout? by bindOptionalView(R.id.swipe_layout)

    private lateinit var section: SectionProvider

    interface SectionProvider {
        var adapterIndexMap: SparseIntArray
        fun fillAdapters(adapter: MergeRecyclerAdapter, context: Context, installedApps: InstalledAppsProvider, clickListener: AppViewHolder.OnClickListener)
        fun createLoader(context: Context, titleFilter: String, sortId: Int, filter: InstalledFilter?, tag: Tag?): Loader<Cursor>
        fun loadFinished(adapter: MergeRecyclerAdapter, loader: Loader<Cursor>, data: Cursor)
        fun loaderReset(adapter: MergeRecyclerAdapter)
    }

    open class DefaultSection : SectionProvider {
        override var adapterIndexMap = SparseIntArray()

        override fun fillAdapters(adapter: MergeRecyclerAdapter, context: Context, installedApps: InstalledAppsProvider, clickListener: AppViewHolder.OnClickListener) {
            val index = adapter.addAdapter(AppListCursorAdapterWrapper(context, installedApps, clickListener))
            adapterIndexMap.put(ADAPTER_WATCHLIST, index)
        }

        override fun createLoader(context: Context, titleFilter: String, sortId: Int, filter: InstalledFilter?, tag: Tag?): Loader<Cursor> {
            return AppListCursorLoader(context, titleFilter, sortId, filter, tag)
        }

        override fun loadFinished(adapter: MergeRecyclerAdapter, loader: Loader<Cursor>, data: Cursor) {
            val watchlistAdapter = getAdapter<AppListCursorAdapterWrapper>(ADAPTER_WATCHLIST, adapter)
            watchlistAdapter.swapData(data as AppListCursor)

            val newCount = (loader as AppListCursorLoader).newCountFiltered
            val updatableCount = loader.updatableCountFiltered

            watchlistAdapter.setNewAppsCount(newCount, updatableCount)
        }

        override fun loaderReset(adapter: MergeRecyclerAdapter) {
            // This is called when the last Cursor provided to onLoadFinished()
            // above is about to be closed.  We need to make sure we are no
            // longer using it.
            val watchlistAdapter = getAdapter<AppListCursorAdapterWrapper>(ADAPTER_WATCHLIST, adapter)
            watchlistAdapter.swapData(null)
        }

        fun <T: RecyclerView.Adapter<*>> getAdapter(id: Int, adapter: MergeRecyclerAdapter): T {
            val index = adapterIndexMap.get(id)
            return adapter.getAdapter(index) as T
        }

        companion object {
            private const val ADAPTER_WATCHLIST = 0
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        listenerIndex = (context as AppWatcherBaseActivity).addQueryChangeListener(this)
        AppLog.d("addQueryChangeListener with index: %d", listenerIndex)
    }

    override fun onDetach() {
        super.onDetach()
        (activity as AppWatcherBaseActivity).removeQueryChangeListener(listenerIndex)
    }

    fun setListVisible(visible: Boolean) {
        if (visible) {
            progressContainer.visibility = View.GONE
            if (adapter.itemCount == 0) {
                emptyView.visibility = View.VISIBLE
                listView.visibility = View.INVISIBLE
            } else {
                emptyView.visibility = View.GONE
                listView.visibility = View.VISIBLE
            }
        } else {
            progressContainer.visibility = View.VISIBLE
            listView.visibility = View.INVISIBLE
            emptyView.visibility = View.GONE
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_applist, container, false)
    }

    fun sectionForClassName(sectionClassName: String): SectionProvider {
        val sectionClass = Class.forName(sectionClassName)
        return sectionClass.newInstance() as SectionProvider
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (listView.adapter != null) {
            return
        }

        emptyView.visibility = View.GONE
        swipeLayout?.setOnRefreshListener(this)

        installedApps = InstalledAppsProvider.PackageManager(activity.packageManager)

        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        listView.layoutManager = layoutManager

        section = sectionForClassName(arguments.getString(ARG_SECTION_PROVIDER))

        // Create an empty adapter we will use to display the loaded data.
        adapter = MergeRecyclerAdapter()
        section.fillAdapters(adapter, activity, installedApps, this)
        listView.adapter = adapter

        // Start out with a progress indicator.
        setListVisible(false)

        sortId = arguments.getInt(ARG_SORT)
        filterId = arguments.getInt(ARG_FILTER)
        tag = arguments.getParcelable<Tag>(ARG_TAG)
        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        loaderManager.initLoader(0, null, this)

        view?.findViewById<View>(android.R.id.button1)?.setOnClickListener {
            val searchIntent = Intent(activity, MarketSearchActivity::class.java)
            searchIntent.putExtra(MarketSearchActivity.EXTRA_KEYWORD, "")
            searchIntent.putExtra(MarketSearchActivity.EXTRA_FOCUS, true)
            startActivity(searchIntent)
        }

        view?.findViewById<View>(android.R.id.button2)?.setOnClickListener {
            startActivity(Intent(activity, ImportInstalledActivity::class.java))
        }

        view?.findViewById<View>(android.R.id.button3)?.setOnClickListener {
            val intent = Intent.makeMainActivity(ComponentName("com.android.vending", "com.android.vending.AssetBrowserActivity"))
            IntentUtils.startActivitySafely(activity, intent)
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        return section.createLoader(activity, titleFilter, sortId, createFilter(filterId), tag)
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor) {
        section.loadFinished(adapter, loader, data)
        setListVisible(true)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        section.loaderReset(adapter)
    }

    protected fun createFilter(filterId: Int): InstalledFilter? {
        if (filterId == Filters.TAB_INSTALLED) {
            return InstalledFilter(true, installedApps)
        } else if (filterId == Filters.TAB_UNINSTALLED) {
            return InstalledFilter(false, installedApps)
        }
        return null
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

    override fun onRefresh() {
       if (!(activity as AppWatcherBaseActivity).requestRefresh()) {
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
