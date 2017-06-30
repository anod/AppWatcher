package com.anod.appwatcher.accounts

import android.accounts.Account
import android.content.Context

import com.anod.appwatcher.Preferences

/**
 * @author alex
 * *
 * @date 2015-03-03
 */
class AccountManager(context: Context, private val prefs: Preferences) {

    private val mAccountManager: android.accounts.AccountManager = android.accounts.AccountManager.get(context)
    var accounts: Array<Account> = mAccountManager.getAccountsByType(AuthTokenProvider.ACCOUNT_TYPE)
        private set
    var currentAccount: Account? = prefs.account
        private set

    fun hasAccounts(): Boolean {
        return accounts.isNotEmpty()
    }

    fun reload() {
        accounts = mAccountManager.getAccountsByType(AuthTokenProvider.ACCOUNT_TYPE)
        currentAccount = prefs.account
    }

    fun getAccount(idx: Int): Account {
        return accounts[idx]
    }

    fun saveCurrentAccount(acc: Account) {
        prefs.account = acc
        currentAccount = acc
    }
}
