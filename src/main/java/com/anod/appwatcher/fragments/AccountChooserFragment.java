package com.anod.appwatcher.fragments;

import android.accounts.Account;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.anod.appwatcher.AppWatcherApplication;
import com.anod.appwatcher.R;
import com.anod.appwatcher.accounts.AccountManager;

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
	private OnAccountSelectionListener mListener;

	private AccountManager mAccountManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        mAccountManager = AppWatcherApplication.provide(getActivity()).accountManager();
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

		if (mAccountManager.hasAccounts()) {
            mSelectedItem = getSelectedItem();
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    if (mAccountManager.getCurrentAccount() == null) {
                        OnAccountSelectionListener listener = (OnAccountSelectionListener)getActivity();
                        listener.onDialogAccountNotFound();
                    }
                }
            });
            builder.setSingleChoiceItems(getChoiceItems(), mSelectedItem, this);
		} else {
            builder.setMessage(R.string.no_regestred_google_accounts);
		}
        AlertDialog dialog = builder.create();

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (mAccountManager.getCurrentAccount() == null) {
                    OnAccountSelectionListener listener = (OnAccountSelectionListener)getActivity();
                    listener.onDialogAccountNotFound();
                }
            }
        });


		// Create the AlertDialog object and return it
		return dialog;
	}

	private int getSelectedItem() {

        Account current = mAccountManager.getCurrentAccount();
		if (mAccountManager.getCurrentAccount() == null) {
			return 0;
		}

        Account[] accounts = mAccountManager.getAccounts();
		for( int i = 0; i < accounts.length; i++) {
			if (accounts[i].equals(current)) {
				return i;
			}
		}

		return 0;
	}

	private CharSequence[] getChoiceItems() {
        Account[] accounts = mAccountManager.getAccounts();
		CharSequence[] items = new CharSequence[accounts.length];

		for( int i = 0; i < accounts.length; i++) {
			items[i] = accounts[i].name;
		}

		return items;
	}


	@Override
	public void onClick(DialogInterface dialogInterface, int i) {
		mSelectedItem = i;
	}

	private void saveAccount() {
		OnAccountSelectionListener listener = mListener;
		if (mAccountManager.hasAccounts()) {
            Account acc = mAccountManager.getAccount(mSelectedItem);
            mAccountManager.saveCurrentAccount(acc);
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
