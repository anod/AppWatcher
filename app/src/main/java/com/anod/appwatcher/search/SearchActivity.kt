package com.anod.appwatcher.search

import android.accounts.Account
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anod.appwatcher.Application
import com.anod.appwatcher.R
import com.anod.appwatcher.accounts.AccountSelectionDialog
import com.anod.appwatcher.accounts.AuthTokenBlocking
import com.anod.appwatcher.accounts.AuthTokenStartIntent
import com.anod.appwatcher.databinding.ActivityMarketSearchBinding
import com.anod.appwatcher.model.AppInfoMetadata
import com.anod.appwatcher.tags.TagSnackbar
import com.anod.appwatcher.utils.EventFlow
import com.anod.appwatcher.utils.Theme
import info.anodsplace.applog.AppLog
import info.anodsplace.framework.app.CustomThemeColors
import info.anodsplace.framework.app.ToolbarActivity
import info.anodsplace.framework.view.Keyboard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("Registered")
open class SearchActivity : ToolbarActivity(), AccountSelectionDialog.SelectionListener {

    private var searchJob: Job? = null
    override val themeRes: Int
        get() = Theme(this).theme
    override val themeColors: CustomThemeColors
        get() = Theme(this).colors

    private var adapter: RecyclerView.Adapter<ResultsAppViewHolder>? = null
    private val action = EventFlow<ResultAction>()
    lateinit var searchView: SearchView

    private val accountSelectionDialog: AccountSelectionDialog by lazy {
        AccountSelectionDialog(this, Application.provide(this).prefs, this)
    }

    private val viewModel: SearchViewModel by viewModels()

    private lateinit var binding: ActivityMarketSearchBinding
    override val layoutView: View
        get() {
            binding = ActivityMarketSearchBinding.inflate(layoutInflater)
            return binding.root
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.retryButton.setOnClickListener { retrySearchResult() }
        binding.list.layoutManager = LinearLayoutManager(this)
        binding.list.visibility = View.GONE
        binding.empty.visibility = View.GONE
        binding.loading.visibility = View.VISIBLE
        binding.retryBox.visibility = View.GONE

        viewModel.initFromIntent(intent)

        viewModel.account = Application.provide(this).prefs.account
        if (viewModel.account == null) {
            accountSelectionDialog.show()
        } else {
            onAccountSelected(viewModel.account!!)
        }

        lifecycleScope.launch {
            viewModel.searchQueryAuthenticated.collectLatest {
                val query = it.first
                val authToken = it.second
                if (query.isBlank()) {
                    showNoResults("")
                } else {
                    search(query, authToken)
                }
            }
        }

        lifecycleScope.launch {
            launch {
                viewModel.appStatusChange.collect {
                    val newStatus = it.first
                    if (newStatus == AppInfoMetadata.STATUS_NORMAL) {
                        TagSnackbar.make(
                            binding.root,
                            it.second!!,
                            viewModel.isShareSource,
                            this@SearchActivity
                        ).show()
                    }
                }
            }

            viewModel.packages.collectLatest {
                adapter?.notifyDataSetChanged()
            }
        }

        lifecycleScope.launchWhenResumed {
            action.collectLatest {
                when (it) {
                    is Delete -> viewModel.delete(it.info)
                    is Add -> viewModel.add(it.info)
                }
            }
        }
    }

    private fun search(query: String, authToken: String) {
        searchJob?.cancel()
        searchJob = lifecycleScope.launch {
            viewModel.search(query, authToken).collectLatest { status ->
                AppLog.d("Search status changed: $status")
                onSearchStatusChange(status)
            }
        }
    }

