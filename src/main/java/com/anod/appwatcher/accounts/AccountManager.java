package com.anod.appwatcher.accounts;

import android.accounts.Account;
import android.content.Context;

import com.anod.appwatcher.Preferences;

/**
 * Created by alex on 8/2/14.
 */
public class AccountManager {

    private final android.accounts.AccountManager mAccountManager;
    private final Preferences mPreferences;
    private final Account[] mAccounts;
    private Account mCurrentAccount;

    public AccountManager(Context context) {
        mAccountManager = android.accounts.AccountManager.get(context);
        mPreferences = new Preferences(context);
        mAccounts = mAccountManager.getAccountsByType(AccountHelper.ACCOUNT_TYPE);
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

    public boolean hasJustOneAccount() {
        return mAccounts.length == 1;
    }

    public Account getAccount(int idx) {
        return mAccounts[idx];
    }

    public void saveCurrentAccount(Account acc) {
        mPreferences.updateAccount(acc);
        mCurrentAccount=acc;
    }
}
