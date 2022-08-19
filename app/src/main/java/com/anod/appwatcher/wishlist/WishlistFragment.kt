package com.anod.appwatcher.wishlist

import android.accounts.Account
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.anod.appwatcher.compose.AppTheme
import com.anod.appwatcher.databinding.FragmentWishlistBinding
import info.anodsplace.framework.app.CustomThemeColors
import info.anodsplace.framework.app.FragmentContainerFactory
import info.anodsplace.framework.app.FragmentToolbarActivity
import org.koin.core.component.KoinComponent

/**
 * @author Alex Gavrishev
 * *
 * @date 16/12/2016.
 */
class WishListFragment : Fragment(), KoinComponent {
    private var _binding: FragmentWishlistBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWishlistBinding.inflate(inflater, container, false)
        binding.composeView.apply {
            // Dispose of the Composition when the view's LifecycleOwner
            // is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                // In Compose world
                AppTheme {

                }
            }

        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        binding.list.layoutManager = LinearLayoutManager(context)
//        binding.retryButton.setOnClickListener {
//            load()
//        }
//
//        binding.list.visibility = View.GONE
//        binding.empty.visibility = View.GONE
//        binding.loading.visibility = View.VISIBLE
//        binding.retryView.visibility = View.GONE
//
//        requireActivity().setTitle(R.string.wishlist)
//
//        val account = requireArguments().getParcelable<Account>(EXTRA_ACCOUNT)
//        val authToken = requireArguments().getString(EXTRA_AUTH_TOKEN) ?: ""
//
//        if (account == null || authToken.isEmpty() || context == null) {
//            Toast.makeText(context, R.string.choose_an_account, Toast.LENGTH_SHORT).show()
//            requireActivity().finish()
//            return
//        } else {
//            this.adapter = ResultsAdapterList(requireContext(), action, viewModel.packages, iconLoader = get(), uploadDateParserCache = get())
//            binding.list.adapter = this.adapter
//        }
//
//        viewModel.appStatusChange.observe(viewLifecycleOwner) {
//            val newStatus = it.first
//            if (newStatus == AppInfoMetadata.STATUS_NORMAL) {
//                TagSnackbar.make(binding.root, it.second!!, false, requireActivity(), prefs).show()
//                binding.list.adapter!!.notifyDataSetChanged()
//            }
//        }
//
//        viewLifecycleOwner.lifecycleScope.launch {
//            viewModel.packages.collectLatest {
//                binding.list.adapter?.notifyDataSetChanged()
//            }
//        }
//
//        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
//            action.collectLatest {
//                when (it) {
//                    is Delete -> viewModel.delete(it.info)
//                    is Add -> viewModel.add(it.info)
//                }
//            }
//        }
//
//        adapter.addLoadStateListener { loadStates ->
//            when (loadStates.refresh) {
//                is LoadState.Loading -> {
//                    binding.loading.isVisible = true
//                }
//                is LoadState.NotLoading -> {
//                    binding.loading.isVisible = false
//                    if (adapter.itemCount == 0) {
//                        showNoResults()
//                    } else {
//                        showListView()
//                    }
//                }
//                is LoadState.Error -> {
//                    binding.loading.isVisible = false
//                    showRetryButton()
//                }
//            }
//        }
//
//        load()
//    }
//
//    private fun load() {
//        loadJob?.cancel()
//        loadJob = lifecycleScope.launch {
//            viewModel.load().collectLatest {
//                adapter.submitData(it)
//            }
//        }
//    }
//
//    private fun showRetryButton() {
//        binding.list.visibility = View.GONE
//        binding.empty.visibility = View.GONE
//        binding.loading.visibility = View.GONE
//        binding.retryView.visibility = View.VISIBLE
//    }
//
//    private fun showListView() {
//        binding.list.visibility = View.VISIBLE
//        binding.empty.visibility = View.GONE
//        binding.loading.visibility = View.GONE
//        binding.retryView.visibility = View.GONE
//    }
//
//    private fun showNoResults() {
//        binding.loading.visibility = View.GONE
//        binding.list.visibility = View.GONE
//        binding.retryView.visibility = View.GONE
//        binding.empty.setText(R.string.no_result_wishlist)
//        binding.empty.visibility = View.VISIBLE
//    }

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