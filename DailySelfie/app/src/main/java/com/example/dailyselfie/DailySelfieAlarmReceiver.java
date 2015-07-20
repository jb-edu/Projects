package com.example.dailyselfie;

/**
 * Created by jb-edu on 15-07-01.
 */

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;


public class DailySelfieAlarmReceiver extends BroadcastReceiver {

    private AlarmManager mAlarmManager;
    private PendingIntent mAlarmIntent;
    private static final int REQUEST_CODE = 10091;
    public static final int NOTIFICATION_ID = 10050;
    public static final long TWO_MINUTES = 120000L;

    @Override
    public void onReceive(Context context, Intent intent) {

        NotificationManager mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent =
                PendingIntent.getActivity(context, 0,
                        new Intent(context, DailySelfieActivity.class), 0);

        // TODO: decide how to handle the notification icons and clean up the code below.
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setAutoCancel(true)
                        .setSmallIcon(R.drawable.ic_notify_dailyselfie)
                        .setContentTitle(context.getString(R.string.app_name))
                        .setContentText(context.getString(R.string.camera_alert))
                        .setTicker(context.getString(R.string.camera_alert_ticker));

        builder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, builder.build());

    }

    public void setAlarm(Context context) {
        mAlarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, DailySelfieAlarmReceiver.class);
        mAlarmIntent =
                PendingIntent.getBroadcast(
                        context, REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        mAlarmManager.setRepeating(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + TWO_MINUTES,
                TWO_MINUTES,
                mAlarmIntent);

        Toast.makeText(context, context.getString(R.string.alarm_set_msg),
                Toast.LENGTH_SHORT).show();

        // Enable the BootReceiver to start alarms when the system boots
        ComponentName receiver = new ComponentName(context, DailySelfieBootReceiver.class);

        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    public void cancelAlarm(Context context) {

        if (mAlarmManager != null) {
            mAlarmManager.cancel(mAlarmIntent);
        }
        else {
            Intent intent = new Intent(context, DailySelfieAlarmReceiver.class);
            mAlarmIntent =
                    PendingIntent.getBroadcast(
                            context, REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            mAlarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            mAlarmManager.cancel(mAlarmIntent);
        }

        Toast.makeText(context, context.getString(R.string.alarm_cancelled_msg),
                Toast.LENGTH_SHORT).show();

        // Disable the BootReceiver from starting alarms when the system boots
        ComponentName receiver = new ComponentName(context, DailySelfieBootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }
}
