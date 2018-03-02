package com.anod.appwatcher.accounts

import android.Manifest
import android.accounts.Account
import android.accounts.AccountManager
import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.widget.Toast

import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.R
import com.anod.appwatcher.utils.DialogMessage
import info.anodsplace.framework.app.ActivityListener

/**
 * @author alex
 * *
 * @date 9/17/13
 */
class AccountSelectionDialog(
        private val activity: AppCompatActivity,
        private val preferences: Preferences,
        private val listener: AccountSelectionDialog.SelectionListener): ActivityListener.ResultListener {

    companion object {
        const val PERMISSION_REQUEST_GET_ACCOUNTS = 123
        const val ACCOUNT_REQUEST = 450
    }

    var account: Account?
        get() = preferences.account
        set(value) { preferences.account = value }

    // Container Activity must implement this interface
    interface SelectionListener {
        fun onAccountSelected(account: Account)
        fun onAccountNotFound(errorMessage: String)
    }

    @TargetApi(Build.VERSION_CODES.M)
    fun show() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = AccountManager.newChooseAccountIntent(
                    account,
                    null,
                    arrayOf(AuthTokenBlocking.ACCOUNT_TYPE),
                    null,
                    null,
                    null,
                    null)
            activity.startActivityForResult(intent, ACCOUNT_REQUEST)
        } else {
            // Use the Builder class for convenient dialog construction
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
                showPermissionsDialog()
                return
            }
            activity.startActivityForResult(AccountSelectionDialogActivity.intent(account, activity),  ACCOUNT_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ACCOUNT_REQUEST)
        {
            if (resultCode == Activity.RESULT_OK && data != null) {
                val name = data.extras.getString(AccountManager.KEY_ACCOUNT_NAME, "")
                val type = data.extras.getString(AccountManager.KEY_ACCOUNT_TYPE, "")
                if (name.isNotBlank() && type.isNotBlank()) {
                    val account = Account(name, type)
                    this.preferences.account = account
                    listener.onAccountSelected(account)
                    return
                }
            }
            if (this.preferences.account == null) {
                val errorMessage = data?.extras?.getString(AccountManager.KEY_ERROR_MESSAGE, "") ?: ""
                listener.onAccountNotFound(errorMessage)
            }
            return
        }
    }

    // For pre SDK 23

    fun onRequestPermissionResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST_GET_ACCOUNTS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // http://stackoverflow.com/questions/33264031/calling-dialogfragments-show-from-within-onrequestpermissionsresult-causes
                Handler().postDelayed({ show() }, 200)
            } else {
                Toast.makeText(activity, "Failed to gain access to Google accounts", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showPermissionsDialog() {
        val dialog = DialogMessage(activity, R.string.choose_an_account, R.string.failed_gain_access, { builder ->
            builder.setPositiveButton(R.string.allow) { _, _ ->
                ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(Manifest.permission.GET_ACCOUNTS),
                        PERMISSION_REQUEST_GET_ACCOUNTS
                )
            }
            builder.setNegativeButton(android.R.string.cancel) { _, _ ->
                if (account == null) {
                    listener.onAccountNotFound("")
                }
            }
        }).create()
        dialog.setOnDismissListener {
            if (account == null) {
                listener.onAccountNotFound("")
            }
        }
        dialog.show()
    }

}
