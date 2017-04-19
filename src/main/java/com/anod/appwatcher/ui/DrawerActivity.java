package com.anod.appwatcher.ui;

import android.accounts.Account;
import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.anod.appwatcher.MarketSearchActivity;
import com.anod.appwatcher.Preferences;
import com.anod.appwatcher.R;
import com.anod.appwatcher.SettingsActivity;
import com.anod.appwatcher.accounts.AccountChooser;
import com.anod.appwatcher.content.DbContentProvider;
import com.anod.appwatcher.content.TagsContentProviderClient;
import com.anod.appwatcher.content.TagsCursor;
import com.anod.appwatcher.fragments.AccountChooserFragment;
import com.anod.appwatcher.installed.ImportInstalledActivity;
import com.anod.appwatcher.model.Tag;
import com.anod.appwatcher.tags.AppsTagActivity;
import com.anod.appwatcher.wishlist.WishlistFragment;

/**
 * @author alex
 * @date 2014-08-07
 */
abstract public class DrawerActivity extends ToolbarActivity implements AccountChooser.OnAccountSelectionListener {
    DrawerLayout mDrawerLayout;
    private TextView mAccountNameView;
    protected NavigationView mNavigationView;
    private AccountChooser mAccountChooser;
    protected Preferences mPreferences;
    private String mAuthToken;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = new Preferences(this);
        mAccountChooser = new AccountChooser(this, mPreferences, this);
        mAccountChooser.init();
    }

    protected void setupDrawer() {
        setupToolbar();

        if (isDrawerEnabled()) {
            mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            mNavigationView = (NavigationView) findViewById(R.id.nav_view);
            setupDrawerContent(mNavigationView);
            updateTags();
            getContentResolver().registerContentObserver(DbContentProvider.TAGS_CONTENT_URI, true, new TagsUpdateObserver(this));
        }
    }

    private void setupDrawerContent(NavigationView navigationView) {
        View headerView = navigationView.getHeaderView(0);

        mAccountNameView = ((TextView) headerView.findViewById(R.id.account_name));
        LinearLayout changeAccount = (LinearLayout) headerView.findViewById(R.id.account_change);
        changeAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAccountChooseClick();
            }
        });

        Preferences pref = new Preferences(this);
        long time = pref.getLastUpdateTime();

        TextView lastUpdateView = (TextView) headerView.findViewById(R.id.last_update);
        if (time > 0) {
            String lastUpdate = getString(R.string.last_update, DateUtils.getRelativeDateTimeString(this, time, DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0));
            lastUpdateView.setText(lastUpdate);
            lastUpdateView.setVisibility(View.VISIBLE);
        } else {
            lastUpdateView.setVisibility(View.GONE);/**/
        }

        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        mDrawerLayout.closeDrawers();
                        onOptionsItemSelected(menuItem);
                        return true;
                    }
                });

    }

    public void updateTags() {
        Menu menu = mNavigationView.getMenu();
        menu.removeGroup(1);

        TagsContentProviderClient tags = new TagsContentProviderClient(this);
        TagsCursor cr = tags.queryAll();
        if (cr == null || !cr.moveToFirst()) {
            return;
        }
        cr.moveToPosition(-1);

        while (cr.moveToNext()) {
            Tag tag = cr.getTag();
            MenuItem item = menu.add(1, tag.id, tag.id, tag.name);
            item.setActionView(R.layout.drawer_tag_indicator);
            ImageView iv = (ImageView) item.getActionView();
            Drawable d = ResourcesCompat.getDrawable(getResources(), R.drawable.circular_color, null);
            DrawableCompat.setTint(d, tag.color);
            iv.setImageDrawable(d);
            item.setIntent(AppsTagActivity.createTagIntent(tag, this));
        }
        cr.close();
    }

    @Override
    public AccountChooserFragment.OnAccountSelectionListener getAccountSelectionListener() {
        return mAccountChooser;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mAccountChooser.onRequestPermissionResult(requestCode, permissions, grantResults);
    }

    protected void onAccountChooseClick() {
        mAccountChooser.showAccountsDialogWithCheck();
    }

    @Override
    public void onAccountSelected(Account account, String authToken) {
        mAuthToken = authToken;
        if (authToken == null) {
            Toast.makeText(this, R.string.failed_gain_access, Toast.LENGTH_LONG).show();
            return;
        }

        if (isDrawerEnabled()) {
            setDrawerAccount(account);
        }
    }

    @Override
    public void onAccountNotFound() {
        Toast.makeText(this, R.string.failed_gain_access, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (isDrawerEnabled()) {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                    return true;
                }
                return super.onOptionsItemSelected(item);
            case R.id.menu_add:
                Intent addActivity = new Intent(this, MarketSearchActivity.class);
                startActivity(addActivity);
                return true;
            case R.id.menu_settings:
                Intent settingsActivity = new Intent(this, SettingsActivity.class);
                startActivity(settingsActivity);
                return true;
            case R.id.menu_act_import:
                startActivity(new Intent(this, ImportInstalledActivity.class));
                return true;
            case R.id.menu_wishlist:
                Bundle args = new Bundle();
                args.putParcelable(WishlistFragment.EXTRA_ACCOUNT, mAccountChooser.getAccount());
                args.putString(WishlistFragment.EXTRA_AUTH_TOKEN, mAuthToken);
                startActivity(FragmentToolbarActivity.intent(WishlistFragment.TAG, args, this));
                return true;
        }
        Intent intent = item.getIntent();
        if (intent != null) {
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected boolean isDrawerEnabled() {
        return true;
    }

    protected void setDrawerAccount(Account account) {
        if (account == null) {
            mAccountNameView.setText(R.string.choose_an_account);
        } else {
            mAccountNameView.setText(account.name);
        }
    }

    public boolean isAuthenticated() {
        return mAuthToken != null;
    }

    public void showAccountsDialogWithCheck() {
        Toast.makeText(this, R.string.failed_gain_access, Toast.LENGTH_LONG).show();
        mAccountChooser.showAccountsDialogWithCheck();
    }

    static class TagsUpdateObserver extends ContentObserver {
        private final DrawerActivity mDrawerActivity;

        TagsUpdateObserver(DrawerActivity drawerActivity) {
            super(new Handler());
            mDrawerActivity = drawerActivity;
        }

        @Override
        public void onChange(boolean selfChange) {
            mDrawerActivity.updateTags();
        }
    }
}
