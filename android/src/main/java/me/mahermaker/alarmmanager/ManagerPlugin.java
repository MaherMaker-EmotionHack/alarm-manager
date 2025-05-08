package me.mahermaker.alarmmanager;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "AlarmManager")
public class ManagerPlugin extends Plugin {

    private static final String LOG_TAG = "ManagerPlugin"; // Added for logging
    public static ManagerPlugin instance; // Added static instance
    private AlarmManagerImplementation implementation;

    @Override
    public void load() {
        super.load();
        instance = this; // Set static instance
        implementation = new AlarmManagerImplementation(); // Removed getContext()
    }

    @PluginMethod
    public void set(PluginCall call) {
        JSObject alarmConfig = call.getData();
        if (alarmConfig == null || !alarmConfig.has("alarmId") || !alarmConfig.has("at")) {
            call.reject("Invalid alarm configuration: missing alarmId or at.");
            return;
        }

        String alarmId = alarmConfig.getString("alarmId");
        implementation.setAlarm(getContext(), alarmConfig);

        JSObject ret = new JSObject();
        ret.put("alarmId", alarmId);
        call.resolve(ret);
    }

    @PluginMethod
    public void cancel(PluginCall call) {
        String alarmId = call.getString("alarmId");
        if (alarmId == null) {
            call.reject("Missing alarmId");
            return;
        }
        implementation.cancelAlarm(getContext(), alarmId);
        call.resolve();
    }

    @PluginMethod
    public void isScheduled(PluginCall call) {
        String alarmId = call.getString("alarmId");
        if (alarmId == null) {
            call.reject("Missing alarmId");
            return;
        }
        boolean scheduled = implementation.isScheduled(getContext(), alarmId);
        JSObject ret = new JSObject();
        ret.put("isScheduled", scheduled);
        call.resolve(ret);
    }

    @PluginMethod
    public void canDrawOverlays(PluginCall call) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            JSObject ret = new JSObject();
            ret.put("value", Settings.canDrawOverlays(getContext()));
            call.resolve(ret);
        } else {
            JSObject ret = new JSObject();
            ret.put("value", true);
            call.resolve(ret);
        }
    }

    @PluginMethod
    public void requestDrawOverlaysPermission(PluginCall call) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(getContext())) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                           Uri.parse("package:" + getContext().getPackageName()));
                if (bridge.getActivity() != null) {
                    bridge.getActivity().startActivity(intent);
                    call.resolve();
                } else {
                    call.reject("Activity context not available to request overlay permission.");
                }
            } else {
                Log.d(LOG_TAG, "Overlay permission already granted."); // Changed to LOG_TAG
                call.resolve();
            }
        } else {
            Log.d(LOG_TAG, "requestDrawOverlaysPermission called on pre-M device, no action needed as permission is granted at install if declared."); // Changed to LOG_TAG
            call.resolve();
        }
    }

    // Method to allow AlarmRingingActivity to notify listeners
    public void notifyAlarmEvent(String eventName, String alarmId, String originalAlarmName) {
        JSObject data = new JSObject();
        data.put("alarmId", alarmId);
        if (originalAlarmName != null) {
            data.put("name", originalAlarmName);
        }
        // Based on how events are typically named (e.g., localNotificationActionPerformed)
        // We might want to make eventName more specific like "alarmDismissed", "alarmSnoozed"
        // For now, using the direct eventName passed.
        Log.d(LOG_TAG, "Notifying listeners for event: " + eventName + " with data: " + data.toString());
        notifyListeners(eventName, data, true);
    }
}
