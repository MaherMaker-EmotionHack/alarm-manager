package me.mahermaker.alarmmanager;

import android.Manifest; // Added import
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager; // Added import
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.ActivityCompat; // Added import
import androidx.core.content.ContextCompat; // Added import

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "AlarmManager")
public class ManagerPlugin extends Plugin {

    private static final String LOG_TAG = "ManagerPlugin";
    public static ManagerPlugin instance;
    private AlarmManagerImplementation implementation;

    private static final int POST_NOTIFICATIONS_REQUEST_CODE = 9001;
    private PluginCall savedPostNotificationCall;

    @Override
    public void load() {
        super.load();
        instance = this; // Set static instance
        implementation = new AlarmManagerImplementation(getContext()); // Pass context
    }

    @PluginMethod
    public void set(PluginCall call) {
        JSObject alarmConfig = call.getData();
        if (alarmConfig == null || !alarmConfig.has("alarmId") || !alarmConfig.has("at")) {
            call.reject("Invalid alarm configuration: missing alarmId or at.");
            return;
        }

        String alarmId = alarmConfig.getString("alarmId");
        boolean exact = alarmConfig.optBoolean("exact", true);
        boolean success = implementation.setAlarm(alarmConfig);

        if (success) {
            JSObject ret = new JSObject();
            ret.put("alarmId", alarmId);
            call.resolve(ret);
        } else {
            String errorMessage = "Failed to schedule alarm. Please check device logs (Logcat) for more details.";
            if (exact && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                android.app.AlarmManager am = (android.app.AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
                if (am != null && !am.canScheduleExactAlarms()) {
                    errorMessage = "Failed to schedule exact alarm: The SCHEDULE_EXACT_ALARM permission is required and not granted. Please request it first.";
                }
            }
            call.reject(errorMessage);
        }
    }

    @PluginMethod
    public void cancel(PluginCall call) {
        JSObject data = call.getData();
        if (data == null) {
            call.reject("Missing request data");
            return;
        }
        String alarmId;
        try {
            if (!data.has("alarmId")) {
                call.reject("Missing alarmId in request data");
                return;
            }
            Object alarmIdObj = data.get("alarmId");
            if (alarmIdObj == null) {
                call.reject("alarmId value is null");
                return;
            }
            alarmId = alarmIdObj.toString();
        } catch (org.json.JSONException e) {
            call.reject("Error accessing alarmId: " + e.getMessage());
            return;
        }

        implementation.cancelAlarm(alarmId);
        call.resolve();
    }

    @PluginMethod
    public void isScheduled(PluginCall call) {
        JSObject data = call.getData();
        if (data == null) {
            call.reject("Missing request data");
            return;
        }
        String alarmId;
        try {
            if (!data.has("alarmId")) {
                call.reject("Missing alarmId in request data");
                return;
            }
            Object alarmIdObj = data.get("alarmId");
            if (alarmIdObj == null) {
                call.reject("alarmId value is null");
                return;
            }
            alarmId = alarmIdObj.toString();
        } catch (org.json.JSONException e) {
            call.reject("Error accessing alarmId: " + e.getMessage());
            return;
        }

        boolean scheduled = implementation.isScheduled(alarmId);
        JSObject ret = new JSObject();
        ret.put("isScheduled", scheduled);
        call.resolve(ret);
    }

    @PluginMethod
    public void checkPermissions(PluginCall call) {
        String permission = call.getString("permission");
        if (permission == null || permission.isEmpty()) {
            call.reject("Permission name is required.");
            return;
        }

        JSObject result = new JSObject();
        String status = "denied";

        switch (permission) {
            case "POST_NOTIFICATIONS":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    status = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED ? "granted" : "denied";
                } else {
                    status = "granted";
                }
                break;
            case "SYSTEM_ALERT_WINDOW":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    status = Settings.canDrawOverlays(getContext()) ? "granted" : "denied";
                } else {
                    status = "granted";
                }
                break;
            case "SCHEDULE_EXACT_ALARM":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    android.app.AlarmManager am = (android.app.AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
                    if (am != null) {
                        status = am.canScheduleExactAlarms() ? "granted" : "denied";
                    } else {
                        status = "denied";
                        Log.e(LOG_TAG, "AlarmManager service not available for SCHEDULE_EXACT_ALARM check.");
                    }
                } else {
                    status = "granted";
                }
                break;
            default:
                call.reject("Unknown permission: " + permission);
                return;
        }
        result.put("status", status);
        call.resolve(result);
    }

    @PluginMethod
    public void requestPermissions(PluginCall call) {
        String permission = call.getString("permission");
        if (permission == null || permission.isEmpty()) {
            call.reject("Permission name is required.");
            return;
        }

        JSObject result = new JSObject();

        switch (permission) {
            case "POST_NOTIFICATIONS":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                        result.put("status", "granted");
                        call.resolve(result);
                    } else {
                        savedPostNotificationCall = call;
                        pluginRequestPermission(Manifest.permission.POST_NOTIFICATIONS, POST_NOTIFICATIONS_REQUEST_CODE);
                    }
                } else {
                    result.put("status", "granted");
                    call.resolve(result);
                }
                break;
            case "SYSTEM_ALERT_WINDOW":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!Settings.canDrawOverlays(getContext())) {
                        try {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:" + getContext().getPackageName()));
                            getActivity().startActivity(intent);
                            result.put("status", "prompt");
                            call.resolve(result);
                        } catch (Exception e) {
                            Log.e(LOG_TAG, "Error starting ACTION_MANAGE_OVERLAY_PERMISSION activity", e);
                            call.reject("Error opening overlay settings: " + e.getMessage());
                        }
                    } else {
                        result.put("status", "granted");
                        call.resolve(result);
                    }
                } else {
                    result.put("status", "granted");
                    call.resolve(result);
                }
                break;
            case "SCHEDULE_EXACT_ALARM":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    android.app.AlarmManager am = (android.app.AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
                    if (am != null && !am.canScheduleExactAlarms()) {
                        try {
                            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                    Uri.parse("package:" + getContext().getPackageName()));
                            getActivity().startActivity(intent);
                            result.put("status", "prompt");
                            call.resolve(result);
                        } catch (Exception e) {
                            Log.e(LOG_TAG, "Error starting ACTION_REQUEST_SCHEDULE_EXACT_ALARM activity", e);
                            call.reject("Error opening exact alarm settings: " + e.getMessage());
                        }
                    } else {
                        result.put("status", (am != null && am.canScheduleExactAlarms()) ? "granted" : "denied");
                        call.resolve(result);
                    }
                } else {
                    result.put("status", "granted");
                    call.resolve(result);
                }
                break;
            default:
                call.reject("Unknown permission: " + permission);
                return;
        }
    }

    @Override
    protected void handleRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.handleRequestPermissionsResult(requestCode, permissions, grantResults);

        if (savedPostNotificationCall == null) {
            return;
        }

        if (requestCode == POST_NOTIFICATIONS_REQUEST_CODE) {
            JSObject result = new JSObject();
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                result.put("status", "granted");
            } else {
                // Check if user selected "Don't ask again"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // Ensure permissions array is not null and has at least one element
                    if (permissions != null && permissions.length > 0 &&
                        !ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permissions[0])) {
                         result.put("status", "denied_permanently"); // Custom status
                    } else {
                        result.put("status", "denied");
                    }
                } else {
                     result.put("status", "denied");
                }
            }
            savedPostNotificationCall.resolve(result);
            savedPostNotificationCall = null;
        }
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
                Log.d(LOG_TAG, "Overlay permission already granted.");
                call.resolve();
            }
        } else {
            Log.d(LOG_TAG, "requestDrawOverlaysPermission called on pre-M device, no action needed as permission is granted at install if declared.");
            call.resolve();
        }
    }

    @PluginMethod
    public void canScheduleExactAlarms(PluginCall call) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            android.app.AlarmManager alarmManager = (android.app.AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
            JSObject ret = new JSObject();
            if (alarmManager != null) {
                ret.put("value", alarmManager.canScheduleExactAlarms());
            } else {
                ret.put("value", false);
            }
            call.resolve(ret);
        } else {
            JSObject ret = new JSObject();
            ret.put("value", true);
            call.resolve(ret);
        }
    }

    @PluginMethod
    public void requestScheduleExactAlarmPermission(PluginCall call) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            android.app.AlarmManager alarmManager = (android.app.AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                           Uri.parse("package:" + getContext().getPackageName()));
                if (bridge.getActivity() != null) {
                    bridge.getActivity().startActivity(intent);
                    call.resolve();
                } else {
                    call.reject("Activity context not available to request exact alarm permission.");
                }
            } else {
                if (alarmManager == null) {
                     call.reject("AlarmManager service not available.");
                } else {
                    Log.d(LOG_TAG, "Exact alarm permission already granted or not needed.");
                    call.resolve();
                }
            }
        } else {
            Log.d(LOG_TAG, "requestScheduleExactAlarmPermission called on pre-S device, no action needed.");
            call.resolve();
        }
    }

    public void notifyAlarmEvent(String eventName, String alarmId, String originalAlarmName) {
        JSObject data = new JSObject();
        data.put("alarmId", alarmId);
        if (originalAlarmName != null) {
            data.put("name", originalAlarmName);
        }
        Log.d(LOG_TAG, "Notifying listeners for event: " + eventName + " with data: " + data.toString());
        notifyListeners(eventName, data, true);
    }
}
