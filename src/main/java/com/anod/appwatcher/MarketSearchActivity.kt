package com.anod.appwatcher

import android.accounts.Account
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import butterknife.bindView
import com.android.volley.VolleyError
import com.anod.appwatcher.accounts.AccountChooser
import com.anod.appwatcher.market.CompositeStateEndpoint
import com.anod.appwatcher.market.DetailsEndpoint
import com.anod.appwatcher.market.PlayStoreEndpointBase
import com.anod.appwatcher.market.SearchEndpoint
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.model.AppInfoMetadata
import com.anod.appwatcher.model.WatchAppList
import com.anod.appwatcher.search.ResultsAdapter
import com.anod.appwatcher.search.ResultsAdapterDetails
import com.anod.appwatcher.search.ResultsAdapterSearch
import com.anod.appwatcher.tags.TagSnackbar
import com.anod.appwatcher.ui.ToolbarActivity
import com.anod.appwatcher.utils.Keyboard
import com.anod.appwatcher.utils.MetricsManagerEvent

class MarketSearchActivity : ToolbarActivity(), AccountChooser.OnAccountSelectionListener, WatchAppList.Listener, CompositeStateEndpoint.Listener {

    private lateinit var adapter: ResultsAdapter

    val loading: LinearLayout by bindView(R.id.loading)
    val listView: RecyclerView by bindView(android.R.id.list)
    val emptyView: TextView by bindView(android.R.id.empty)
    val retryView: LinearLayout by bindView(R.id.retry_box)
    val retryButton: Button by bindView(R.id.retry)
    lateinit var searchView: SearchView

    private var initiateSearch = false
    private var isShareSource = false

