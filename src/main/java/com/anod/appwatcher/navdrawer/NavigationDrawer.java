package com.anod.appwatcher.navdrawer;

import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.anod.appwatcher.R;
import com.anod.appwatcher.navdrawer.list.Factory;
import com.anod.appwatcher.navdrawer.list.Main;

import butterknife.ButterKnife;
import butterknife.InjectView;


/**
 * @author alex
 * @date 2014-10-20
 */
public class NavigationDrawer implements AdapterView.OnItemClickListener {
    private final ActionBarDrawerToggle mDrawerToggle;
    private final CharSequence mTitle;
    private final CharSequence mDrawerTitle;
    private final AppCompatActivity mContext;
    private final Listener mListener;
    @InjectView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @InjectView(R.id.navdrawer_items_list)
    ListView mItemsView;
    private BaseAdapter mAdapter;

    public interface Listener {
        void onDrawerItemClick(int listType, Item item);
    }

    public NavigationDrawer(final AppCompatActivity activity, Listener listener) {
        ButterKnife.inject(this,activity);

        mTitle = mDrawerTitle = activity.getTitle();

        mListener = listener;
        mContext = activity;
        mAdapter = new Main(mContext);
        ( (DrawerAdapter)mAdapter).init();
        mItemsView.setAdapter(mAdapter);
        mItemsView.setOnItemClickListener(this);

        mDrawerToggle = new ActionBarDrawerToggle(activity, mDrawerLayout, R.string.drawer_open, R.string.drawer_close)
        {

            // Called when a drawer has settled in a completely closed state.
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                activity.getSupportActionBar().setTitle(mTitle);
                activity.invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            // Called when a drawer has settled in a completely open state.
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                activity.getSupportActionBar().setTitle(mDrawerTitle);
                activity.invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setHomeButtonEnabled(true);
    }

    public void syncState() {
        mDrawerToggle.syncState();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item);
    }

    public void refresh() {
        mAdapter = new Main(mContext);
        mItemsView.setAdapter(mAdapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DrawerAdapter adapter = (DrawerAdapter) mItemsView.getAdapter();
        Item item = adapter.getItem(position);
        if (item.isNav) {
            mAdapter = Factory.create(item.id, mContext);
            ((DrawerAdapter)mAdapter).init();
            setAdapter(mAdapter);
        } else {
            mListener.onDrawerItemClick(adapter.getListType(), item);
        }
    }

    private void setAdapter(BaseAdapter baseAdapter) {
        mAdapter = baseAdapter;
        mItemsView.setAdapter(mAdapter);
    }
}
