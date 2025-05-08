# alarm-manager

alarm for ionic

## Install

```bash
npm install alarm-manager
npx cap sync
```

## Android Configuration

This plugin requires certain permissions and configurations on Android to function correctly, especially for displaying an alarm screen when the app is in the background or the device is locked.

1.  **Exact Alarm Permission**: `SCHEDULE_EXACT_ALARM` (and `USE_EXACT_ALARM` for Android 12+) are automatically added to your `AndroidManifest.xml`. For Android 12 (API 31) and above, users might need to grant a special "Alarms & reminders" permission to your app through system settings if it targets API 31+.
2.  **Display Over Other Apps**: `SYSTEM_ALERT_WINDOW` permission is crucial for the alarm screen to appear directly, especially when the app is in the background. This permission is added to the manifest, but for Android 6.0 (API 23) and above, it must be **manually granted by the user** through your app's settings page in the Android system settings.
3.  **Wake Lock**: `WAKE_LOCK` permission is added to ensure the device wakes up when an alarm fires.
4.  **Notifications**: `POST_NOTIFICATIONS` is added for Android 13 (API 33) and above. Although this version aims to show the activity directly, this permission is good practice.

Ensure your app requests or guides the user to grant the "Display over other apps" permission for reliable alarm display.

## API

<docgen-index>

* [`set(...)`](#set)
* [`cancel(...)`](#cancel)
* [`isScheduled(...)`](#isscheduled)
* [`canDrawOverlays()`](#candrawoverlays)
* [`requestDrawOverlaysPermission()`](#requestdrawoverlayspermission)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### set(...)

```typescript
set(options: AlarmConfig) => Promise<{ alarmId: string; }>
```

Sets a one-time or repeating alarm.

| Param         | Type                            |
| ------------- | ------------------------------- |
| **`options`** | <code>AlarmConfig</code>           |

**Returns:** <code>Promise&lt;{ alarmId: string; }&gt;</code>

--------------------

### cancel(...)

```typescript
cancel(options: { alarmId: string; }) => Promise<void>
```

Cancels an existing alarm.

| Param         | Type                            |
| ------------- | ------------------------------- |
| **`options`** | <code>{ alarmId: string; }</code> |

**Returns:** <code>Promise&lt;void&gt;</code>

--------------------

### isScheduled(...)

```typescript
isScheduled(options: { alarmId: string; }) => Promise<{ isScheduled: boolean; }>
```

Checks if an alarm with the given ID is scheduled.

| Param         | Type                            |
| ------------- | ------------------------------- |
| **`options`** | <code>{ alarmId: string; }</code> |

**Returns:** <code>Promise&lt;{ isScheduled: boolean; }&gt;</code>

--------------------

### canDrawOverlays()

```typescript
canDrawOverlays() => Promise<{ value: boolean; }>
```

Checks if the app has permission to draw over other apps.
This is required for the alarm screen to show up directly on Android M+.

**Returns:** <code>Promise&lt;{ value: boolean; }&gt;</code>

**Platforms:** Android

--------------------

### requestDrawOverlaysPermission()

```typescript
requestDrawOverlaysPermission() => Promise<void>
```

Opens the system settings screen for the app where the user can grant
the "Display over other apps" permission.
Call `canDrawOverlays()` again after the user returns to your app
to confirm if the permission was granted.

**Returns:** <code>Promise&lt;void&gt;</code>

**Platforms:** Android

--------------------

</docgen-api>
