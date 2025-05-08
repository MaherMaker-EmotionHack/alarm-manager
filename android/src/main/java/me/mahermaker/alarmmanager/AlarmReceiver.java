package me.mahermaker.alarmmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "AlarmReceiver";
    public static final String ALARM_CHANNEL_ID = "alarm_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = null;
        if (powerManager != null) {
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG + "::AlarmWakeLock");
            wakeLock.acquire(10 * 1000L /* 10 seconds, service will take over */);
            Log.d(TAG, "Partial WakeLock acquired by Receiver");
        } else {
            Log.w(TAG, "PowerManager not available, cannot acquire WakeLock.");
        }

        String alarmId = intent.getStringExtra("alarmId");
        String name = intent.getStringExtra("name");
        long atTime = intent.getLongExtra("at", System.currentTimeMillis());
        String extraData = intent.getStringExtra("extra");
        String soundUri = intent.getStringExtra("soundUri");

        Log.i(TAG, "Alarm received! Forwarding to AlarmForegroundService. ID: " + (alarmId != null ? alarmId : "No ID") +
                ", Name: " + (name != null ? name : "N/A"));

        Intent serviceIntent = new Intent(context, AlarmForegroundService.class);
        serviceIntent.setAction(AlarmForegroundService.ACTION_SHOW_ALARM);
        serviceIntent.putExtra("alarmId", alarmId);
        serviceIntent.putExtra("name", name);
        serviceIntent.putExtra("at", atTime);
        serviceIntent.putExtra("extra", extraData);
        serviceIntent.putExtra("soundUri", soundUri);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
        Log.d(TAG, "Started AlarmForegroundService.");

        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            Log.d(TAG, "Partial WakeLock released by Receiver");
        }
    }
}
