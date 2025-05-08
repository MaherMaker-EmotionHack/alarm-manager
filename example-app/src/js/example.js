import { AlarmManager, AlarmAction } from 'alarm-manager';

// --- DOM Elements ---
// Permissions
const checkOverlayPermissionBtn = document.getElementById('checkOverlayPermission');
const requestOverlayPermissionBtn = document.getElementById('requestOverlayPermission');
const overlayPermissionStatusSpan = document.getElementById('overlayPermissionStatus');

const checkExactAlarmPermissionBtn = document.getElementById('checkExactAlarmPermission');
const requestExactAlarmPermissionBtn = document.getElementById('requestExactAlarmPermission');
const exactAlarmPermissionStatusSpan = document.getElementById('exactAlarmPermissionStatus');

const checkNotificationPermissionBtn = document.getElementById('checkNotificationPermission');
const requestNotificationPermissionBtn = document.getElementById('requestNotificationPermission');
const notificationPermissionStatusSpan = document.getElementById('notificationPermissionStatus');

// Schedule Alarm
const alarmIdInput = document.getElementById('alarmId');
const alarmTimeInput = document.getElementById('alarmTime');
const alarmTitleInput = document.getElementById('alarmTitle');
const alarmBodyInput = document.getElementById('alarmBody');
const exactAlarmCheckbox = document.getElementById('exactAlarm');
const scheduleAlarmBtn = document.getElementById('scheduleAlarm');

// Manage Alarm
const manageAlarmIdInput = document.getElementById('manageAlarmId');
const cancelAlarmBtn = document.getElementById('cancelAlarm');
const isScheduledBtn = document.getElementById('isScheduled');
const checkedAlarmIdSpan = document.getElementById('checkedAlarmId');
const isScheduledStatusSpan = document.getElementById('isScheduledStatus');

// Alarm Status & Events
const lastScheduledAlarmIdSpan = document.getElementById('lastScheduledAlarmId');
const lastScheduledTimeSpan = document.getElementById('lastScheduledTime');
const eventLogDiv = document.getElementById('eventLog');

// --- Utility Functions ---
function logEvent(eventName, data) {
  if (!eventLogDiv) {
    // Fallback to console if eventLogDiv is not found (e.g., if script runs before DOM is fully ready)
    console.log(`[${new Date().toLocaleTimeString()}] ${eventName}:`, data);
    return;
  }
  const logEntry = document.createElement('p');
  // Improved serialization for undefined and Error objects
  const dataString = data === undefined ? 'undefined' : JSON.stringify(data, (key, value) => {
    if (value instanceof Error) {
      const errorObject = { message: value.message, name: value.name };
      // Include stack if available and non-empty
      if (value.stack) {
        errorObject.stack = value.stack;
      }
      return errorObject;
    }
    return value;
  }, 2); // Using 2 for pretty print indentation

  logEntry.textContent = `[${new Date().toLocaleTimeString()}] ${eventName}: ${dataString}`;
  if (eventLogDiv.firstChild) {
    eventLogDiv.insertBefore(logEntry, eventLogDiv.firstChild);
  } else {
    eventLogDiv.appendChild(logEntry);
  }
}

function updateDateTimeInput() {
  const now = new Date();
  now.setMinutes(now.getMinutes() + 5); // Default to 5 minutes in the future
  now.setSeconds(0);
  now.setMilliseconds(0);
  alarmTimeInput.value = now.toISOString().slice(0, 16);
}

// --- Permission Handling ---
async function checkOverlayPerm() {
  try {
    const result = await AlarmManager.checkPermissions({ permission: 'SYSTEM_ALERT_WINDOW' });
    logEvent('checkOverlayPermission RawResult', result); // Log raw result
    if (result && typeof result.status === 'string') {
      overlayPermissionStatusSpan.textContent = result.status;
    } else {
      overlayPermissionStatusSpan.textContent = 'Unknown/Error';
      logEvent('checkOverlayPermissionWarning', { message: 'Received unexpected result structure', data: result });
    }
  } catch (e) {
    overlayPermissionStatusSpan.textContent = 'Exception';
    logEvent('checkOverlayPermissionError', e);
  }
}

