package com.anod.appwatcher.wishlist

import android.accounts.Account
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.android.volley.VolleyError
import com.anod.appwatcher.Application
import com.anod.appwatcher.R
import com.anod.appwatcher.content.AddWatchAppAsyncTask
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.model.AppInfoMetadata
import com.anod.appwatcher.content.WatchAppList
import com.anod.appwatcher.tags.TagSnackbar
import finsky.api.model.DfeModel
import info.anodsplace.playstore.PlayStoreEndpoint
import info.anodsplace.playstore.WishlistEndpoint

import kotlinx.android.synthetic.main.fragment_wishlist.*

/**
 * @author Alex Gavrishev
 * *
 * @date 16/12/2016.
 */
class WishlistFragment : androidx.fragment.app.Fragment(), WatchAppList.Listener, PlayStoreEndpoint.Listener {

    private var endpoint: WishlistEndpoint? = null
    private val watchAppList: WatchAppList by lazy { WatchAppList(this) }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        watchAppList.attach(context!!)
        endpoint?.listener = this
    }

    override fun onDetach() {
        super.onDetach()
        endpoint?.listener = null
        watchAppList.detach()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_wishlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        list.layoutManager = LinearLayoutManager(context)
        retryButton.setOnClickListener { endpoint?.startAsync() }

        list.visibility = View.GONE
        empty.visibility = View.GONE
        loading.visibility = View.VISIBLE
        retryView.visibility = View.GONE

        activity!!.setTitle(R.string.wishlist)

        val account = arguments!!.getParcelable<Account>(EXTRA_ACCOUNT)
        val authToken = arguments!!.getString(EXTRA_AUTH_TOKEN)

        if (account == null || authToken.isEmpty() || context == null) {
            Toast.makeText(context, R.string.choose_an_account, Toast.LENGTH_SHORT).show()
            activity!!.finish()
        } else {
            startLoadingList(account, authToken, context!!)
        }
    }

    override fun onWatchListChangeSuccess(info: AppInfo, newStatus: Int) {
        if (newStatus == AppInfoMetadata.STATUS_NORMAL) {
            TagSnackbar.make(activity!!, info, false).show()
        }
        context?.sendBroadcast(Intent(AddWatchAppAsyncTask.listChanged))
        list.adapter!!.notifyDataSetChanged()
    }

    override fun onWatchListChangeError(info: AppInfo, error: Int) {
        if (WatchAppList.ERROR_ALREADY_ADDED == error) {
            Toast.makeText(context, R.string.app_already_added, Toast.LENGTH_SHORT).show()
            list.adapter!!.notifyDataSetChanged()
        } else if (error == WatchAppList.ERROR_INSERT) {
            Toast.makeText(context, R.string.error_insert_app, Toast.LENGTH_SHORT).show()
        }
    }

    private fun startLoadingList(account: Account, authSubToken: String, context: Context) {
        val endpoint = WishlistEndpoint(context, Application.provide(context).requestQueue, Application.provide(context).deviceInfo, account, true)
        endpoint.authToken = authSubToken
        endpoint.listener = this

        val adapter = ResultsAdapterWishList(context, endpoint, watchAppList)
        list.adapter = adapter

        this.endpoint = endpoint
        endpoint.startAsync()
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
        empty.setText(R.string.no_result_found)
        empty.visibility = View.VISIBLE
    }

    override fun onDataChanged(data: DfeModel) {
        if (endpoint!!.count == 0) {
            showNoResults()
        } else {
            showListView()
            list.adapter!!.notifyDataSetChanged()
        }
    }

    override fun onErrorResponse(error: VolleyError) {
        loading.visibility = View.GONE
        showRetryButton()
    }

    companion object {
        const val TAG = "wishlist"
        const val EXTRA_ACCOUNT = "extra_account"
        const val EXTRA_AUTH_TOKEN = "extra_auth_token"
    }
}
