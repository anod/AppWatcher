package com.anod.appwatcher.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.anod.appwatcher.utils.AppLog;

public class AuthenticationService extends Service {

    private Authenticator mAuthenticator;

    @Override
    public void onCreate() {
    	AppLog.d("SampleSyncAdapter Authentication Service started.");
        mAuthenticator = new Authenticator(this);
    }

    @Override
    public void onDestroy() {
       	AppLog.d("SampleSyncAdapter Authentication Service stopped.");
    }

    @Override
    public IBinder onBind(Intent intent) {
       	AppLog.d("getBinder()...  returning the AccountAuthenticator binder for intent "
                    + intent);
        return mAuthenticator.getIBinder();
    }
}
