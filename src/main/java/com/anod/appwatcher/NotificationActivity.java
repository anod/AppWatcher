package com.anod.appwatcher;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.anod.appwatcher.sync.SyncNotification;
import com.anod.appwatcher.utils.IntentUtils;

public class NotificationActivity extends Activity {
    public static final String EXTRA_TYPE = "type";
    public static final int TYPE_PLAY = 1;
    public static final int TYPE_DISMISS = 2;
    public static final String EXTRA_PKG = "pkg";

    /* (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        SyncNotification sn = new SyncNotification(this);
        sn.cancel();
        int type = intent.getIntExtra(EXTRA_TYPE, 0);
        if (type == TYPE_PLAY) {
            String pkg = intent.getStringExtra(EXTRA_PKG);
            startActivity(IntentUtils.createPlayStoreIntent(pkg));
        } else if (type == TYPE_DISMISS) {
            // Nothing
        }

        finish();
    }


}
