package me.mahermaker.alarmmanager;

import android.app.Activity;
import android.app.AlarmManager; // Added this import
import android.app.KeyguardManager;
import android.app.NotificationManager; // Added this import
import android.app.PendingIntent; // Added this import
import android.content.Context;
import android.content.Intent;
import android.graphics.Color; // Import Color
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.View; // Import View
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout; // Import RelativeLayout
import android.widget.TextView;
import android.widget.Toast; // Added this import

import androidx.core.app.NotificationCompat; // Added this import
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject; // Import JSONObject
import org.json.JSONException; // Import JSONException

import java.io.IOException;
import java.text.SimpleDateFormat; // Added this import
import java.util.Date; // Added this import
import java.util.Locale; // Added this import

import me.mahermaker.alarmmanager.R;

public class AlarmRingingActivity extends AppCompatActivity {
    private static final String TAG = "AlarmRingingActivity";
    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;
    private String alarmId;
    private String alarmName;
    private String alarmSoundUri; // Can be passed via intent extra if customizable per alarm
    private boolean isSnoozed = false;
    private static final long[] VIBRATION_PATTERN = {0, 500, 1000}; // Off, On, Off, On...
    private static final int SNOOZE_DURATION_MS = 10 * 60 * 1000; // 10 minutes
    private static final int SNOOZE_NOTIFICATION_ID = AlarmForegroundService.SERVICE_NOTIFICATION_ID + 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate started");

