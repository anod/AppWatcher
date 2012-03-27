package com.anod.appwatcher;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.anod.appwatcher.client.TokenHelper;
import com.anod.appwatcher.client.TokenHelper.CallBack;

public class AppWatcherActivity extends SherlockFragmentActivity {
	protected String mAuthToken;

	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.main, menu);
        
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
//            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//            SearchView searchView = (SearchView) menu.findItem(R.id.menu_add).getActionView();
//            searchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(this, MarketSearchActivity.class)));
//            searchView.setIconifiedByDefault(false);
//            searchView.setSubmitButtonEnabled(true);
//        }
        
        return true;
    }

    @Override
    public boolean onSearchRequested() {
    	if (mAuthToken == null) {
    		Log.w("AppWatcher", "Empty Auth Token");
    		return false;
    	}
    	
        Bundle appData = new Bundle();
        appData.putString(MarketSearchActivity.EXTRA_TOKEN, mAuthToken);
        startSearch(null, false, appData, false);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_add:
        	TokenHelper helper = new TokenHelper(this, new CallBack() {

				@Override
				public void onTokenReceive(String authToken) {
		        	if (authToken == null) {
		        		Toast.makeText(AppWatcherActivity.this, R.string.failed_gain_access, Toast.LENGTH_LONG).show();
		        	} else {
		        		mAuthToken = authToken;
		        		onSearchRequested();
		        	}
				}
			});
        	helper.requestToken();
        	return true;
        case R.id.menu_refresh:
        	return true;        	
        default:
            return onOptionsItemSelected(item);
        }
    }    

	
}
