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
	private boolean mInvalidateToken;
	private AccountManager mAccountManager;

	public MarketTokenHelper(Context context) {
		mContext = context;
        mAccountManager = AccountManager.get(mContext);
	}

	public String requestToken() {
		String token = blockingGetAuthToken();
		if (token == null) {
			return null;
		}
		if(mInvalidateToken) {
			mAccountManager.invalidateAuthToken(ACCOUNT_TYPE, token);
			token = blockingGetAuthToken();
		}
		return token;
	}
	
	@SuppressWarnings("deprecation")
	private String blockingGetAuthToken() {
    	String authToken = null;
		try {
	        Account[] accounts = mAccountManager.getAccountsByType(ACCOUNT_TYPE);
	        // Take first account, not important
	        authToken = mAccountManager.blockingGetAuthToken(
	        	accounts[0],
	        	AUTH_TOKEN_TYPE,
	        	true
	        );
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return authToken;
	}
}
