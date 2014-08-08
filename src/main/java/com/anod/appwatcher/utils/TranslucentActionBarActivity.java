package com.anod.appwatcher.utils;

import android.content.res.Resources;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.anod.appwatcher.R;
import com.readystatesoftware.systembartint.SystemBarTintManager;

/**
 * @author alex
 * @date 2014-08-07
 */
abstract public class TranslucentActionBarActivity extends ActionBarActivity {


    protected int mPixelInsetBottom;
    protected boolean mIsNavigationAtBottom;

    protected void initSystemBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SystemBarTintManager sysbars = new SystemBarTintManager(this);
            Resources r = getResources();
            sysbars.setStatusBarTintColor(r.getColor(R.color.abs__background_holo_dark));
            sysbars.setStatusBarAlpha(1);
            sysbars.setStatusBarTintEnabled(true);

            SystemBarTintManager.SystemBarConfig cfg = sysbars.getConfig();


            mIsNavigationAtBottom = cfg.isNavigationAtBottom();

            FrameLayout frame = (FrameLayout) findViewById(R.id.activity_frame);
            frame.setBackgroundColor(r.getColor(R.color.abs__background_holo_light));
            if (mIsNavigationAtBottom) {
                mPixelInsetBottom = cfg.getPixelInsetBottom();
                frame.setPadding(0,cfg.getPixelInsetTop(true),0,0);
            } else {
                frame.setPadding(0,cfg.getPixelInsetTop(true),cfg.getPixelInsetRight(),0);
            }
        }
    }

    public void adjustListView(ListView lv) {
        if (mPixelInsetBottom == 0 || !mIsNavigationAtBottom) {
            return;
        }
        View v = new View(this);
        AbsListView.LayoutParams params = new AbsListView.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,mPixelInsetBottom);
        v.setLayoutParams(params);
        lv.addFooterView(v);
    }


}
