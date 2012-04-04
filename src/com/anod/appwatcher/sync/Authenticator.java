package com.anod.appwatcher.sync;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

public class Authenticator extends AbstractAccountAuthenticator {
	private static final String ACCOUNT_NAME = "AppWatcherAccount";	
    private static final String ACCOUNT_TOKEN = "com.anod.appwatcher.account.token";
	private static final String ACCOUNT_TYPE = "com.anod.appwatcher.account";
	private static final String TAG = "Authenticator";	
    // Authentication Service context
    private final Context mContext;
    private AccountManager mAccountManager;
    
    
    public static Account getAccount() {
    	return new Account(ACCOUNT_NAME, ACCOUNT_TYPE);
    }
    
	public Authenticator(Context context) {
		super(context);
        mContext = context;
        mAccountManager = AccountManager.get(context);        
	}

	@Override
	public Bundle addAccount(AccountAuthenticatorResponse response,
			String accountType, String authTokenType,
			String[] requiredFeatures, Bundle options)
			throws NetworkErrorException {

		// add the account to the AccountManager /
		Account account = new Account(ACCOUNT_NAME, ACCOUNT_TYPE);
		mAccountManager.addAccountExplicitly(account, null, null);
		final Bundle bundle = new Bundle();
        
		bundle.putString(AccountManager.KEY_ACCOUNT_NAME, ACCOUNT_NAME);
		bundle.putString(AccountManager.KEY_ACCOUNT_TYPE, ACCOUNT_TYPE);
		bundle.putString(AccountManager.KEY_AUTHTOKEN, ACCOUNT_TOKEN);

        //return result
        response.onResult(bundle);

        return null;//result returned via response instead        
	}

	@Override
	public Bundle confirmCredentials(AccountAuthenticatorResponse response,
			Account account, Bundle options) throws NetworkErrorException {
        final Bundle bundle = new Bundle();
        Log.v(TAG, "confirmCredentials() " + account.toString());        
        bundle.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, true);
		return bundle;
	}

	@Override
	public Bundle editProperties(AccountAuthenticatorResponse response,
			String accountType) {
        throw new UnsupportedOperationException();
	}

	@Override
	public Bundle getAuthToken(AccountAuthenticatorResponse response,
			Account account, String authTokenType, Bundle options)
			throws NetworkErrorException {
		Log.v(TAG, "getAuthToken()");
        final Bundle result = new Bundle();
        result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
        result.putString(AccountManager.KEY_ACCOUNT_TYPE, ACCOUNT_TYPE);
        result.putString(AccountManager.KEY_AUTHTOKEN, ACCOUNT_TOKEN);
        return result;
	}

	@Override
	public String getAuthTokenLabel(String authTokenType) {
        // null means we don't support multiple authToken types
        Log.v(TAG, "getAuthTokenLabel()");
		return null;
	}

	@Override
	public Bundle hasFeatures(AccountAuthenticatorResponse response,
			Account account, String[] features) throws NetworkErrorException {
        // This call is used to query whether the Authenticator supports
        // specific features. We don't expect to get called, so we always
        // return false (no) for any queries.
        Log.v(TAG, "hasFeatures()");
        final Bundle result = new Bundle();
        result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false);
        return result;
	}

	@Override
	public Bundle updateCredentials(AccountAuthenticatorResponse response,
			Account account, String authTokenType, Bundle options)
			throws NetworkErrorException {
        Log.v(TAG, "updateCredentials()");
        return null;
	}

}
