package com.anod.appwatcher.accounts

import android.accounts.Account
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.widget.Toast

import com.anod.appwatcher.Preferences
import com.anod.appwatcher.fragments.AccountChooserFragment

/**
 * @author alex
 * *
 * @date 9/17/13
 */
class AccountChooser(
        private val mActivity: AppCompatActivity,
        preferences: Preferences,
        private val mListener: AccountChooser.OnAccountSelectionListener?) : AccountChooserFragment.OnAccountSelectionListener {
    private val mAuthTokenProvider: AuthTokenProvider
    private val mContext: Context
    var account: Account? = preferences.account
        private set

    private fun showAccountsDialog() {
        val accountsDialog = AccountChooserFragment.newInstance()
        accountsDialog.show(mActivity.supportFragmentManager, "accountsDialog")
    }

    fun showAccountsDialogWithCheck() {
        showAccountsDialog()
    }

    fun onRequestPermissionResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == AccountChooserFragment.PERMISSION_REQUEST_GET_ACCOUNTS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // http://stackoverflow.com/questions/33264031/calling-dialogfragments-show-from-within-onrequestpermissionsresult-causes
                Handler().postDelayed({ showAccountsDialog() }, 200)
            } else {
                Toast.makeText(mActivity, "Failed to gain access to Google accounts", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Container Activity must implement this interface
    interface OnAccountSelectionListener : AccountChooserFragment.AccountSelectionProvider {
        fun onAccountSelected(account: Account, authSubToken: String?)
        fun onAccountNotFound()
    }

    init {
        if (mActivity !is AccountChooserFragment.AccountSelectionProvider) {
            throw ClassCastException(mActivity.toString() + " must implement OnAccountSelectionListener")
        }
        mContext = mActivity
        mAuthTokenProvider = AuthTokenProvider(mContext)
    }

    fun init() {
        val account = this.account

        if (account == null) {
            showAccountsDialogWithCheck()
        } else {
            mAuthTokenProvider.requestToken(mActivity, account, object : AuthTokenProvider.AuthenticateCallback {
                override fun onAuthTokenAvailable(token: String) {
                    mListener?.onAccountSelected(account, token)
                }

                override fun onUnRecoverableException(errorMessage: String) {
                    mListener?.onAccountSelected(account, null)
                }
            })
        }
    }

    override fun onDialogAccountSelected(account: Account) {
        this.account = account
        mAuthTokenProvider.requestToken(mActivity, account, object : AuthTokenProvider.AuthenticateCallback {
            override fun onAuthTokenAvailable(token: String) {
                mListener?.onAccountSelected(account, token)
            }

            override fun onUnRecoverableException(errorMessage: String) {
                mListener?.onAccountSelected(account, null)
            }
        })

    }

    override fun onDialogAccountNotFound() {
        mListener?.onAccountNotFound()
    }
}
