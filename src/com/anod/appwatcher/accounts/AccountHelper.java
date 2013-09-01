package com.anod.appwatcher.accounts;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Toast;

import com.anod.appwatcher.Preferences;

public class AccountHelper {
	
	private static final String AUTH_TOKEN_TYPE = "android";
	public static final String ACCOUNT_TYPE = "com.google";
	private Context mContext;
	private AccountManager mAccountManager;

	public AccountHelper(Context context) {
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
		Account acc = (new Preferences(mContext)).getAccount();
		if (acc == null) {
			return null;
		}
		try {
	        // Take first account, not important
			AccountManagerFuture<Bundle> future;

			if (activity == null) {
				future = mAccountManager.getAuthToken(
					acc, AUTH_TOKEN_TYPE, true, null, null
				);
			} else {
				future = mAccountManager.getAuthToken(
					acc, AUTH_TOKEN_TYPE, null, activity , null, null
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
