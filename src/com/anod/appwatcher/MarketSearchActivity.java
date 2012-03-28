package com.anod.appwatcher;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.anod.appwatcher.market.AppIconLoader;
import com.anod.appwatcher.market.AppsResponseLoader;
import com.commonsware.cwac.endless.EndlessAdapter;
import com.gc.android.market.api.MarketSession;
import com.gc.android.market.api.model.Market.App;

public class MarketSearchActivity extends SherlockListActivity {
    
	public static final String EXTRA_TOKEN = "extra_token";
	private AppsAdapter mAdapter;
	private MarketSession mMarketSession;
	private AppsResponseLoader mResponseLoader;
	private Context mContext;
	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.market_search);
		mContext = (Context)this;

		mMarketSession = new MarketSession();
		TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE); 
		mMarketSession.setOperator(
			tm.getNetworkOperatorName(), 
			tm.getSimOperatorName(), 
			tm.getNetworkOperator(),
			tm.getSimOperator()
		);

		mAdapter = new AppsAdapter(this,R.layout.market_app_row, mMarketSession);

		setListAdapter(mAdapter);
		getListView().setOnItemClickListener(itemClickListener);

		
		ActionBar bar = getSupportActionBar();
		bar.setCustomView(R.layout.searchbox);
		bar.setDisplayShowCustomEnabled(true);
		EditText edit = (EditText)bar.getCustomView();
		edit.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				showResults();
				return true;
			}
		});
		handleIntent(getIntent());
	}

	private void showResults() {
    	mAdapter.clear();
		EditText editText = (EditText)getSupportActionBar().getCustomView();
		String query = editText.getText().toString();
		if (query.length() > 0) {
			mResponseLoader = new AppsResponseLoader(mMarketSession, query);
			new RetreiveResultsTask().execute();
		}
	}
	
    @Override
    protected void onNewIntent(Intent intent) {
        // Because this activity has set launchMode="singleTop", the system calls this method
        // to deliver the intent if this activity is currently the foreground activity when
        // invoked again (when the user executes a search from this activity, we don't create
        // a new instance of this activity, so the system delivers the search intent here)
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String authSubToken = intent.getStringExtra(EXTRA_TOKEN);
		mMarketSession.setAuthSubToken(authSubToken);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.searchbox, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_search:
        	showResults();
        	return true;        	
        default:
            return onOptionsItemSelected(item);
        }
    }    

    final OnItemClickListener itemClickListener  = new OnItemClickListener() {
		@Override
		public void onItemClick (AdapterView<?> parent, View view, int position, long id) {
			App app = mAdapter.getItem(position);
			AlertDialog dialog = (new AlertDialog.Builder(mContext))
				.setMessage(app.toString())
				.setCancelable(true)
				.create();
			dialog.show();
		}
    };     
   
    class RetreiveResultsTask extends AsyncTask<String, Void, List<App>> {
        protected List<App> doInBackground(String... queries) {
        	return mResponseLoader.load();
        }
        
        @Override
        protected void onPostExecute(List<App> list) {
        	if (list == null) {
        		return;
        	}
    		mAdapter.addAll(list);
    		if (mResponseLoader.hasNext()) {
    			getListView().setAdapter(new AppsEndlessAdapter(
    				mContext, mAdapter, R.layout.pending
    			));
    		}
        }
    };
  
    class AppsEndlessAdapter extends EndlessAdapter {

		private List<App> mCache;

		public AppsEndlessAdapter(Context context, ListAdapter wrapped,
				int pendingResource) {
			super(context, wrapped, pendingResource);
		}

		@Override
		protected boolean cacheInBackground() throws Exception {
			if (mResponseLoader.moveToNext()) {
				mCache = mResponseLoader.load();
				return (mCache == null) ? false : true;
			}
			return false;
		}

		@Override
		protected void appendCachedData() {
			if (mCache != null) {
				AppsAdapter adapter = (AppsAdapter)getWrappedAdapter();
				adapter.addAll(mCache);
			}
		}
    	
    }
 
    class AppsAdapter extends ArrayAdapter<App> {
		class ViewHolder {
			TextView title;
			TextView details;
			ImageView icon;
		}

		private AppIconLoader mIconLoader;

		public AppsAdapter(Context context, int textViewResourceId, MarketSession session) {
			super(context, textViewResourceId);
			mIconLoader = new AppIconLoader(session);
		}
 
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.market_app_row, null);
                holder = new ViewHolder();
                holder.title = (TextView)v.findViewById(R.id.title);
                holder.details = (TextView)v.findViewById(R.id.details);
                v.setTag(holder);
            } else {
            	holder = (ViewHolder)v.getTag();
            }
            App app = (App)getItem(position);
            holder.title.setText(app.getTitle()+" "+app.getVersion());
            holder.details.setText(app.getCreator());
            ImageView icon = (ImageView)v.findViewById(R.id.icon);
            mIconLoader.loadImage(app.getId(), icon);
            
			return v;
		}

    }
    	

}