    val accountChooser: AccountChooser by lazy {
        AccountChooser(this, App.provide(this).prefs, this)
    }
    private var searchQuery = ""
    private var hasFocus = false
    private var isPackageSearch = false
    private lateinit var endpoints: CompositeStateEndpoint
    private lateinit var watchAppList: WatchAppList

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_market_search)
        setupToolbar()

        endpoints = CompositeStateEndpoint(this)
        endpoints.add(SEARCH_ENDPOINT_ID, SearchEndpoint(this, true))
        endpoints.add(DETAILS_ENDPOINT_ID, DetailsEndpoint(this))

        watchAppList = WatchAppList(this)

        retryButton.setOnClickListener { retrySearchResult() }
        loading.visibility = View.GONE
        listView.layoutManager = LinearLayoutManager(this)
        listView.visibility = View.GONE
        emptyView.visibility = View.GONE
        loading.visibility = View.VISIBLE
        retryView.visibility = View.GONE

        initFromIntent(intent)
        accountChooser.init()
    }

    private fun initFromIntent(i: Intent?) {
        if (i == null) {
            return
        }
        val keyword = i.getStringExtra(EXTRA_KEYWORD)
        if (keyword != null) {
            searchQuery = keyword
        }
        isPackageSearch = i.getBooleanExtra(EXTRA_PACKAGE, false)
        initiateSearch = i.getBooleanExtra(EXTRA_EXACT, false)
        isShareSource = i.getBooleanExtra(EXTRA_SHARE, false)
        hasFocus = i.getBooleanExtra(EXTRA_FOCUS, false)


        if (isPackageSearch) {
            endpoints.setActive(DETAILS_ENDPOINT_ID)
            adapter = ResultsAdapterDetails(this, endpoints.get(DETAILS_ENDPOINT_ID) as DetailsEndpoint, watchAppList)
        } else {
            endpoints.setActive(SEARCH_ENDPOINT_ID)
            adapter = ResultsAdapterSearch(this, endpoints.get(SEARCH_ENDPOINT_ID) as SearchEndpoint, watchAppList)
        }
    }

    override fun onPause() {
        watchAppList.detach()
        super.onPause()
    }

    override fun onResume() {
        watchAppList.attach(this)
        super.onResume()
    }

    private fun searchResults() {
        listView.adapter = adapter
        endpoints.reset()
        showLoading()

        MetricsManagerEvent.track(this, "perform_search", "SEARCH_QUERY", searchQuery, "SEARCH_PACKAGE", isPackageSearch.toString(), "FROM_SHARE", isShareSource.toString())

        if (searchQuery.isNotEmpty()) {
            if (isPackageSearch) {
                val url = AppInfo.createDetailsUrl(searchQuery)
                (endpoints[DETAILS_ENDPOINT_ID] as DetailsEndpoint).url = url
            }
            (endpoints[SEARCH_ENDPOINT_ID] as SearchEndpoint).query = searchQuery
            endpoints.active().startAsync()
        } else {
            showNoResults("")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.searchbox, menu)

        val searchItem = menu.findItem(R.id.menu_search)
        searchView = searchItem.actionView as SearchView
        searchView.setIconifiedByDefault(false)
        searchItem.expandActionView()

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchQuery = query
                searchResultsDelayed()
                Keyboard.hide(searchView, this@MarketSearchActivity)
                return false
            }

            override fun onQueryTextChange(query: String): Boolean {
                return false
            }
        })

        searchView.setQuery(searchQuery, true)
        if (hasFocus) {
            searchView.post { searchView.requestFocus() }
        } else {
            Keyboard.hide(searchView, this)
        }
        return true
    }

    private fun searchResultsDelayed() {
        if (endpoints.authSubToken.isEmpty()) {
            initiateSearch = true
        } else {
            searchResults()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_search -> {
                searchResultsDelayed()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onAccountSelected(account: Account, authSubToken: String?) {
        if (authSubToken == null) {
            if (App.with(this).isNetworkAvailable) {
                Toast.makeText(this, R.string.failed_gain_access, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, R.string.check_connection, Toast.LENGTH_SHORT).show()
            }
            finish()
            return
        }
        endpoints.setAccount(account, authSubToken)
        if (initiateSearch && searchQuery.isNotEmpty()) {
            searchResults()
        } else {
            showNoResults("")
        }
    }

    override fun onAccountNotFound() {
        if (App.with(this).isNetworkAvailable) {
            Toast.makeText(this, R.string.failed_gain_access, Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, R.string.check_connection, Toast.LENGTH_SHORT).show()
        }
        finish()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        accountChooser.onRequestPermissionResult(requestCode, permissions, grantResults)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        accountChooser.onActivityResult(requestCode, resultCode, data)
    }

    private fun showRetryButton() {
        listView.visibility = View.GONE
        emptyView.visibility = View.GONE
        loading.visibility = View.GONE
        retryView.visibility = View.VISIBLE
    }

    private fun showLoading() {
        listView.visibility = View.GONE
        emptyView.visibility = View.GONE
        loading.visibility = View.VISIBLE
        retryView.visibility = View.GONE
    }

    private fun showNoResults(query: String) {
        loading.visibility = View.GONE
        listView.visibility = View.GONE
        retryView.visibility = View.GONE
        val noResStr = if (query.isNotEmpty()) getString(R.string.no_result_found, query) else getString(R.string.search_for_app)
        emptyView.text = noResStr
        emptyView.visibility = View.VISIBLE
    }

    private fun showListView() {
        listView.visibility = View.VISIBLE
        emptyView.visibility = View.GONE
        loading.visibility = View.GONE
        retryView.visibility = View.GONE
    }

    private fun retrySearchResult() {
        if (adapter.isEmpty) {
            searchResultsDelayed()
        } else {
            endpoints.active().startAsync()
        }
    }

    override fun onWatchListChangeSuccess(info: AppInfo, newStatus: Int) {
        adapter.notifyDataSetChanged()
        if (newStatus == AppInfoMetadata.STATUS_NORMAL) {
            TagSnackbar.make(this, info, isShareSource).show()
        }
    }

    override fun onWatchListChangeError(info: AppInfo, error: Int) {
        if (WatchAppList.ERROR_ALREADY_ADDED == error) {
            Toast.makeText(this, R.string.app_already_added, Toast.LENGTH_SHORT).show()
            adapter.notifyDataSetChanged()
        } else if (error == WatchAppList.ERROR_INSERT) {
            Toast.makeText(this, R.string.error_insert_app, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDataChanged(id: Int, endpoint: PlayStoreEndpointBase) {
        if (id == DETAILS_ENDPOINT_ID) {
            if ((endpoint as DetailsEndpoint).document != null) {
                showListView()
                adapter.notifyDataSetChanged()
            } else {
                adapter = ResultsAdapterSearch(this, endpoints[SEARCH_ENDPOINT_ID] as SearchEndpoint, watchAppList)
                listView.adapter = adapter
                endpoints.setActive(SEARCH_ENDPOINT_ID).startAsync()
            }
        } else {
            val searchEndpoint = endpoint as SearchEndpoint
            if (searchEndpoint.count == 0) {
                showNoResults(searchEndpoint.query)
            } else {
                showListView()
                adapter.notifyDataSetChanged()
            }
        }
    }

    override fun onErrorResponse(id: Int, endpoint: PlayStoreEndpointBase, error: VolleyError) {
        if (!App.with(this).isNetworkAvailable) {
            loading.visibility = View.GONE
            showRetryButton()
            Toast.makeText(this, R.string.check_connection, Toast.LENGTH_SHORT).show()
            return
        }
        if (id == DETAILS_ENDPOINT_ID) {
            adapter = ResultsAdapterSearch(this, endpoints.get(SEARCH_ENDPOINT_ID) as SearchEndpoint, watchAppList)
            listView.adapter = adapter
            endpoints.setActive(SEARCH_ENDPOINT_ID).startAsync()
        } else {
            loading.visibility = View.GONE
            showRetryButton()
        }
    }

    companion object {
        const val EXTRA_KEYWORD = "keyword"
        const val EXTRA_EXACT = "exact"
        const val EXTRA_SHARE = "share"
        const val EXTRA_FOCUS = "focus"
        const val EXTRA_PACKAGE = "package"

        private const val DETAILS_ENDPOINT_ID = 0
        private const val SEARCH_ENDPOINT_ID = 1
    }
}
