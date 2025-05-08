# @mahermaker/android-alarm-manager

[![npm version](https://img.shields.io/npm/v/@mahermaker/android-alarm-manager.svg)](https://www.npmjs.com/package/@mahermaker/android-alarm-manager)
<!-- Add other badges like build status, license, etc. later -->

Capacitor plugin for scheduling precise alarms on Android, with support for custom UI and foreground services to ensure reliability.

## Supported Platforms

- **Android** (iOS and Web are not supported)

## Installation

```bash
npm install @mahermaker/android-alarm-manager
npx cap sync
```

## Android Configuration

Add the following permissions and declarations to your app's `android/app/src/main/AndroidManifest.xml` file:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Core Permissions -->
    <uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
    <!-- For Android 12 (API 31) and above, USE_EXACT_ALARM is an alias and also works -->
    <!-- <uses-permission android:name="android.permission.USE_EXACT_ALARM" /> -->

    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <!-- Required for Android 13 (API 33) and above to show notifications. -->

    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <!-- Ensures the device wakes up to trigger the alarm. -->

    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
    <!-- Allows the plugin to reschedule alarms after the device reboots. -->
    <!-- Note: Ensure your BootReceiver is correctly implemented to handle rescheduling. -->

    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <!-- Required to run a foreground service for reliable alarm delivery. -->

    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE" />
    <!--
        For Android 14 (API 34) and above, if your alarm is critical (e.g., a user-set alarm clock),
        you must declare this permission and specify `android:foregroundServiceType="specialUse"`
        for your service. You will also need to declare this special use case in the Google Play Console.
        See: https://developer.android.com/develop/background-work/services/foreground-services#special-use-cases
    -->

    <!-- Optional Permissions -->
    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
    <!--
        Required if you want the alarm to display a full-screen activity (like AlarmRingingActivity)
        directly, especially on older Android versions or for more intrusive alarms.
        The plugin provides `checkPermissions` and `requestPermissions` for this.
    -->

    <uses-permission android:name="android.permission.VIBRATE" />
    <!-- If your alarm notifications or ringing activity should vibrate. -->

    <uses-permission android:name="android.permission.USE_FULL_SCREEN_INTENT" />
    <!--
        Often used in conjunction with SYSTEM_ALERT_WINDOW and notifications to launch
        full-screen activities for alarms.
    -->


    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/AppTheme">

        <!-- Plugin Components -->
        <receiver
            android:name="me.mahermaker.alarmmanager.AlarmReceiver"
            android:enabled="true"
            android:exported="false" />
            <!-- exported should be false if only the app itself schedules alarms -->

        <receiver
            android:name="me.mahermaker.alarmmanager.BootReceiver"
            android:enabled="true"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED" />
                <action android:name="android.intent.action.QUICKBOOT_POWERON" />
                <!-- For HTC devices -->
                <action android:name="com.htc.intent.action.QUICKBOOT_POWERON"/>
            </intent-filter>
        </receiver>

        <service
            android:name="me.mahermaker.alarmmanager.AlarmForegroundService"
            android:exported="false"
            android:foregroundServiceType="specialUse" />
            <!--
                Adjust foregroundServiceType as needed. For Android 14+ critical alarms, "specialUse" is appropriate.
                For older versions, or less critical alarms, you might remove it or use other types like "systemAlertWindow"
                if applicable, though "specialUse" is becoming the standard for alarms needing foreground services.
                If not targeting API 34+, you might not need foregroundServiceType or can use a more general one if required by older APIs.
                If your app targets Android 9 (API 28) or lower, you don't need this attribute.
                If targeting Android 10 (API 29) to Android 13 (API 33) and not using "specialUse", ensure you have a valid type if any.
                For maximum compatibility and future-proofing for alarms, "specialUse" is recommended if you meet the criteria.
            -->

        <activity
            android:name="me.mahermaker.alarmmanager.AlarmRingingActivity"
            android:exported="false"
            android:launchMode="singleTask"
            android:showOnLockScreen="true"
            android:turnScreenOn="true"
            android:theme="@style/Theme.AppCompat.NoActionBar"
            android:taskAffinity=""
            android:excludeFromRecents="true" />
            <!--
                Ensure the theme used (e.g., Theme.AppCompat.NoActionBar) is available in your project.
                Adjust screenOrientation, configChanges as needed.
            -->

        <!-- Your existing activities, services, and other application components -->
        <activity
            android:name="YOUR_APP_PACKAGE.MainActivity" <!-- Adjust to your actual MainActivity package -->
            android:configChanges="orientation|keyboardHidden|keyboard|screenSize|locale|smallestScreenSize|screenLayout|uiMode"
            android:exported="true"
            android:label="@string/title_activity_main"
            android:launchMode="singleTask"
            android:theme="@style/AppTheme.NoActionBarLaunch">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

    </application>
</manifest>
```

## API

<!-- This section will be auto-generated by npm run docgen -->
<!-- You can run `npm run docgen` in the `alarm-manager` directory to generate this. -->

```typescript
// Placeholder for API documentation - run docgen to fill this
// Example:
// set(options: AlarmConfig): Promise<{ alarmId: string }>;
// cancel(options: { alarmId: string }): Promise<void>;
// isScheduled(options: { alarmId: string }): Promise<{ isScheduled: boolean }>;
// checkPermissions(options: { permission: PermissionName }): Promise<PermissionStatusResult>;
// requestPermissions(options: { permission: PermissionName }): Promise<PermissionStatusResult>;
// addListener(eventName: string, listenerFunc: (eventData: AlarmEventData) => void): Promise<PluginListenerHandle> & PluginListenerHandle;
```

## Usage Examples

Here's how to use the plugin in your Capacitor project:

```typescript
import { AlarmManager, AlarmConfig, PermissionName, AlarmEventData } from '@mahermaker/android-alarm-manager';
import type { PluginListenerHandle } from '@capacitor/core';

// --- Permission Handling ---

async function checkAndRequestPermissions() {
  try {
    // Check Notification Permission (Android 13+)
    let notifResult = await AlarmManager.checkPermissions({ permission: 'POST_NOTIFICATIONS' });
    console.log('Notification permission status:', notifResult.status);
    if (notifResult.status !== 'granted') {
      notifResult = await AlarmManager.requestPermissions({ permission: 'POST_NOTIFICATIONS' });
      console.log('After request, Notification permission status:', notifResult.status);
    }

    // Check Exact Alarm Permission (Android 12+)
    let exactAlarmResult = await AlarmManager.checkPermissions({ permission: 'SCHEDULE_EXACT_ALARM' });
    console.log('Schedule Exact Alarm permission status:', exactAlarmResult.status);
    if (exactAlarmResult.status !== 'granted') {
      exactAlarmResult = await AlarmManager.requestPermissions({ permission: 'SCHEDULE_EXACT_ALARM' });
      // This might open settings, user needs to grant it there.
      // Re-check after user returns from settings if needed.
      console.log('After request, Schedule Exact Alarm permission status:', exactAlarmResult.status);
    }

    // Check Overlay Permission (Optional, for custom alarm UI)
    let overlayResult = await AlarmManager.checkPermissions({ permission: 'SYSTEM_ALERT_WINDOW' });
    console.log('System Alert Window permission status:', overlayResult.status);
    if (overlayResult.status !== 'granted') {
      overlayResult = await AlarmManager.requestPermissions({ permission: 'SYSTEM_ALERT_WINDOW' });
      // This might open settings.
      console.log('After request, System Alert Window permission status:', overlayResult.status);
    }

  } catch (error) {
    console.error('Error checking or requesting permissions:', error);
  }
}

// --- Scheduling an Alarm ---

async function scheduleNewAlarm() {
  const alarmTime = new Date();
  alarmTime.setSeconds(alarmTime.getSeconds() + 30); // Alarm in 30 seconds

  const alarmConfig: AlarmConfig = {
    alarmId: 'myUniqueAlarm123', // Must be a string
    at: alarmTime.getTime(),    // Timestamp in milliseconds
    name: 'My Test Alarm',
    exact: true,
    extra: { customData: 'any_relevant_data', meetingId: 456 },
    uiOptions: {
      titleText: 'Wakey Wakey!',
      alarmNameText: 'Morning Call',
      // backgroundColor: '#FFFF00', // Example: Yellow
      // dismissButtonText: 'Stop It!',
    }
  };

  try {
    const result = await AlarmManager.set(alarmConfig);
    console.log('Alarm scheduled successfully:', result); // { alarmId: "myUniqueAlarm123" }
  } catch (error) {
    console.error('Failed to schedule alarm:', error);
    // Example error: "Failed to schedule exact alarm: The SCHEDULE_EXACT_ALARM permission is required and not granted. Please request it first."
  }
}

// --- Cancelling an Alarm ---

async function cancelExistingAlarm(id: string) {
  try {
    await AlarmManager.cancel({ alarmId: id });
    console.log(`Alarm ${id} cancelled successfully.`);
  } catch (error) {
    console.error(`Failed to cancel alarm ${id}:`, error);
  }
}

// --- Checking if an Alarm is Scheduled ---

async function checkAlarmStatus(id: string) {
  try {
    const result = await AlarmManager.isScheduled({ alarmId: id });
    console.log(`Alarm ${id} isScheduled:`, result.isScheduled);
  } catch (error) {
    console.error(`Failed to check status for alarm ${id}:`, error);
  }
}

// --- Listening to Alarm Events ---

let alarmDismissedListener: PluginListenerHandle | null = null;

async function startListeningToAlarmEvents() {
  // Example: Listen for when an alarm is dismissed from the ringing activity
  alarmDismissedListener = await AlarmManager.addListener('alarmDismissed', (eventData: AlarmEventData) => {
    console.log('Alarm Dismissed Event:', eventData); // { alarmId: "...", name: "..." }
    // Update your UI, clear alarm state, etc.
  });

  // You can add listeners for other custom events your plugin might emit, e.g., 'alarmFired', 'alarmSnoozed'
  // await AlarmManager.addListener('alarmFired', (eventData: AlarmEventData) => {
  //   console.log('Alarm Fired Event:', eventData);
  // });
}

async function stopListeningToAlarmEvents() {
  if (alarmDismissedListener) {
    await alarmDismissedListener.remove();
    alarmDismissedListener = null;
    console.log('Alarm dismissed listener removed.');
  }
}

// Call these functions as needed in your app
// checkAndRequestPermissions();
// scheduleNewAlarm();
// checkAlarmStatus('myUniqueAlarm123');
// cancelExistingAlarm('myUniqueAlarm123');
// startListeningToAlarmEvents();
// To stop listening (e.g., in a component's ngOnDestroy or equivalent):
// stopListeningToAlarmEvents();

```

## Foreground Service and Reliability

This plugin utilizes a foreground service (`AlarmForegroundService.java`) to enhance the reliability of alarm delivery, especially when the app is in the background or has been closed by the system. When an alarm is triggered by the Android `AlarmManager`, the `AlarmReceiver` starts this foreground service. The service then typically acquires a wakelock, posts a notification (if configured), and starts the `AlarmRingingActivity` to alert the user.

Using a foreground service is crucial for tasks that need to run immediately and reliably, like alarms. Be mindful of Android's restrictions and requirements for foreground services, especially the `FOREGROUND_SERVICE_SPECIAL_USE` permission and Play Store declaration for Android 14+.

## Customization

### UI Options
You can customize the appearance of the default alarm ringing screen (`AlarmRingingActivity`) by passing `uiOptions` when scheduling an alarm. See the `AlarmConfig` interface in `definitions.ts` for available options (e.g., `titleText`, `backgroundColor`, button texts and colors).

### Advanced Customization
For more significant changes to the ringing screen's behavior or layout, you can directly modify:
- `alarm-manager/android/src/main/java/me/mahermaker/alarmmanager/AlarmRingingActivity.java`
- `alarm-manager/android/src/main/res/layout/activity_alarm_ringing.xml`
- `alarm-manager/android/src/main/res/values/styles.xml` (for themes/styles if needed)

Remember to rebuild your Android project after making native modifications.

## Troubleshooting / FAQ

*   **Alarms not firing:**
    *   **Permissions:** Double-check all required permissions in `AndroidManifest.xml`. Use the plugin's `checkPermissions` and `requestPermissions` methods to ensure they are granted at runtime, especially `SCHEDULE_EXACT_ALARM` (Android 12+), `POST_NOTIFICATIONS` (Android 13+), and `SYSTEM_ALERT_WINDOW` (if using full-screen UI).
    *   **Battery Optimization:** Aggressive battery optimization settings on some Android devices (especially from manufacturers like Xiaomi, Huawei, OnePlus, Samsung) can prevent alarms from firing or delay them significantly. Instruct users to exclude your app from battery optimization or add it to an "allow list." This is a common Android issue, not specific to this plugin.
    *   **Device Reboot:** If alarms are not restored after reboot, ensure your `BootReceiver` is correctly implemented and registered in `AndroidManifest.xml` to re-schedule your alarms. (The plugin provides a `BootReceiver` entry in the manifest, but you need to verify its implementation for rescheduling logic).
    *   **Alarm ID:** Ensure `alarmId` is a unique string for each alarm you schedule.
    *   **Timestamp (`at`):** Verify the timestamp is in the future and in milliseconds.
    *   **Logcat:** Check Android Studio's Logcat for detailed error messages from the plugin (filter by tags like `AlarmManagerImpl`, `AlarmReceiver`, `AlarmForegroundService`, `ManagerPlugin`).

*   **"Failed to schedule exact alarm: The SCHEDULE_EXACT_ALARM permission is required..."**
    *   On Android 12 (API 31) and above, your app needs the "Alarms & reminders" special app permission to use `setExactAndAllowWhileIdle`. Use `AlarmManager.requestPermissions({ permission: 'SCHEDULE_EXACT_ALARM' })` which will take the user to the app's settings page to grant this.

*   **UI not appearing or appearing incorrectly:**
    *   Ensure `SYSTEM_ALERT_WINDOW` permission is granted if you expect a full-screen activity.
    *   Check `uiOptions` passed during scheduling.
    *   Review `AlarmRingingActivity.java` and `activity_alarm_ringing.xml` for any custom modifications.

## Contributing

Please see [CONTRIBUTING.md](CONTRIBUTING.md) for details on how to contribute to this project.

## License

This plugin is licensed under the [MIT License](LICENSE).

---

*This README was partially generated and will be completed with auto-generated API docs.*
