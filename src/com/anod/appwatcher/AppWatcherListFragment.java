package com.anod.appwatcher;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.anod.appwatcher.accounts.MarketTokenHelper;
import com.anod.appwatcher.accounts.MarketTokenHelper.CallBack;
import com.anod.appwatcher.market.MarketSessionHelper;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.model.AppListCursor;
import com.anod.appwatcher.model.AppListTable;

public class AppWatcherListFragment extends SherlockListFragment implements LoaderManager.LoaderCallbacks<Cursor>{
    private CursorAdapter mAdapter;
	class ViewHolder {
		int rowId;
		String appId;
		TextView title;
		TextView details;
		TextView version;
		ImageView icon;
		LinearLayout newIndicator;
		LinearLayout options;
		Button removeBtn;
		Button marketBtn;
		Button changelogBtn;
	}
	private ViewHolder mSelectedHolder = null;
	private Animation mAnimSlideOut;
	
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

        getListView().setItemsCanFocus(true);
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        
        // Start out with a progress indicator.
        setListShown(false);        
        
        mAnimSlideOut = AnimationUtils.loadAnimation(getActivity(), R.anim.slideout);
        
        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(0, null, this);

    }

    
    /* (non-Javadoc)
	 * @see android.support.v4.app.ListFragment#onListItemClick(android.widget.ListView, android.view.View, int, long)
	 */
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		ViewHolder holder = (ViewHolder)v.getTag();
		if (mSelectedHolder == null) {
			mSelectedHolder = holder;
			mSelectedHolder.options.startAnimation(mAnimSlideOut);
			mSelectedHolder.options.setVisibility(View.VISIBLE);
			return;
		}
		if (mSelectedHolder.rowId == holder.rowId) {
			if (holder.options.getVisibility() == View.VISIBLE) {
				holder.options.setVisibility(View.GONE);
				mSelectedHolder = null;
			} else {
				holder.options.setVisibility(View.VISIBLE);
			}
		} else {
			mSelectedHolder.options.setVisibility(View.GONE);
			mSelectedHolder = holder;
			holder.options.startAnimation(mAnimSlideOut);			
			holder.options.setVisibility(View.VISIBLE);
		}
	}


	private class ListCursorAdapter extends CursorAdapter {
        private static final String URL_PLAY_STORE = "market://details?id=%s";
		private LayoutInflater mInflater;
        private Bitmap mDefaultIcon;
		private String mVersionText;
		private String mUpdateText;
		private int mDefColor;
		private int mUpdateTextColor;

		public ListCursorAdapter(Context context, Cursor c, int flags) {
			super(context, c, flags);
	        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        Resources r = getResources();
	        mVersionText = r.getString(R.string.version);
	        mUpdateText = r.getString(R.string.update);
	        mUpdateTextColor = r.getColor(R.color.blue_new);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			AppListCursor wrapper = new AppListCursor(cursor);
			AppInfo app = wrapper.getAppInfo();
			boolean hide = false;
            if (mSelectedHolder != null && mSelectedHolder.rowId != app.getRowId()) {
            	hide = true;
            }			
			ViewHolder holder = (ViewHolder)view.getTag();
			holder.rowId = app.getRowId();		
            holder.title.setText(app.getTitle());
            holder.details.setText(app.getCreator());
			holder.removeBtn.setTag(app);
			holder.marketBtn.setTag(app.getPackageName());
			holder.changelogBtn.setTag(app.getAppId());
			Bitmap icon = app.getIcon();
            if (icon == null) {
	           	if (mDefaultIcon == null) {
	           		mDefaultIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_empty);
	           	}
	           	icon = mDefaultIcon;
            }
            if (hide) {
            	holder.options.setVisibility(View.GONE);
            }
            holder.icon.setImageBitmap(icon);
            if (app.getStatus() == AppInfo.STATUS_UPDATED) {
                holder.version.setText(String.format(mUpdateText, app.getVersionName()));
                holder.version.setTextColor(mUpdateTextColor);
            	holder.newIndicator.setVisibility(View.VISIBLE);
            } else {
                holder.version.setText(String.format(mVersionText, app.getVersionName()));
                holder.version.setTextColor(mDefColor);
              
                //TextView.getTextColors().getDefaultColor()
            	holder.newIndicator.setVisibility(View.INVISIBLE);

            }
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View v = mInflater.inflate(R.layout.list_row, parent, false);
			ViewHolder holder = new ViewHolder();
			holder.rowId = -1;
            holder.title = (TextView)v.findViewById(R.id.title);
            holder.details = (TextView)v.findViewById(R.id.details);
            holder.icon = (ImageView)v.findViewById(R.id.icon);
            holder.version = (TextView)v.findViewById(R.id.version);
            mDefColor = holder.version.getTextColors().getDefaultColor();            
            holder.options = (LinearLayout)v.findViewById(R.id.options);
            holder.newIndicator = (LinearLayout)v.findViewById(R.id.new_indicator);
            holder.options.setVisibility(View.GONE);
            v.setTag(holder);
            
            holder.removeBtn = (Button)holder.options.findViewById(R.id.remove_btn);
            holder.removeBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					AppInfo app = (AppInfo)v.getTag();
					RemoveDialogFragment removeDialog = RemoveDialogFragment.newInstance(
						app.getTitle(), app.getRowId()
					);
				    removeDialog.show(getFragmentManager(), "removeDialog");
				}
			});

            holder.marketBtn = (Button)holder.options.findViewById(R.id.market_btn);
            holder.marketBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					String pkg = (String)v.getTag();
					String url = String.format(URL_PLAY_STORE, pkg);
					Intent intent = new Intent (Intent.ACTION_VIEW, Uri.parse(url));
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);				
					startActivity(intent);
				
				}
			});

            holder.changelogBtn = (Button)holder.options.findViewById(R.id.changelog_btn);
            holder.changelogBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					final String appId = (String)v.getTag();
		        	MarketTokenHelper helper = new MarketTokenHelper(getActivity(), true, new CallBack() {
						@Override
						public void onTokenReceive(String authToken) {
				        	if (authToken == null) {
				        		Toast.makeText(getActivity(), R.string.failed_gain_access, Toast.LENGTH_LONG).show();
				        	} else {
								Intent intent = new Intent(getActivity(), AppChangelogActivity.class);
								intent.putExtra(AppChangelogActivity.EXTRA_APP_ID, appId);
								intent.putExtra(MarketSessionHelper.EXTRA_TOKEN, authToken);
				        		startActivity(intent);
				        	}
						}
					});
		        	helper.requestToken();
				}
			});

			return v;
		}
    	
    }
    
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), AppListContentProvider.CONTENT_URI,
        		AppListTable.APPLIST_PROJECTION, null, null,
        		AppListTable.Columns.KEY_STATUS + " DESC, " +AppListTable.Columns.KEY_TITLE + " COLLATE LOCALIZED ASC");
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