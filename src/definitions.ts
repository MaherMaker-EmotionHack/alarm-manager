import type { PluginListenerHandle } from '@capacitor/core';

/**
 * Represents a JavaScript object, typically used for passing unstructured data.
 */
export interface JSObject {
  [key: string]: any;
}

/**
 * Defines the configurable UI elements for the alarm ringing screen.
 * All properties are optional. If not provided, system or XML defaults will be used.
 */
export interface UiOptions {
  backgroundColor?: string;         // e.g., '#FF0000' for red
  titleText?: string;               // Text for the main title (e.g., "Wake Up!")
  titleColor?: string;              // Color for the title text
  alarmNameText?: string;           // Text for displaying the alarm's name
  alarmNameColor?: string;          // Color for the alarm name text
  dismissButtonText?: string;       // Text for the dismiss button
  dismissButtonBackgroundColor?: string; // Background color for the dismiss button
  dismissButtonTextColor?: string;  // Text color for the dismiss button
  snoozeButtonText?: string;        // Text for the snooze button
  snoozeButtonBackgroundColor?: string;  // Background color for the snooze button
  snoozeButtonTextColor?: string;   // Text color for the snooze button
}

/**
 * Defines the possible names for permissions that can be checked or requested.
 */
export type PermissionName =
  | 'POST_NOTIFICATIONS'
  | 'SYSTEM_ALERT_WINDOW'
  | 'SCHEDULE_EXACT_ALARM';

/**
 * Defines the possible statuses for a permission.
 * - granted: The permission is granted.
 * - denied: The permission is denied.
 * - prompt: The user will be prompted (or was just prompted) to grant the permission. This is often the case for permissions that require opening a system settings page.
 * - denied_permanently: (Android specific for POST_NOTIFICATIONS) The permission was denied and the user selected "Don't ask again".
 */
export type PermissionStatus = 'granted' | 'denied' | 'prompt' | 'denied_permanently';

export interface PermissionStatusResult {
  status: PermissionStatus;
}

export interface AlarmConfig {
  alarmId: string;      // Unique ID for the alarm
  at: number;           // Timestamp (milliseconds since epoch) for the alarm
  name?: string;        // Optional name for the alarm (e.g., "Wake up")
  exact?: boolean;      // Default true. Whether the alarm needs to be exact (uses AlarmManager.setExactAndAllowWhileIdle)
  // sound?: string;    // Sound will be handled by the activity/page that opens
  extra?: JSObject;     // Optional extra data to pass to the alarm receiver, accessible in AlarmReceiver/Activity
  uiOptions?: UiOptions;  // Optional UI customization for the ringing screen
}

/**
 * Defines the data structure for events emitted by the alarm plugin.
 */
export interface AlarmEventData {
  /**
   * The unique ID of the alarm that triggered the event.
   */
  alarmId: string;
  /**
   * The optional name of the alarm, as provided in AlarmConfig.
   */
  name?: string;
  // Future: could add eventType: 'alarmFired' | 'alarmDismissed' | 'alarmSnoozed';
}

export interface AlarmManagerPlugin {
  /**
   * Sets a one-time alarm.
   * @param options - The configuration for the alarm.
   * @returns A promise that resolves with the ID of the set alarm.
   */
  set(options: AlarmConfig): Promise<{ alarmId: string }>;

  /**
   * Cancels an existing alarm.
   * @param options - An object containing the ID of the alarm to cancel.
   * @returns A promise that resolves when the alarm is cancelled.
   */
  cancel(options: { alarmId: string }): Promise<void>;

  /**
   * Checks if an alarm with the given ID is scheduled.
   * @param options - An object containing the ID of the alarm to check.
   * @returns A promise that resolves with an object indicating whether the alarm is scheduled.
   */
  isScheduled(options: { alarmId: string }): Promise<{ isScheduled: boolean }>;

  /**
   * Checks the status of a given permission.
   * @param options - An object containing the name of the permission to check.
   * @returns A promise that resolves with an object containing the permission status.
   * @platform android
   * @since NEXT_VERSION (replace with actual version)
   */
  checkPermissions(options: { permission: PermissionName }): Promise<PermissionStatusResult>;

  /**
   * Requests a given permission.
   * For POST_NOTIFICATIONS on Android 13+, this will show a system dialog.
   * For SYSTEM_ALERT_WINDOW and SCHEDULE_EXACT_ALARM, this may open the system settings page for the app.
   * @param options - An object containing the name of the permission to request.
   * @returns A promise that resolves with an object containing the permission status after the request.
   * @platform android
   * @since NEXT_VERSION (replace with actual version)
   */
  requestPermissions(options: { permission: PermissionName }): Promise<PermissionStatusResult>;

  /**
   * Checks if the app has permission to draw over other apps.
   * @deprecated Use `checkPermissions({ permission: 'SYSTEM_ALERT_WINDOW' })` instead.
   * @platform android
   * @returns A promise that resolves with an object indicating whether the permission is granted.
   */
  canDrawOverlays(): Promise<{value: boolean}>;

  /**
   * Requests permission to draw over other apps.
   * @deprecated Use `requestPermissions({ permission: 'SYSTEM_ALERT_WINDOW' })` instead.
   * Should be called if `canDrawOverlays()` returns false before scheduling alarms
   * that are expected to show a full-screen UI.
   * On Android M+ (API 23+), this opens the system settings screen for the app.
   * @platform android
   * @returns A promise that resolves when the request is made (does not guarantee permission was granted).
   */
  requestDrawOverlaysPermission(): Promise<void>;

  /**
   * Checks if the app has permission to schedule exact alarms.
   * @deprecated Use `checkPermissions({ permission: 'SCHEDULE_EXACT_ALARM' })` instead.
   * Required on Android 12 (API 31) and above for `exact: true` alarms.
   * On older versions, this will always return true if the `SCHEDULE_EXACT_ALARM` permission is in the manifest.
   * @platform android
   * @returns A promise that resolves with an object indicating whether the permission is granted.
   */
  canScheduleExactAlarms(): Promise<{value: boolean}>;

  /**
   * Requests permission to schedule exact alarms.
   * @deprecated Use `requestPermissions({ permission: 'SCHEDULE_EXACT_ALARM' })` instead.
   * On Android 12 (API 31) and above, this will open the system settings for the app
   * where the user can grant the "Alarms & reminders" special permission.
   * On older versions, this is a no-op and resolves immediately.
   * @platform android
   * @returns A promise that resolves when the request is made (does not guarantee permission was granted).
   */
  requestScheduleExactAlarmPermission(): Promise<void>;

  /**
   * Listens for alarm events such as 'alarmFired', 'alarmDismissed', 'alarmSnoozed'.
   * The actual event names will be defined by the plugin's implementation when an alarm triggers an action.
   * @param eventName - The name of the event to listen for (e.g., "alarmFired", "alarmDismissed").
   * @param listenerFunc - The function to call when the event occurs, receiving an `AlarmEventData` object.
   * @returns A promise that resolves with a handle to the listener, which can be used to remove the listener.
   */
  addListener(eventName: string, listenerFunc: (eventData: AlarmEventData) => void): Promise<PluginListenerHandle> & PluginListenerHandle;

}
