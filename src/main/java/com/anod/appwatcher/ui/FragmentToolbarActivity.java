package com.anod.appwatcher.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.anod.appwatcher.R;
import com.anod.appwatcher.wishlist.WishlistFragment;

import info.anodsplace.android.log.AppLog;

/**
 * @author algavris
 * @date 16/12/2016.
 */

public class FragmentToolbarActivity extends ToolbarActivity {

    private static final String EXTRA_FRAGMENT = "extra_fragment";
    private static final String EXTRA_ARGUMENTS = "extra_arguments";

    static Intent intent(String fragmentTag, Bundle args, Context context)
    {
        Intent intent = new Intent(context, FragmentToolbarActivity.class);
        intent.putExtra(EXTRA_FRAGMENT, fragmentTag);
        intent.putExtra(EXTRA_ARGUMENTS, args);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        setupToolbar();

        if (savedInstanceState == null) {
            String fragmentTag = getIntent().getStringExtra(EXTRA_FRAGMENT);
            Fragment f = createFragment(fragmentTag);
            if (f == null)
            {
                AppLog.e("Missing fragment for tag: " + fragmentTag);
                finish();
                return;
            }
            f.setArguments(getIntent().getBundleExtra(EXTRA_ARGUMENTS));

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.activity_content, f, fragmentTag)
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

}