    private suspend fun onSearchStatusChange(status: SearchStatus) = withContext(Dispatchers.Main) {
        val context = this@SearchActivity
        when (status) {
            is Loading -> showLoading()
            is DetailsAvailable -> {
                showListView()
                adapter = ResultsAdapterSingle(context, action, viewModel.packages, status.document)
                binding.list.adapter = adapter
            }
            is NoNetwork -> {
                binding.loading.isVisible = false
                showRetryButton()
                Toast.makeText(context, R.string.check_connection, Toast.LENGTH_SHORT).show()
            }
            is Error -> {
                binding.loading.isVisible = false
                showRetryButton()
            }
            is NoResults -> {
                showNoResults(viewModel.searchQuery.value)
            }
            is SearchPage -> {
                if (adapter !is ResultsAdapterList) {
                    adapter = ResultsAdapterList(context, action, viewModel.packages).apply {
                        addLoadStateListener { loadState ->
                            if (loadState.refresh is LoadState.NotLoading) {
                                if (itemCount > 0) {
                                    showListView()
                                } else {
                                    showNoResults(viewModel.searchQuery.value)
                                }
                            }
                        }
                    }
                    binding.list.adapter = adapter
                }
                (adapter as ResultsAdapterList).submitData(status.pagingData)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.searchbox, menu)

        val searchItem = menu.findItem(R.id.menu_search)
        searchView = searchItem.actionView as SearchView
        searchItem.expandActionView()

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                viewModel.searchQuery.value = query
                Keyboard.hide(searchView, this@SearchActivity)
                return false
            }

            override fun onQueryTextChange(query: String): Boolean {
                return false
            }
        })

        val searchQuery = viewModel.searchQuery.value
        searchView.setQuery(searchQuery, true)
        if (viewModel.hasFocus) {
            searchView.post { searchView.requestFocus() }
        } else {
            Keyboard.hide(searchView, this)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_search -> {
                retrySearchResult()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onAccountSelected(account: Account) {
        viewModel.account = account
        lifecycleScope.launch {
            try {
                val token = AuthTokenBlocking(applicationContext).retrieve(account)
                if (token.isNotBlank()) {
                    viewModel.authToken.value = token
                } else {
                    if (Application.provide(this@SearchActivity).networkConnection.isNetworkAvailable) {
                        Toast.makeText(this@SearchActivity, R.string.failed_gain_access, Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@SearchActivity, R.string.check_connection, Toast.LENGTH_SHORT).show()
                    }
                    finish()
                }
            } catch (e: AuthTokenStartIntent) {
                startActivity(e.intent)
                finish()
            }

        }
    }

    override fun onAccountNotFound(errorMessage: String) {
        if (Application.provide(this).networkConnection.isNetworkAvailable) {
            if (errorMessage.isNotBlank()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, R.string.failed_gain_access, Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, R.string.check_connection, Toast.LENGTH_SHORT).show()
        }
        finish()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        accountSelectionDialog.onRequestPermissionResult(requestCode, permissions, grantResults)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        accountSelectionDialog.onActivityResult(requestCode, resultCode, data)
    }

    private fun showRetryButton() {
        binding.list.visibility = View.GONE
        binding.empty.visibility = View.GONE
        binding.loading.visibility = View.GONE
        binding.retryBox.visibility = View.VISIBLE
    }

    private fun showLoading() {
        binding.list.visibility = View.GONE
        binding.empty.visibility = View.GONE
        binding.loading.visibility = View.VISIBLE
        binding.retryBox.visibility = View.GONE
    }

    private fun showNoResults(query: String) {
        binding.loading.visibility = View.GONE
        binding.list.visibility = View.GONE
        binding.retryBox.visibility = View.GONE
        binding.empty.text = if (query.isNotEmpty()) getString(R.string.no_result_found, query) else getString(R.string.search_for_app)
        binding.empty.visibility = View.VISIBLE
    }

    private fun showListView() {
        binding.list.visibility = View.VISIBLE
        binding.empty.visibility = View.GONE
        binding.loading.visibility = View.GONE
        binding.retryBox.visibility = View.GONE
    }

    private fun retrySearchResult() {
        val query = viewModel.searchQuery.value
        if (query.isBlank()) {
            showNoResults("")
        } else {
            val authToken = viewModel.authToken.value
            if (authToken.isEmpty()) {
                if (viewModel.account == null) {
                    accountSelectionDialog.show()
                } else {
                    onAccountSelected(viewModel.account!!)
                }
            } else {
                search(query, authToken)
            }
        }
    }

    companion object {
        const val EXTRA_KEYWORD = "keyword"
        const val EXTRA_EXACT = "exact"
        const val EXTRA_SHARE = "share"
        const val EXTRA_FOCUS = "focus"
        const val EXTRA_PACKAGE = "package"
    }
}
