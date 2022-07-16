package com.anod.appwatcher.watchlist

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.anod.appwatcher.BuildConfig
import com.anod.appwatcher.MarketSearchActivity
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.AppListItem
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.databinding.FragmentApplistBinding
import com.anod.appwatcher.databinding.ListItemEmptyBinding
import com.anod.appwatcher.installed.InstalledFragment
import com.anod.appwatcher.model.Filters
import com.anod.appwatcher.tags.AppsTagSelectDialog
import com.anod.appwatcher.utils.EventFlow
import com.anod.appwatcher.utils.appScope
import com.anod.appwatcher.utils.prefs
import info.anodsplace.applog.AppLog
import info.anodsplace.framework.app.CustomThemeActivity
import info.anodsplace.framework.app.FragmentContainerFactory
import info.anodsplace.framework.content.startActivitySafely
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

sealed class WishListAction
object SearchInStore : WishListAction()
class SectionHeaderClick(val header: SectionHeader) : WishListAction()
class Installed(val importMode: Boolean) : WishListAction()
object ShareFromStore : WishListAction()
class AddAppToTag(val tag: Tag) : WishListAction()
class EmptyButton(val idx: Int) : WishListAction()
class ItemClick(val app: App, val index: Int) : WishListAction()
class ItemLongClick(val app: App, val index: Int) : WishListAction()

open class WatchListFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener, KoinComponent {
    protected lateinit var adapter: WatchListPagingAdapter
    protected val viewModel: WatchListViewModel by viewModels { viewModelFactory() }

    private var loadJob: Job? = null
    private val action = EventFlow<WishListAction>()
    private val stateViewModel: WatchListStateViewModel by activityViewModels()
    private var _binding: FragmentApplistBinding? = null
    val binding get() = _binding!!

    protected open fun viewModelFactory(): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AppsWatchListViewModel(get()) as T
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentApplistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (prefs.enablePullToRefresh) {
            binding.swipeLayout.setOnRefreshListener(this)
        } else {
            binding.swipeLayout.isEnabled = false
        }

        binding.progress.isVisible = true
        val metrics = resources.displayMetrics
        binding.swipeLayout.setDistanceToTriggerSync((16 * metrics.density).toInt())

        val args = requireArguments()
        viewModel.sortId = args.getInt(ARG_SORT)
        viewModel.filterId = args.getInt(ARG_FILTER)
        viewModel.tag = args.getParcelable(ARG_TAG)

        // Setup layout manager
        val layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        binding.listView.layoutManager = layoutManager

        // Setup header decorator
        adapter = WatchListPagingAdapter(
                viewModel.installedApps,
                viewModel.recentlyInstalledPackages,
                viewLifecycleOwner,
                action,
                { emptyBinding -> createEmptyViewHolder(emptyBinding, action) },
                { appItem -> getItemSelection(appItem) },
                viewModel.selection,
                requireContext(),
                iconLoader = get(),
                packageManager = get()
        )
        binding.listView.adapter = adapter

