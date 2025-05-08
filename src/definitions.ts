import type { PluginListenerHandle } from '@capacitor/core';

export interface AlarmManagerPlugin {
  /**
   * Sets a one-time or repeating alarm.
   * @param options - AlarmConfig
   * @returns Promise<{ alarmId: string }>
   */
  set(options: AlarmConfig): Promise<{ alarmId: string }>;

  /**
   * Cancels an existing alarm.
   * @param options - { alarmId: string }
   * @returns Promise<void>
   */
  cancel(options: { alarmId: string }): Promise<void>;

  /**
   * Checks if an alarm with the given ID is scheduled.
   * @param options - { alarmId: string }
   * @returns Promise<{ isScheduled: boolean }>
   */
  isScheduled(options: { alarmId: string }): Promise<{ isScheduled: boolean }>;

  /**
   * Checks if the app has permission to draw over other apps.
   * This is required for the alarm screen to show up directly on Android M+.
   * @platform android
   * @since 1.0.0 
   */
  canDrawOverlays(): Promise<{value: boolean}>;

  /**
   * Opens the system settings screen for the app where the user can grant
   * the "Display over other apps" permission.
   * You should call `canDrawOverlays()` again after the user returns to your app
   * to confirm if the permission was granted.
   * @platform android
   * @since 1.0.0
   */
  requestDrawOverlaysPermission(): Promise<void>;

  // Potentially add listeners for alarm events if needed directly in the plugin
  // addListener(eventName: 'alarmFired', listenerFunc: (alarm: { alarmId: string }) => void): Promise<PluginListenerHandle> & PluginListenerHandle;
  // removeAllListeners(): Promise<void>;
}

export interface AlarmConfig {
  alarmId: string;      // Unique ID for the alarm
  at: number;           // Timestamp (milliseconds since epoch) for the alarm
  name?: string;        // Optional name for the alarm (e.g., "Wake up")
  exact?: boolean;      // Whether the alarm needs to be exact (uses AlarmManager.setExactAndAllowWhileIdle)
  window?: number;      // Optional window in milliseconds for inexact alarms (if exact is false)
  repeat?: boolean;     // If true, the alarm will repeat
  every?: 'minute' | 'hour' | 'day' | 'week' | 'month' | 'year'; // Interval for repeating alarms
  count?: number;       // How many times to repeat (if undefined or 0, repeats indefinitely if repeat is true)
  // sound?: string;    // Sound will be handled by the activity/page that opens
  // fullScreenIntent?: boolean; // This will be a primary function of the alarm firing
  // notificationTitle?: string; // Title for the notification shown before full-screen intent
  // notificationBody?: string;  // Body for the notification shown before full-screen intent
  // notificationSmallIcon?: string; // Small icon for notification
  // activityClassName?: string; // Optional: specify a custom activity to launch. Defaults to MainActivity or a specific AlarmActivity.
  extra?: Record<string, any>; // Optional: any extra data to pass to the alarm receiver/activity
}
