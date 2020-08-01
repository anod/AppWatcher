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
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anod.appwatcher.Application
import com.anod.appwatcher.R
import com.anod.appwatcher.accounts.AccountSelectionDialog
import com.anod.appwatcher.accounts.AuthTokenBlocking
import com.anod.appwatcher.accounts.AuthTokenStartIntent
import com.anod.appwatcher.model.AppInfoMetadata
import com.anod.appwatcher.tags.TagSnackbar
import com.anod.appwatcher.utils.SingleLiveEvent
import com.anod.appwatcher.utils.Theme
import info.anodsplace.framework.AppLog
import info.anodsplace.framework.app.CustomThemeColors
import info.anodsplace.framework.app.ToolbarActivity
import info.anodsplace.framework.view.Keyboard
import kotlinx.android.synthetic.main.activity_market_search.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest

@ExperimentalPagingApi
@ExperimentalCoroutinesApi
@SuppressLint("Registered")
open class SearchActivity : ToolbarActivity(), AccountSelectionDialog.SelectionListener {

    private var searchJob: Job? = null
    override val themeRes: Int
        get() = Theme(this).theme
    override val themeColors: CustomThemeColors
        get() = Theme(this).colors

    private var adapter: RecyclerView.Adapter<ResultsAppViewHolder>? = null
    private val action = SingleLiveEvent<ResultAction>()
    lateinit var searchView: SearchView

    private val accountSelectionDialog: AccountSelectionDialog by lazy {
        AccountSelectionDialog(this, Application.provide(this).prefs, this)
    }

    private val viewModel: SearchViewModel by viewModels()

    override val layoutResource: Int
        get() = R.layout.activity_market_search

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        retryButton.setOnClickListener { retrySearchResult() }
        list.layoutManager = LinearLayoutManager(this)
        list.visibility = View.GONE
        empty.visibility = View.GONE
        loading.visibility = View.VISIBLE
        retryBox.visibility = View.GONE

        viewModel.initFromIntent(intent)

        viewModel.account = Application.provide(this).prefs.account
        if (viewModel.account == null) {
            accountSelectionDialog.show()
        } else {
            onAccountSelected(viewModel.account!!)
        }

        viewModel.searchQueryAuthenticated.observe(this, Observer {
            val query = it.first
            val authToken = it.second
            if (query.isBlank()) {
                showNoResults("")
            } else {
                search(query, authToken)
            }
        })

        viewModel.appStatusChange.observe(this, Observer {
            val newStatus = it.first
            if (newStatus == AppInfoMetadata.STATUS_NORMAL) {
                TagSnackbar.make(this, it.second!!, viewModel.isShareSource).show()
            }
        })

        viewModel.packages.observe(this, Observer {
            adapter?.notifyDataSetChanged()
        })

        action.observe(this, Observer {
            when (it) {
                is Delete -> viewModel.delete(it.info)
                is Add -> viewModel.add(it.info)
            }
        })
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

    @ExperimentalPagingApi
    private suspend fun onSearchStatusChange(status: SearchStatus) = withContext(Dispatchers.Main) {
        val context = this@SearchActivity
        when (status) {
            is Loading -> showLoading()
            is DetailsAvailable -> {
                showListView()
                adapter = ResultsAdapterSingle(context, action, viewModel.packages, status.document)
                list.adapter = adapter
            }
            is NoNetwork -> {
                loading.isVisible = false
                showRetryButton()
                Toast.makeText(context, R.string.check_connection, Toast.LENGTH_SHORT).show()
            }
            is Error -> {
                loading.isVisible = false
                showRetryButton()
            }
            is NoResults -> {
                showNoResults(viewModel.searchQuery.value ?: "")
            }
            is SearchPage -> {
                if (adapter !is ResultsAdapterList) {
                    adapter = ResultsAdapterList(context, action, viewModel.packages).apply {
                        addDataRefreshListener { isEmpty ->
                            if (isEmpty) {
                                showNoResults(viewModel.searchQuery.value ?: "")
                            } else {
                                showListView()
                            }
                        }
                    }
                    list.adapter = adapter
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

        val searchQuery = viewModel.searchQuery.value ?: ""
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
        list.visibility = View.GONE
        empty.visibility = View.GONE
        loading.visibility = View.GONE
        retryBox.visibility = View.VISIBLE
    }

    private fun showLoading() {
        list.visibility = View.GONE
        empty.visibility = View.GONE
        loading.visibility = View.VISIBLE
        retryBox.visibility = View.GONE
    }

    private fun showNoResults(query: String) {
        loading.visibility = View.GONE
        list.visibility = View.GONE
        retryBox.visibility = View.GONE
        empty.text = if (query.isNotEmpty()) getString(R.string.no_result_found, query) else getString(R.string.search_for_app)
        empty.visibility = View.VISIBLE
    }

    private fun showListView() {
        list.visibility = View.VISIBLE
        empty.visibility = View.GONE
        loading.visibility = View.GONE
        retryBox.visibility = View.GONE
    }

    private fun retrySearchResult() {
        val query = viewModel.searchQuery.value ?: ""
        if (query.isBlank()) {
            showNoResults("")
        } else {
            val authToken = viewModel.authToken.value ?: ""
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
