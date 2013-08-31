package com.anod.appwatcher.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.app.ShareCompat.IntentBuilder;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.anod.appwatcher.AppWatcherActivity;
import com.anod.appwatcher.ChangelogActivity;
import com.anod.appwatcher.R;
import com.anod.appwatcher.market.MarketInfo;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.model.AppListCursor;
import com.anod.appwatcher.model.AppListCursorLoader;
import com.anod.appwatcher.utils.AppLog;
import com.anod.appwatcher.utils.IntentUtils;

import java.sql.Timestamp;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshAttacher;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;

public class AppWatcherListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>,AppWatcherActivity.QueryChangeListener,PullToRefreshAttacher.OnRefreshListener, AppWatcherActivity.RefreshListener {
    
    private CursorAdapter mAdapter;
	private int mNewAppsCount;
	private int mTotalCount;
	private String mCurFilter = "";

	public ListView mList;
	private boolean mListShown;
	private View mProgressContainer;
	private View mListContainer;

	class ViewHolder {
		AppInfo app;
		int position;
		View section;
		TextView sectionText;
		TextView sectionCount;
		TextView title;
		TextView details;
		TextView version;
		TextView price;
		ImageView icon;
		LinearLayout newIndicator;
		LinearLayout options;
		ImageButton removeBtn;
		Button marketBtn;
		Button changelogBtn;
		ImageButton shareBtn;
		TextView updateDate;
	}
	private ViewHolder mSelectedHolder = null;
	private Animation mAnimSlideOut;
	private boolean mIsBigScreen;
	private PackageManager mPackageManager;
	private SparseBooleanArray mInstalledCache = new SparseBooleanArray();

	private PullToRefreshAttacher mPullToRefreshAttacher;

	/** Called when the activity is first created. */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

