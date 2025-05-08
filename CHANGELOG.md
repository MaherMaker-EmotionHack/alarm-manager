# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1.0] - 2025-05-08

### Added
- Initial release of the Android Alarm Manager plugin.
- Core functionality to schedule, cancel, and check the status of alarms.
- Support for exact alarms using `AlarmManager.setExactAndAllowWhileIdle()` or `setAlarmClock()`.
- Integration with a foreground service (`AlarmForegroundService`) for improved reliability when the app is in the background or closed.
- Customizable full-screen alarm notification activity (`AlarmRingingActivity`) with options for title, name, background color, and dismiss button text.
- Ability to pass extra data (JSON string) with alarms.
- Support for custom alarm sounds (content URI).
- Comprehensive permission handling:
    - Methods to check and request permissions for `POST_NOTIFICATIONS` (Android 13+), `SCHEDULE_EXACT_ALARM` (Android 12+), and `SYSTEM_ALERT_WINDOW`.
    - Specific error messaging when exact alarm scheduling fails due to missing permissions.
- Event listener for `alarmDismissed` when the alarm is dismissed from `AlarmRingingActivity`.
- Basic `BootReceiver` included in the manifest to facilitate rescheduling alarms after device reboot (requires user implementation for alarm persistence and rescheduling logic).
- API documentation generation via `npm run docgen`.
- Initial `README.md`, `CONTRIBUTING.md`, and `CHANGELOG.md`.

---
*This changelog was started on 2025-05-08.*