async function requestOverlayPerm() {
  try {
    const result = await AlarmManager.requestPermissions({ permission: 'SYSTEM_ALERT_WINDOW' });
    logEvent('requestOverlayPermission RawResult', result);
    if (result && typeof result.status === 'string') {
      overlayPermissionStatusSpan.textContent = result.status;
    } else {
      overlayPermissionStatusSpan.textContent = 'Unknown/Error';
      logEvent('requestOverlayPermissionWarning', { message: 'Received unexpected result structure', data: result });
    }
  } catch (e) {
    overlayPermissionStatusSpan.textContent = 'Exception';
    logEvent('requestOverlayPermissionError', e);
  }
}

async function checkExactAlarmPerm() {
  try {
    const result = await AlarmManager.checkPermissions({ permission: 'SCHEDULE_EXACT_ALARM' });
    logEvent('checkExactAlarmPermission RawResult', result);
    if (result && typeof result.status === 'string') {
      exactAlarmPermissionStatusSpan.textContent = result.status;
    } else {
      exactAlarmPermissionStatusSpan.textContent = 'Unknown/Error';
      logEvent('checkExactAlarmPermissionWarning', { message: 'Received unexpected result structure', data: result });
    }
  } catch (e) {
    exactAlarmPermissionStatusSpan.textContent = 'Exception';
    logEvent('checkExactAlarmPermissionError', e);
  }
}

async function requestExactAlarmPerm() {
  try {
    const result = await AlarmManager.requestPermissions({ permission: 'SCHEDULE_EXACT_ALARM' });
    logEvent('requestExactAlarmPermission RawResult', result);
    if (result && typeof result.status === 'string') {
      exactAlarmPermissionStatusSpan.textContent = result.status;
    } else {
      exactAlarmPermissionStatusSpan.textContent = 'Unknown/Error';
      logEvent('requestExactAlarmPermissionWarning', { message: 'Received unexpected result structure', data: result });
    }
  } catch (e) {
    exactAlarmPermissionStatusSpan.textContent = 'Exception';
    logEvent('requestExactAlarmPermissionError', e);
  }
}

async function checkNotificationPerm() {
  try {
    const result = await AlarmManager.checkPermissions({ permission: 'POST_NOTIFICATIONS' });
    logEvent('checkNotificationPermission RawResult', result);
    if (result && typeof result.status === 'string') {
      notificationPermissionStatusSpan.textContent = result.status;
    } else {
      notificationPermissionStatusSpan.textContent = 'Unknown/Error';
      logEvent('checkNotificationPermissionWarning', { message: 'Received unexpected result structure', data: result });
    }
  } catch (e) {
    notificationPermissionStatusSpan.textContent = 'Exception';
    logEvent('checkNotificationPermissionError', e);
  }
}

async function requestNotificationPerm() {
  try {
    const result = await AlarmManager.requestPermissions({ permission: 'POST_NOTIFICATIONS' });
    logEvent('requestNotificationPermission RawResult', result);
    if (result && typeof result.status === 'string') {
      notificationPermissionStatusSpan.textContent = result.status;
    } else {
      notificationPermissionStatusSpan.textContent = 'Unknown/Error';
      logEvent('requestNotificationPermissionWarning', { message: 'Received unexpected result structure', data: result });
    }
  } catch (e) {
    notificationPermissionStatusSpan.textContent = 'Exception';
    logEvent('requestNotificationPermissionError', e);
  }
}

