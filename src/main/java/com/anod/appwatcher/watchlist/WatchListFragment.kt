package com.anod.appwatcher.watchlist

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import com.anod.appwatcher.*
import com.anod.appwatcher.details.DetailsActivity
import com.anod.appwatcher.installed.ImportInstalledActivity
import com.anod.appwatcher.model.*
import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.search.SearchActivity
import info.anodsplace.framework.AppLog
import info.anodsplace.framework.content.InstalledApps
import info.anodsplace.framework.content.startActivitySafely
import info.anodsplace.framework.widget.recyclerview.MergeRecyclerAdapter

open class WatchListFragment : Fragment(), AppViewHolder.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    protected lateinit var installedApps: InstalledApps

    private lateinit var listView: RecyclerView
    private lateinit var emptyView: View
    private var swipeLayout: SwipeRefreshLayout? = null

    lateinit var progress: ProgressBar
    private lateinit var section: Section

    protected var tag: Tag? = null
    private var sortId: Int = 0
    private var filterId: Int = 0
    private var titleFilter = ""

    private val stateViewModel: WatchListStateViewModel by lazy { ViewModelProviders.of(activity!!).get(WatchListStateViewModel::class.java) }

    interface Section {
        val adapter: MergeRecyclerAdapter
        var adapterIndexMap: SparseIntArray
        fun viewModel(fragment: WatchListFragment): WatchListViewModel
        fun attach(fragment: WatchListFragment, installedApps: InstalledApps, clickListener: AppViewHolder.OnClickListener, onLoadFinished: () -> Unit)
        val isEmpty: Boolean
    }

    private val prefs: Preferences by lazy {
        App.provide(context!!).prefs
    }

    // Must have empty constructor
    open class DefaultSection: Section {
        private var showRecentlyUpdated = false
        override var adapterIndexMap = SparseIntArray()
        override val adapter: MergeRecyclerAdapter by lazy { MergeRecyclerAdapter() }

        override fun attach(fragment: WatchListFragment, installedApps: InstalledApps, clickListener: AppViewHolder.OnClickListener, onLoadFinished: () -> Unit) {
            val context = fragment.context!!
            val viewModel = viewModel(fragment)
            viewModel.showRecentlyUpdated = fragment.prefs.showRecentlyUpdated
            val index = adapter.add(AppInfoAdapter(context, installedApps, clickListener))
            adapterIndexMap.put(ADAPTER_WATCHLIST, index)

            viewModel.appList.observe(fragment, Observer {
                list ->
                getInnerAdapter<AppInfoAdapter>(ADAPTER_WATCHLIST).updateList(list ?: emptyList())
                onLoadFinished()
            })
        }

        override fun viewModel(fragment: WatchListFragment): WatchListViewModel {
            return ViewModelProviders.of(fragment).get(WatchListViewModel::class.java)
        }

        fun <T : RecyclerView.Adapter<*>> getInnerAdapter(id: Int): T {
            val index = adapterIndexMap.get(id)
            return adapter[index] as T
        }

        override val isEmpty: Boolean
            get() = getInnerAdapter<AppInfoAdapter>(ADAPTER_WATCHLIST).itemCount == 0

        companion object {
            const val ADAPTER_WATCHLIST = 0
        }
    }

    private var isListVisible: Boolean
        get() = listView.visibility == View.VISIBLE
        set(visible) {
            if (visible) {
                if (section.isEmpty) {
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

    private fun sectionForClassName(sectionClassName: String): Section {
        val sectionClass = Class.forName(sectionClassName)
        return sectionClass.newInstance() as Section
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listView = view.findViewById(android.R.id.list)
        emptyView = view.findViewById(android.R.id.empty)
        swipeLayout = view.findViewById(R.id.swipe_layout)
        
        if (prefs.enablePullToRefresh) {
            swipeLayout?.setOnRefreshListener(this)
        } else {
            swipeLayout?.isEnabled = false
            swipeLayout = null
        }
        
        val metrics = resources.displayMetrics
        swipeLayout?.setDistanceToTriggerSync((16 * metrics.density).toInt())

        progress = view.findViewById(R.id.progress)

        installedApps = InstalledApps.PackageManager(activity!!.packageManager)

        // Setup layout manager
        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        listView.layoutManager = layoutManager

        // Setup adapter for the sеction
        section = sectionForClassName(arguments!!.getString(ARG_SECTION_PROVIDER))
        section.attach(this, installedApps, this) {
            progress.visibility = View.GONE
            this.isListVisible = true
        }
        // Setup header decorator
        listView.addItemDecoration(HeaderItemDecorator(
                section.viewModel(this).sections,
                this, context!!))
        listView.adapter = section.adapter

        listView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val isOnTop = !recyclerView.canScrollVertically(-1)
                swipeLayout?.isEnabled = isOnTop
            }
        })

        // Start out with a progress indicator.
        this.isListVisible = false

        sortId = arguments!!.getInt(ARG_SORT)
        filterId = arguments!!.getInt(ARG_FILTER)
        tag = arguments!!.getParcelable(ARG_TAG)

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

        section.viewModel(this).load(titleFilter, sortId, createFilter(filterId), tag)

        stateViewModel.sortId.observe(this, Observer {
            this.sortId = it ?: 0
            reload()
        })

        stateViewModel.titleFilter.observe(this, Observer {
            this.titleFilter = it ?: ""
            reload()
        })

        stateViewModel.listState.observe(this, Observer {
            when (it) {
                is SyncStarted -> { swipeLayout?.isRefreshing = true }
                else -> {
                    swipeLayout?.isRefreshing = false
                    reload()
                }
            }
        })
    }

    private fun createFilter(filterId: Int): AppListFilter {
        when (filterId) {
            Filters.TAB_INSTALLED -> return AppListFilterInclusion(AppListFilterInclusion.Installed(), installedApps)
            Filters.TAB_UNINSTALLED -> return AppListFilterInclusion(AppListFilterInclusion.Uninstalled(), installedApps)
            Filters.TAB_UPDATABLE -> return AppListFilterInclusion(AppListFilterInclusion.Updatable(), installedApps)
            else -> return AppListFilterInclusion(AppListFilterInclusion.All(), installedApps)
        }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_APP_INFO && resultCode == Activity.RESULT_OK) {
            if (data!!.extras != null) {
                reload()
            }
        }
    }

    override fun onRefresh() {
        val isRefreshing = (stateViewModel.listState.value is SyncStarted)
        if (!isRefreshing && !(activity as WatchListActivity).requestRefresh()) {
            stateViewModel.listState.value = SyncStarted()
        }
    }

    internal fun reload() {
        section.viewModel(this).load(titleFilter, sortId, createFilter(filterId), tag)
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
