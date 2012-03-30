package com.anod.appwatcher;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.model.AppListCursor;
import com.anod.appwatcher.model.AppListTable;

public class AppWatcherListFragment extends SherlockListFragment implements LoaderManager.LoaderCallbacks<Cursor>{
    private CursorAdapter mAdapter;

	/** Called when the activity is first created. */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Give some text to display if there is no data.  In a real
        // application this would come from a resource.
        setEmptyText(getString(R.string.no_data));
        
        // We have a menu item to show in action bar.
        setHasOptionsMenu(true);
        
        // Create an empty adapter we will use to display the loaded data.
        mAdapter = new ListCursorAdapter(getActivity(), null, 0);
        setListAdapter(mAdapter);

        // Start out with a progress indicator.
        setListShown(false);        
        
        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(0, null, this); 
    }
    
    private class ListCursorAdapter extends CursorAdapter {
        private LayoutInflater mInflater;
        private Bitmap mDefaultIcon;
    	class ViewHolder {
			TextView title;
			TextView details;
			ImageView icon;
		}
		public ListCursorAdapter(Context context, Cursor c, int flags) {
			super(context, c, flags);
	        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);			
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			AppListCursor wrapper = new AppListCursor(cursor);
			AppInfo app = wrapper.getAppInfo();
			ViewHolder holder = (ViewHolder)view.getTag();
            holder.title.setText(app.getTitle()+" "+app.getVersionName());
            holder.details.setText(app.getCreator());
            Bitmap icon = app.getIcon();
            if (icon == null) {
	           	if (mDefaultIcon == null) {
	           		mDefaultIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_empty);
	           	}
	           	icon = mDefaultIcon;
            }
            holder.icon.setImageBitmap(icon);            
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View v = mInflater.inflate(R.layout.list_row, parent, false);
			ViewHolder holder = new ViewHolder();
            holder.title = (TextView)v.findViewById(R.id.title);
            holder.details = (TextView)v.findViewById(R.id.details);
            holder.icon = (ImageView)v.findViewById(R.id.icon);            
            v.setTag(holder);
            
			return v;
		}
    	
    }
    
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), AppListContentProvider.CONTENT_URI,
        		AppListTable.APPLIST_PROJECTION, null, null,
        		AppListTable.Columns.KEY_TITLE + " COLLATE LOCALIZED ASC");
    }

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mAdapter.swapCursor(data);
        
        // The list should now be shown.
        if (isResumed()) {
            setListShown(true);
        } else {
            setListShownNoAnimation(true);
        }        
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mAdapter.swapCursor(null);
	}

}