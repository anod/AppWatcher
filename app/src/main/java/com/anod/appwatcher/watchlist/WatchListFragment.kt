package com.anod.appwatcher.watchlist

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.anod.appwatcher.*
import com.anod.appwatcher.R
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.installed.InstalledFragment
import com.anod.appwatcher.model.Filters
import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.tags.AppsTagSelectActivity
import com.anod.appwatcher.utils.SingleLiveEvent
import info.anodsplace.framework.AppLog
import info.anodsplace.framework.app.CustomThemeActivity
import info.anodsplace.framework.app.FragmentFactory
import info.anodsplace.framework.content.startActivitySafely
import kotlinx.android.synthetic.main.fragment_applist.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed class WishListAction
object SearchInStore : WishListAction()
object ImportInstalled : WishListAction()
object ShareFromStore : WishListAction()
class AddAppToTag(val tag: Tag) : WishListAction()
class EmptyButton(val idx: Int) : WishListAction()
class ItemClick(val app: App) : WishListAction()

open class WatchListFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {
    protected val application: AppWatcherApplication
        get() = requireContext().applicationContext as AppWatcherApplication
    protected val prefs: Preferences by lazy {
        Application.provide(requireContext()).prefs
    }

    private var loadJob: Job? = null
    private lateinit var adapter: WatchListPagingAdapter
    private val action = SingleLiveEvent<WishListAction>()

    private var isListVisible: Boolean
        get() = listView.isVisible
        set(visible) {
            listView.isVisible = visible
            progress.isVisible = false
        }

    private val stateViewModel: WatchListStateViewModel by activityViewModels()
    internal val viewModel: WatchListViewModel by viewModels { viewModelFactory() }

    protected open fun viewModelFactory(): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T = AppsWatchListViewModel(application) as T
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_applist, container, false)
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
        viewModel.sortId = args.getInt(ARG_SORT)
        viewModel.filterId = args.getInt(ARG_FILTER)
        viewModel.tag = args.getParcelable(ARG_TAG)

        // Setup layout manager
        val layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        listView.layoutManager = layoutManager

        // Setup header decorator
        adapter = WatchListPagingAdapter(
                viewModel.installedApps,
                viewLifecycleOwner.lifecycleScope,
                action,
                { emptyView -> createEmptyViewHolder(emptyView, action) },
                requireContext())
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

        action.map { mapEmptyAction(it) }.observe(viewLifecycleOwner, Observer {
            when (it) {
                is SearchInStore -> startActivity(MarketSearchActivity.intent(requireContext(), "", true))
                is ImportInstalled -> startActivity(InstalledFragment.intent(
                        Preferences.SORT_DATE_DESC,
                        requireContext(),
                        (activity as CustomThemeActivity).themeRes,
                        (activity as CustomThemeActivity).themeColors))
                is ShareFromStore -> activity?.startActivitySafely(Intent.makeMainActivity(ComponentName("com.android.vending", "com.android.vending.AssetBrowserActivity")))
                is AddAppToTag -> startActivity(AppsTagSelectActivity.createIntent(viewModel.tag!!, requireActivity()))
                is ItemClick -> {
                    val app = it.app
                    if (BuildConfig.DEBUG) {
                        AppLog.d(app.packageName)
                        Toast.makeText(activity, app.packageName, Toast.LENGTH_SHORT).show()
                    }
                    openAppDetails(app)
                }
            }
        })

        stateViewModel.sortId.observe(viewLifecycleOwner) {
            viewModel.sortId = it ?: 0
            reload()
        }

        stateViewModel.titleFilter.observe(viewLifecycleOwner) {
            viewModel.titleFilter = it ?: ""
            reload()
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

        viewModel.changes.observe(viewLifecycleOwner) { }

        reload()
    }

    protected open fun openAppDetails(app: App) {
        (requireActivity() as WatchListActivity).openAppDetails(app.appId, app.rowId, app.detailsUrl)
    }

    protected open fun config(filterId: Int) = WatchListPagingSource.Config(
            showRecentlyUpdated = prefs.showRecentlyUpdated,
            showOnDevice = filterId == Filters.TAB_ALL && prefs.showOnDevice,
            showRecentlyInstalled = filterId == Filters.TAB_ALL && prefs.showRecent
    )

    private fun reload() {
        isListVisible = false
        loadJob?.cancel()
        loadJob = lifecycleScope.launch {
            viewModel.load(config(viewModel.filterId)).collectLatest { result ->
                isListVisible = true
                AppLog.d("Load status changed: $result")
                adapter.submitData(result)
            }
        }
    }

    protected open fun createEmptyViewHolder(emptyView: View, action: SingleLiveEvent<WishListAction>) = EmptyViewHolder(emptyView, !prefs.showRecent, action)

    protected open fun mapEmptyAction(it: WishListAction): WishListAction {
        if (it is EmptyButton) {
            return when (it.idx) {
                1 -> SearchInStore
                2 -> ImportInstalled
                3 -> ShareFromStore
                else -> throw IllegalArgumentException("Unknown Idx")
            }
        }
        return it
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
            private val tag: Tag?
    ) : FragmentFactory("watch-list-$filterId-$sortId-${tag?.hashCode()}") {

        override fun create(): Fragment? = WatchListFragment().also {
            it.arguments = Bundle().apply {
                putInt(ARG_FILTER, filterId)
                putInt(ARG_SORT, sortId)
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
    }
}
