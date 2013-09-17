package com.anod.appwatcher.accounts;

import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.support.v7.app.ActionBarActivity;

import com.anod.appwatcher.Preferences;
import com.anod.appwatcher.accounts.AccountHelper;
import com.anod.appwatcher.fragments.AccountChooserFragment;

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


	// Container Activity must implement this interface
	public interface OnAccountSelectionListener {
		public void onAccountSelected(final Account account,final String authSubToken);
		public void onAccountNotFound();
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
			AccountChooserFragment accountsDialog = AccountChooserFragment.newInstance();
			accountsDialog.show(mActivity.getSupportFragmentManager(), "accountsDialog");
			accountsDialog.setListener(this);
		} else {
			mAccountHelper.requestToken(mActivity, mSyncAccount, new AccountHelper.AuthenticateCallback() {
				@Override
				public void onAuthTokenAvailable(String token) {
					mListener.onAccountSelected(mSyncAccount, token);
				}

				@Override
				public void onUnRecoverableException(String errorMessage) {

				}
			});


		}
	}

	@Override
	public void onAccountSelected(final Account account) {
		mSyncAccount = account;
		mAccountHelper.requestToken(mActivity, account, new AccountHelper.AuthenticateCallback() {
			@Override
			public void onAuthTokenAvailable(String token) {
				if (mListener != null) {
					mListener.onAccountSelected(account, token);
				}
			}

			@Override
			public void onUnRecoverableException(String errorMessage) {

			}
		});

	}

	@Override
	public void onAccountNotFound() {
		if (mListener != null) {
			mListener.onAccountNotFound();
		}
	}
}
