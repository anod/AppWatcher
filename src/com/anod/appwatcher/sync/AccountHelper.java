package com.anod.appwatcher.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

public class AccountHelper {
	private static final String ACCOUNT_TYPE = "com.google";

	
    public static Account getAccount(Context context) {
    	AccountManager am = AccountManager.get(context);
    	Account[] accounts = am.getAccountsByType(ACCOUNT_TYPE);
    	// TODO Check if account exists
    	return accounts[0];
    }

}
