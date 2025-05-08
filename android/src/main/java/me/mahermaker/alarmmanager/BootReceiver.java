package me.mahermaker.alarmmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "Boot completed, attempting to reschedule alarms.");

            // TODO: Implement logic to retrieve stored alarms
            // For example, using AlarmStorage class (to be created)
            // AlarmStorage alarmStorage = new AlarmStorage(context);
            // List<Alarm> alarms = alarmStorage.getAllAlarms();
            //
            // AlarmHelper alarmHelper = new AlarmHelper(context);
            // for (Alarm alarm : alarms) {
            //     if (alarm.isEnabled()) {
            //         Log.d(TAG, "Rescheduling alarm: " + alarm.getName());
            //         alarmHelper.scheduleAlarm(alarm);
            //     }
            // }

            // For now, let's just log that we would be rescheduling.
            // This part will be fleshed out once AlarmStorage and AlarmHelper have more functionality.
            Log.i(TAG, "Placeholder: Logic to re-schedule alarms would go here.");

            // Example: Reschedule a test alarm (REMOVE THIS IN ACTUAL IMPLEMENTATION)
            // This is just to demonstrate that the BootReceiver is working.
            // Alarm testAlarm = new Alarm();
            // testAlarm.setId(999); // Example ID
            // testAlarm.setHour(7);
            // testAlarm.setMinute(0);
            // testAlarm.setName("Test Boot Alarm");
            // testAlarm.setEnabled(true);
            //
            // AlarmHelper alarmHelper = new AlarmHelper(context);
            // alarmHelper.scheduleAlarm(testAlarm);
            // Log.d(TAG, "Scheduled a test alarm from BootReceiver.");
        } else {
            Log.w(TAG, "Received intent with unexpected action: " + (intent != null ? intent.getAction() : "null intent"));
        }
    }
}
