package com.anod.appwatcher.model;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.CursorLoader;
import android.text.TextUtils;

import com.anod.appwatcher.AppListContentProvider;

/**
 * @author alex
 * @date 8/11/13
 */
public class AppListCursorLoader extends CursorLoader {
	private int mNewCount;

	public AppListCursorLoader(Context context, String titleFilter) {
		super(
			context,
			AppListContentProvider.CONTENT_URI,
			AppListTable.APPLIST_PROJECTION, null, null,
			AppListTable.Columns.KEY_STATUS + " DESC, " +AppListTable.Columns.KEY_TITLE + " COLLATE LOCALIZED ASC"
		);
		if (!TextUtils.isEmpty(titleFilter)) {
			setSelection(AppListTable.Columns.KEY_TITLE + " LIKE ?");
			setSelectionArgs(new String[] { "%"+titleFilter+"%" });
		}
	}

	@Override
	public Cursor loadInBackground() {
		Cursor cr = super.loadInBackground();

		loadNewCount();

		return new AppListCursor(cr);
	}

	public int getNewCount() {
		return mNewCount;
	}

	private void loadNewCount() {
		AppListContentProviderClient cl = new AppListContentProviderClient(getContext());
		mNewCount = cl.queryUpdatesCount();
		cl.release();
	}
}
