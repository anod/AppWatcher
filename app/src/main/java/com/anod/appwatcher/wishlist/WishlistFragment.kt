package com.anod.appwatcher.wishlist

import android.accounts.Account
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.anod.appwatcher.R
import com.anod.appwatcher.model.AppInfoMetadata
import com.anod.appwatcher.search.Add
import com.anod.appwatcher.search.Delete
import com.anod.appwatcher.search.ResultAction
import com.anod.appwatcher.search.ResultsAdapterList
import com.anod.appwatcher.tags.TagSnackbar
import com.anod.appwatcher.utils.SingleLiveEvent
import info.anodsplace.framework.app.CustomThemeColors
import info.anodsplace.framework.app.FragmentFactory
import info.anodsplace.framework.app.FragmentToolbarActivity
import info.anodsplace.framework.view.Keyboard
import kotlinx.android.synthetic.main.fragment_wishlist.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest

/**
 * @author Alex Gavrishev
 * *
 * @date 16/12/2016.
 */
@ExperimentalPagingApi
@ExperimentalCoroutinesApi
@FlowPreview
@InternalCoroutinesApi
class WishListFragment : Fragment() {

    private var loadJob: Job? = null
    private val viewModel: WishListViewModel by viewModels()
    lateinit var searchView: SearchView
    lateinit var adapter: ResultsAdapterList
    private val action = SingleLiveEvent<ResultAction>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_wishlist, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.searchbox, menu)
        val searchItem = menu.findItem(R.id.menu_search)
        searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                viewModel.nameFilter = query
                Keyboard.hide(searchView, requireContext())
                load()
                return false
            }

            override fun onQueryTextChange(query: String): Boolean {
                return false
            }
        })
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        list.layoutManager = LinearLayoutManager(context)
        retryButton.setOnClickListener {
            load()
        }

        list.visibility = View.GONE
        empty.visibility = View.GONE
        loading.visibility = View.VISIBLE
        retryView.visibility = View.GONE

        requireActivity().setTitle(R.string.wishlist)

        val account = requireArguments().getParcelable<Account>(EXTRA_ACCOUNT)
        val authToken = requireArguments().getString(EXTRA_AUTH_TOKEN) ?: ""

        if (account == null || authToken.isEmpty() || context == null) {
            Toast.makeText(context, R.string.choose_an_account, Toast.LENGTH_SHORT).show()
            requireActivity().finish()
            return
        } else {
            viewModel.init(account, authToken)
            this.adapter = ResultsAdapterList(requireContext(), action, viewModel.packages)
            list.adapter = this.adapter
        }

        viewModel.appStatusChange.observe(viewLifecycleOwner, Observer {
            val newStatus = it.first
            if (newStatus == AppInfoMetadata.STATUS_NORMAL) {
                TagSnackbar.make(requireActivity(), it.second!!, false).show()
                list.adapter!!.notifyDataSetChanged()
            }
        })

        viewModel.packages.observe(viewLifecycleOwner, Observer {
            list.adapter?.notifyDataSetChanged()
        })

        action.observe(this, Observer {
            when (it) {
                is Delete -> viewModel.delete(it.info)
                is Add -> viewModel.add(it.info)
            }
        })

        adapter.addDataRefreshListener { isEmpty ->
            if (isEmpty) {
                showNoResults()
            } else {
                showListView()
            }
        }

        adapter.addLoadStateListener { loadStates ->
            when (loadStates.refresh) {
                is LoadState.Loading -> {
                    loading.isVisible = true
                }
                is LoadState.NotLoading -> {
                    loading.isVisible = false
                    showListView()
                }
                is LoadState.Error -> {
                    loading.isVisible = false
                    showRetryButton()
                }
            }
        }

        load()
    }

    private fun load() {
        loadJob?.cancel()
        loadJob = lifecycleScope.launch {
            viewModel.load().collectLatest {
                adapter.submitData(it)
            }
        }
    }

    private fun showRetryButton() {
        list.visibility = View.GONE
        empty.visibility = View.GONE
        loading.visibility = View.GONE
        retryView.visibility = View.VISIBLE
    }

    private fun showListView() {
        list.visibility = View.VISIBLE
        empty.visibility = View.GONE
        loading.visibility = View.GONE
        retryView.visibility = View.GONE
    }

    private fun showNoResults() {
        loading.visibility = View.GONE
        list.visibility = View.GONE
        retryView.visibility = View.GONE
        empty.setText(R.string.no_result_wishlist)
        empty.visibility = View.VISIBLE
    }

    companion object {
        const val EXTRA_ACCOUNT = "extra_account"
        const val EXTRA_AUTH_TOKEN = "extra_auth_token"

        private class Factory : FragmentFactory("wishlist") {
            override fun create() = WishListFragment()
        }

        fun intent(context: Context, themeRes: Int, themeColors: CustomThemeColors, account: Account?, authToken: String?) = FragmentToolbarActivity.intent(
                Factory(),
                Bundle().apply {
                    putParcelable(EXTRA_ACCOUNT, account)
                    putString(EXTRA_AUTH_TOKEN, authToken)
                },
                themeRes,
                themeColors,
                context)
    }
}
