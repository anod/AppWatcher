package com.anod.appwatcher.accounts;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Context;
import android.support.v7.app.ActionBarActivity;

import com.anod.appwatcher.AppListContentProvider;
import com.anod.appwatcher.Preferences;
import com.anod.appwatcher.fragments.AccountChooserFragment;

import org.acra.ACRA;

/**
 * @author alex
 * @date 9/17/13
 */
public class AccountChooserHelper implements AccountChooserFragment.OnAccountSelectionListener {
	private final AccountHelper mAccountHelper;
	private final Context mContext;
	private final OnAccountSelectionListener mListener;
	private ActionBarActivity mActivity;
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
		public void onHelperAccountSelected(final Account account, final String authSubToken);
		public void onHelperAccountNotFound();
	}

	public AccountChooserHelper(ActionBarActivity activity, Preferences preferences, OnAccountSelectionListener listener) {
		mActivity = activity;
		mPreferences = preferences;
		mContext = (Context)mActivity;
		mAccountHelper = new AccountHelper(mContext);
		mListener = listener;
	}

	public void init() {
		mSyncAccount = mPreferences.getAccount();
		if (mSyncAccount == null) {
            // Do not display dialog if only one account available
            AccountManager accountManager = new AccountManager(mContext);
            if (accountManager.hasJustOneAccount()) {
                mSyncAccount = accountManager.getAccount(0);
                accountManager.saveCurrentAccount(mSyncAccount);
            } else {
                showAccountsDialog();
            }
		} else {
			ACRA.getErrorReporter().putCustomData("HasAccountSelected", mSyncAccount != null ? "true" : "false");
			mAccountHelper.requestToken(mActivity, mSyncAccount, new AccountHelper.AuthenticateCallback() {
				@Override
				public void onAuthTokenAvailable(String token) {
					initAutoSync(mSyncAccount);
					mListener.onHelperAccountSelected(mSyncAccount, token);
				}

				@Override
				public void onUnRecoverableException(String errorMessage) {

				}
			});


		}
	}

	public void setSync(boolean autoSync) {
		long pollFrequency = (mPreferences.isWifiOnly()) ? TWO_HOURS_IN_SEC : SIX_HOURS_IN_SEC;
		if (mSyncAccount != null) {
			mAccountHelper.setSync(mSyncAccount, autoSync, pollFrequency);
		}
	}

	private void initAutoSync(Account account) {
		boolean autoSync = true;
		if (!mPreferences.checkFirstLaunch()) {
			autoSync = ContentResolver.getSyncAutomatically(account, AppListContentProvider.AUTHORITY);
		}
		mAccountHelper.setAccountSyncable(account);
		setSync(autoSync);
	}

	@Override
	public void onDialogAccountSelected(final Account account) {
		mSyncAccount = account;
		mAccountHelper.requestToken(mActivity, account, new AccountHelper.AuthenticateCallback() {
			@Override
			public void onAuthTokenAvailable(String token) {
				initAutoSync(account);
				ACRA.getErrorReporter().putCustomData("HasAccountSelected", mSyncAccount != null ? "true" : "false");
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
