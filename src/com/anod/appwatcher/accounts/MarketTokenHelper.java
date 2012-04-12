package com.anod.appwatcher.accounts;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.content.Context;
import android.os.Bundle;

public class MarketTokenHelper {
	
	private static final String AUTH_TOKEN_TYPE = "android";
	private static final String ACCOUNT_TYPE = "com.google";
	private Context mContext;
	private AccountManager mAccountManager;

	public MarketTokenHelper(Context context) {
		mContext = context;
        mAccountManager = AccountManager.get(mContext);
	}

	public String requestToken() {
		String token = getAuthToken();
		if (token == null) {
			return null;
		}
		mAccountManager.invalidateAuthToken(ACCOUNT_TYPE, token);
		token = getAuthToken();
		return token;
	}
	
	private String getAuthToken() {
    	String authToken = null;
		try {
	        Account[] accounts = mAccountManager.getAccountsByType(ACCOUNT_TYPE);
	        // Take first account, not important
	        @SuppressWarnings("deprecation")
			AccountManagerFuture<Bundle> future = mAccountManager.getAuthToken(
	        	accounts[0], AUTH_TOKEN_TYPE, true, null, null
	        );
	        authToken = future.getResult().getString(AccountManager.KEY_AUTHTOKEN);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return authToken;
	}

}
