package com.anod.appwatcher;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.SimpleCursorAdapter;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.anod.appwatcher.model.AppListTable;

public class AppWatcherActivity extends SherlockListActivity {
    private SimpleCursorAdapter mAdapter;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Create an empty adapter we will use to display the loaded data.
        mAdapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_2, null,
                new String[] { AppListTable.KEY_PACKAGE , AppListTable.KEY_TITLE },
                new int[] { android.R.id.text1, android.R.id.text2 }, 0);
        setListAdapter(mAdapter);

        
        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getSupportLoaderManager().initLoader(0, null, new ListLoaderCallbacks()); 
    }

    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            return false;
        case R.id.menu_add:
        	return true;
        case R.id.menu_refresh:
        	return true;        	
        default:
            return onOptionsItemSelected(item);
        }
    }    
    
    class ListLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {
    
	    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
	        return new CursorLoader(AppWatcherActivity.this, AppListContentProvider.CONTENT_URI,
	        		AppListTable.APPLIST_PROJECTION, null, null,
	        		AppListTable.KEY_PACKAGE + " COLLATE LOCALIZED ASC");
	    }
	
	
		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
	        // Swap the new cursor in.  (The framework will take care of closing the
	        // old cursor once we return.)
	        mAdapter.swapCursor(data);
		}
	
	
		@Override
		public void onLoaderReset(Loader<Cursor> loader) {
			// This is called when the last Cursor provided to onLoadFinished()
	        // above is about to be closed.  We need to make sure we are no
	        // longer using it.
	        mAdapter.swapCursor(null);
		}
    }
}