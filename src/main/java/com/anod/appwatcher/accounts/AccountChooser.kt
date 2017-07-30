package com.anod.appwatcher.accounts

import android.Manifest
import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.widget.Toast

import com.anod.appwatcher.Preferences
import com.anod.appwatcher.R

/**
 * @author alex
 * *
 * @date 9/17/13
 */
class AccountChooser(
        private val activity: AppCompatActivity,
        private val preferences: Preferences,
        private val listener: AccountChooser.OnAccountSelectionListener?) {

    private val authTokenProvider: AuthTokenProvider = AuthTokenProvider(activity)

    companion object {
        const val PERMISSION_REQUEST_GET_ACCOUNTS = 123
        const val ACCOUNT_REQUEST = 450
    }

    var account: Account?
        get() = preferences.account
        set(value) { preferences.account = value }

    // Container Activity must implement this interface
    interface OnAccountSelectionListener {
        fun onAccountSelected(account: Account, authSubToken: String?)
        fun onAccountNotFound()
    }

    fun showAccountsDialogWithCheck() {
        showAccountsDialog()
    }

    fun onRequestPermissionResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST_GET_ACCOUNTS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // http://stackoverflow.com/questions/33264031/calling-dialogfragments-show-from-within-onrequestpermissionsresult-causes
                Handler().postDelayed({ showAccountsDialog() }, 200)
            } else {
                Toast.makeText(activity, "Failed to gain access to Google accounts", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun init() {
        val account = this.preferences.account

        if (account == null) {
            showAccountsDialogWithCheck()
        } else {
            authTokenProvider.requestToken(activity, account, object : AuthTokenProvider.AuthenticateCallback {
                override fun onAuthTokenAvailable(token: String) {
                    listener?.onAccountSelected(account, token)
                }

                override fun onUnRecoverableException(errorMessage: String) {
                    listener?.onAccountSelected(account, null)
                }
            })
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ACCOUNT_REQUEST)
        {
            if (resultCode == Activity.RESULT_OK && data != null) {
                val name = data.extras.getString(AccountManager.KEY_ACCOUNT_NAME, "")
                val type = data.extras.getString(AccountManager.KEY_ACCOUNT_TYPE, "")
                if (name.isNotBlank() && type.isNotBlank()) {
                    val account = Account(name, type)
                    this.preferences.account = account
                    authTokenProvider.requestToken(activity, account, object : AuthTokenProvider.AuthenticateCallback {
                        override fun onAuthTokenAvailable(token: String) {
                            listener?.onAccountSelected(account, token)
                        }

                        override fun onUnRecoverableException(errorMessage: String) {
                            listener?.onAccountSelected(account, null)
                        }
                    })
                    return
                }
            }
            listener?.onAccountNotFound()
            return
        }
    }

    private fun showAccountsDialog() {

        // Use the Builder class for convenient dialog construction
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            showPermissionsDialog()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = AccountManager.newChooseAccountIntent(
                    null,
                    null,
                    arrayOf(AuthTokenProvider.ACCOUNT_TYPE),
                    null,
                    null,
                    null,
                    null)
            activity.startActivityForResult(intent,  ACCOUNT_REQUEST)
        } else {
            activity.startActivityForResult(AccountChooserActivity.intent(account, activity),  ACCOUNT_REQUEST)
        }
    }

    private fun showPermissionsDialog() {
        val builder = AlertDialog.Builder(activity, R.style.AlertDialog)
        builder.setTitle(R.string.choose_an_account)
        builder.setMessage(R.string.failed_gain_access)
        builder.setPositiveButton(R.string.allow) { _, _ ->
            ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.GET_ACCOUNTS),
                    PERMISSION_REQUEST_GET_ACCOUNTS
            )
        }
        builder.setNegativeButton(android.R.string.cancel) { _, _ ->
            if (account == null) {
                listener?.onAccountNotFound()
            }
        }
        val dialog = builder.create()
        dialog.setOnDismissListener {
            if (account == null) {
                listener?.onAccountNotFound()
            }
        }
    }



}
