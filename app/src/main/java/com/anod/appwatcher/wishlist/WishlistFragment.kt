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
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.anod.appwatcher.R
import com.anod.appwatcher.databinding.FragmentWishlistBinding
import com.anod.appwatcher.model.AppInfoMetadata
import com.anod.appwatcher.search.Add
import com.anod.appwatcher.search.Delete
import com.anod.appwatcher.search.ResultAction
import com.anod.appwatcher.search.ResultsAdapterList
import com.anod.appwatcher.tags.TagSnackbar
import com.anod.appwatcher.utils.EventFlow
import com.anod.appwatcher.utils.prefs
import info.anodsplace.framework.app.CustomThemeColors
import info.anodsplace.framework.app.FragmentContainerFactory
import info.anodsplace.framework.app.FragmentToolbarActivity
import info.anodsplace.framework.view.Keyboard
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

/**
 * @author Alex Gavrishev
 * *
 * @date 16/12/2016.
 */
class WishListFragment : Fragment(), KoinComponent {

    private var loadJob: Job? = null
    private val viewModel: WishListViewModel by viewModels()
    lateinit var searchView: SearchView
    lateinit var adapter: ResultsAdapterList
    private val action = EventFlow<ResultAction>()
    private var _binding: FragmentWishlistBinding? = null
    val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWishlistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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

        binding.list.layoutManager = LinearLayoutManager(context)
        binding.retryButton.setOnClickListener {
            load()
        }

        binding.list.visibility = View.GONE
        binding.empty.visibility = View.GONE
        binding.loading.visibility = View.VISIBLE
        binding.retryView.visibility = View.GONE

        requireActivity().setTitle(R.string.wishlist)

        val account = requireArguments().getParcelable<Account>(EXTRA_ACCOUNT)
        val authToken = requireArguments().getString(EXTRA_AUTH_TOKEN) ?: ""

        if (account == null || authToken.isEmpty() || context == null) {
            Toast.makeText(context, R.string.choose_an_account, Toast.LENGTH_SHORT).show()
            requireActivity().finish()
            return
        } else {
            this.adapter = ResultsAdapterList(requireContext(), action, viewModel.packages, iconLoader = get(), uploadDateParserCache = get())
            binding.list.adapter = this.adapter
        }

        viewModel.appStatusChange.observe(viewLifecycleOwner) {
            val newStatus = it.first
            if (newStatus == AppInfoMetadata.STATUS_NORMAL) {
                TagSnackbar.make(binding.root, it.second!!, false, requireActivity(), prefs).show()
                binding.list.adapter!!.notifyDataSetChanged()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.packages.collectLatest {
                binding.list.adapter?.notifyDataSetChanged()
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            action.collectLatest {
                when (it) {
                    is Delete -> viewModel.delete(it.info)
                    is Add -> viewModel.add(it.info)
                }
            }
        }

        adapter.addLoadStateListener { loadStates ->
            when (loadStates.refresh) {
                is LoadState.Loading -> {
                    binding.loading.isVisible = true
                }
                is LoadState.NotLoading -> {
                    binding.loading.isVisible = false
                    if (adapter.itemCount == 0) {
                        showNoResults()
                    } else {
                        showListView()
                    }
                }
                is LoadState.Error -> {
                    binding.loading.isVisible = false
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
        binding.list.visibility = View.GONE
        binding.empty.visibility = View.GONE
        binding.loading.visibility = View.GONE
        binding.retryView.visibility = View.VISIBLE
    }

    private fun showListView() {
        binding.list.visibility = View.VISIBLE
        binding.empty.visibility = View.GONE
        binding.loading.visibility = View.GONE
        binding.retryView.visibility = View.GONE
    }

    private fun showNoResults() {
        binding.loading.visibility = View.GONE
        binding.list.visibility = View.GONE
        binding.retryView.visibility = View.GONE
        binding.empty.setText(R.string.no_result_wishlist)
        binding.empty.visibility = View.VISIBLE
    }

    companion object {
        const val EXTRA_ACCOUNT = "extra_account"
        const val EXTRA_AUTH_TOKEN = "extra_auth_token"

        private class Factory : FragmentContainerFactory("wishlist") {
            override fun create() = WishListFragment()
        }

        fun intent(context: Context, themeRes: Int, themeColors: CustomThemeColors, account: Account?, authToken: String?) = FragmentToolbarActivity.intent(
                context,
                Factory(),
                Bundle().apply {
                    putParcelable(EXTRA_ACCOUNT, account)
                    putString(EXTRA_AUTH_TOKEN, authToken)
                },
                themeRes,
                themeColors)
    }
}