// --- Alarm Actions ---
async function scheduleAlarm() {
  const alarmId = parseInt(alarmIdInput.value, 10);
  const at = new Date(alarmTimeInput.value).getTime();
  const title = alarmTitleInput.value || undefined;
  const body = alarmBodyInput.value || undefined;
  const exact = exactAlarmCheckbox.checked;

  if (isNaN(alarmId) || isNaN(at)) {
    logEvent('scheduleAlarmError', { error: 'Invalid Alarm ID or Time' });
    console.error('Please enter a valid Alarm ID and Time.');
    return;
  }

  const alarmDetails = {
    alarmId: alarmId,
    at: at,
    title: title,
    body: body,
    exact: exact,
    actions: [
      { id: 'snooze', title: 'Snooze (1 min)' },
      { id: 'dismiss', title: 'Dismiss' }
    ]
  };

  try {
    logEvent('scheduleAlarmAttempt', alarmDetails);
    const result = await AlarmManager.set(alarmDetails);
    logEvent('scheduleAlarmSuccess', result);
    lastScheduledAlarmIdSpan.textContent = alarmId.toString();
    lastScheduledTimeSpan.textContent = new Date(at).toLocaleString();
    console.log(`Alarm ${alarmId} scheduled for ${new Date(at).toLocaleString()}`);
  } catch (e) {
    logEvent('scheduleAlarmError', e);
    console.error('Error scheduling alarm:', e);
  }
}

async function cancelAlarm() {
  const alarmId = parseInt(manageAlarmIdInput.value, 10);
  if (isNaN(alarmId)) {
    logEvent('cancelAlarmError', { error: 'Invalid Alarm ID' });
    console.error('Please enter a valid Alarm ID.');
    return;
  }
  try {
    logEvent('cancelAlarmAttempt', { alarmId });
    const result = await AlarmManager.cancel({ alarmId });
    logEvent('cancelAlarmSuccess', result);
    console.log(`Alarm ${alarmId} cancel attempt finished. Result:`, result);
    if (lastScheduledAlarmIdSpan.textContent === alarmId.toString()) {
      lastScheduledAlarmIdSpan.textContent = 'N/A';
      lastScheduledTimeSpan.textContent = 'N/A';
    }
  } catch (e) {
    logEvent('cancelAlarmError', e);
    console.error('Error cancelling alarm:', e);
  }
}

async function checkIsScheduled() {
  const alarmId = parseInt(manageAlarmIdInput.value, 10);
  if (isNaN(alarmId)) {
    logEvent('isScheduledError', { error: 'Invalid Alarm ID' });
    console.error('Please enter a valid Alarm ID.');
    return;
  }
  try {
    logEvent('isScheduledAttempt', { alarmId });
    const result = await AlarmManager.isScheduled({ alarmId });
    logEvent('isScheduledSuccess', result);
    checkedAlarmIdSpan.textContent = alarmId.toString();

    if (result && typeof result.isScheduled === 'boolean') {
      isScheduledStatusSpan.textContent = result.isScheduled ? 'Scheduled' : 'Not Scheduled';
      console.log(`Alarm ${alarmId} is ${result.isScheduled ? 'Scheduled' : 'Not Scheduled'}.`);
    } else {
      isScheduledStatusSpan.textContent = 'Unknown/Error';
      logEvent('isScheduledWarning', { message: 'Unexpected result structure', data: result });
      console.warn(`Alarm ${alarmId} status could not be determined. Result:`, result);
    }
  } catch (e) {
    logEvent('isScheduledError', e);
    checkedAlarmIdSpan.textContent = alarmId.toString();
    isScheduledStatusSpan.textContent = 'Error checking status';
    console.error(`Error checking alarm status for ID ${alarmId}:`, e);
  }
}

