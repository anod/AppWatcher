package com.anod.appwatcher;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.anod.appwatcher.accounts.MarketTokenLoader;
import com.anod.appwatcher.actionbarcompat.ActionBarActivity;
import com.anod.appwatcher.market.AppIconLoader;
import com.anod.appwatcher.market.AppsResponseLoader;
import com.anod.appwatcher.market.DeviceIdHelper;
import com.anod.appwatcher.market.MarketSessionHelper;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.model.AppListContentProviderClient;
import com.commonsware.cwac.endless.EndlessAdapter;
import com.gc.android.market.api.MarketSession;
import com.gc.android.market.api.model.Market.App;

public class MarketSearchActivity extends ActionBarActivity implements LoaderCallbacks<String>{
	public static final String EXTRA_KEYWORD = "keyword";
	public static final String EXTRA_EXACT = "exact";
	public static final String EXTRA_SHARE = "share";

	private AppsAdapter mAdapter;
	private MarketSession mMarketSession;
	private AppIconLoader mIconLoader;
	private AppsResponseLoader mResponseLoader;
	private Context mContext;
	private LinearLayout mLoading;
	private RelativeLayout mDeviceIdMessage = null;
	private ListView mListView;
	private EditText mSearchEdit;
	private boolean mFirstTimeRun = false; 
	private boolean mShareSource = false;

	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.market_search);
		mContext = (Context)this;

		mLoading = (LinearLayout)findViewById(R.id.loading);
		mLoading.setVisibility(View.GONE);
		
		final Preferences prefs = new Preferences(this);
		String deviceId = DeviceIdHelper.getDeviceId(this,prefs);
		MarketSessionHelper helper = new MarketSessionHelper(mContext);
		mMarketSession = helper.create(deviceId, null);
        
		mIconLoader = new AppIconLoader(mMarketSession);
		mAdapter = new AppsAdapter(this,R.layout.market_app_row);
		
		mListView = (ListView)findViewById(android.R.id.list);
		mListView.setEmptyView(findViewById(android.R.id.empty));
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(itemClickListener); 

		getActionBarHelper().setActionBarCustomView(R.layout.searchbox);
		mSearchEdit = (EditText)getActionBarHelper().getCustomView();
		mSearchEdit.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
	            searchResults();
	            return true;
			}
		});
		
		initFromIntent(getIntent());
		
		getSupportLoaderManager().initLoader(0, null, this).forceLoad();
	}

	private void searchResults() {
		String query = mSearchEdit.getText().toString();
		
        // hide virtual keyboard
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mSearchEdit.getWindowToken(), 0);

        mListView.setAdapter(mAdapter);
		
		mAdapter.clear();
		mIconLoader.clearCache();

		mListView.setVisibility(View.GONE);
		mListView.getEmptyView().setVisibility(View.GONE);		
		
		mLoading.setVisibility(View.VISIBLE);
		if (query.length() > 0) {
			mResponseLoader = new AppsResponseLoader(mMarketSession, query);
			new RetreiveResultsTask().execute();
		} else {
			showNoResults("");
		}
	}
	
	private void showNoResults(String query) {
		mLoading.setVisibility(View.GONE);
		String noResStr = (query.length() > 0) ? getString(R.string.no_result_found, query) : getString(R.string.search_for_app);
		TextView tv = (TextView)mListView.getEmptyView();
		tv.setText(noResStr);
		tv.setVisibility(View.VISIBLE);
		showDeviceIdMessage();
	}
	
	private void showDeviceIdMessage() {
		if (mDeviceIdMessage!=null) {
			Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.flyin);
			mDeviceIdMessage.setAnimation(anim);
	        anim.start();
	        mDeviceIdMessage.setVisibility(View.VISIBLE);
		}
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.searchbox, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_search:
        	searchResults();
        	return true;        	
        default:
            return onOptionsItemSelected(item);
        }
    }    

    final OnItemClickListener itemClickListener  = new OnItemClickListener() {
		@Override
		public void onItemClick (AdapterView<?> parent, View view, int position, long id) {
			App app = mAdapter.getItem(position);
			Bitmap icon = mIconLoader.getCachedImage(app.getId());
			AppInfo info = new AppInfo(app, icon);
			AppListContentProviderClient cr = new AppListContentProviderClient(MarketSearchActivity.this);
			Uri uri = cr.insert(info);
			cr.release();
			
			if (uri == null) {
				Toast.makeText(mContext, R.string.error_insert_app, Toast.LENGTH_SHORT).show();
			} else {
				if (mShareSource) {
					String msg = getString(R.string.app_stored, app.getTitle());
					Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
				}
				finish();
			}
		}
	};

	class RetreiveResultsTask extends AsyncTask<String, Void, List<App>> {
		protected List<App> doInBackground(String... queries) {
			List<App> apps = mResponseLoader.load();
			if (apps != null) {
				for (App app : apps) {
					mIconLoader.precacheIcon(app.getId());
				}
			}
			return apps;
		}

		@Override
		protected void onPostExecute(List<App> list) {
			if (list == null || list.size() == 0) {
				showNoResults(mResponseLoader.getQuery());
				return;
			}
			mLoading.setVisibility(View.GONE);
			adapterAddAll(mAdapter, list);

			if (mResponseLoader.hasNext()) {
				mListView.setAdapter(new AppsEndlessAdapter(mContext, mAdapter, R.layout.pending));
			}

			showDeviceIdMessage();
		}
	};

	@SuppressLint("NewApi")
	private void adapterAddAll(ArrayAdapter<App> adapter, List<App> list) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			adapter.addAll(list);
		} else {
			for (App app : list) {
				adapter.add(app);
			}
		}
	}

	class AppsEndlessAdapter extends EndlessAdapter {

		private List<App> mCache;

		public AppsEndlessAdapter(Context context, ListAdapter wrapped, int pendingResource) {
			super(context, wrapped, pendingResource);
		}

		@Override
		protected boolean cacheInBackground() throws Exception {
			if (mResponseLoader.moveToNext()) {
				mCache = mResponseLoader.load();
				return (mCache == null || mCache.size() == 0) ? false : true;
			}
			return false;
		}

		@Override
		protected void appendCachedData() {
			if (mCache != null) {
				AppsAdapter adapter = (AppsAdapter) getWrappedAdapter();
				adapterAddAll(adapter, mCache);
			}
		}

	}

	class AppsAdapter extends ArrayAdapter<App> {
		private Bitmap mDefaultIcon;

		public AppsAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
		}

		class ViewHolder {
			TextView title;
			TextView details;
			ImageView icon;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.market_app_row, null);
				holder = new ViewHolder();
				holder.title = (TextView) v.findViewById(R.id.title);
				holder.details = (TextView) v.findViewById(R.id.details);
				v.setTag(holder);
			} else {
				holder = (ViewHolder) v.getTag();
			}
			App app = (App) getItem(position);
			holder.title.setText(app.getTitle() + " " + app.getVersion());
			holder.details.setText(app.getCreator());
			ImageView icon = (ImageView) v.findViewById(R.id.icon);
			if (mDefaultIcon == null) {
				mDefaultIcon = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_empty);
			}
			icon.setImageBitmap(mDefaultIcon);
			mIconLoader.loadImage(app.getId(), icon);

			return v;
		}

	}

	@Override
	public Loader<String> onCreateLoader(int id, Bundle args) {
		return new MarketTokenLoader(this);
	}

	@Override
	public void onLoadFinished(Loader<String> loader, String authSubToken) {
		if (authSubToken == null) {
			Toast.makeText(this, R.string.failed_gain_access, Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		mMarketSession.setAuthSubToken(authSubToken);
		if (mFirstTimeRun && !TextUtils.isEmpty(mSearchEdit.getText())) {
			searchResults();
		} else {
			// hide virtual keyboard
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(mSearchEdit, 0);
		}
	}

	private void initFromIntent(Intent i) {
		if (i == null) {
			return;
		}
		String keyword = i.getStringExtra(EXTRA_KEYWORD);
		if (keyword != null) {
			mSearchEdit.setText(keyword);
		}
		mFirstTimeRun = i.getBooleanExtra(EXTRA_EXACT, false);
		mShareSource = i.getBooleanExtra(EXTRA_SHARE, false);
	}

	@Override
	public void onLoaderReset(Loader<String> loader) {
		// TODO Auto-generated method stub
	}

}
