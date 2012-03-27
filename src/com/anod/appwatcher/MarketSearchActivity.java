package com.anod.appwatcher;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gc.android.market.api.MarketSession;
import com.gc.android.market.api.MarketSession.Callback;
import com.gc.android.market.api.model.Market.App;
import com.gc.android.market.api.model.Market.AppsRequest;
import com.gc.android.market.api.model.Market.AppsResponse;
import com.gc.android.market.api.model.Market.ResponseContext;

@SuppressWarnings("unused")
public class MarketSearchActivity extends ListActivity {
    
	public static final String EXTRA_TOKEN = "extra_token";
	private AppsResponseAdapter mAdapter;
	private MarketSession mMarketSession;
	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.market_search);
		
		mAdapter = new AppsResponseAdapter();
		setListAdapter(mAdapter);
		
		mMarketSession = new MarketSession();
		
		TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE); 
		mMarketSession.setOperator(
			tm.getSimOperatorName(), 
			tm.getSimOperator()
		) ; 
		 
		handleIntent(getIntent());
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
        final Bundle appData = intent.getBundleExtra(SearchManager.APP_DATA);
        String authSubToken = appData.getString(EXTRA_TOKEN);
		mMarketSession.setAuthSubToken(authSubToken);
    	if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            // handles a search query
            String query = intent.getStringExtra(SearchManager.QUERY);
            new RetreiveResultsTask().execute(query);
        }
    }

    class RetreiveResultsTask extends AsyncTask<String, Void, AppsResponse> {

    	class ResponseWrapper {
    		AppsResponse response;
    	}
        protected AppsResponse doInBackground(String... queries) {
    		AppsRequest appsRequest = AppsRequest.newBuilder()
	            .setQuery(queries[0])
	            .setStartIndex(0).setEntriesCount(10)
	            .setWithExtendedInfo(true)
	            .build();
    		final ResponseWrapper respWrapper = new ResponseWrapper();
    		Log.i("AppWatcher", mMarketSession.toString());
    		Log.i("AppWatcher", appsRequest.toString());
    		try {
				mMarketSession.append(appsRequest, new Callback<AppsResponse>() {
			         @Override
			         public void onResult(ResponseContext context, AppsResponse response) {
			        	 respWrapper.response = response;
			        	 Log.i("AppWatcher", response.toString());
			         }
				});
				mMarketSession.flush();
    		} catch(Exception e) {
    			Log.e("AppWatcher", e.getMessage());
    			return null;
    		}
            return respWrapper.response;
        }
        
        @Override
        protected void onPostExecute(AppsResponse response) {
        	if (response != null) {
	        	 mAdapter.setAppsResponse(response);
        	}
        }
    };
  
 
	class AppsResponseAdapter extends BaseAdapter {
		AppsResponse mAppsResponse = null;
		class ViewHolder {
			TextView title;
		}
		public void setAppsResponse(AppsResponse response) {
			mAppsResponse = response;
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return (mAppsResponse == null) ? 0 : mAppsResponse.getAppCount();
		}

		@Override
		public Object getItem(int position) {
			return (mAppsResponse == null) ? null : mAppsResponse.getApp(position);
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
                
                v.setTag(holder);
            } else {
            	holder = (ViewHolder)v.getTag();
            }
            App app = (App)getItem(position);
            holder.title.setText(app.getTitle()+" "+app.getVersion());
			return v;
		}

		@Override
		public int getViewTypeCount() {
			return 1;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public boolean isEmpty() {
			return (mAppsResponse == null) ? true : (mAppsResponse.getAppCount() == 0);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
		
	}	
}
