package info.anodsplace.playstore

import android.accounts.Account

import com.android.volley.VolleyError

/**
 * @author algavris
 * *
 * @date 27/08/2016.
 */
interface PlayStoreEndpoint {
    var listener: Listener?
    val authSubToken: String

    fun setAccount(account: Account, authSubToken: String): PlayStoreEndpoint

    fun startAsync()
    fun startSync()
    fun reset()

    interface Listener {
        fun onDataChanged()
        fun onErrorResponse(error: VolleyError)
    }
}
