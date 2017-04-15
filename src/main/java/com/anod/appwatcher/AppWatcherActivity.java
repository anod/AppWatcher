package com.anod.appwatcher;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.SearchView;
import android.widget.TextView;

import com.anod.appwatcher.fragments.AppWatcherListFragment;
import com.anod.appwatcher.installed.InstalledSectionProvider;
import com.anod.appwatcher.model.Filters;
import com.anod.appwatcher.sync.SyncScheduler;
import com.anod.appwatcher.ui.AppWatcherBaseActivity;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.metrics.MetricsManager;
import net.hockeyapp.android.utils.Util;

import info.anodsplace.android.log.AppLog;

public class AppWatcherActivity extends AppWatcherBaseActivity implements
        TextView.OnEditorActionListener, SearchView.OnQueryTextListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
        MetricsManager.register(getApplication());

        if (mPreferences.useAutoSync())
        {
            SyncScheduler.schedule(this, mPreferences.isRequiresCharging());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        AppLog.d("Mark updates as viewed.");
        mPreferences.markViewed(true);
        CrashManager.register(this, Util.getAppIdentifier(this), App.provide(this).crashListener());
    }

    protected @LayoutRes int getContentLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected int getMenuResource() {
        return R.menu.main;
    }

    protected AppWatcherBaseActivity.Adapter createViewPagerAdapter() {
        AppWatcherBaseActivity.Adapter adapter = new AppWatcherBaseActivity.Adapter(getSupportFragmentManager());
        adapter.addFragment(AppWatcherListFragment.newInstance(
                Filters.TAB_ALL,
                mPreferences.getSortIndex(),
                new AppWatcherListFragment.DefaultSection(),
                null), getString(R.string.tab_all));
        adapter.addFragment(AppWatcherListFragment.newInstance(
                Filters.TAB_INSTALLED,
                mPreferences.getSortIndex(),
                new InstalledSectionProvider(),
                null), getString(R.string.tab_installed));
        adapter.addFragment(AppWatcherListFragment.newInstance(
                Filters.TAB_UNINSTALLED,
                mPreferences.getSortIndex(),
                new AppWatcherListFragment.DefaultSection(),
                null), getString(R.string.tab_not_installed));
        return adapter;
    }
}
