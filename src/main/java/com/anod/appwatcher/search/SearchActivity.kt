package com.anod.appwatcher.search

import android.accounts.Account
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.android.volley.VolleyError
import com.anod.appwatcher.App
import com.anod.appwatcher.R
import com.anod.appwatcher.accounts.AccountSelectionDialog
import com.anod.appwatcher.accounts.AuthTokenAsync
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.model.AppInfoMetadata
import com.anod.appwatcher.model.WatchAppList
import com.anod.appwatcher.tags.TagSnackbar
import com.anod.appwatcher.utils.Theme
import info.anodsplace.framework.app.ToolbarActivity
import info.anodsplace.framework.view.Keyboard
import info.anodsplace.playstore.CompositeStateEndpoint
import info.anodsplace.playstore.DetailsEndpoint
import info.anodsplace.playstore.PlayStoreEndpointBase
import info.anodsplace.playstore.SearchEndpoint
import kotlinx.android.synthetic.main.activity_market_search.*

open class SearchActivity : ToolbarActivity(), AccountSelectionDialog.SelectionListener, WatchAppList.Listener, CompositeStateEndpoint.Listener {

    override val themeRes: Int
        get() =  Theme(this).theme

    private var adapter: ResultsAdapter? = null

    lateinit var searchView: SearchView

    private var initiateSearch = false
    private var isShareSource = false
    private var account: Account? = null

    val accountSelectionDialog: AccountSelectionDialog by lazy {
        AccountSelectionDialog(this, App.provide(this).prefs, this)
    }
    private var searchQuery = ""
    private var hasFocus = false
    private var isPackageSearch = false
    private val endpoints: CompositeStateEndpoint by lazy { CompositeStateEndpoint(this) }
    private val watchAppList: WatchAppList by lazy { WatchAppList(this) }

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

        initFromIntent(intent)

        val account = App.provide(this).prefs.account
        if (account== null) {
            accountSelectionDialog.show()
        } else {
            onAccountSelected(account)
        }
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
    }

    override fun onPause() {
        watchAppList.detach()
        super.onPause()
    }

    override fun onResume() {
        watchAppList.attach(this)
        super.onResume()
    }

    private fun searchResults(account: Account) {
        showLoading()

        if (searchQuery.isNotEmpty()) {
            val requestQueue = App.provide(this).requestQueue
            val deviceInfo = App.provide(this).deviceInfo
            if (isPackageSearch) {
                val detailsUrl = AppInfo.createDetailsUrl(searchQuery)
                endpoints.put(DETAILS_ENDPOINT_ID, DetailsEndpoint(this, requestQueue, deviceInfo, account, detailsUrl))
            }
            endpoints.put(SEARCH_ENDPOINT_ID, SearchEndpoint(this, requestQueue, deviceInfo, account, searchQuery, true))

            if (isPackageSearch) {
                endpoints.activeId = DETAILS_ENDPOINT_ID
                adapter = ResultsAdapterDetails(this, endpoints[DETAILS_ENDPOINT_ID] as DetailsEndpoint, watchAppList)
            } else {
                endpoints.activeId = SEARCH_ENDPOINT_ID
                adapter = ResultsAdapterSearch(this, endpoints[SEARCH_ENDPOINT_ID] as SearchEndpoint, watchAppList)
            }
            list.adapter = adapter
            endpoints.reset()
            endpoints.active.startAsync()
        } else {
            showNoResults("")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.searchbox, menu)

        val searchItem = menu.findItem(R.id.menu_search)
        searchView = searchItem.actionView as SearchView
        searchItem.expandActionView()

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchQuery = query
                searchResultsDelayed()
                Keyboard.hide(searchView, this@SearchActivity)
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
        val account = this.account
        if (endpoints.authToken.isEmpty() || account == null) {
            initiateSearch = true
        } else {
            searchResults(account)
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

    override fun onAccountSelected(account: Account) {
        this.account = account
        AuthTokenAsync(this).request(this, account, object : AuthTokenAsync.Callback {
            override fun onToken(token: String) {
                endpoints.authToken = token
                if (initiateSearch && searchQuery.isNotEmpty()) {
                    searchResults(account)
                } else {
                    showNoResults("")
                }
            }

            override fun onError(errorMessage: String) {
                if (App.provide(this@SearchActivity).networkConnection.isNetworkAvailable) {
                    Toast.makeText(this@SearchActivity, R.string.failed_gain_access, Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@SearchActivity, R.string.check_connection, Toast.LENGTH_SHORT).show()
                }
                finish()
            }
        })
    }

    override fun onAccountNotFound(errorMessage: String) {
        if (App.provide(this).networkConnection.isNetworkAvailable) {
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
        if (adapter?.isNotEmpty == true) {
            endpoints.active.startAsync()
        } else {
            searchResultsDelayed()
        }
    }

    override fun onWatchListChangeSuccess(info: AppInfo, newStatus: Int) {
        adapter?.notifyDataSetChanged()
        if (newStatus == AppInfoMetadata.STATUS_NORMAL) {
            TagSnackbar.make(this, info, isShareSource).show()
        }
    }

    override fun onWatchListChangeError(info: AppInfo, error: Int) {
        if (WatchAppList.ERROR_ALREADY_ADDED == error) {
            Toast.makeText(this, R.string.app_already_added, Toast.LENGTH_SHORT).show()
            adapter?.notifyDataSetChanged()
        } else if (error == WatchAppList.ERROR_INSERT) {
            Toast.makeText(this, R.string.error_insert_app, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDataChanged(id: Int, endpoint: PlayStoreEndpointBase) {
        if (id == DETAILS_ENDPOINT_ID) {
            if ((endpoint as DetailsEndpoint).document != null) {
                showListView()
                adapter?.notifyDataSetChanged()
            } else {
                adapter = ResultsAdapterSearch(this, endpoints[SEARCH_ENDPOINT_ID] as SearchEndpoint, watchAppList)
                list.adapter = adapter
                endpoints.activate(SEARCH_ENDPOINT_ID).startAsync()
            }
        } else {
            val searchEndpoint = endpoint as SearchEndpoint
            if (searchEndpoint.count == 0) {
                showNoResults(searchEndpoint.query)
            } else {
                showListView()
                adapter?.notifyDataSetChanged()
            }
        }
    }

    override fun onErrorResponse(id: Int, endpoint: PlayStoreEndpointBase, error: VolleyError) {
        if (!App.provide(this).networkConnection.isNetworkAvailable) {
            loading.visibility = View.GONE
            showRetryButton()
            Toast.makeText(this, R.string.check_connection, Toast.LENGTH_SHORT).show()
            return
        }
        if (id == DETAILS_ENDPOINT_ID) {
            adapter = ResultsAdapterSearch(this, endpoints[SEARCH_ENDPOINT_ID] as SearchEndpoint, watchAppList)
            list.adapter = adapter
            endpoints.activate(SEARCH_ENDPOINT_ID).startAsync()
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
