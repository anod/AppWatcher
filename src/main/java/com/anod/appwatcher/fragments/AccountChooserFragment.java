package com.anod.appwatcher.fragments;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.anod.appwatcher.Preferences;
import com.anod.appwatcher.R;
import com.anod.appwatcher.accounts.AccountHelper;

/**
 * @author alex
 * @date 8/24/13
 */
public class AccountChooserFragment extends DialogFragment implements DialogInterface.OnClickListener{

	public static AccountChooserFragment newInstance() {
		AccountChooserFragment frag = new AccountChooserFragment();
		Bundle args = new Bundle();
		frag.setArguments(args);
		return frag;
	}

	// Container Activity must implement this interface
	public interface OnAccountSelectionListener {
		public void onDialogAccountSelected(Account account);
		public void onDialogAccountNotFound();
	}

	private int mSelectedItem;
	private Account mCurrentAccount;
	private OnAccountSelectionListener mListener;

	private AccountManager mAccountManager;
	private Account[] mAccounts;
	private Preferences mPreferences;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAccountManager = AccountManager.get(getActivity());
		mPreferences = new Preferences(getActivity());
		mAccounts = mAccountManager.getAccountsByType(AccountHelper.ACCOUNT_TYPE);
		mCurrentAccount = mPreferences.getAccount();
	}


	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			mListener = (OnAccountSelectionListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnAccountSelectionListener");
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		builder.setTitle(R.string.choose_an_account)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						saveAccount();
					}
				})
		;

		if (mAccounts.length == 0) {
			builder.setMessage("No registered google accounts");
		} else {
			mSelectedItem = getSelectedItem();
			builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					if (mCurrentAccount == null) {
						OnAccountSelectionListener listener = (OnAccountSelectionListener)getActivity();
						listener.onDialogAccountNotFound();
					}
				}
			});
			builder.setSingleChoiceItems(getChoiceItems(), mSelectedItem, this);

		}


		// Create the AlertDialog object and return it
		return builder.create();
	}

	private int getSelectedItem() {


		if (mCurrentAccount == null) {
			return 0;
		}

		for( int i = 0; i < mAccounts.length; i++) {
			if (mAccounts[i].equals(mCurrentAccount)) {
				return i;
			}
		}

		return 0;
	}

	private CharSequence[] getChoiceItems() {
		CharSequence[] items = new CharSequence[mAccounts.length];

		for( int i = 0; i < mAccounts.length; i++) {
			items[i] = mAccounts[i].name;
		}

		return items;
	}


	@Override
	public void onClick(DialogInterface dialogInterface, int i) {
		mSelectedItem = i;
	}

	private void saveAccount() {
		OnAccountSelectionListener listener = mListener;
		if (mAccounts.length > 0) {
			Account acc = mAccounts[mSelectedItem];
			mPreferences.updateAccount(acc);
			if (mListener != null) {
				listener.onDialogAccountSelected(acc);
			}
		} else {
			if (mListener != null) {
				listener.onDialogAccountNotFound();
			}
		}
	}
}
