package com.anod.appwatcher.utils;

import android.content.Context;
import android.support.v4.view.MenuItemCompat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.anod.appwatcher.R;

/**
 * @author alex
 * @date 2014-11-15
 */

public class MenuItemAnimation {

    private MenuItem mMenuItem;
    private Context mContext;
    private int mAnimResource;
    private boolean mInvisibleMode;

    public MenuItemAnimation(Context context, int animResource) {
        mContext = context;
        mAnimResource = animResource;
    }

    public void setMenuItem(MenuItem menuItem) {
        mMenuItem = menuItem;
    }
//R.anim.rotate
    /**
     * stop refresh button animation
     */
    public void stop() {
        if (mMenuItem == null) {
            return;
        }
        View actionView = MenuItemCompat.getActionView(mMenuItem);
        if (actionView != null) {
            actionView.clearAnimation();
            MenuItemCompat.setActionView(mMenuItem,null);
        }
        if (mInvisibleMode) {
            mMenuItem.setVisible(false);
        }
    }

    /**
     * Animate refresh button
     */
    public void start() {
        if (mMenuItem == null) {
            return;
        }
        View actionView = MenuItemCompat.getActionView(mMenuItem);
        //already animating
        if (actionView != null) {
            return;
        }
        if (mInvisibleMode) {
            mMenuItem.setVisible(true);
        }
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ImageView iv = (ImageView) inflater.inflate(R.layout.widget_refresh_action, null);

        Animation rotation = AnimationUtils.loadAnimation(mContext, mAnimResource);
        rotation.setRepeatCount(Animation.INFINITE);
        iv.startAnimation(rotation);

        MenuItemCompat.setActionView(mMenuItem, iv);

    }

    public void setInvisibleMode(boolean invisibleMode) {
        mInvisibleMode = invisibleMode;
    }

    public boolean isInvisibleMode() {
        return mInvisibleMode;
    }
}
