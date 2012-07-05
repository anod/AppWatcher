/*
 * Copyright 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.anod.appwatcher.actionbarcompat;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.anod.appwatcher.R;

/**
 * An extension of {@link ActionBarHelper} that provides Android 3.0-specific functionality for
 * Honeycomb tablets. It thus requires API level 11.
 */
@TargetApi(11)
public class ActionBarHelperHoneycomb extends ActionBarHelper {
    private Menu mOptionsMenu;
	private Animation mAnimRotation;
	private ImageView mRefreshView;
    
    protected ActionBarHelperHoneycomb(Activity activity) {
        super(activity);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mOptionsMenu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void setRefreshActionItemState(boolean refreshing) {
        // On Honeycomb, we can set the state of the refresh button by giving it a custom
        // action view.
        if (mOptionsMenu == null) {
            return;
        }

        final MenuItem refreshItem = mOptionsMenu.findItem(R.id.menu_refresh);
        if (refreshItem != null) {
            if (refreshing) {
                if (mRefreshView == null) {
            	    mAnimRotation = AnimationUtils.loadAnimation(mActivity, R.anim.rotate);
            	    mAnimRotation.setRepeatCount(Animation.INFINITE);

            	    LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            	    mRefreshView = (ImageView) inflater.inflate(R.layout.refresh_action_view, null);
                }

                refreshItem.setActionView(mRefreshView);
                mRefreshView.startAnimation(mAnimRotation);
            } else {
            	if (mRefreshView != null) {
            		 mRefreshView.clearAnimation();
            	}
                refreshItem.setActionView(null);
            }
        }
    }

    /**
     * Returns a {@link Context} suitable for inflating layouts for the action bar. The
     * implementation for this method in {@link ActionBarHelperICS} asks the action bar for a
     * themed context.
     */
    protected Context getActionBarThemedContext() {
        return mActivity;
    }
    
    @Override
    public void setActionBarCustomView(int resource) {
		ActionBar bar = mActivity.getActionBar();
		bar.setCustomView(resource);
		bar.setDisplayShowCustomEnabled(true);
    }
    
    @Override
	public View getCustomView() {
		return mActivity.getActionBar().getCustomView();
	}
}
