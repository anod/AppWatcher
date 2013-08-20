package com.anod.appwatcher.accounts;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Toast;

public class MarketTokenHelper {
	
	private static final String AUTH_TOKEN_TYPE = "android";
	private static final String ACCOUNT_TYPE = "com.google";
	private Context mContext;
	private AccountManager mAccountManager;

	public MarketTokenHelper(Context context) {
		mContext = context;
        mAccountManager = AccountManager.get(mContext);
	}

	public String requestToken(Activity activity) {
		String token = getAuthToken(activity);
		if (token == null) {
			return null;
		}
		mAccountManager.invalidateAuthToken(ACCOUNT_TYPE, token);
		token = getAuthToken(activity);
		return token;
	}
	
	private String getAuthToken(Activity activity) {
    	String authToken = null;
		try {
	        Account[] accounts = mAccountManager.getAccountsByType(ACCOUNT_TYPE);
	        // Take first account, not important
			AccountManagerFuture<Bundle> future;

			if (activity == null) {
				future = mAccountManager.getAuthToken(
					accounts[0], AUTH_TOKEN_TYPE, true, null, null
				);
			} else {
				future = mAccountManager.getAuthToken(
						accounts[0], AUTH_TOKEN_TYPE, null, activity , null, null
				);
			}
	        authToken = future.getResult().getString(AccountManager.KEY_AUTHTOKEN);
	    } catch (Exception e) {
	        e.printStackTrace();
			//Toast.makeText(mContext.getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
		}
	    return authToken;
	}


}
