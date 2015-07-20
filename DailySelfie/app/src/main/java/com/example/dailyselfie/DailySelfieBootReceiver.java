package com.example.dailyselfie;

/**
 * Created by jb-edu on 15-07-01.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * This BroadcastReceiver automatically re-starts the DailySelfie alarm when the device is
 * rebooted.
 *
 * Adapted from: https://developer.android.com/training/scheduling/alarms.html#boot
 */
public class DailySelfieBootReceiver extends BroadcastReceiver {
    DailySelfieAlarmReceiver mAlarm = new DailySelfieAlarmReceiver();
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(context.getString(R.string.boot_completed_ref))
                || intent.getAction().equals(context.getString(R.string.quickboot_poweron_ref)))
        {
            mAlarm.setAlarm(context);
        }
    }
}
