package me.mahermaker.alarmmanager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import com.getcapacitor.JSObject;

public class AlarmManagerImplementation {

    private static final String TAG = "AlarmManagerImpl";
    private Context context; // Added context field

    // Constructor to receive context
    public AlarmManagerImplementation(Context context) {
        this.context = context;
    }

    public boolean setAlarm(JSObject alarmConfig) {
        Log.d(TAG, "setAlarm called with config: " + alarmConfig.toString());

        String alarmId = alarmConfig.getString("alarmId");
        long at = alarmConfig.optLong("at"); // Use optLong for safety, though we validate in ManagerPlugin
        boolean exact = alarmConfig.optBoolean("exact", true);
        String name = alarmConfig.optString("name", "Alarm");
        JSObject extras = alarmConfig.getJSObject("extra");
        JSObject uiOptions = alarmConfig.getJSObject("uiOptions"); // Get UI options

        String uiOptionsJson = null;
        if (uiOptions != null) {
            try {
                uiOptionsJson = uiOptions.toString(); // Convert JSObject to JSON String
                Log.d(TAG, "Received uiOptions as JSObject, converted to JSON: " + uiOptionsJson);
            } catch (Exception e) {
                Log.e(TAG, "Error converting uiOptions JSObject to String", e);
            }
        } else {
            Log.d(TAG, "uiOptions JSObject was null.");
        }

        if (alarmId == null || alarmId.isEmpty() || at == 0) {
            Log.e(TAG, "Alarm ID or time 'at' is missing or invalid.");
            return false; // Indicate failure
        }

        Intent intent = new Intent(this.context, AlarmReceiver.class); // Use this.context
        intent.setAction("me.mahermaker.alarmmanager.ALARM_TRIGGER_ACTION_" + alarmId); // Unique action per alarm
        intent.putExtra("alarmId", alarmId);
        intent.putExtra("name", name);
        if (extras != null) {
            intent.putExtra("extra", extras.toString()); // Pass extras as a JSON string
        }
        if (uiOptionsJson != null) {
            intent.putExtra("uiOptions", uiOptionsJson); // Pass uiOptions as a JSON string
        }

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this.context, // Use this.context
                alarmId.hashCode(), // Use alarmId's hashcode as request code for uniqueness
                intent,
                flags
        );

        AlarmManager alarmManager = (AlarmManager) this.context.getSystemService(Context.ALARM_SERVICE); // Use this.context

        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager service is null.");
            return false; // Indicate failure
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                Log.w(TAG, "Cannot schedule exact alarms. App needs SCHEDULE_EXACT_ALARM permission or user disabled it.");
                if (exact) { // If exact was specifically requested, treat as failure for now.
                    Log.e(TAG, "Exact alarm requested but cannot be scheduled. Failing.");
                    return false; // Indicate failure
                }
            }

            if (exact) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pendingIntent);
                    Log.i(TAG, "Scheduled exact alarm while idle for ID: " + alarmId + " at " + at);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, at, pendingIntent);
                    Log.i(TAG, "Scheduled exact alarm for ID: " + alarmId + " at " + at);
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, at, pendingIntent);
                    Log.i(TAG, "Scheduled legacy (exact-ish) alarm for ID: " + alarmId + " at " + at);
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pendingIntent);
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, at, pendingIntent);
                }
                Log.i(TAG, "Scheduled inexact alarm for ID: " + alarmId + " at " + at);
            }
            return true; // Indicate success
        } catch (SecurityException se) {
            Log.e(TAG, "SecurityException while scheduling alarm. Check permissions (SCHEDULE_EXACT_ALARM).", se);
            return false; // Indicate failure
        } catch (Exception e) {
            Log.e(TAG, "Exception while scheduling alarm for ID: " + alarmId, e);
            return false; // Indicate failure
        }
    }

    public void cancelAlarm(String alarmId) {
        Log.d(TAG, "cancelAlarm called for ID: " + alarmId);
        if (alarmId == null || alarmId.isEmpty()) {
            Log.w(TAG, "Cannot cancel alarm: ID is null or empty.");
            return;
        }

        Intent intent = new Intent(this.context, AlarmReceiver.class); // Use this.context
        intent.setAction("me.mahermaker.alarmmanager.ALARM_TRIGGER_ACTION_" + alarmId);

        int flags = PendingIntent.FLAG_NO_CREATE; // Important: FLAG_NO_CREATE to check existence before cancelling
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this.context, // Use this.context
                alarmId.hashCode(),
                intent,
                flags
        );

        AlarmManager alarmManager = (AlarmManager) this.context.getSystemService(Context.ALARM_SERVICE); // Use this.context
        if (alarmManager != null && pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel(); // Also cancel the PendingIntent itself
            Log.i(TAG, "Cancelled alarm with ID: " + alarmId);
        } else {
            if (alarmManager == null) Log.w(TAG, "AlarmManager service is null, cannot cancel alarm ID: " + alarmId);
            if (pendingIntent == null) Log.w(TAG, "PendingIntent for alarm ID " + alarmId + " not found, may not be scheduled or action/ID mismatch.");
        }
    }

    public boolean isScheduled(String alarmId) {
        Log.d(TAG, "isScheduled called for ID: " + alarmId);
        if (alarmId == null || alarmId.isEmpty()) {
            Log.w(TAG, "Cannot check schedule: ID is null or empty.");
            return false;
        }

        Intent intent = new Intent(this.context, AlarmReceiver.class); // Use this.context
        intent.setAction("me.mahermaker.alarmmanager.ALARM_TRIGGER_ACTION_" + alarmId);

        int flags = PendingIntent.FLAG_NO_CREATE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this.context, // Use this.context
                alarmId.hashCode(),
                intent,
                flags
        );
        boolean isSet = pendingIntent != null;
        Log.i(TAG, "Alarm ID " + alarmId + " isScheduled: " + isSet);
        return isSet;
    }
}
