package com.anod.appwatcher.fragments

import android.Manifest
import android.accounts.Account
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.DialogFragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import com.anod.appwatcher.App

import com.anod.appwatcher.AppWatcherApplication
import com.anod.appwatcher.R
import com.anod.appwatcher.accounts.AccountManager

/**
 * @author alex
 * *
 * @date 8/24/13
 */
class AccountChooserFragment : DialogFragment(), DialogInterface.OnClickListener {

    interface AccountSelectionProvider : ActivityCompat.OnRequestPermissionsResultCallback {
        val accountSelectionListener: OnAccountSelectionListener
    }

    // Container Activity must implement this interface
    interface OnAccountSelectionListener {
        fun onDialogAccountSelected(account: Account)
        fun onDialogAccountNotFound()
    }

    private var mSelectedItem: Int = 0
    private var mListener: OnAccountSelectionListener? = null

    private lateinit var mAccountManager: AccountManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAccountManager = App.provide(activity).accountManager
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mListener = (context as AccountSelectionProvider).accountSelectionListener
        } catch (e: ClassCastException) {
            throw ClassCastException(context!!.toString() + " must implement OnAccountSelectionListener")
        }

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the Builder class for convenient dialog construction
        val builder = AlertDialog.Builder(activity, R.style.AlertDialog)
        builder.setTitle(R.string.choose_an_account)

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            builder.setMessage(R.string.failed_gain_access)
            builder.setPositiveButton(R.string.allow) { _, _ ->
                ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(Manifest.permission.GET_ACCOUNTS),
                        PERMISSION_REQUEST_GET_ACCOUNTS
                )
            }
        } else {
            builder.setPositiveButton(android.R.string.ok) { _, _ -> saveAccount() }

            mAccountManager.reload()
            if (mAccountManager.hasAccounts()) {
                mSelectedItem = selectedItem
                builder.setSingleChoiceItems(choiceItems, mSelectedItem, this)
            } else {
                builder.setMessage(R.string.no_registered_google_accounts)
            }
        }

        builder.setNegativeButton(android.R.string.cancel) { _, _ ->
            if (mAccountManager.currentAccount == null) {
                mListener?.onDialogAccountNotFound()
            }
        }

        val dialog = builder.create()
        dialog.setOnDismissListener {
            if (mAccountManager.currentAccount == null) {
                mListener?.onDialogAccountNotFound()
            }
        }


        // Create the AlertDialog object and return it
        return dialog
    }

    private val selectedItem: Int
        get() {

            val current = mAccountManager.currentAccount ?: return 0
            val accounts = mAccountManager.accounts
            return accounts.indices.firstOrNull { accounts[it] == current } ?: 0
        }

    private val choiceItems: Array<CharSequence>
        get() {
            val accounts = mAccountManager.accounts
            return accounts.map { it.name }.toTypedArray()
        }


    override fun onClick(dialogInterface: DialogInterface, i: Int) {
        mSelectedItem = i
    }

    private fun saveAccount() {
        if (mAccountManager.hasAccounts()) {
            val acc = mAccountManager.getAccount(mSelectedItem)
            mAccountManager.saveCurrentAccount(acc)
            mListener?.onDialogAccountSelected(acc)
        } else {
            mListener?.onDialogAccountNotFound()
        }
    }

    companion object {
        val PERMISSION_REQUEST_GET_ACCOUNTS = 123

        fun newInstance(): AccountChooserFragment {
            val frag = AccountChooserFragment()
            val args = Bundle()
            frag.arguments = args
            return frag
        }
    }
}
