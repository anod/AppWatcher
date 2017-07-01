package com.anod.appwatcher

import android.accounts.Account
import android.content.Intent
import android.os.Bundle
import android.support.v4.view.MenuItemCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import com.android.volley.VolleyError
import com.anod.appwatcher.accounts.AccountChooser
import com.anod.appwatcher.fragments.AccountChooserFragment
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

    private lateinit var mAdapter: ResultsAdapter

    @BindView(R.id.loading)
    lateinit var mLoading: LinearLayout
    @BindView(android.R.id.list)
    lateinit var mListView: RecyclerView
    @BindView(android.R.id.empty)
    lateinit var mEmptyView: TextView
    @BindView(R.id.retry_box)
    lateinit var mRetryView: LinearLayout
    @BindView(R.id.retry)
    lateinit var mRetryButton: Button
    lateinit var mSearchView: SearchView

    private var mInitiateSearch = false
    private var mShareSource = false

    private var mAccountChooser: AccountChooser? = null
    private var mSearchQuery: String = ""
    private var mFocus: Boolean = false
    private var mPackageSearch: Boolean = false
    private lateinit var mEndpoints: CompositeStateEndpoint
    private lateinit var mWatchAppList: WatchAppList

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_market_search)
        setupToolbar()

        ButterKnife.bind(this)

        mEndpoints = CompositeStateEndpoint(this)
        mEndpoints.add(SEARCH_ENDPOINT_ID, SearchEndpoint(this, true))
        mEndpoints.add(DETAILS_ENDPOINT_ID, DetailsEndpoint(this))

        mWatchAppList = WatchAppList(this)

        mRetryButton.setOnClickListener { retrySearchResult() }
        mLoading.visibility = View.GONE
        mListView.layoutManager = LinearLayoutManager(this)
        mListView.visibility = View.GONE
        mEmptyView.visibility = View.GONE
        mLoading.visibility = View.VISIBLE
        mRetryView.visibility = View.GONE

        initFromIntent(intent)
    }

    private fun initFromIntent(i: Intent?) {
        if (i == null) {
            return
        }
        val keyword = i.getStringExtra(EXTRA_KEYWORD)
        if (keyword != null) {
            mSearchQuery = keyword
        }
        mPackageSearch = i.getBooleanExtra(EXTRA_PACKAGE, false)
        mInitiateSearch = i.getBooleanExtra(EXTRA_EXACT, false)
        mShareSource = i.getBooleanExtra(EXTRA_SHARE, false)
        mFocus = i.getBooleanExtra(EXTRA_FOCUS, false)


        if (mPackageSearch) {
            mEndpoints.setActive(DETAILS_ENDPOINT_ID)
            mAdapter = ResultsAdapterDetails(this, mEndpoints.get(DETAILS_ENDPOINT_ID) as DetailsEndpoint, mWatchAppList)
        } else {
            mEndpoints.setActive(SEARCH_ENDPOINT_ID)
            mAdapter = ResultsAdapterSearch(this, mEndpoints.get(SEARCH_ENDPOINT_ID) as SearchEndpoint, mWatchAppList)
        }
    }

    override fun onPause() {
        mWatchAppList.detach()
        super.onPause()
        mEndpoints.listener = null
    }

    override fun onResume() {
        mWatchAppList.attach(this)
        super.onResume()
        mAccountChooser = AccountChooser(this, App.provide(this).prefs, this)
        mAccountChooser!!.init()
    }

    private fun searchResults() {
        mListView.adapter = mAdapter
        mEndpoints.reset()
        showLoading()

        MetricsManagerEvent.track(this, "perform_search", "SEARCH_QUERY", mSearchQuery, "SEARCH_PACKAGE", mPackageSearch.toString(), "FROM_SHARE", mShareSource.toString())

        if (mSearchQuery.isNotEmpty()) {
            if (mPackageSearch) {
                val url = AppInfo.createDetailsUrl(mSearchQuery)
                (mEndpoints.get(DETAILS_ENDPOINT_ID) as DetailsEndpoint).url = url
            }
            (mEndpoints.get(SEARCH_ENDPOINT_ID) as SearchEndpoint).query = mSearchQuery
            mEndpoints.active().startAsync()
        } else {
            showNoResults("")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.searchbox, menu)

        val searchItem = menu.findItem(R.id.menu_search)
        mSearchView = MenuItemCompat.getActionView(searchItem) as SearchView
        mSearchView.setIconifiedByDefault(false)
        MenuItemCompat.expandActionView(searchItem)

        mSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                mSearchQuery = query
                searchResultsDelayed()
                Keyboard.hide(mSearchView, this@MarketSearchActivity)
                return false
            }

            override fun onQueryTextChange(query: String): Boolean {
                return false
            }
        })

        mSearchView.setQuery(mSearchQuery, true)
        if (mFocus) {
            mSearchView.post { mSearchView.requestFocus() }
        } else {
            Keyboard.hide(mSearchView, this)
        }
        return true
    }

    private fun searchResultsDelayed() {
        if (mEndpoints.authSubToken.isEmpty()) {
            mInitiateSearch = true
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
        mEndpoints.setAccount(account, authSubToken)
        if (mInitiateSearch && !TextUtils.isEmpty(mSearchQuery)) {
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
        mAccountChooser!!.onRequestPermissionResult(requestCode, permissions, grantResults)
    }

    override val accountSelectionListener: AccountChooserFragment.OnAccountSelectionListener
        get() = mAccountChooser!!

    private fun showRetryButton() {
        mListView.visibility = View.GONE
        mEmptyView.visibility = View.GONE
        mLoading.visibility = View.GONE
        mRetryView.visibility = View.VISIBLE
    }

    private fun showLoading() {
        mListView.visibility = View.GONE
        mEmptyView.visibility = View.GONE
        mLoading.visibility = View.VISIBLE
        mRetryView.visibility = View.GONE
    }

    private fun showNoResults(query: String) {
        mLoading.visibility = View.GONE
        mListView.visibility = View.GONE
        mRetryView.visibility = View.GONE
        val noResStr = if (query.isNotEmpty()) getString(R.string.no_result_found, query) else getString(R.string.search_for_app)
        mEmptyView.text = noResStr
        mEmptyView.visibility = View.VISIBLE
    }

    private fun showListView() {
        mListView.visibility = View.VISIBLE
        mEmptyView.visibility = View.GONE
        mLoading.visibility = View.GONE
        mRetryView.visibility = View.GONE
    }

    private fun retrySearchResult() {
        if (mAdapter.isEmpty) {
            searchResultsDelayed()
        } else {
            mEndpoints.active().startAsync()
        }
    }

    override fun onWatchListChangeSuccess(info: AppInfo, newStatus: Int) {
        mAdapter.notifyDataSetChanged()
        if (newStatus == AppInfoMetadata.STATUS_NORMAL) {
            TagSnackbar.make(this, info, mShareSource).show()
        }
    }

    override fun onWatchListChangeError(info: AppInfo, error: Int) {
        if (WatchAppList.ERROR_ALREADY_ADDED == error) {
            Toast.makeText(this, R.string.app_already_added, Toast.LENGTH_SHORT).show()
            mAdapter.notifyDataSetChanged()
        } else if (error == WatchAppList.ERROR_INSERT) {
            Toast.makeText(this, R.string.error_insert_app, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDataChanged(id: Int, endpoint: PlayStoreEndpointBase) {
        if (id == DETAILS_ENDPOINT_ID) {
            if ((endpoint as DetailsEndpoint).document != null) {
                showListView()
                mAdapter.notifyDataSetChanged()
            } else {
                mAdapter = ResultsAdapterSearch(this, mEndpoints[SEARCH_ENDPOINT_ID] as SearchEndpoint, mWatchAppList)
                mListView.adapter = mAdapter
                mEndpoints.setActive(SEARCH_ENDPOINT_ID).startAsync()
            }
        } else {
            val searchEndpoint = endpoint as SearchEndpoint
            if (searchEndpoint.count == 0) {
                showNoResults(searchEndpoint.query)
            } else {
                showListView()
                mAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onErrorResponse(id: Int, endpoint: PlayStoreEndpointBase, error: VolleyError) {
        if (!App.with(this).isNetworkAvailable) {
            mLoading.visibility = View.GONE
            showRetryButton()
            Toast.makeText(this, R.string.check_connection, Toast.LENGTH_SHORT).show()
            return
        }
        if (id == DETAILS_ENDPOINT_ID) {
            mAdapter = ResultsAdapterSearch(this, mEndpoints.get(SEARCH_ENDPOINT_ID) as SearchEndpoint, mWatchAppList)
            mListView.adapter = mAdapter
            mEndpoints.setActive(SEARCH_ENDPOINT_ID).startAsync()
        } else {
            mLoading.visibility = View.GONE
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
