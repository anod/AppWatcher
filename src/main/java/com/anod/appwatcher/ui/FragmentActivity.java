package com.anod.appwatcher.ui;

import android.accounts.Account;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.anod.appwatcher.R;
import com.anod.appwatcher.accounts.AccountChooserHelper;
import com.anod.appwatcher.wishlist.WishlistFragment;

import java.util.List;

/**
 * @author algavris
 * @date 16/12/2016.
 */

public class FragmentActivity extends DrawerActivity {

    private static final String EXTRA_FRAGMENT = "extra_fragment";

    static Intent intent(String fragmentTag, Context context)
    {
        Intent intent = new Intent(context, FragmentActivity.class);
        intent.putExtra(EXTRA_FRAGMENT, fragmentTag);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        setupDrawer();
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);

        if (savedInstanceState == null) {
            String fragmentTag = getIntent().getStringExtra(EXTRA_FRAGMENT);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.activity_content, createFragment(fragmentTag), fragmentTag)
                    .commit();
        }
    }

    private Fragment createFragment(String fragmentTag) {
        if (WishlistFragment.TAG.equals(fragmentTag))
        {
            return new WishlistFragment();
        }
        return null;
    }

    @Override
    public void onHelperAccountSelected(Account account, String authToken) {
        super.onHelperAccountSelected(account, authToken);
        List<Fragment> list = getSupportFragmentManager().getFragments();
        if (list == null) {
            return;
        }
        for (Fragment f: list) {
            if (f instanceof AccountChooserHelper.OnAccountSelectionListener)
            {
                ((AccountChooserHelper.OnAccountSelectionListener) f).onHelperAccountSelected(account, authToken);
            }
        }
    }

    @Override
    public void onHelperAccountNotFound() {
        super.onHelperAccountNotFound();
        List<Fragment> list = getSupportFragmentManager().getFragments();
        if (list == null) {
            return;
        }
        for (Fragment f: list) {
            if (f instanceof AccountChooserHelper.OnAccountSelectionListener)
            {
                ((AccountChooserHelper.OnAccountSelectionListener) f).onHelperAccountNotFound();
            }
        }
    }
}
