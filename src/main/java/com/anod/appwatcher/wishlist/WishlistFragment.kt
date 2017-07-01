package com.anod.appwatcher.wishlist

import android.accounts.Account
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import com.android.volley.VolleyError
import com.anod.appwatcher.R
import com.anod.appwatcher.market.PlayStoreEndpoint
import com.anod.appwatcher.market.WishlistEndpoint
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.model.AppInfoMetadata
import com.anod.appwatcher.model.WatchAppList
import com.anod.appwatcher.tags.TagSnackbar

/**
 * @author algavris
 * *
 * @date 16/12/2016.
 */

class WishlistFragment : Fragment(), WatchAppList.Listener, PlayStoreEndpoint.Listener {

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

    private var mEndpoint: WishlistEndpoint? = null
    private var mWatchAppList: WatchAppList? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (mEndpoint == null) {
            mEndpoint = WishlistEndpoint(context!!, true)
        }

        if (mWatchAppList == null) {
            mWatchAppList = WatchAppList(this)
        }

        mWatchAppList!!.attach(context!!)
        mEndpoint!!.listener = this
    }

    override fun onDetach() {
        super.onDetach()
        mEndpoint!!.listener = null
        mWatchAppList!!.detach()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_wishlist, container, false)
        ButterKnife.bind(this, view)

        val context = context

        mListView.layoutManager = LinearLayoutManager(context)
        mRetryButton.setOnClickListener { mEndpoint!!.startAsync() }

        mListView.visibility = View.GONE
        mEmptyView.visibility = View.GONE
        mLoading.visibility = View.VISIBLE
        mRetryView.visibility = View.GONE

        activity.setTitle(R.string.wishlist)

        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val account = arguments.getParcelable<Account>(EXTRA_ACCOUNT)
        val authToken = arguments.getString(EXTRA_AUTH_TOKEN)

        if (account == null || TextUtils.isEmpty(authToken)) {
            Toast.makeText(context, R.string.choose_an_account, Toast.LENGTH_SHORT).show()
            activity.finish()
        } else {
            startLoadingList(account, authToken!!)
        }
    }

    override fun onWatchListChangeSuccess(info: AppInfo, newStatus: Int) {
        if (newStatus == AppInfoMetadata.STATUS_NORMAL) {
            TagSnackbar.make(activity, info, false).show()
        }
        mListView.adapter.notifyDataSetChanged()
    }

    override fun onWatchListChangeError(info: AppInfo, error: Int) {
        if (WatchAppList.ERROR_ALREADY_ADDED == error) {
            Toast.makeText(context, R.string.app_already_added, Toast.LENGTH_SHORT).show()
            mListView.adapter.notifyDataSetChanged()
        } else if (error == WatchAppList.ERROR_INSERT) {
            Toast.makeText(context, R.string.error_insert_app, Toast.LENGTH_SHORT).show()
        }
    }


    private fun startLoadingList(account: Account, authSubToken: String) {
        mEndpoint!!.setAccount(account, authSubToken)

        val context = context

        val adapter = ResultsAdapterWishlist(context, mEndpoint!!, mWatchAppList!!)
        mListView.adapter = adapter

        mEndpoint!!.startAsync()
    }


    private fun showRetryButton() {
        mListView.visibility = View.GONE
        mEmptyView.visibility = View.GONE
        mLoading.visibility = View.GONE
        mRetryView.visibility = View.VISIBLE
    }

    private fun showListView() {
        mListView.visibility = View.VISIBLE
        mEmptyView.visibility = View.GONE
        mLoading.visibility = View.GONE
        mRetryView.visibility = View.GONE
    }

    private fun showNoResults() {
        mLoading.visibility = View.GONE
        mListView.visibility = View.GONE
        mRetryView.visibility = View.GONE
        mEmptyView.setText(R.string.no_result_found)
        mEmptyView.visibility = View.VISIBLE
    }

    override fun onDataChanged() {
        if (mEndpoint!!.count == 0) {
            showNoResults()
        } else {
            showListView()
            mListView.adapter.notifyDataSetChanged()
        }
    }

    override fun onErrorResponse(error: VolleyError) {
        mLoading.visibility = View.GONE
        showRetryButton()
    }

    companion object {
        const val TAG = "wishlist"
        const val EXTRA_ACCOUNT = "extra_account"
        const val EXTRA_AUTH_TOKEN = "extra_auth_token"
    }
}
