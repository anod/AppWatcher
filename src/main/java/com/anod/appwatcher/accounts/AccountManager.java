package com.anod.appwatcher.accounts;

import android.accounts.Account;
import android.content.Context;

import com.anod.appwatcher.Preferences;

/**
 * @author alex
 * @date 2015-03-03
 */
public class AccountManager {

    private final android.accounts.AccountManager mAccountManager;
    private final Preferences mPreferences;
    private Account[] mAccounts;
    private Account mCurrentAccount;

    public AccountManager(Context context) {
        mAccountManager = android.accounts.AccountManager.get(context);
        mPreferences = new Preferences(context);
        mAccounts = mAccountManager.getAccountsByType(AuthTokenProvider.ACCOUNT_TYPE);
        mCurrentAccount = mPreferences.getAccount();
    }

    public Account[] getAccounts() {
        return mAccounts;
    }

    public Account getCurrentAccount() {
        return mCurrentAccount;
    }

    public boolean hasAccounts() {
        return mAccounts.length > 0;
    }

    public void reload()
    {
        mAccounts = mAccountManager.getAccountsByType(AuthTokenProvider.ACCOUNT_TYPE);
        mCurrentAccount = mPreferences.getAccount();
    }

    public Account getAccount(int idx) {
        return mAccounts[idx];
    }

    public void saveCurrentAccount(Account acc) {
        mPreferences.updateAccount(acc);
        mCurrentAccount=acc;
    }
}
