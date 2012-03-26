package com.anod.appwatcher;

import android.os.Bundle;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.anod.appwatcher.client.TokenHelper;

public class AppWatcherActivity extends SherlockFragmentActivity {
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
        	TokenHelper helper = new TokenHelper(this);
        	String token = helper.requestToken();
        	if (token == null) {
        		Toast.makeText(this, R.string.failed_gain_access, Toast.LENGTH_LONG).show();
        	} else {
        		
        	}
        	return true;
        case R.id.menu_refresh:
        	return true;        	
        default:
            return onOptionsItemSelected(item);
        }
    }    

	
}
