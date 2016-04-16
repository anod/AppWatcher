package com.anod.appwatcher.ui;

import android.content.Intent;

/**
 * Created by alex on 7/9/14.
 */
public interface ActivityListener {

    interface ResultListener {
        void onActivityResult(int requestCode, int resultCode, Intent data);
    }
}
