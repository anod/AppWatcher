package com.anod.appwatcher.sync;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.text.Html;

import com.anod.appwatcher.AppWatcherActivity;
import com.anod.appwatcher.NotificationActivity;
import com.anod.appwatcher.R;

import java.util.List;

/**
 * @author alex
 * @date 2014-09-24
 */
public class SyncNotification {

    static final int NOTIFICATION_ID = 1;

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

    public Notification create(List<SyncAdapter.UpdatedApp> updatedApps) {
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
            addExtraInfo(app, builder);
        } else {
            addMultipleExtraInfo(updatedApps, builder);
        }

        return builder.build();
    }

    private void addMultipleExtraInfo(List<SyncAdapter.UpdatedApp> updatedApps, NotificationCompat.Builder builder) {
        boolean isUpdatable = false;

        StringBuilder sb = new StringBuilder();
        for (SyncAdapter.UpdatedApp app: updatedApps) {
            if (app.installedVersionCode > 0 && app.versionCode > app.installedVersionCode)
            {
                isUpdatable = true;
            }
            sb.append(app.title).append("\n");
        }

        if (!isUpdatable)
        {
            return;
        }

        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(sb.toString()));

        Intent updateIntent = createActionIntent(Uri.parse("com.anod.appwatcher://play/myapps/1"), NotificationActivity.TYPE_MYAPPS_UPDATE);
        builder.addAction(R.drawable.ic_system_update_alt_white_24dp, mContext.getString(R.string.noti_action_update),
                PendingIntent.getActivity(mContext, 0, updateIntent, 0)
        );

        Intent readIntent = createActionIntent(Uri.parse("com.anod.appwatcher://dismiss/"), NotificationActivity.TYPE_DISMISS);
        builder.addAction(R.drawable.ic_clear_white_24dp, mContext.getString(R.string.dismiss),
                PendingIntent.getActivity(mContext, 0, readIntent, 0)
        );

    }

    private void addExtraInfo(SyncAdapter.UpdatedApp app, NotificationCompat.Builder builder) {
        String changes = app.recentChanges;
        if (changes != null) {
            if (changes.equals("")) {
                changes = mContext.getString(R.string.no_recent_changes);
            } else {
                builder.setContentText(Html.fromHtml(changes));
            }
            builder.setStyle(
                new NotificationCompat.BigTextStyle().bigText(Html.fromHtml(changes))
            );

            Intent playIntent = createActionIntent(Uri.parse("com.anod.appwatcher://play/" + app.pkg), NotificationActivity.TYPE_PLAY);
            playIntent.putExtra(NotificationActivity.EXTRA_PKG, app.pkg);

            builder.addAction(R.drawable.ic_play_arrow_white_24dp, mContext.getString(R.string.store),
                    PendingIntent.getActivity(mContext, 0, playIntent, 0)
            );

            if (app.installedVersionCode > 0) {
                Intent updateIntent = createActionIntent(Uri.parse("com.anod.appwatcher://play/myapps/1"), NotificationActivity.TYPE_MYAPPS_UPDATE);
                builder.addAction(R.drawable.ic_system_update_alt_white_24dp, mContext.getString(R.string.noti_action_update),
                        PendingIntent.getActivity(mContext, 0, updateIntent, 0)
                );
            }

            Intent readIntent = createActionIntent(Uri.parse("com.anod.appwatcher://dismiss/"), NotificationActivity.TYPE_DISMISS);
            builder.addAction(R.drawable.ic_clear_white_24dp, mContext.getString(R.string.dismiss),
                    PendingIntent.getActivity(mContext, 0, readIntent, 0)
            );
        }
    }

    private Intent createActionIntent(Uri uri,int type)
    {
        Intent intent = new Intent(mContext, NotificationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(uri);
        intent.putExtra(NotificationActivity.EXTRA_TYPE, type);
        return intent;
    }

    private String renderNotificationText(List<SyncAdapter.UpdatedApp> apps) {
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

    private String renderNotificationTitle(List<SyncAdapter.UpdatedApp> apps) {
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
