import { WebPlugin } from '@capacitor/core';

import type { AlarmManagerPlugin, AlarmConfig } from './definitions';

export class AlarmManagerWeb extends WebPlugin implements AlarmManagerPlugin {
  async set(options: AlarmConfig): Promise<{ alarmId: string }> {
    console.warn('AlarmManager.set() is not implemented on the web.', options);
    return { alarmId: options.alarmId };
  }

  async cancel(options: { alarmId: string }): Promise<void> {
    console.warn('AlarmManager.cancel() is not implemented on the web.', options);
    return Promise.resolve();
  }

  async isScheduled(options: { alarmId: string }): Promise<{ isScheduled: boolean }> {
    console.warn('AlarmManager.isScheduled() is not implemented on the web.', options);
    return { isScheduled: false };
  }

  async canDrawOverlays(): Promise<{value: boolean}> {
    console.warn('AlarmManager.canDrawOverlays() is not applicable on the web.');
    // Typically, web apps don't have an equivalent concept for drawing over other OS-level apps.
    // Returning true might be misleading, false is safer to indicate unavailability.
    return { value: false }; 
  }

  async requestDrawOverlaysPermission(): Promise<void> {
    console.warn('AlarmManager.requestDrawOverlaysPermission() is not applicable on the web.');
    return Promise.resolve();
  }
}
