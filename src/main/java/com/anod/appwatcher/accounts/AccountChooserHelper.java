package com.anod.appwatcher.accounts;

import android.accounts.Account;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.anod.appwatcher.Preferences;
import com.anod.appwatcher.fragments.AccountChooserFragment;

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

    private void showAccountsDialog() {
        AccountChooserFragment accountsDialog = AccountChooserFragment.newInstance();
        accountsDialog.show(mActivity.getSupportFragmentManager(), "accountsDialog");
    }

    public void showAccountsDialogWithCheck() {
        showAccountsDialog();
    }

    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == AccountChooserFragment.PERMISSION_REQUEST_GET_ACCOUNTS)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                // http://stackoverflow.com/questions/33264031/calling-dialogfragments-show-from-within-onrequestpermissionsresult-causes
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showAccountsDialog();
                    }
                }, 200);
            }
            else
            {
                Toast.makeText(mActivity, "Failed to gain access to Google accounts", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Container Activity must implement this interface
    public interface OnAccountSelectionListener extends AccountChooserFragment.AccountSelectionProvider {
        void onHelperAccountSelected(final Account account, final String authSubToken);
        void onHelperAccountNotFound();
    }

    public AccountChooserHelper(AppCompatActivity activity, Preferences preferences, OnAccountSelectionListener listener) {
        mActivity = activity;
        if (!(mActivity instanceof AccountChooserFragment.AccountSelectionProvider)) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnAccountSelectionListener");
        }
        mPreferences = preferences;
        mContext = mActivity;
        mAuthTokenProvider = new AuthTokenProvider(mContext);
        mListener = listener;
    }

    public Account getAccount() {
        return mSyncAccount;
    }

    public void init() {
        mSyncAccount = mPreferences.getAccount();

        if (mSyncAccount == null) {
            showAccountsDialogWithCheck();
        } else {
            mAuthTokenProvider.requestToken(mActivity, mSyncAccount, new AuthTokenProvider.AuthenticateCallback() {
                @Override
                public void onAuthTokenAvailable(String token) {
                    if (mListener != null) {
                        mListener.onHelperAccountSelected(mSyncAccount, token);
                    }
                }

                @Override
                public void onUnRecoverableException(String errorMessage) {
                    if (mListener != null) {
                        mListener.onHelperAccountSelected(mSyncAccount, null);
                    }
                }
            });


        }
    }

    @Override
    public void onDialogAccountSelected(final Account account) {
        mSyncAccount = account;
        mAuthTokenProvider.requestToken(mActivity, account, new AuthTokenProvider.AuthenticateCallback() {
            @Override
            public void onAuthTokenAvailable(String token) {
                if (mListener != null) {
                    mListener.onHelperAccountSelected(account, token);
                }
            }

            @Override
            public void onUnRecoverableException(String errorMessage) {
                if (mListener != null) {
                    mListener.onHelperAccountSelected(mSyncAccount, null);
                }
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
