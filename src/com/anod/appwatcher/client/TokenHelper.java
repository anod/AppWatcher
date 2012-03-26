package com.anod.appwatcher.client;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

public class TokenHelper {
	
	private static final String ACCOUNT_TYPE = "com.google";
	private Activity mActivity;

	public TokenHelper(Activity activity) {
		mActivity = activity;
	}

	public String requestToken() {
		return updateToken(false);
	}
	
	private String updateToken(boolean invalidateToken) {
	    String authToken = null;
	    try {
	        AccountManager am = AccountManager.get((Context)mActivity);
	        Account[] accounts = am.getAccountsByType(ACCOUNT_TYPE);
	        AccountManagerFuture<Bundle> accountManagerFuture;
            accountManagerFuture = am.getAuthToken(accounts[0], "android", null, mActivity, null, null);
	        Bundle authTokenBundle = accountManagerFuture.getResult();
	        authToken = authTokenBundle.getString(AccountManager.KEY_AUTHTOKEN).toString();
	        if(invalidateToken) {
	            am.invalidateAuthToken(ACCOUNT_TYPE, authToken);
	            authToken = updateToken(false);
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return authToken;
	}
}
