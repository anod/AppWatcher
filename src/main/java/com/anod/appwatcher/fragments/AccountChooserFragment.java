package com.anod.appwatcher.fragments;

import android.Manifest;
import android.accounts.Account;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.anod.appwatcher.AppWatcherApplication;
import com.anod.appwatcher.R;
import com.anod.appwatcher.accounts.AccountManager;

/**
 * @author alex
 * @date 8/24/13
 */
public class AccountChooserFragment extends DialogFragment implements DialogInterface.OnClickListener {

    public static final int PERMISSION_REQUEST_GET_ACCOUNTS = 123;

    public static AccountChooserFragment newInstance() {
        AccountChooserFragment frag = new AccountChooserFragment();
        Bundle args = new Bundle();
        frag.setArguments(args);
        return frag;
    }

    public interface AccountSelectionProvider extends ActivityCompat.OnRequestPermissionsResultCallback {
        OnAccountSelectionListener getAccountSelectionListener();
    }

    // Container Activity must implement this interface
    public interface OnAccountSelectionListener {
        void onDialogAccountSelected(Account account);
        void onDialogAccountNotFound();
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
            mListener = ((AccountSelectionProvider) activity).getAccountSelectionListener();
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnAccountSelectionListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.choose_an_account);


        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED)
        {
            builder.setMessage("Allow access to list of you Google accounts");
            builder.setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    ActivityCompat.requestPermissions(
                            getActivity(),
                            new String[] { Manifest.permission.GET_ACCOUNTS },
                            PERMISSION_REQUEST_GET_ACCOUNTS
                    );
                }
            });
        }
        else
        {
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                saveAccount();
                }
            });

            mAccountManager.reload();
            if (mAccountManager.hasAccounts()) {
                mSelectedItem = getSelectedItem();
                builder.setSingleChoiceItems(getChoiceItems(), mSelectedItem, this);
            } else {
                builder.setMessage(R.string.no_registered_google_accounts);
            }
        }

        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (mAccountManager.getCurrentAccount() == null) {
                    if (mListener != null) {
                        mListener.onDialogAccountNotFound();
                    }
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (mAccountManager.getCurrentAccount() == null) {
                    if (mListener != null) {
                        mListener.onDialogAccountNotFound();
                    }
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
        for (int i = 0; i < accounts.length; i++) {
            if (accounts[i].equals(current)) {
                return i;
            }
        }

        return 0;
    }

    private CharSequence[] getChoiceItems() {
        Account[] accounts = mAccountManager.getAccounts();
        CharSequence[] items = new CharSequence[accounts.length];

        for (int i = 0; i < accounts.length; i++) {
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
