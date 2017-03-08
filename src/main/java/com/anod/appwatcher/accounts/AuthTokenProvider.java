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

import java.io.IOException;

import info.anodsplace.android.log.AppLog;

public class AuthTokenProvider {

    private static final String AUTH_TOKEN_TYPE = "androidmarket";
    public static final String ACCOUNT_TYPE = "com.google";
    private final AccountManager mAccountManager;

    public AuthTokenProvider(Context context) {
        mAccountManager = AccountManager.get(context);
    }


    public String requestTokenBlocking(Activity activity, Account acc) throws AuthenticatorException, OperationCanceledException, IOException {
        String token = getAuthToken(activity, acc);

        if (token != null) {
            mAccountManager.invalidateAuthToken(ACCOUNT_TYPE, token);
        }

        token = getAuthToken(activity, acc);
        return token;
    }

    public void requestToken(Activity activity, Account acc, AuthenticateCallback callback) {

        (new GetTokenTask(activity, acc, this, callback)).execute(0);

    }

    private String getAuthToken(Activity activity, Account acc) throws AuthenticatorException, OperationCanceledException, IOException {
        if (acc == null) {
            return null;
        }
        AccountManagerFuture<Bundle> future;

        future = mAccountManager.getAuthToken(
                acc, AUTH_TOKEN_TYPE, null, activity, null, null
        );

        String authToken = future.getResult().getString(AccountManager.KEY_AUTHTOKEN);
        return authToken;
    }

    public interface AuthenticateCallback {
        void onAuthTokenAvailable(String token);

        void onUnRecoverableException(final String errorMessage);
    }

    private static class GetTokenTask extends AsyncTask<Integer, Void, String> {
        private final AuthTokenProvider mAuthTokenProvider;
        private Account mAccount;
        private Activity mActivity;
        private AuthenticateCallback mCallback;

        public GetTokenTask(Activity activity, Account acc, AuthTokenProvider helper, AuthenticateCallback callback) {
            mAccount = acc;
            mActivity = activity;
            mCallback = callback;
            mAuthTokenProvider = helper;
        }

        @Override
        protected String doInBackground(Integer... params) {
            String token = null;
            try {
                token = mAuthTokenProvider.requestTokenBlocking(mActivity, mAccount);
            } catch (IOException | OperationCanceledException | AuthenticatorException e) {
                AppLog.e(e.getMessage(), e);
            }
            return token;
        }


        @Override
        protected void onPostExecute(String token) {
            super.onPostExecute(token);
            if (token == null) {
                mCallback.onUnRecoverableException("Cannot retrieve authorization token");
            } else {
                mCallback.onAuthTokenAvailable(token);
            }
        }
    }

}
