package com.anod.appwatcher.sync;

import com.anod.appwatcher.AppListContentProvider;
import com.anod.appwatcher.model.AppListCursor;
import com.anod.appwatcher.model.AppListTable;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private final AccountManager mAccountManager;

    private final Context mContext;
    
	public SyncAdapter(Context context, boolean autoInitialize) {
		super(context, autoInitialize);
        mContext = context;
        mAccountManager = AccountManager.get(context);
	}

	@Override
	public void onPerformSync(Account account, Bundle extras, String authority,
			ContentProviderClient provider, SyncResult syncResult) {

		Log.v("AppWatcher", "onPerformSync()");
		try {
			AppListCursor apps = loadApps(provider); 
			
			
		} catch (RemoteException e) {
			
			
		}
	
	}

	/**
	 * @param provider
	 * @throws RemoteException
	 */
	private AppListCursor loadApps(ContentProviderClient provider)
			throws RemoteException {
		Cursor cursor = provider.query(
			AppListContentProvider.CONTENT_URI,
			AppListTable.APPLIST_PROJECTION, 
			null, null, null
		);
		if (cursor!=null) {
			return new AppListCursor(cursor);
		}
		return null;
	}

}
