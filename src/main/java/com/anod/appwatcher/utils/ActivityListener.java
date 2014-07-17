package com.anod.appwatcher.utils;

import android.content.Intent;

/**
 * Created by alex on 7/9/14.
 */
public interface ActivityListener {

    public interface ResultListener {
        public void onActivityResult(int requestCode, int resultCode, Intent data);
    }
}
