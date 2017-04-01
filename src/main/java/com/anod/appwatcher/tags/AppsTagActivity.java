package com.anod.appwatcher.tags;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.anod.appwatcher.R;
import com.anod.appwatcher.fragments.AppWatcherListFragment;
import com.anod.appwatcher.installed.InstalledSectionProvider;
import com.anod.appwatcher.model.Filters;
import com.anod.appwatcher.model.Tag;
import com.anod.appwatcher.ui.AppWatcherBaseActivity;

/**
 * @author algavris
 * @date 18/03/2017.
 */

public class AppsTagActivity extends AppWatcherBaseActivity {
    public static final String EXTRA_TAG = "extra_tag";

    private Tag mTag;

    public static Intent createTagIntent(Tag tag, Context context) {
        Intent intent = new Intent(context, AppsTagActivity.class);
        intent.putExtra(EXTRA_TAG, tag);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mTag = getIntentExtras().getParcelable(EXTRA_TAG);
        assert mTag != null;
        super.onCreate(savedInstanceState);

        setTitle(mTag.name);
    }

    @Override
    protected int getContentLayout() {
        return R.layout.activity_main;
    }

    protected AppWatcherBaseActivity.Adapter createViewPagerAdapter() {
        AppWatcherBaseActivity.Adapter adapter = new AppWatcherBaseActivity.Adapter(getSupportFragmentManager());
        adapter.addFragment(AppsTagListFragment.newInstance(
                Filters.TAB_ALL,
                mPreferences.getSortIndex(),
                new AppsTagListFragment.DefaultSection(),
                mTag), getString(R.string.tab_all));
        adapter.addFragment(AppsTagListFragment.newInstance(
                Filters.TAB_INSTALLED,
                mPreferences.getSortIndex(),
                new AppsTagListFragment.DefaultSection(),
                mTag), getString(R.string.tab_installed));
        adapter.addFragment(AppsTagListFragment.newInstance(
                Filters.TAB_UNINSTALLED,
                mPreferences.getSortIndex(),
                new AppsTagListFragment.DefaultSection(),
                mTag), getString(R.string.tab_not_installed));
        return adapter;
    }

    @Override
    protected boolean isDrawerEnabled() {
        return false;
    }
}
