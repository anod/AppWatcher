package com.anod.appwatcher.accounts;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import com.anod.appwatcher.AppListContentProvider;
import com.anod.appwatcher.Preferences;
import com.anod.appwatcher.fragments.AccountChooserFragment;

import net.hockeyapp.android.CrashManager;

import info.anodsplace.android.log.AppLog;

/**
 * @author alex
 * @date 9/17/13
 */
public class AccountChooserHelper implements AccountChooserFragment.OnAccountSelectionListener {
	private final AuthTokenProvider mAuthTokenProvider;
	private final Context mContext;
	private final OnAccountSelectionListener mListener;
	private AppCompatActivity mActivity;
	private Preferences mPreferences;
	private Account mSyncAccount;

	private static final int TWO_HOURS_IN_SEC = 7200;
	private static final int SIX_HOURS_IN_SEC = 21600;

    public void showAccountsDialog() {
        AccountChooserFragment accountsDialog = AccountChooserFragment.newInstance();
        accountsDialog.show(mActivity.getSupportFragmentManager(), "accountsDialog");
    }


    // Container Activity must implement this interface
	public interface OnAccountSelectionListener {
		void onHelperAccountSelected(final Account account, final String authSubToken);
		void onHelperAccountNotFound();
	}

	public AccountChooserHelper(AppCompatActivity activity, Preferences preferences, OnAccountSelectionListener listener) {
		mActivity = activity;
		mPreferences = preferences;
		mContext = (Context)mActivity;
		mAuthTokenProvider = new AuthTokenProvider(mContext);
		mListener = listener;
	}

    public Account getAccount() {
        return mSyncAccount;
    }

	public void init() {
		mSyncAccount = mPreferences.getAccount();

		if (mSyncAccount == null) {
            // Do not display dialog if only one account available
            //AccountManager accountManager = new AccountManager(mContext);
           // if (accountManager.hasJustOneAccount()) {
            //    final Account account = accountManager.getAccount(0);
            //    accountManager.saveCurrentAccount(account);
            //    onDialogAccountSelected(account);
            //} else {
                showAccountsDialog();
            //}
		} else {
			mAuthTokenProvider.requestToken(mActivity, mSyncAccount, new AuthTokenProvider.AuthenticateCallback() {
				@Override
				public void onAuthTokenAvailable(String token) {
					initAutoSync(mSyncAccount);
					if (mListener != null) {
						mListener.onHelperAccountSelected(mSyncAccount, token);
					}
				}

				@Override
				public void onUnRecoverableException(String errorMessage) {
					AppLog.e(errorMessage);
				}
			});


		}
	}

	public void setSync(boolean autoSync) {
		long pollFrequency = (mPreferences.isWifiOnly()) ? TWO_HOURS_IN_SEC : SIX_HOURS_IN_SEC;
		if (mSyncAccount != null) {
			mAuthTokenProvider.setSync(mSyncAccount, autoSync, pollFrequency);
		}
	}

	private void initAutoSync(Account account) {
		boolean autoSync = true;
		if (!mPreferences.checkFirstLaunch()) {
			autoSync = ContentResolver.getSyncAutomatically(account, AppListContentProvider.AUTHORITY);
		}
		mAuthTokenProvider.setAccountSyncable(account);
		setSync(autoSync);
	}

	@Override
	public void onDialogAccountSelected(final Account account) {
		mSyncAccount = account;
		mAuthTokenProvider.requestToken(mActivity, account, new AuthTokenProvider.AuthenticateCallback() {
			@Override
			public void onAuthTokenAvailable(String token) {
				initAutoSync(account);
				if (mListener != null) {
					mListener.onHelperAccountSelected(account, token);
				}
			}

			@Override
			public void onUnRecoverableException(String errorMessage) {

			}
		});

	}

	@Override
	public void onDialogAccountNotFound() {
		if (mListener != null) {
			mListener.onHelperAccountNotFound();
		}
	}
}
