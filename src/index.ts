import { registerPlugin } from '@capacitor/core';

import type { AlarmManagerPlugin } from './definitions';

const AlarmManager = registerPlugin<AlarmManagerPlugin>('AlarmManager', {
  web: () => import('./web').then((m) => new m.AlarmManagerWeb()),
});

export * from './definitions';
export { AlarmManager };
