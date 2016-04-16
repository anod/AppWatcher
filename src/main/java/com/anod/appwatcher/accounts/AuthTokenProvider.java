package com.anod.appwatcher.accounts;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import com.anod.appwatcher.AppListContentProvider;

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
                AppLog.e("transient error encountered: " + e.getMessage(), e);
                mCallback.onUnRecoverableException(e.getMessage());
            }
            return token;
        }


        @Override
        protected void onPostExecute(String token) {
            super.onPostExecute(token);
            if (token == null) {
                mCallback.onUnRecoverableException("Token is null");
            } else {
                mCallback.onAuthTokenAvailable(token);
            }
        }
    }


    public void setAccountSyncable(Account account) {

        Account[] accounts = mAccountManager.getAccountsByType(ACCOUNT_TYPE);
        for (int i = 0; i < accounts.length; i++) {
            int syncable = 0;
            if (accounts[i].equals(account)) {
                syncable = 1;
            }
            AppLog.d("Set " + accounts[i].name + " syncable = " + syncable);
            ContentResolver.setIsSyncable(accounts[i], AppListContentProvider.AUTHORITY, syncable);
        }

    }

    /**
     * setup sync according to current settings
     */
    public void setSync(Account account, boolean autoSync, long pollFrequency) {
        Bundle params = new Bundle();

        AppLog.d("Set sync for " + account.name + ", autoSync" + autoSync);
        if (autoSync) {
            ContentResolver.setSyncAutomatically(account, AppListContentProvider.AUTHORITY, true);
            ContentResolver.addPeriodicSync(account, AppListContentProvider.AUTHORITY, params, pollFrequency);
        } else {
            ContentResolver.removePeriodicSync(account, AppListContentProvider.AUTHORITY, params);
            ContentResolver.setSyncAutomatically(account, AppListContentProvider.AUTHORITY, false);
        }

    }
}
