package com.anod.appwatcher;

import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.support.v7.app.ActionBarActivity;

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
		public void onAccountSelected(Account account);
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
			mAccountHelper.requestToken(mActivity);

			mListener.onAccountSelected(mSyncAccount);
		}
	}

	@Override
	public void onAccountSelected(Account account) {
		mSyncAccount = account;
		mAccountHelper.requestToken(mActivity);
		mListener.onAccountSelected(account);
	}

	@Override
	public void onAccountNotFound() {
		mListener.onAccountNotFound();
	}
}
