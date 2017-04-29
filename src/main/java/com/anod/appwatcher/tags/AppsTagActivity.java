package com.anod.appwatcher.tags;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v7.graphics.Palette;
import android.view.MenuItem;

import com.anod.appwatcher.R;
import com.anod.appwatcher.model.Filters;
import com.anod.appwatcher.model.Tag;
import com.anod.appwatcher.ui.AppWatcherBaseActivity;

import java.util.ArrayList;

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

        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        appBarLayout.setBackgroundColor(mTag.color);

        float[] hsv = new float[3];
        Color.colorToHSV(mTag.color, hsv);
        hsv[2] *= 0.6f;
        int dark = Color.HSVToColor(hsv);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setSelectedTabIndicatorColor(dark);

        setTitle(mTag.name);
    }

    @Override
    protected int getContentLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected int getMenuResource() {
        return R.menu.main_tag;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_act_addtag)
        {
            startActivity(AppsTagSelectActivity.createIntent(mTag, this));
            return true;
        }
        return super.onOptionsItemSelected(item);
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