// --- Event Listeners ---
document.addEventListener('DOMContentLoaded', () => {
  // Set default alarm time
  updateDateTimeInput();

  // Permissions
  checkOverlayPermissionBtn.addEventListener('click', checkOverlayPerm);
  requestOverlayPermissionBtn.addEventListener('click', requestOverlayPerm);
  checkExactAlarmPermissionBtn.addEventListener('click', checkExactAlarmPerm);
  requestExactAlarmPermissionBtn.addEventListener('click', requestExactAlarmPerm);
  checkNotificationPermissionBtn.addEventListener('click', checkNotificationPerm);
  requestNotificationPermissionBtn.addEventListener('click', requestNotificationPerm);

  // Initial permission checks
  checkOverlayPerm();
  checkExactAlarmPerm();
  checkNotificationPerm();

  // Schedule Alarm
  scheduleAlarmBtn.addEventListener('click', scheduleAlarm);

  // Manage Alarm
  cancelAlarmBtn.addEventListener('click', cancelAlarm);
  isScheduledBtn.addEventListener('click', checkIsScheduled);

  // Plugin Event Listeners
  AlarmManager.addListener('alarmFired', (info) => {
    logEvent('alarmFired', info);
    console.log('[EVENT] Alarm Fired:', info);
  });

  AlarmManager.addListener('alarmAction', (info) => {
    logEvent('alarmAction', info);
    console.log('[EVENT] Alarm Action:', info);

    if (info && info.actionId === 'snooze') {
      if (info.alarm && typeof info.alarm === 'object' && info.alarm !== null) {
        if (typeof info.alarm.alarmId === 'undefined') {
          logEvent('alarmActionSnoozeError', { message: 'Snooze action for alarm without alarmId.', receivedInfo: info });
          return;
        }

        const snoozeTime = Date.now() + 60000;

        const snoozeAlarm = {
          ...info.alarm,
          at: snoozeTime,
          title: info.alarm.title ? `(Snoozed) ${info.alarm.title}` : 'Snoozed Alarm',
        };

        AlarmManager.set(snoozeAlarm).then(() => {
          logEvent('snoozeRescheduled', { alarmId: snoozeAlarm.alarmId, at: snoozeAlarm.at, title: snoozeAlarm.title });
          if (lastScheduledAlarmIdSpan) lastScheduledAlarmIdSpan.textContent = snoozeAlarm.alarmId.toString();
          if (lastScheduledTimeSpan) lastScheduledTimeSpan.textContent = new Date(snoozeTime).toLocaleString();
        }).catch(e => {
          logEvent('snoozeRescheduleError', { error: e, alarmDetails: snoozeAlarm });
        });
      } else {
        logEvent('alarmActionSnoozeIgnored', { message: 'Snooze action received but alarm data is missing, null, or not an object.', receivedInfo: info });
      }
    }
  });

  AlarmManager.addListener('alarmDismissed', (info) => {
    logEvent('alarmDismissed', info);
    console.log('[EVENT] Alarm Dismissed:', info);
    // You might want to update UI elements here if an alarm is dismissed
    // For example, if the dismissed alarm was the last scheduled one:
    if (info && info.alarmId && lastScheduledAlarmIdSpan.textContent === info.alarmId.toString()) {
        lastScheduledAlarmIdSpan.textContent = 'N/A';
        lastScheduledTimeSpan.textContent = 'N/A';
        // Also update the specific alarm's status if you are tracking it
        if (manageAlarmIdInput.value === info.alarmId.toString()) {
            checkedAlarmIdSpan.textContent = info.alarmId.toString();
            isScheduledStatusSpan.textContent = 'Dismissed/Cancelled';
        }
    }
  });

  AlarmManager.addListener('permissionStatusChanged', (info) => {
    logEvent('permissionStatusChanged', info);
    if (info.permission === 'SYSTEM_ALERT_WINDOW') checkOverlayPerm();
    if (info.permission === 'SCHEDULE_EXACT_ALARM') checkExactAlarmPerm();
    if (info.permission === 'POST_NOTIFICATIONS') checkNotificationPerm();
  });

  logEvent('exampleJsLoaded', { message: 'Example JS initialized and listeners attached.' });
});

// Expose to window for easier debugging if needed, not strictly necessary
window.AlarmManager = AlarmManager;
window.exampleApp = {
  scheduleAlarm,
  cancelAlarm,
  checkIsScheduled,
  checkOverlayPerm,
  requestOverlayPerm,
  checkExactAlarmPerm,
  requestExactAlarmPerm,
  checkNotificationPerm,
  requestNotificationPerm,
  logEvent
};
