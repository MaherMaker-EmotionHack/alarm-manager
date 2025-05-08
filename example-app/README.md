## Alarm Manager Plugin Example App

This app was created using [`@capacitor/create-app`](https://github.com/ionic-team/create-capacitor-app) and serves as a comprehensive example for the `alarm-manager` Capacitor plugin.

It demonstrates how to use the `alarm-manager` plugin to:
*   Check and request necessary permissions (Overlay, Exact Alarm, Notifications).
*   Schedule new alarms (both exact and inexact).
*   Provide custom titles and bodies for alarm notifications.
*   Include custom actions in alarm notifications (e.g., "Snooze", "Dismiss").
*   Cancel existing alarms.
*   Check if an alarm is currently scheduled.
*   Listen to and display plugin events (`alarmFired`, `alarmAction`, `permissionStatusChanged`).

### Prerequisites

*   Ensure you have Node.js, npm, and a compatible Android development environment (Android Studio, SDKs) set up.
*   The `alarm-manager` plugin (the parent directory of this example app) should be built first, or you should ensure its native parts are correctly linked if developing simultaneously.

### Setup and Running this Example

1.  **Install Dependencies for the Plugin:**
    Navigate to the plugin directory (`../`) and run:
    ```bash
    npm install
    ```

2.  **Install Dependencies for the Example App:**
    Navigate to this example app's directory (`alarm-app/alarm-manager/example-app/`) and run:
    ```bash
    npm install
    ```

3.  **Link the Local Plugin:**
    The example app is configured to use the local `alarm-manager` plugin via `\"alarm-manager\": \"file:..\"` in its `package.json`. This should automatically link to your local plugin when you run `npm install` in the example app's directory.

4.  **Sync Capacitor Assets:**
    ```bash
    npx cap sync android
    ```

5.  **Open the Android Project in Android Studio:**
    ```bash
    npx cap open android
    ```

6.  **Run the App from Android Studio:**
    *   Build and run the app on an Android emulator or a connected physical device.
    *   **Important for Android 12+ (Exact Alarm Permission):** The app allows you to check and request the "Alarms & reminders" special app permission. You may need to grant this manually via System Settings -> Apps -> [Your Example App Name] -> Permissions -> Alarms & reminders if the in-app request doesn't directly take you there or isn't sufficient on certain OS versions.
    *   **Important for Android 6+ (Overlay Permission):** The app allows you to check and request the "Display over other apps" permission. This is often required for the full-screen alarm activity to appear correctly.
    *   **Important for Android 13+ (Notification Permission):** The app allows you to check and request the "Post Notifications" permission. This is required for alarm notifications to be displayed.
    *   **Important for Android 14+ (Foreground Service Type for Alarms):** The plugin declares the necessary foreground service type for alarms. Ensure your device/emulator is Android 14+ to test this specific behavior. No extra user steps are typically needed if permissions are correctly declared in the plugin's manifest and the app requests `SCHEDULE_EXACT_ALARM`.

7.  **Test Alarm Functionality:**
    *   Use the example app's UI to check and request permissions.
    *   Schedule an alarm using the provided inputs.
    *   Observe the behavior when the alarm fires (it should show a notification and potentially a full-screen activity, depending on the OS and if the app is in the foreground/background).
    *   Interact with alarm notification actions (e.g., "Snooze", "Dismiss").
    *   Cancel a scheduled alarm.
    *   Check if an alarm is scheduled.
    *   Observe the event log within the app for plugin events.

### Key Features Demonstrated

*   **Permission Handling:** Checking and requesting `SYSTEM_ALERT_WINDOW` (Overlay), `SCHEDULE_EXACT_ALARM`, and `POST_NOTIFICATIONS` permissions.
*   **Scheduling Alarms:** Setting alarms with specific times, titles, bodies, and custom notification actions.
*   **Cancelling Alarms:** Removing previously scheduled alarms.
*   **Status Checking:** Verifying if a specific alarm is currently scheduled.
*   **Event Listening:** Real-time logging of `alarmFired`, `alarmAction` (when a notification action is tapped), and `permissionStatusChanged` events.
*   **Basic Snooze Implementation:** The example includes a basic client-side snooze implementation upon receiving an `alarmAction` event with a "snooze" action ID.

This example app is intended to showcase the core functionalities of the `alarm-manager` plugin in a clear and interactive way.
