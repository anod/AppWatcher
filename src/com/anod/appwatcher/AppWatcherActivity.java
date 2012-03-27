package com.anod.appwatcher;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.anod.appwatcher.client.TokenHelper;
import com.anod.appwatcher.client.TokenHelper.CallBack;

public class AppWatcherActivity extends SherlockFragmentActivity {
	protected String mAuthToken;
	private AppWatcherActivity mContext;

	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mContext = this;
	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.main, menu);       
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
		        		Intent intent = new Intent(mContext, MarketSearchActivity.class);
		        		intent.putExtra(MarketSearchActivity.EXTRA_TOKEN, authToken);
		        		startActivity(intent);
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