        // When an item inserted into top there is no indication and list maintains previous position
        // Request to scroll to the top in this case
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (_binding == null) {
                    return
                }
                if (positionStart == 0 && (binding.listView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition() == 0) {
                    if (isVisible) {
                        binding.listView.scrollToPosition(0)
                    }
                }
            }
        })

        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            adapter
                    .loadStateFlow
                    .drop(1) // fix empty view flickering
                    .collectLatest { loadState ->
                        val isEmpty = (
                                loadState.source.refresh is LoadState.NotLoading
                                        && adapter.itemCount < 1
                                )
                        AppLog.d("loadStateFlow: ${loadState.source.refresh}, isEmpty: $isEmpty")
                        if (isEmpty) {
                            if (binding.emptyView.childCount == 0) {
                                val emptyBinding = ListItemEmptyBinding.inflate(layoutInflater, binding.emptyView, true)
                                createEmptyViewHolder(emptyBinding, action)
                            }
                            binding.emptyView.isVisible = true
                            binding.listView.isVisible = false
                        } else {
                            binding.emptyView.isVisible = false
                            binding.listView.isVisible = true
                        }
                    }
        }

        if (prefs.enablePullToRefresh) {
            binding.listView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val isOnTop = !recyclerView.canScrollVertically(-1)
                    binding.swipeLayout.isEnabled = isOnTop
                }
            })
        }

        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            action.collectLatest {
                val mapped = mapAction(it)
                onListAction(mapped)
            }
        }

        stateViewModel.sortId.observe(viewLifecycleOwner) {
            viewModel.sortId = it ?: 0
            reload()
        }

        stateViewModel.titleFilter.distinctUntilChanged().observe(viewLifecycleOwner) {
            viewModel.titleFilter = it ?: ""
            reload()
        }

        stateViewModel.listState.observe(viewLifecycleOwner) {
            when (it) {
                is SyncStarted -> {
                    binding.swipeLayout.isRefreshing = true
                }
                else -> {
                    binding.swipeLayout.isRefreshing = false
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.changes.collect {
                adapter.refresh()
            }
        }

        reload()
    }

    protected open fun onListAction(action: WishListAction) {
        when (action) {
            is SearchInStore -> startActivity(MarketSearchActivity.intent(requireContext(), "", true))
            is Installed -> startActivity(InstalledFragment.intent(
                    action.importMode,
                    requireContext(),
                    (activity as CustomThemeActivity).themeRes,
                    (activity as CustomThemeActivity).themeColors))
            is ShareFromStore -> activity?.startActivitySafely(Intent.makeMainActivity(ComponentName("com.android.vending", "com.android.vending.AssetBrowserActivity")))
            is AddAppToTag -> AppsTagSelectDialog.show(action.tag, childFragmentManager)
            is ItemClick -> {
                val app = action.app
                if (BuildConfig.DEBUG) {
                    AppLog.d(app.packageName)
                }
                openAppDetails(app)
            }
            else -> {}
        }
    }

    protected open fun getItemSelection(appItem: AppListItem): AppViewHolder.Selection = AppViewHolder.Selection.None

    protected fun openAppDetails(app: App) {
        (requireActivity() as AppDetailsRouter).openAppDetails(app.appId, app.rowId, app.detailsUrl)
    }

    protected open fun config() = WatchListPagingSource.Config(
            showRecentlyUpdated = prefs.showRecentlyUpdated,
            showOnDevice = viewModel.filterId == Filters.TAB_ALL && prefs.showOnDevice,
            showRecentlyInstalled = viewModel.filterId == Filters.TAB_ALL && prefs.showRecent
    )

    fun reload(initialKey: Int? = null) {
        binding.listView.isVisible = false
        loadJob?.cancel()
        loadJob = lifecycleScope.launchWhenCreated {
            onReload()
            viewModel.load(config(), initialKey = initialKey).collectLatest { result ->
                binding.listView.isVisible = true
                binding.progress.isVisible = false
                AppLog.d("Load status changed: $initialKey - $result")
                adapter.submitData(result)
            }
        }
    }

    open suspend fun onReload() {}

    protected open fun createEmptyViewHolder(
            emptyBinding: ListItemEmptyBinding,
            action: EventFlow<WishListAction>
    ) = EmptyViewHolder(emptyBinding, false, action)

    protected open fun mapAction(it: WishListAction): WishListAction {
        if (it is EmptyButton) {
            return when (it.idx) {
                1 -> SearchInStore
                2 -> Installed(true)
                3 -> ShareFromStore
                else -> throw IllegalArgumentException("Unknown Idx")
            }
        }
        if (it is SectionHeaderClick) {
            return when (it.header) {
                is RecentlyInstalledHeader -> Installed(false)
                else -> throw IllegalArgumentException("Not supported header")
            }
        }
        return it
    }

    override fun onRefresh() {
        val isRefreshing = (stateViewModel.listState.value is SyncStarted)
        if (!isRefreshing) {
            appScope.launch {
                stateViewModel.requestRefresh().collect { }
            }
        }
    }

    class Factory(
            private val filterId: Int,
            private val sortId: Int,
            private val tag: Tag?
    ) : FragmentContainerFactory("watch-list-$filterId-$sortId-${tag?.hashCode()}") {

        override fun create(): Fragment = WatchListFragment().also {
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
        internal const val ARG_SHOW_ACTION = "showAction"
    }
}