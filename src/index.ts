import { registerPlugin, PluginListenerHandle } from '@capacitor/core';

import type { AlarmManagerPlugin, AlarmConfig, PermissionName, PermissionStatusResult } from './definitions';

const unimplementedWarning = (platform: 'web' | 'ios', methodName: string) =>
  `Warning: Method AlarmManager.${methodName} was called on ${platform}, but the '@mahermaker/android-alarm-manager' plugin is Android-only. This method will have no effect or return a default value.`;

const unimplementedError = (platform: 'web' | 'ios', methodName: string) =>
  new Error(`AlarmManager.${methodName} is not available on ${platform}. The '@mahermaker/android-alarm-manager' plugin is Android-only.`);

class UnimplementedAlarmManager implements AlarmManagerPlugin {
  constructor(private platform: 'web' | 'ios') {
    console.warn(
      `The '@mahermaker/android-alarm-manager' plugin is loaded on ${this.platform}, but it is Android-only. Most functions will not work as expected.`
    );
  }

  set(_options: AlarmConfig): Promise<{ alarmId: string; }> {
    console.warn(unimplementedWarning(this.platform, 'set'));
    return Promise.reject(unimplementedError(this.platform, 'set'));
  }

  cancel(_options: { alarmId: string; }): Promise<void> {
    console.warn(unimplementedWarning(this.platform, 'cancel'));
    return Promise.reject(unimplementedError(this.platform, 'cancel'));
  }

  isScheduled(_options: { alarmId: string; }): Promise<{ isScheduled: boolean; }> {
    console.warn(unimplementedWarning(this.platform, 'isScheduled'));
    return Promise.resolve({ isScheduled: false }); // Benign default
  }

  // New generic permission methods
  checkPermissions(options: { permission: PermissionName; }): Promise<PermissionStatusResult> {
    console.warn(unimplementedWarning(this.platform, `checkPermissions for ${options.permission} (Android-only method)`));
    // For web/iOS, assume permission is not applicable or granted by default if no specific web API exists
    return Promise.resolve({ status: 'granted' });
  }

  requestPermissions(options: { permission: PermissionName; }): Promise<PermissionStatusResult> {
    console.warn(unimplementedWarning(this.platform, `requestPermissions for ${options.permission} (Android-only method)`));
    // For web/iOS, assume permission is not applicable or granted by default
    return Promise.resolve({ status: 'granted' });
  }

  // Old permission methods (to be deprecated)
  /** @deprecated Use checkPermissions({ permission: 'SYSTEM_ALERT_WINDOW' }) instead. */
  canDrawOverlays(): Promise<{ value: boolean; }> {
    console.warn(unimplementedWarning(this.platform, 'canDrawOverlays (deprecated, use checkPermissions)'));
    return this.checkPermissions({permission: 'SYSTEM_ALERT_WINDOW'}).then(res => ({value: res.status === 'granted'}));
  }

  /** @deprecated Use requestPermissions({ permission: 'SYSTEM_ALERT_WINDOW' }) instead. */
  requestDrawOverlaysPermission(): Promise<void> {
    console.warn(unimplementedWarning(this.platform, 'requestDrawOverlaysPermission (deprecated, use requestPermissions)'));
    return this.requestPermissions({permission: 'SYSTEM_ALERT_WINDOW'}).then(() => undefined);
  }

  /** @deprecated Use checkPermissions({ permission: 'SCHEDULE_EXACT_ALARM' }) instead. */
  canScheduleExactAlarms(): Promise<{ value: boolean; }> {
    console.warn(unimplementedWarning(this.platform, 'canScheduleExactAlarms (deprecated, use checkPermissions)'));
    return this.checkPermissions({permission: 'SCHEDULE_EXACT_ALARM'}).then(res => ({value: res.status === 'granted'}));
  }

  /** @deprecated Use requestPermissions({ permission: 'SCHEDULE_EXACT_ALARM' }) instead. */
  requestScheduleExactAlarmPermission(): Promise<void> {
    console.warn(unimplementedWarning(this.platform, 'requestScheduleExactAlarmPermission (deprecated, use requestPermissions)'));
    return this.requestPermissions({permission: 'SCHEDULE_EXACT_ALARM'}).then(() => undefined);
  }

  addListener(eventName: string, _listenerFunc: (eventData: any) => void): Promise<PluginListenerHandle> & PluginListenerHandle {
    console.warn(unimplementedWarning(this.platform, `addListener for event '${eventName}'`));

    const removeMethod = async (): Promise<void> => {
      // No-op for unimplemented web/ios listener removal
      console.warn(unimplementedWarning(this.platform, `Attempted to remove listener for event '${eventName}', but this is an unimplemented ${this.platform} stub.`));
    };

    const actualHandle: PluginListenerHandle = {
      remove: removeMethod,
    };

    const promise = Promise.resolve(actualHandle);

    // Cast the promise and attach the remove method to satisfy Promise<PluginListenerHandle> & PluginListenerHandle
    const result = promise as (Promise<PluginListenerHandle> & PluginListenerHandle);
    result.remove = removeMethod;

    return result;
  }

  removeAllListeners(): Promise<void> {
    console.warn(unimplementedWarning(this.platform, 'removeAllListeners'));
    return Promise.resolve();
  }
}

const AlarmManager = registerPlugin<AlarmManagerPlugin>('AlarmManager', {
  web: () => new UnimplementedAlarmManager('web'),
  ios: () => new UnimplementedAlarmManager('ios'),
  // Native Android implementation will be linked automatically by Capacitor
  // based on the plugin name 'AlarmManager' and its presence in the android directory.
});

export * from './definitions';
export { AlarmManager };
