package com.anod.appwatcher.accounts;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Toast;

import com.anod.appwatcher.Preferences;
import com.anod.appwatcher.utils.AppLog;

import java.io.IOException;

public class AccountHelper {
	
	private static final String AUTH_TOKEN_TYPE = "android";
	public static final String ACCOUNT_TYPE = "com.google";
	private final AccountManager mAccountManager;

	public AccountHelper(Context context) {
		mAccountManager = AccountManager.get(context);
	}

	public void requestToken(Activity activity, Account acc, AuthenticateCallback callback) {

		(new GetTokenTask(activity, acc, mAccountManager, callback)).execute(null);

	}

	public static interface AuthenticateCallback {
		public void onAuthTokenAvailable(String token);
		public void onUnRecoverableException(final String errorMessage);
	}

	private static class GetTokenTask extends AsyncTask<Void, Void, String> {
		private Account mAccount;
		private Activity mActivity;
		private AuthenticateCallback mCallback;
		private final AccountManager mAccountManager;

		public GetTokenTask(Activity activity, Account acc, AccountManager accountManager, AuthenticateCallback callback) {
			mAccount = acc;
			mActivity = activity;
			mCallback = callback;
			mAccountManager = accountManager;
		}

		@Override
		protected String doInBackground(Void... params) {
			try {
				String token = getAuthToken(mActivity, mAccount);

				if (token != null) {
					mAccountManager.invalidateAuthToken(ACCOUNT_TYPE, token);
				}

				token = getAuthToken(mActivity, mAccount);
				return token;
			} catch (IOException e) {
				AppLog.d("transient error encountered: " + e.getMessage());
				mCallback.onUnRecoverableException(e.getMessage());
			} catch (AuthenticatorException e) {
				AppLog.d("transient error encountered: " + e.getMessage());
				mCallback.onUnRecoverableException(e.getMessage());
			} catch (OperationCanceledException e) {
				AppLog.d("transient error encountered: " + e.getMessage());
				mCallback.onUnRecoverableException(e.getMessage());
			}
			return null;
		}

		private String getAuthToken(Activity activity, Account acc) throws AuthenticatorException, OperationCanceledException, IOException {
			String authToken = null;
			if (acc == null) {
				return null;
			}
			AccountManagerFuture<Bundle> future;


			future = mAccountManager.getAuthToken(
				acc, AUTH_TOKEN_TYPE, null, activity , null, null
			);

			authToken = future.getResult().getString(AccountManager.KEY_AUTHTOKEN);
			return authToken;
		}

		@Override
		protected void onPostExecute(String token) {
			super.onPostExecute(token);
			mCallback.onAuthTokenAvailable(token);
		}
	}

}