        // Flags to show activity over lock screen and turn screen on
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            if (keyguardManager != null) {
                keyguardManager.requestDismissKeyguard(this, null);
            }
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }

        setContentView(R.layout.activity_alarm_ringing);

        TextView alarmNameTextView = findViewById(R.id.alarm_name_text_view);
        TextView alarmRingingTitleTextView = findViewById(R.id.alarm_ringing_title);
        Button dismissButton = findViewById(R.id.dismiss_button);
        Button snoozeButton = findViewById(R.id.snooze_button);
        RelativeLayout layout = findViewById(R.id.alarm_ringing_layout);

        Intent intent = getIntent();
        alarmId = intent.getStringExtra("alarmId");
        alarmName = intent.getStringExtra("name"); // General alarm name from parameters
        String uiOptionsJson = intent.getStringExtra("uiOptions");

        Log.d(TAG, "Received alarmId: " + alarmId + ", alarmName: " + alarmName);
        Log.d(TAG, "Received uiOptionsJson: " + (uiOptionsJson != null ? uiOptionsJson : "null"));

        // Texts are initially set by XML defaults from setContentView(R.layout.activity_alarm_ringing)

        boolean alarmNameTextWasSetByUiOptions = false;

        // Apply UI customizations from uiOptionsJson if present
        if (uiOptionsJson != null && !uiOptionsJson.isEmpty()) {
            try {
                JSONObject uiOptions = new JSONObject(uiOptionsJson);
                applyUiOptions(uiOptions, layout, alarmRingingTitleTextView, alarmNameTextView, dismissButton, snoozeButton);

                if (uiOptions.has("alarmNameText") && !uiOptions.optString("alarmNameText").isEmpty()) {
                    alarmNameTextWasSetByUiOptions = true;
                }

            } catch (JSONException e) {
                Log.e(TAG, "Error parsing UI options JSON in onCreate", e);
            }
        }

        // Fallback for alarmNameTextView if not set by uiOptions.alarmNameText
        if (!alarmNameTextWasSetByUiOptions) {
            if (alarmName != null && !alarmName.isEmpty()) {
                alarmNameTextView.setText(alarmName); // Use intent's alarmName
            } else {
                if (alarmNameTextView.getText() == null ||
                    alarmNameTextView.getText().toString().isEmpty() ||
                    alarmNameTextView.getText().toString().equals(getString(R.string.default_alarm_name_xml))) {
                    alarmNameTextView.setText(getString(R.string.default_alarm_name_fallback));
                }
            }
        }

        Log.d(TAG, "Final Alarm Title: " + alarmRingingTitleTextView.getText().toString());
        Log.d(TAG, "Final Alarm Name Displayed: " + alarmNameTextView.getText().toString());

        // Default sound if not specified
        alarmSoundUri = intent.getStringExtra("soundUri");
        if (alarmSoundUri == null || alarmSoundUri.isEmpty()) {
            alarmSoundUri = Settings.System.DEFAULT_ALARM_ALERT_URI.toString();
        }

        dismissButton.setOnClickListener(v -> dismissAlarm());
        snoozeButton.setOnClickListener(v -> snoozeAlarm());

        startAlarmSoundAndVibration();
        Log.d(TAG, "onCreate finished");
    }

    private void applyUiOptions(JSONObject uiOptions, RelativeLayout layout, TextView titleView, TextView nameView, Button dismissButton, Button snoozeButton) {
        Log.d(TAG, "Applying UI options: " + uiOptions.toString());
        try {
            if (uiOptions.has("backgroundColor")) {
                String bgColor = uiOptions.optString("backgroundColor");
                if (bgColor != null && !bgColor.isEmpty()) {
                    Log.d(TAG, "Setting backgroundColor: " + bgColor);
                    layout.setBackgroundColor(Color.parseColor(bgColor));
                }
            }

            String titleText = uiOptions.optString("titleText");
            if (titleText != null && !titleText.isEmpty()) {
                Log.d(TAG, "Setting titleText: " + titleText);
                titleView.setText(titleText);
            } else {
                Log.d(TAG, "titleText not found or empty in uiOptions, using XML default: " + titleView.getText());
            }

            String titleColor = uiOptions.optString("titleColor");
            if (titleColor != null && !titleColor.isEmpty()) {
                Log.d(TAG, "Setting titleColor: " + titleColor);
                titleView.setTextColor(Color.parseColor(titleColor));
            }

            String alarmNameOptText = uiOptions.optString("alarmNameText");
            if (alarmNameOptText != null && !alarmNameOptText.isEmpty()) {
                Log.d(TAG, "Setting alarmNameText from uiOptions: " + alarmNameOptText);
                nameView.setText(alarmNameOptText);
            } else {
                Log.d(TAG, "alarmNameText not found or empty in uiOptions. Current nameView text: " + nameView.getText());
            }

            String alarmNameOptColor = uiOptions.optString("alarmNameColor");
            if (alarmNameOptColor != null && !alarmNameOptColor.isEmpty()) {
                Log.d(TAG, "Setting alarmNameColor: " + alarmNameOptColor);
                nameView.setTextColor(Color.parseColor(alarmNameOptColor));
            }

            String dismissText = uiOptions.optString("dismissButtonText");
            if (dismissText != null && !dismissText.isEmpty()) {
                Log.d(TAG, "Setting dismissButtonText: " + dismissText);
                dismissButton.setText(dismissText);
            }
            if (uiOptions.has("dismissButtonBackgroundColor")) {
                String bgColor = uiOptions.optString("dismissButtonBackgroundColor");
                if (bgColor != null && !bgColor.isEmpty()) {
                    Log.d(TAG, "Setting dismissButtonBackgroundColor: " + bgColor);
                    dismissButton.setBackgroundColor(Color.parseColor(bgColor));
                }
            }
            if (uiOptions.has("dismissButtonTextColor")) {
                String textColor = uiOptions.optString("dismissButtonTextColor");
                if (textColor != null && !textColor.isEmpty()) {
                    Log.d(TAG, "Setting dismissButtonTextColor: " + textColor);
                    dismissButton.setTextColor(Color.parseColor(textColor));
                }
            }

            String snoozeText = uiOptions.optString("snoozeButtonText");
            if (snoozeText != null && !snoozeText.isEmpty()) {
                Log.d(TAG, "Setting snoozeButtonText: " + snoozeText);
                snoozeButton.setText(snoozeText);
            }
            if (uiOptions.has("snoozeButtonBackgroundColor")) {
                String bgColor = uiOptions.optString("snoozeButtonBackgroundColor");
                if (bgColor != null && !bgColor.isEmpty()) {
                    Log.d(TAG, "Setting snoozeButtonBackgroundColor: " + bgColor);
                    snoozeButton.setBackgroundColor(Color.parseColor(bgColor));
                }
            }
            if (uiOptions.has("snoozeButtonTextColor")) {
                String textColor = uiOptions.optString("snoozeButtonTextColor");
                if (textColor != null && !textColor.isEmpty()) {
                    Log.d(TAG, "Setting snoozeButtonTextColor: " + textColor);
                    snoozeButton.setTextColor(Color.parseColor(textColor));
                }
            }

        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Error parsing color string in UI options", e);
        }
    }

    private void startAlarmSoundAndVibration() {
        Log.d(TAG, "Starting alarm sound and vibration. Sound URI: " + alarmSoundUri);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(VIBRATION_PATTERN, 0));
            } else {
                vibrator.vibrate(VIBRATION_PATTERN, 0);
            }
            Log.d(TAG, "Vibration started.");
        } else {
            Log.w(TAG, "Vibrator not available or doesn't have vibrator functionality.");
        }

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(this, Uri.parse(alarmSoundUri));
            mediaPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .build()
            );
            mediaPlayer.setLooping(true);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                Log.d(TAG, "MediaPlayer prepared and started.");
            });
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e(TAG, "MediaPlayer error: what=" + what + ", extra=" + extra);
                playFallbackSound();
                return true;
            });
        } catch (IOException e) {
            Log.e(TAG, "IOException setting data source for MediaPlayer: " + e.getMessage(), e);
            playFallbackSound();
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "IllegalArgumentException for sound URI: " + e.getMessage(), e);
            playFallbackSound();
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException for sound URI: " + e.getMessage(), e);
            playFallbackSound();
        }
    }

    private void playFallbackSound() {
        Log.d(TAG, "Attempting to play fallback sound.");
        if (mediaPlayer != null) {
            mediaPlayer.reset();
        } else {
            mediaPlayer = new MediaPlayer();
        }
        try {
            Uri fallbackUri = Settings.System.DEFAULT_ALARM_ALERT_URI;
            if (fallbackUri != null) {
                mediaPlayer.setDataSource(this, fallbackUri);
                mediaPlayer.setAudioAttributes(
                        new AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .setUsage(AudioAttributes.USAGE_ALARM)
                                .build()
                );
                mediaPlayer.setLooping(true);
                mediaPlayer.prepareAsync();
                mediaPlayer.setOnPreparedListener(mp -> {
                    mp.start();
                    Log.d(TAG, "Fallback MediaPlayer prepared and started.");
                });
                mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                    Log.e(TAG, "Fallback MediaPlayer error: what=" + what + ", extra=" + extra);
                    return true;
                });
            } else {
                Log.e(TAG, "Fallback URI (DEFAULT_ALARM_ALERT_URI) is null.");
            }
        } catch (Exception ex) {
            Log.e(TAG, "Exception playing fallback sound: " + ex.getMessage(), ex);
        }
    }

    private void stopAlarmSoundAndVibration() {
        Log.d(TAG, "Stopping alarm sound and vibration.");
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
            Log.d(TAG, "MediaPlayer stopped and released.");
        }
        if (vibrator != null) {
            vibrator.cancel();
            vibrator = null;
            Log.d(TAG, "Vibrator cancelled.");
        }
    }

    private void dismissAlarm() {
        Log.d(TAG, "Dismiss button clicked. Alarm ID: " + alarmId);
        stopAlarmSoundAndVibration();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(AlarmForegroundService.SERVICE_NOTIFICATION_ID);
            Log.d(TAG, "Foreground service notification cancelled.");
        }

        Intent serviceIntent = new Intent(this, AlarmForegroundService.class);
        stopService(serviceIntent);
        Log.d(TAG, "AlarmForegroundService stopped due to dismiss.");

        Log.i(TAG, "Alarm dismissed: " + alarmId);

        if (ManagerPlugin.instance != null) {
            ManagerPlugin.instance.notifyAlarmEvent("alarmDismissed", alarmId, alarmName);
        }
        finishAndRemoveTask();
    }

    private void snoozeAlarm() {
        Log.d(TAG, "Snooze button clicked. Alarm ID: " + alarmId);
        stopAlarmSoundAndVibration();
        isSnoozed = true;

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(AlarmForegroundService.SERVICE_NOTIFICATION_ID);
            Log.d(TAG, "Foreground service notification cancelled.");
        }

        Intent serviceIntent = new Intent(this, AlarmForegroundService.class);
        stopService(serviceIntent);
        Log.d(TAG, "AlarmForegroundService stopped due to snooze.");

        long snoozeTime = System.currentTimeMillis() + SNOOZE_DURATION_MS;

        String nextSnoozeName;
        if (this.alarmName != null && this.alarmName.endsWith(" (Snoozed)")) {
            nextSnoozeName = this.alarmName;
        } else if (this.alarmName != null) {
            nextSnoozeName = this.alarmName + " (Snoozed)";
        } else {
            nextSnoozeName = "Alarm (Snoozed)";
        }

        Intent snoozeIntent = new Intent(this, AlarmReceiver.class);
        snoozeIntent.setAction("SNOOZE_ALARM_ACTION_" + alarmId);
        snoozeIntent.putExtra("alarmId", alarmId + "_snooze");
        snoozeIntent.putExtra("name", nextSnoozeName);
        snoozeIntent.putExtra("at", snoozeTime);
        snoozeIntent.putExtra("soundUri", alarmSoundUri);

        int pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntentFlags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingSnoozeBroadcastIntent = PendingIntent.getBroadcast(
                this,
                (alarmId != null ? alarmId.hashCode() : System.identityHashCode(this)) + 1,
                snoozeIntent,
                pendingIntentFlags
        );

        AlarmManager androidAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (androidAlarmManager != null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !androidAlarmManager.canScheduleExactAlarms()) {
                    Log.w(TAG, "Cannot schedule exact alarms for snooze. App needs SCHEDULE_EXACT_ALARM permission or user disabled it.");
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    androidAlarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, snoozeTime, pendingSnoozeBroadcastIntent);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    androidAlarmManager.setExact(AlarmManager.RTC_WAKEUP, snoozeTime, pendingSnoozeBroadcastIntent);
                } else {
                    androidAlarmManager.set(AlarmManager.RTC_WAKEUP, snoozeTime, pendingSnoozeBroadcastIntent);
                }
                Log.i(TAG, "Snooze alarm scheduled for ID: " + alarmId + "_snooze" + " at " + new java.util.Date(snoozeTime));

                if (notificationManager != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    String formattedSnoozeTime = sdf.format(new Date(snoozeTime));

                    Intent mainAppIntent = getPackageManager().getLaunchIntentForPackage(getPackageName());
                    PendingIntent mainAppPendingIntent = null;
                    if (mainAppIntent != null) {
                        mainAppIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        mainAppPendingIntent = PendingIntent.getActivity(
                                this,
                                SNOOZE_NOTIFICATION_ID + 100,
                                mainAppIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0)
                        );
                    } else {
                        Log.e(TAG, "Could not get launch intent for package to create snooze notification content intent.");
                    }

                    NotificationCompat.Builder snoozeNotificationBuilder = new NotificationCompat.Builder(this, AlarmReceiver.ALARM_CHANNEL_ID)
                            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                            .setContentTitle(nextSnoozeName)
                            .setContentText("Next alarm at " + formattedSnoozeTime)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setContentIntent(mainAppPendingIntent)
                            .setAutoCancel(true)
                            .setSilent(true);

                    notificationManager.notify(SNOOZE_NOTIFICATION_ID, snoozeNotificationBuilder.build());
                    Log.d(TAG, "Snooze notification posted for " + formattedSnoozeTime);
                }

            } catch (SecurityException se) {
                Log.e(TAG, "SecurityException while scheduling snooze alarm.", se);
            } catch (Exception e) {
                Log.e(TAG, "Exception while scheduling snooze alarm.", e);
            }
        } else {
            Log.e(TAG, "AlarmManager service is null, cannot schedule snooze.");
        }

        if (ManagerPlugin.instance != null) {
            ManagerPlugin.instance.notifyAlarmEvent("alarmSnoozed", alarmId, alarmName);
        }

        String snoozeConfirmation = String.format(Locale.getDefault(), "Snoozed for %d minutes.", SNOOZE_DURATION_MS / (60 * 1000));
        Toast.makeText(this, snoozeConfirmation, Toast.LENGTH_SHORT).show();
        finishAndRemoveTask();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy called. Snoozed: " + isSnoozed);
        if (!isSnoozed) {
            stopAlarmSoundAndVibration();
        }
        Intent serviceIntent = new Intent(this, AlarmForegroundService.class);
        stopService(serviceIntent);
        Log.d(TAG, "AlarmForegroundService potentially stopped in onDestroy if not snoozed/dismissed.");
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent called. Activity was already running.");
        setIntent(intent);

        alarmId = intent.getStringExtra("alarmId");
        alarmName = intent.getStringExtra("name");
        alarmSoundUri = intent.getStringExtra("soundUri");
        String uiOptionsJson = intent.getStringExtra("uiOptions");

        Log.d(TAG, "onNewIntent - Received alarmId: " + alarmId + ", alarmName: " + alarmName);
        Log.d(TAG, "onNewIntent - Received uiOptionsJson: " + (uiOptionsJson != null ? uiOptionsJson : "null"));

        if (alarmSoundUri == null || alarmSoundUri.isEmpty()) {
            alarmSoundUri = Settings.System.DEFAULT_ALARM_ALERT_URI.toString();
        }

        TextView alarmNameTextView = findViewById(R.id.alarm_name_text_view);
        TextView alarmRingingTitleTextView = findViewById(R.id.alarm_ringing_title);
        Button dismissButton = findViewById(R.id.dismiss_button);
        Button snoozeButton = findViewById(R.id.snooze_button);
        RelativeLayout layout = findViewById(R.id.alarm_ringing_layout);

        alarmRingingTitleTextView.setText(getString(R.string.default_alarm_title_xml));
        alarmNameTextView.setText(getString(R.string.default_alarm_name_xml));
        dismissButton.setText(getString(R.string.default_dismiss_button_xml));
        snoozeButton.setText(getString(R.string.default_snooze_button_xml));

        boolean alarmNameTextWasSetByNewUiOptions = false;

        if (uiOptionsJson != null && !uiOptionsJson.isEmpty()) {
            try {
                JSONObject uiOptions = new JSONObject(uiOptionsJson);
                applyUiOptions(uiOptions, layout, alarmRingingTitleTextView, alarmNameTextView, dismissButton, snoozeButton);

                if (uiOptions.has("alarmNameText") && !uiOptions.optString("alarmNameText").isEmpty()) {
                    alarmNameTextWasSetByNewUiOptions = true;
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing UI options JSON in onNewIntent", e);
            }
        }

        if (!alarmNameTextWasSetByNewUiOptions) {
            if (alarmName != null && !alarmName.isEmpty()) {
                alarmNameTextView.setText(alarmName);
            } else {
                if (alarmNameTextView.getText() == null ||
                    alarmNameTextView.getText().toString().isEmpty() ||
                    alarmNameTextView.getText().toString().equals(getString(R.string.default_alarm_name_xml))) {
                    alarmNameTextView.setText(getString(R.string.default_alarm_name_fallback));
                }
            }
        }

        Log.d(TAG, "onNewIntent - Updated with new alarm data - ID: " + alarmId + ", Displayed Name: " + alarmNameTextView.getText().toString());

        stopAlarmSoundAndVibration();
        startAlarmSoundAndVibration();
    }
}