	public void setListShown(boolean shown, boolean animate){
		if (mListShown == shown) {
			return;
		}
		mListShown = shown;
		if (shown) {
			if (animate) {
				mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
						getActivity(), android.R.anim.fade_out));
				mListContainer.startAnimation(AnimationUtils.loadAnimation(
						getActivity(), android.R.anim.fade_in));
			}
			mProgressContainer.setVisibility(View.GONE);
			mListContainer.setVisibility(View.VISIBLE);
		} else {
			if (animate) {
				mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
						getActivity(), android.R.anim.fade_in));
				mListContainer.startAnimation(AnimationUtils.loadAnimation(
						getActivity(), android.R.anim.fade_out));
			}
			mProgressContainer.setVisibility(View.VISIBLE);
			mListContainer.setVisibility(View.INVISIBLE);
		}
	}
	public void setListShown(boolean shown){
		setListShown(shown, true);
	}
	public void setListShownNoAnimation(boolean shown) {
		setListShown(shown, false);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		int INTERNAL_EMPTY_ID = 0x00ff0001;
		View root = inflater.inflate(R.layout.applist_fragment, container, false);
		(root.findViewById(R.id.internalEmpty)).setId(INTERNAL_EMPTY_ID);
		mList = (ListView) root.findViewById(android.R.id.list);
		mListContainer =  root.findViewById(R.id.listContainer);
		mProgressContainer = root.findViewById(R.id.progressContainer);
		mListShown = true;
		return root;
	}


	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// We have a menu item to show in action bar.
		setHasOptionsMenu(true);

		// Create an empty adapter we will use to display the loaded data.
		mAdapter = new ListCursorAdapter(getActivity(), null, 0);
		setListAdapter(mAdapter);

		Resources r = getResources();
		mIsBigScreen = r.getBoolean(R.bool.is_large_screen);
		// Start out with a progress indicator.
		setListShown(false);

		mAnimSlideOut = AnimationUtils.loadAnimation(getActivity(), R.anim.slideout);

		mPackageManager = getActivity().getPackageManager();

		AppWatcherActivity act = (AppWatcherActivity)getActivity();

		act.setQueryChangeListener(this);
		act.setRefreshListener(this);

		mPullToRefreshAttacher = act.getPullToRefreshAttacher();

		// Retrieve the PullToRefreshLayout from the content view
		PullToRefreshLayout ptrLayout = (PullToRefreshLayout) view.findViewById(R.id.ptr_layout);

		// Give the PullToRefreshAttacher to the PullToRefreshLayout, along with a refresh listener.
		ptrLayout.setPullToRefreshAttacher(mPullToRefreshAttacher, this);

		// Set the Refreshable View to be the ListView and the refresh listener to be this.
		//mPullToRefreshAttacher.addRefreshableView(getListView(), this);

		// Prepare the loader.  Either re-connect with an existing one,
		// or start a new one.
		getLoaderManager().initLoader(0, null, this);
	}

	private class ListCursorAdapter extends CursorAdapter {
		private LayoutInflater mInflater;
        private Bitmap mDefaultIcon;
		private String mVersionText;
		private String mUpdateText;
		private int mDefColor;
		private int mUpdateTextColor;
		private java.text.DateFormat mDateFormat;
		private Timestamp mTimestamp;

		public ListCursorAdapter(Context context, Cursor c, int flags) {
			super(context, c, flags);
	        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        Resources r = context.getResources();
	        mVersionText = r.getString(R.string.version);
	        mUpdateText = r.getString(R.string.update);
	        mUpdateTextColor = r.getColor(R.color.blue_new);
			mDateFormat = android.text.format.DateFormat
					.getMediumDateFormat(context.getApplicationContext());
            mTimestamp = new Timestamp(System.currentTimeMillis());

		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			AppListCursor wrapper = (AppListCursor)cursor;
			AppInfo app = wrapper.getAppInfo();
			boolean hide = false;
            if (mSelectedHolder != null && mSelectedHolder.app.getRowId() != app.getRowId()) {
            	hide = true;
            }
			ViewHolder holder = (ViewHolder)view.getTag();
			holder.position = cursor.getPosition();
			holder.app = app;
            holder.title.setText(app.getTitle());
            holder.details.setText(app.getCreator());
			holder.removeBtn.setTag(app);
			holder.marketBtn.setTag(app.getPackageName());
			holder.changelogBtn.setTag(app.getAppId());
			holder.shareBtn.setTag(app);
			holder.icon.setTag(holder);
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
				holder.newIndicator.setVisibility(View.INVISIBLE);
			}
			
			boolean isInstalled = isAppInstalled(app);
			if (isInstalled) {
				holder.price.setText(R.string.installed);
				holder.price.setVisibility(View.VISIBLE);
			} else {
				holder.price.setText("Price");
				holder.price.setVisibility(View.INVISIBLE);
			}

			if (mNewAppsCount > 0 && TextUtils.isEmpty(mCurFilter)) {
				if (holder.position == 0) {
					holder.sectionText.setText(context.getString(R.string.recently_updated));
					holder.sectionCount.setText(String.valueOf(mNewAppsCount));
					holder.section.setVisibility(View.VISIBLE);
				} else if (holder.position == mNewAppsCount) {
					holder.sectionText.setText(context.getString(R.string.watching));
					holder.sectionCount.setText(String.valueOf(mTotalCount - mNewAppsCount));
					holder.section.setVisibility(View.VISIBLE);
				} else {
					holder.section.setVisibility(View.GONE);
				}
			} else {
				holder.section.setVisibility(View.GONE);
			}
			
			long updateTime = app.getUpdateTime();
			
			if (updateTime > 0) {
				mTimestamp.setTime(updateTime);
				AppLog.d("Update time [" + updateTime + "]:" + mTimestamp.toString());
				holder.updateDate.setText(mDateFormat.format(mTimestamp));
				holder.updateDate.setVisibility(View.VISIBLE);
			} else {
				holder.updateDate.setVisibility(View.GONE);
			}
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View v = mInflater.inflate(R.layout.list_row, parent, false);
		    v.setClickable(true);
		    v.setFocusable(true);

		    ViewHolder holder = newViewHolder(v);
			v.setTag(holder);

			return v;
		}

		/**
		 * @param v
		 */
		private ViewHolder newViewHolder(View v) {
			ViewHolder holder = new ViewHolder();
			holder.app = null;
			holder.position = 0;
			holder.section = (View)v.findViewById(R.id.sec_header);
			holder.sectionText = (TextView)v.findViewById(R.id.sec_header_title);
			holder.sectionCount = (TextView)v.findViewById(R.id.sec_header_count);
            holder.title = (TextView)v.findViewById(R.id.title);
            holder.details = (TextView)v.findViewById(R.id.details);
            holder.icon = (ImageView)v.findViewById(R.id.app_icon);
            holder.version = (TextView)v.findViewById(R.id.version);
            holder.price = (TextView)v.findViewById(R.id.price);
            holder.options = (LinearLayout)v.findViewById(R.id.options);
            holder.newIndicator = (LinearLayout)v.findViewById(R.id.new_indicator);
            holder.updateDate = (TextView)v.findViewById(R.id.update_date);
            
            mDefColor = holder.version.getTextColors().getDefaultColor();            
            if (!mIsBigScreen) {
            	holder.options.setVisibility(View.GONE);
            	v.setOnClickListener(new OnClickListener() {
    				@Override
    				public void onClick(View v) {
    					onItemClick(v);
    				}
    			});
            } else {
            	v.setOnClickListener(new OnClickListener() {
    				@Override
    				public void onClick(View v) {
    					ViewHolder holder = (ViewHolder)v.getTag();
    					final String appId = holder.app.getAppId();
    					onChangelogClick(appId);
    				}
    			});
            }

            
            holder.removeBtn = (ImageButton)holder.options.findViewById(R.id.remove_btn);
            holder.removeBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					onRemoveClick(v);
				}
			});

            holder.marketBtn = (Button)holder.options.findViewById(R.id.market_btn);
            holder.marketBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					onPlayStoreClick(v);
				
				}
			});

            holder.changelogBtn = (Button)holder.options.findViewById(R.id.changelog_btn);
            holder.changelogBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					final String appId = (String)v.getTag();
					onChangelogClick(appId);
				}
			});

            holder.shareBtn = (ImageButton)holder.options.findViewById(R.id.share_btn);
            holder.shareBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					onShareClick(v);
				}

			});
            
            holder.icon.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					onIconClick(v);
				}
			});
            return holder;
		}
    }
    
	public void onItemClick(View v) {
		ViewHolder holder = (ViewHolder)v.getTag();
		switchItemOptions(holder);
	}

	/**
	 * @param holder
	 */
	public void switchItemOptions(ViewHolder holder) {
		if (mSelectedHolder == null) {
			expandItemOptions(holder);
			return;
		}
		if (mSelectedHolder.app.getRowId() == holder.app.getRowId()) {
			if (holder.options.getVisibility() == View.VISIBLE) {
				holder.options.setVisibility(View.GONE);
				mSelectedHolder = null;
			} else {
				holder.options.setVisibility(View.VISIBLE);
			}
		} else {
			//getListView().scr
			mSelectedHolder.options.setVisibility(View.GONE);
			expandItemOptions(holder);
		}
	}

	/**
	 * replace current selected holder
	 * and scroll down
	 * @param holder
	 */
	private void expandItemOptions(ViewHolder holder) {
		mSelectedHolder = holder;
		mSelectedHolder.options.startAnimation(mAnimSlideOut);
		mSelectedHolder.options.setVisibility(View.VISIBLE);
		
		int lastVisiblePos = getListView().getLastVisiblePosition();
		if (lastVisiblePos == holder.position
		 || lastVisiblePos == (holder.position + 1)) {
			getListView().smoothScrollToPosition(lastVisiblePos);
		}
	}

	private void onRemoveClick(View v) {
		AppInfo app = (AppInfo)v.getTag();
		RemoveDialogFragment removeDialog = RemoveDialogFragment.newInstance(
			app.getTitle(), app.getRowId()
		);
	    removeDialog.show(getFragmentManager(), "removeDialog");
	}
	
	private void onPlayStoreClick(View v) {
		String pkg = (String)v.getTag();
		Intent intent = IntentUtils.createPlayStoreIntent(pkg);
		startActivity(intent);
	}

	private void onShareClick(View v) {
		AppInfo app = (AppInfo)v.getTag();
		IntentBuilder builder = ShareCompat.IntentBuilder.from(getActivity());
		if (app.getStatus() == AppInfo.STATUS_UPDATED) {
			builder.setSubject(getString(R.string.share_subject_updated, app.getTitle()));
		} else {
			builder.setSubject(getString(R.string.share_subject_normal, app.getTitle()));
		}
		builder.setText(String.format(MarketInfo.URL_WEB_PLAY_STORE, app.getPackageName()));
		builder.setType("text/plain");
		builder.startChooser();
	}
	
	private void onChangelogClick(final String appId) {
		Intent intent = new Intent(getActivity(), ChangelogActivity.class);
		intent.putExtra(ChangelogActivity.EXTRA_APP_ID, appId);
		startActivity(intent);
	}
	
	/**
	 * 
	 * @param v
	 */
	private void onIconClick(View v) {
		ViewHolder holder = (ViewHolder)v.getTag();
		boolean isInstalled = isAppInstalled(holder.app);
		if (isInstalled) {
			Intent appInfo = IntentUtils.createApplicationDetailsIntent(holder.app.getPackageName());
			startActivity(appInfo);
		} else {
			switchItemOptions(holder);
		}
	}
	
	/**
	 * 
	 * @param app
	 * @return
	 */
	private boolean isAppInstalled(AppInfo app) {
		if (mInstalledCache.indexOfKey(app.getRowId()) >= 0) {
			return mInstalledCache.get(app.getRowId());
		}
		
		Intent appIntent = mPackageManager.getLaunchIntentForPackage(app.getPackageName());
		boolean isInstalled = (appIntent != null);
		mInstalledCache.put(app.getRowId(), isInstalled);
		return isInstalled;
	}
	
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new AppListCursorLoader(getActivity(), mCurFilter);
    }

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mAdapter.swapCursor(data);
		mNewAppsCount = ((AppListCursorLoader)loader).getNewCount();
		mTotalCount = data.getCount();
		
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

	@Override
	public void onQueryTextChanged(String newQuery) {
		String newFilter = !TextUtils.isEmpty(newQuery) ? newQuery : "";
		if (!TextUtils.equals(newFilter,mCurFilter)) {
			mCurFilter = newFilter;
			getLoaderManager().restartLoader(0, null, this);
		}
	}

	@Override
	public void onRefreshStarted(View view) {
		((AppWatcherActivity)getActivity()).requestRefresh();
	}

	@Override
	public void onRefreshFinish() {
		mPullToRefreshAttacher.setRefreshComplete();
	}
}
