package com.anod.appwatcher.sync;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.anod.appwatcher.AppWatcherActivity;
import com.anod.appwatcher.NotificationActivity;
import com.anod.appwatcher.R;
import com.anod.appwatcher.market.AppLoader;

import java.util.ArrayList;

/**
 * @author alex
 * @date 2014-09-24
 */
public class SyncNotification {

    public static final int NOTIFICATION_ID = 1;

    private final Context mContext;

    public SyncNotification(Context context) {
        mContext = context;
    }

    public void show(Notification notification) {
        NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, notification);
    }
    public void cancel() {
        NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(NOTIFICATION_ID);
    }

    public Notification create(ArrayList<SyncAdapter.UpdatedApp> updatedApps, AppLoader loader) {
        Intent notificationIntent = new Intent(mContext, AppWatcherActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Uri data = Uri.parse("com.anod.appwatcher://notification");
        notificationIntent.setData(data);
        notificationIntent.putExtra(AppWatcherActivity.EXTRA_FROM_NOTIFICATION, true);
        PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);

        String title = renderNotificationTitle(updatedApps);
        String text = renderNotificationText(updatedApps);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
        builder
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_stat_update)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(contentIntent)
                .setTicker(title)
        ;
        if (updatedApps.size() == 1) {
            SyncAdapter.UpdatedApp app = updatedApps.get(0);
            addExtraInfo(app, builder, loader);
        }

        return builder.build();
    }


    private void addExtraInfo(SyncAdapter.UpdatedApp app, NotificationCompat.Builder builder,AppLoader loader) {
        loader.setExtended(true);
        String changes = loader.loadRecentChanges(app.appId);
        if (changes != null) {
            if (changes.equals("")) {
                changes = mContext.getString(R.string.no_recent_changes);
            } else {
                builder.setContentText(changes);
            }
            builder.setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(changes)
            );

            Intent playIntent = new Intent(mContext, NotificationActivity.class);
            playIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            playIntent.setData(Uri.parse("com.anod.appwatcher://play/" + app.pkg));
            playIntent.putExtra(NotificationActivity.EXTRA_TYPE, NotificationActivity.TYPE_PLAY);
            playIntent.putExtra(NotificationActivity.EXTRA_PKG, app.pkg);
            builder.addAction(R.drawable.ic_action_playback_play_white, mContext.getString(R.string.store),
                    PendingIntent.getActivity(mContext, 0, playIntent, 0)
            );

            Intent readIntent = new Intent(mContext, NotificationActivity.class);
            readIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            readIntent.setData(Uri.parse("com.anod.appwatcher://dismiss/"));
            readIntent.putExtra(NotificationActivity.EXTRA_TYPE, NotificationActivity.TYPE_DISMISS);

            builder.addAction(R.drawable.ic_action_cancel_white, mContext.getString(R.string.dismiss),
                    PendingIntent.getActivity(mContext, 0, readIntent, 0)
            );

        }
    }

    private String renderNotificationText(ArrayList<SyncAdapter.UpdatedApp> apps) {
        int count = apps.size();
        if (count == 1) {
            return mContext.getString(R.string.notification_click);
        }
        if (count > 2) {
            return mContext.getString(
                    R.string.notification_2_apps_more,
                    apps.get(0).title,
                    apps.get(1).title
            );
        }
        return mContext.getString(R.string.notification_2_apps,
                apps.get(0).title,
                apps.get(1).title
        );
    }

    private String renderNotificationTitle(ArrayList<SyncAdapter.UpdatedApp> apps) {
        String title;
        int count = apps.size();
        if (count == 1) {
            title = mContext.getString(R.string.notification_one_updated, apps.get(0).title);
        } else {
            title = mContext.getString(R.string.notification_many_updates, count);
        }
        return title;
    }


}
