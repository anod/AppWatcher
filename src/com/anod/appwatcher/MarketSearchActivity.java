package com.anod.appwatcher;

import com.gc.android.market.api.MarketSession;
import com.gc.android.market.api.MarketSession.Callback;
import com.gc.android.market.api.model.Market.AppsRequest;
import com.gc.android.market.api.model.Market.AppsResponse;
import com.gc.android.market.api.model.Market.ResponseContext;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

public class MarketSearchActivity extends ListActivity {
    
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
			tm.getNetworkOperatorName(), 
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
    	if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            // handles a search query
            String query = intent.getStringExtra(SearchManager.QUERY);
            showResults(query);
        }
    }

	private void showResults(String query) {
		AppsRequest appsRequest = AppsRequest.newBuilder()
            .setQuery(query)
            .setStartIndex(0).setEntriesCount(10)
            .setWithExtendedInfo(true)
            .build();
		                       
		mMarketSession.append(appsRequest, new Callback<AppsResponse>() {
	         @Override
	         public void onResult(ResponseContext context, AppsResponse response) {
	                  // Your code here
	                  // response.getApp(0).getCreator() ...
	                  // see AppsResponse class definition for more infos
	        	 mAdapter.setAppsResponse(response);
	         }
		});
		
	}
 
	class AppsResponseAdapter implements ListAdapter {
		AppsResponse mAppsResponse = null;
		
		public void setAppsResponse(AppsResponse response) {
			mAppsResponse = response;
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
		public long getItemId(int position) {
			return (mAppsResponse == null) ? 0 : position;
		}

		@Override
		public int getItemViewType(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			return null;
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
		public void registerDataSetObserver(DataSetObserver observer) {
		}

		@Override
		public void unregisterDataSetObserver(DataSetObserver observer) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean areAllItemsEnabled() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isEnabled(int position) {
			// TODO Auto-generated method stub
			return false;
		}
		
	}	
}
