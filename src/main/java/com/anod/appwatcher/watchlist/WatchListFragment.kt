package com.anod.appwatcher.watchlist

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
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
import com.anod.appwatcher.search.SearchActivity
import com.anod.appwatcher.utils.UpdateAll
import info.anodsplace.framework.AppLog
import info.anodsplace.framework.content.InstalledApps
import info.anodsplace.framework.content.startActivitySafely
import info.anodsplace.framework.widget.recyclerview.MergeRecyclerAdapter


open class WatchListFragment : Fragment(), WatchListActivity.EventListener, AppViewHolder.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private var titleFilter = ""
    private var sortId: Int = 0
    private var filterId: Int = 0

    protected lateinit var installedApps: InstalledApps
    protected var tag: Tag? = null

    private var listenerIndex: Int = 0

    private lateinit var listView: RecyclerView
    private lateinit var emptyView: View
    private var swipeLayout: SwipeRefreshLayout? = null

    lateinit var progress: ProgressBar
    private lateinit var section: Section

    interface Section {
        val adapter: MergeRecyclerAdapter
        var adapterIndexMap: SparseIntArray
        fun viewModel(fragment: WatchListFragment): WatchListViewModel
        fun attach(fragment: WatchListFragment, installedApps: InstalledApps, clickListener: AppViewHolder.OnClickListener, onLoadFinished: () -> Unit)
        val isEmpty: Boolean
    }

    // Must have empty constructor
    open class DefaultSection: Section {
        private var showRecentlyUpdated = false
        override var adapterIndexMap = SparseIntArray()
        override val adapter: MergeRecyclerAdapter by lazy { MergeRecyclerAdapter() }

        override fun attach(fragment: WatchListFragment, installedApps: InstalledApps, clickListener: AppViewHolder.OnClickListener, onLoadFinished: () -> Unit) {
            val context = fragment.context!!
            val viewModel = viewModel(fragment)
            viewModel.showRecentlyUpdated = App.provide(context).prefs.showRecentlyUpdated
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

    fun sectionForClassName(sectionClassName: String): Section {
        val sectionClass = Class.forName(sectionClassName)
        return sectionClass.newInstance() as Section
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listView = view.findViewById(android.R.id.list)
        emptyView = view.findViewById(android.R.id.empty)
        swipeLayout = view.findViewById(R.id.swipe_layout)
        swipeLayout?.setOnRefreshListener(this)
        progress = view.findViewById(R.id.progress)

        installedApps = InstalledApps.PackageManager(activity!!.packageManager)

        // Setup layout manager
        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        listView.layoutManager = layoutManager

        // Setup header decorator

        // Setup adapter for the sеction
        section = sectionForClassName(arguments!!.getString(ARG_SECTION_PROVIDER))
        section.attach(this, installedApps, this) {
            progress.visibility = View.GONE
            this.isListVisible = true
        }
        listView.addItemDecoration(HeaderItemDecorator(
                section.viewModel(this).sections,
                this, context!!))
        listView.adapter = section.adapter

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
    }

    private fun createFilter(filterId: Int): AppListFilter {
        when (filterId) {
            Filters.TAB_INSTALLED -> return AppListFilterInclusion(AppListFilterInclusion.Installed(), installedApps)
            Filters.TAB_UNINSTALLED -> return AppListFilterInclusion(AppListFilterInclusion.Uninstalled(), installedApps)
            Filters.TAB_UPDATABLE -> return AppListFilterInclusion(AppListFilterInclusion.Updatable(), installedApps)
            else -> return AppListFilterInclusion(AppListFilterInclusion.All(), installedApps)
        }
    }

    override fun onSortChanged(sortIndex: Int) {
        sortId = sortIndex
        reload()
    }

    override fun onQueryTextChanged(newQuery: String) {
        val newFilter = if (!TextUtils.isEmpty(newQuery)) newQuery else ""
        if (!TextUtils.equals(newFilter, titleFilter)) {
            titleFilter = newFilter
            reload()
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
        val context = context ?: return
        UpdateAll(context, App.provide(context).prefs).withConfirmation()
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
        if (!(activity as WatchListActivity).requestRefresh()) {
            swipeLayout?.isRefreshing = false
        }
    }

    private fun reload() {
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
