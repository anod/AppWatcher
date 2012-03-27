package com.anod.appwatcher.client;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.Context;
import android.os.Bundle;

public class TokenHelper {
	
	private static final String AUTH_TOKEN_TYPE = "android";
	private static final String ACCOUNT_TYPE = "com.google";
	private Context mContext;
	private CallBack mCallback;
    private AccountManagerCallback<Bundle> myCallback = new AccountManagerCallback<Bundle>() {
        @Override
        public void run(final AccountManagerFuture<Bundle> future) {
        	String authToken = null;
            try {
            	authToken = future.getResult().get(AccountManager.KEY_AUTHTOKEN).toString();
           } catch (Exception e) {
               // handle error
           }
           onTokenReceive(authToken);
        };
        
    };
	private boolean mInvalidateToken;
	private AccountManager mAccountManager;

    public interface CallBack {
		public void onTokenReceive(String authToken);
    }
    
	public TokenHelper(Context context, TokenHelper.CallBack callback) {
		mContext = context;
		mCallback = callback;
        mAccountManager = AccountManager.get(mContext);
	}

	protected void onTokenReceive(String authToken) {
        if(mInvalidateToken) {
        	mAccountManager.invalidateAuthToken(ACCOUNT_TYPE, authToken);
            updateToken(false);
        }
        if (mCallback!=null) {
     	   mCallback.onTokenReceive(authToken);
        }
		
	}

	public void requestToken() {
		updateToken(false);
	}
	
	@SuppressWarnings("deprecation")
	private void updateToken(boolean invalidateToken) {
	    try {
	        Account[] accounts = mAccountManager.getAccountsByType(ACCOUNT_TYPE);
	        // Take first account, not impoertant
	        mAccountManager.getAuthToken(
	        	accounts[0],
	        	AUTH_TOKEN_TYPE,
	        	true, myCallback, null
	        );
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
}
