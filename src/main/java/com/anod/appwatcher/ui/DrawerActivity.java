package com.anod.appwatcher.ui;

import android.accounts.Account;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.anod.appwatcher.R;

/**
 * @author alex
 * @date 2014-08-07
 */
abstract public class DrawerActivity extends ToolbarActivity {
    DrawerLayout mDrawerLayout;
    private TextView mAccountNameView;

    protected void setupDrawer() {
        setupToolbar();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }
    }

    private void setupDrawerContent(NavigationView navigationView) {

        mAccountNameView = ((TextView)navigationView.findViewById(R.id.account_name));

        mAccountNameView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAccountChooseClick();
            }
        });

        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }

    protected abstract void onAccountChooseClick();

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    protected void setDrawerAccount(Account account) {
        if (account == null) {
            mAccountNameView.setText(R.string.choose_an_account);
        } else {
            mAccountNameView.setText(account.name);
        }
    }
}
