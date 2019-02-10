package com.cruxlab.parkurbn;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class NotificationPublisher extends BroadcastReceiver {

    public static String NOTIFICATION_CANCEL = "notification-cancel";
    public static String NOTIFICATION_ID = "notification-id";
    public static String NOTIFICATION = "notification";

    @Override
    public void onReceive(final Context context, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (intent.hasExtra(NOTIFICATION) && intent.hasExtra(NOTIFICATION_ID)) {
            int id = intent.getIntExtra(NOTIFICATION_ID, 0);
            Notification notification = intent.getParcelableExtra(NOTIFICATION);
            final String className = ParkUrbnApplication.isAppOnForeground(context);
            if (className != null) {
                Intent sendIntent = new Intent("OPEN_PARKING_SOON_DIALOG");
                sendIntent.putExtra("className", className);
                context.sendBroadcast(sendIntent);
            } else {
                notificationManager.notify(id, notification);
            }
            SharedPrefsManager.get().clearRemindBeforeTimeMins();
        } else if (intent.hasExtra(NOTIFICATION_CANCEL)) {
            int id = intent.getIntExtra(NOTIFICATION_CANCEL, 0);
            notificationManager.cancel(id);
        }

    }
}