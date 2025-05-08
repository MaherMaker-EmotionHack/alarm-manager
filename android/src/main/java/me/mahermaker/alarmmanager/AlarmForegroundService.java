package me.mahermaker.alarmmanager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class AlarmForegroundService extends Service {
    private static final String TAG = "AlarmForegroundService";
    public static final String ACTION_SHOW_ALARM = "me.mahermaker.alarmmanager.ACTION_SHOW_ALARM";
    public static final int SERVICE_NOTIFICATION_ID = 2; // Changed from private to public
    private PowerManager.WakeLock wakeLock;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG + "::ServiceWakeLock");
            wakeLock.setReferenceCounted(false); // Manage wakelock manually
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand received");
        if (intent != null && ACTION_SHOW_ALARM.equals(intent.getAction())) {
            if (wakeLock != null && !wakeLock.isHeld()) {
                wakeLock.acquire(10 * 60 * 1000L /* 10 minutes */); // Acquire with timeout
                Log.d(TAG, "WakeLock acquired");
            }

            String alarmId = intent.getStringExtra("alarmId");
            String name = intent.getStringExtra("name");
            long atTime = intent.getLongExtra("at", System.currentTimeMillis());
            // String soundUri = intent.getStringExtra("soundUri"); // Will be used by AlarmRingingActivity

            Log.i(TAG, "Processing alarm: ID=" + alarmId + ", Name=" + name);

            // Create notification channel (similar to AlarmReceiver)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(AlarmReceiver.ALARM_CHANNEL_ID,
                        "Alarm Channel", NotificationManager.IMPORTANCE_MAX);
                channel.setDescription("Channel for alarm notifications");
                channel.setSound(null, null); // Sound handled by activity
                channel.enableVibration(false); // Vibration handled by activity
                channel.setBypassDnd(true);
                NotificationManager notificationManager = getSystemService(NotificationManager.class);
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(channel);
                }
            }

            // Intent to start AlarmRingingActivity
            Intent activityIntent = new Intent(this, AlarmRingingActivity.class);
            activityIntent.putExtra("alarmId", alarmId);
            activityIntent.putExtra("name", name);
            activityIntent.putExtra("at", atTime);
            // activityIntent.putExtra("soundUri", soundUri);
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(this,
                    300, // Unique request code
                    activityIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0));

            Notification notification = new NotificationCompat.Builder(this, AlarmReceiver.ALARM_CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // Replace with your app icon
                    .setContentTitle(name != null ? name : "Alarm")
                    .setContentText("Alarm is active.")
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setFullScreenIntent(fullScreenPendingIntent, true)
                    .setOngoing(true) // Foreground service notifications are typically ongoing
                    .build();

            // Start foreground service with the notification
            // For Android 14+ (API 34), foregroundServiceType must be specified in manifest
            // For Android Q (API 29) to 13 (API 33), foregroundServiceType in startForeground
            // For older, just startForeground.
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Type will be inferred from manifest for API 34+
                    // For 29-33, if specific types are needed, they can be added here.
                    // However, for alarms, the manifest declaration is key.
                    startForeground(SERVICE_NOTIFICATION_ID, notification);
                } else {
                    startForeground(SERVICE_NOTIFICATION_ID, notification);
                }
                Log.d(TAG, "Service started in foreground.");

                // Attempt to launch the full-screen activity directly
                // This is the crucial part - will the foreground service have enough privilege?
                try {
                    Log.d(TAG, "Attempting to send fullScreenPendingIntent directly.");
                    fullScreenPendingIntent.send();
                } catch (PendingIntent.CanceledException e) {
                    Log.e(TAG, "fullScreenPendingIntent was canceled", e);
                }

            } catch (Exception e) {
                Log.e(TAG, "Error starting foreground service or sending intent", e);
            }


            // The service should stop itself after the alarm activity is launched or if it fails.
            // However, AlarmRingingActivity will be responsible for the user interaction.
            // We can stop this service once AlarmRingingActivity is up.
            // For now, let it run until explicitly stopped or AlarmRingingActivity handles it.
            // Consider adding a timeout to stopSelf if AlarmRingingActivity doesn't launch.
        } else {
            Log.w(TAG, "Service started with unexpected intent or null action. Stopping.");
            stopSelf();
        }
        // If the service is killed, it will not be restarted unless there are pending intents.
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            Log.d(TAG, "WakeLock released");
        }
        Log.d(TAG, "onDestroy");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // Not a bound service
    }
}
