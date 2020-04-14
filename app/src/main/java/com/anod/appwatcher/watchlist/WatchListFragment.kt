package com.anod.appwatcher.watchlist

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.anod.appwatcher.Application
import com.anod.appwatcher.BuildConfig
import com.anod.appwatcher.MarketSearchActivity
import com.anod.appwatcher.R
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.installed.ImportInstalledFragment
import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.search.SearchActivity
import info.anodsplace.framework.AppLog
import info.anodsplace.framework.app.CustomThemeActivity
import info.anodsplace.framework.app.FragmentFactory
import info.anodsplace.framework.content.startActivitySafely
import kotlinx.android.synthetic.main.fragment_applist.*

open class WatchListFragment : Fragment(), AppViewHolder.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    lateinit var section: Section

    private val stateViewModel: WatchListStateViewModel by activityViewModels()

    private val prefs: Preferences by lazy {
        Application.provide(requireContext()).prefs
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

        if (prefs.enablePullToRefresh) {
            swipeLayout.setOnRefreshListener(this)
        } else {
            swipeLayout.isEnabled = false
        }

        val metrics = resources.displayMetrics
        swipeLayout.setDistanceToTriggerSync((16 * metrics.density).toInt())

        val args = requireArguments()
        val sortId = args.getInt(ARG_SORT)
        val filterId = args.getInt(ARG_FILTER)
        val tag: Tag? = args.getParcelable(ARG_TAG)

        // Setup adapter for the section
        section = sectionForClassName(args.getString(ARG_SECTION_PROVIDER)!!)
        val viewModel = section.viewModel(this)
        section.attach(this, viewModel.installedApps, this)

        viewModel.init(sortId, tag, filterId, prefs)

        // Setup layout manager
        val layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        listView.layoutManager = layoutManager

        // Setup header decorator
        listView.addItemDecoration(HeaderItemDecorator(viewModel.sections, this, requireContext()))
        val adapter = section.adapter
        listView.adapter = adapter

        // When an item inserted into top there is no indication and list maintains previous position
        // Request to scroll to the top in this case
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (positionStart == 0 && (listView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition() == 0) {
                    listView.scrollToPosition(0)
                }
            }
        })

        if (prefs.enablePullToRefresh) {
            listView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val isOnTop = !recyclerView.canScrollVertically(-1)
                    swipeLayout.isEnabled = isOnTop
                }
            })
        }

        // Start out with a progress indicator.
        this.isListVisible = false

        view.findViewById<View>(android.R.id.button1)?.setOnClickListener {
            val searchIntent = Intent(activity, MarketSearchActivity::class.java)
            searchIntent.putExtra(SearchActivity.EXTRA_KEYWORD, "")
            searchIntent.putExtra(SearchActivity.EXTRA_FOCUS, true)
            startActivity(searchIntent)
        }

        view.findViewById<View>(android.R.id.button2)?.setOnClickListener {
            startActivity(ImportInstalledFragment.intent(
                    requireContext(),
                    (activity as CustomThemeActivity).themeRes,
                    (activity as CustomThemeActivity).themeColors))
        }

        view.findViewById<View>(android.R.id.button3)?.setOnClickListener {
            val intent = Intent.makeMainActivity(ComponentName("com.android.vending", "com.android.vending.AssetBrowserActivity"))
            activity?.startActivitySafely(intent)
        }

        stateViewModel.sortId.observe(viewLifecycleOwner) {
            viewModel.sortId = it ?: 0
            viewModel.reload.value = true
        }

        stateViewModel.titleFilter.observe(viewLifecycleOwner) {
            viewModel.titleFilter = it ?: ""
            viewModel.reload.value = true
        }

        stateViewModel.listState.observe(viewLifecycleOwner) {
            when (it) {
                is SyncStarted -> {
                    swipeLayout.isRefreshing = true
                }
                else -> {
                    swipeLayout.isRefreshing = false
                }
            }
        }

        viewModel.result.observe(viewLifecycleOwner) {
            val headers = it.sections
            viewModel.sections.value = headers
            section.onModelLoaded(it)
            progress.visibility = View.GONE
            isListVisible = true
        }
    }

    override fun onItemClick(app: App) {
        if (BuildConfig.DEBUG) {
            AppLog.d(app.packageName)
            Toast.makeText(activity, app.packageName, Toast.LENGTH_SHORT).show()
        }

        (requireActivity() as WatchListActivity).openAppDetails(app.appId, app.rowId, app.detailsUrl)
    }

    override fun onRefresh() {
        val isRefreshing = (stateViewModel.listState.value is SyncStarted)
        if (!isRefreshing) {
            stateViewModel.requestRefresh().observe(viewLifecycleOwner, Observer { })
        }
    }

    class Factory(
            private val filterId: Int,
            private val sortId: Int,
            private val sectionClass: Class<Section>,
            private val tag: Tag?
    ) : FragmentFactory("wish-list-$filterId-$sortId-${sectionClass.name}-${tag?.hashCode()}") {

        override fun create(): Fragment? = WatchListFragment().also {
            it.arguments = Bundle().apply {
                putInt(ARG_FILTER, filterId)
                putInt(ARG_SORT, sortId)
                putString(ARG_SECTION_PROVIDER, sectionClass.name)
                tag?.let { tag ->
                    putParcelable(ARG_TAG, tag)
                }
            }
        }
    }

    companion object {
        internal const val ARG_FILTER = "filter"
        internal const val ARG_SORT = "sort"
        internal const val ARG_TAG = "tag"
        internal const val ARG_SECTION_PROVIDER = "section"
    }
}
