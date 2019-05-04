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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.android.volley.VolleyError
import com.anod.appwatcher.Application
import com.anod.appwatcher.R
import com.anod.appwatcher.content.AddWatchAppAsyncTask
import com.anod.appwatcher.model.AppInfoMetadata
import com.anod.appwatcher.tags.TagSnackbar
import finsky.api.model.DfeModel
import info.anodsplace.framework.app.CustomThemeColors
import info.anodsplace.framework.app.FragmentToolbarActivity
import info.anodsplace.playstore.PlayStoreEndpoint
import info.anodsplace.playstore.WishlistEndpoint

import kotlinx.android.synthetic.main.fragment_wishlist.*

/**
 * @author Alex Gavrishev
 * *
 * @date 16/12/2016.
 */
class WishlistFragment : Fragment(), PlayStoreEndpoint.Listener {

    private var endpoint: WishlistEndpoint? = null

    private val viewModel: WishlistViewModel by lazy {
        ViewModelProviders.of(this).get(WishlistViewModel::class.java)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        endpoint?.listener = this
    }

    override fun onDetach() {
        super.onDetach()
        endpoint?.listener = null
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
        val authToken = arguments!!.getString(EXTRA_AUTH_TOKEN) ?: ""

        if (account == null || authToken.isEmpty() || context == null) {
            Toast.makeText(context, R.string.choose_an_account, Toast.LENGTH_SHORT).show()
            activity!!.finish()
        } else {
            startLoadingList(account, authToken, context!!)
        }

        viewModel.appStatusChange.observe(this, Observer {
            val newStatus = it.first
            if (newStatus == AppInfoMetadata.STATUS_NORMAL) {
                TagSnackbar.make(activity!!, it.second!!, false).show()
                list.adapter!!.notifyDataSetChanged()
            }
        })
    }

    private fun startLoadingList(account: Account, authSubToken: String, context: Context) {
        val endpoint = WishlistEndpoint(context, Application.provide(context).requestQueue, Application.provide(context).deviceInfo, account, true)
        endpoint.authToken = authSubToken
        endpoint.listener = this

        val adapter = ResultsAdapterWishList(context, endpoint, viewModel)
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
        private const val TAG = "wishlist"
        const val EXTRA_ACCOUNT = "extra_account"
        const val EXTRA_AUTH_TOKEN = "extra_auth_token"

        fun intent(context: Context, themeRes: Int, themeColors: CustomThemeColors, account: Account?, authToken: String?) = FragmentToolbarActivity.intent(
                TAG,
                { WishlistFragment() },
                themeRes,
                themeColors,
                Bundle().apply {
                    putParcelable(EXTRA_ACCOUNT, account)
                    putString(EXTRA_AUTH_TOKEN, authToken)
                },
                context)
    }
}
