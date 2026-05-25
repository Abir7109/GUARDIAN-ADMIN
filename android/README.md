# Guardian — Anti-Theft

A Samsung One UI 6 anti-theft Android app that detects when your phone is stolen, triggers a piercing alarm, locks the device, sends the thief's location to emergency contacts, and actively resists shutdown attempts.

---

## Features

### 1. Protection Arm / Disarm
- **Shield toggle** on the home screen arms or disarms the full protection system.
- When arming, the user sets a **PIN** via `GuardLockActivity`.
- Location tracking begins in the background every 5 minutes.
- Protection status is persisted in `SharedPreferences` and synced to Firebase.

### 2. Trigger Sources (Alarm Activation)
The alarm activates when any of these detect the trigger keyword (default: **"Where are you"**):

- **SMS Receiver** — Listens for incoming SMS containing the keyword. Sends an automatic response with GPS location, device info, and battery level.
- **WhatsApp / Notification Monitor** — Uses `NotificationListenerService` to read WhatsApp/IM notifications. If the keyword is found, auto-replies via `RemoteInput` with the same emergency message.
- **Firebase Remote Trigger** — Admin can remotely trigger the alarm via FCM push.

### 3. Safe Location
- **Safe Location card** on the home screen with an interactive **OpenStreetMap (Leaflet)** map.
- Toggle ON captures current GPS position as the **safe zone center** (default 50m radius).
- The map is fully interactive: **tap** to place, **drag** the marker, **pinch-zoom**.
- "SET SAFE LOCATION" button opens a **full-screen map** for precise placement, returning coordinates on confirm.
- **Alarm suppression**: When a trigger event fires, the app checks if the current location is within the safe zone. If yes, the alarm is silently suppressed — no siren, no lock screen.

### 4. Power-Off Resistance (Samsung One UI 6)
Samsung processes the power button long-press at system level — `onKeyEvent()` cannot intercept it. The app uses a multi-layer approach:

- **Window Watcher** (`SecurePhoneAccessibilityService`):
  - Monitors `TYPE_WINDOW_STATE_CHANGED` / `TYPE_WINDOWS_CHANGED` events.
  - Detects Samsung's power menu class: `SamsungGlobalActionsDialogBase$ActionsDialog`.
  - **During alarm**: dismisses the power menu via `GLOBAL_ACTION_LOCK_SCREEN`, then re-launches the alarm overlay after 150ms.
  - **During protection (no alarm)**: triggers **Emergency Protocol** — vibrates, sends SMS to emergency contacts, locks the phone with `GuardLockActivity`.
- **Key Event Blocking**: Short-press power, volume down, and volume up keys are consumed during alarm state.
- **Shutdown Receiver**: `ACTION_SHUTDOWN` is caught as a final backup — sends last-known GPS location + event log to Firebase with a 5-second timeout.

### 5. Emergency Protocol
When a power-off attempt is detected while protection is ON (not yet in alarm state):

1. Phone **vibrates** immediately.
2. **SMS sent** to every saved emergency contact with:
   - Device model, location (Google Maps link), battery level, network type, timestamp.
3. Location uploaded to **Firebase**.
4. Event logged as `emergency_triggered`.
5. Screen **locked** with `GuardLockActivity` PIN overlay.

### 6. Alarm Overlay (`AlarmOverlayActivity`)
- Full-screen lock overlay with **PIN** entry.
- Uses `startLockTask()` for **kiosk mode** — thief cannot leave.
- **Hidden volume-up 3×** silences siren and shows PIN entry.
- **Focus watchdog**: If focus is lost (e.g., power menu), re-asserts within 50ms.
- **5 wrong PINs → 30-second lockout** (siren continues).
- Siren plays at **max volume** with vibration, re-checked every 500ms via a volume guard thread.
- Location fetched on alarm start (5 retries, 5-second intervals), uploaded to Firebase.

### 7. Defense Features (`ProtectionService`)
Background service running while armed:

- **Pocket Shield** — Proximity sensor triggers alarm if device is removed from pocket/dark place.
- **Charger Unplug** — Alarm when power adapter is disconnected.
- **SIM Change Alert** — Alarm on SIM removal.
- **USB Block** — Alarm on USB connection.
- **Location Tracking** — GPS coordinates uploaded to Firebase every 5 minutes.

### 8. Profile Tab
- **Real user data** loaded from Firebase (display name, email, phone, clearance level, membership status).
- **Edit Profile** — Inline dialog to update display name and phone number, saved to Firestore.
- **Alert Preferences** — Editable trigger keyword (default: "Where are you").
- **Emergency Contacts** — Add contacts via **device contact picker** (`ACTION_PICK` with `Phone.CONTENT_URI`). Each contact shows name, phone, call button, and delete (✕). Saved to `SharedPreferences`. Used by Emergency Protocol for SMS alerts.
- **Security Logs** — Last 10 events loaded from Firebase Firestore with formatted date, source, and details.
- **Logout** — Signs out of Firebase, clears preferences, returns to splash screen.

### 9. Setup Wizard
6-step initialization on first launch:
1. Notification Listener permission
2. Accessibility Service permission
3. Device Admin policy
4. Overlay permission
5. Location permission (fine + background)
6. SMS permission

### 10. Admin Panel (Firebase)
Remote admin capabilities via Firestore + FCM:
- Remote alarm trigger, lock, stop alarm
- Force update notifications
- Policy config push (trigger keyword, PIN attempts, siren type, volume enforcement)
- Global announcements
- User block/unblock
- Event log monitoring and resolution
- Dashboard metrics

### 11. Firebase Integration
- **Authentication**: Email/password + Google Sign-In.
- **Firestore collections**: `users`, `events`, `sessions`, `appConfig`, `admins`, `broadcasts`, `analytics`, `audit_logs`, `policies`, `metrics`.
- **Remote Config**: Force update, maintenance mode, global messages.
- **Cloud Messaging (FCM)**: Admin commands, remote triggers.

---

## Architecture

### Key Components

| Component | Role |
|---|---|
| `MainActivity` | Bottom nav (Home, Features, Profile) |
| `HomeFragment` | Shield toggle, safe zone map, status |
| `FeaturesFragment` | 4 defense toggles |
| `ProfileFragment` | User data, emergency contacts, logs, settings |
| `ProtectionService` | Background monitoring (sensors, USB, SIM, location) |
| `SirenService` | Foreground audio + vibration loop |
| `AlarmOverlayActivity` | PIN lock screen overlay with kiosk mode |
| `GuardLockActivity` | PIN set screen on arm |
| `NotificationMonitorService` | WhatsApp/IM keyword detection + auto-reply |
| `SmsReceiver` | SMS keyword detection + auto-reply |
| `SecurePhoneAccessibilityService` | Power menu detection, key event blocking, emergency trigger |
| `ShutdownReceiver` | Final location + log on shutdown |
| `TriggerManager` | Orchestrates alarm: location → SMS reply → siren → overlay |
| `EmergencyNotifier` | Sends SMS + Firebase on power-off attempt |
| `MainViewModel` | Shared ViewModel for arm state + feature toggles |
| `FirebaseManager` | All Firebase interactions (auth, Firestore, FCM, Remote Config) |
| `PreferencesManager` | SharedPreferences wrapper |
| `LocationHelper` | GPS / fused location provider with fallbacks |
| `NotificationReplier` | WhatsApp RemoteInput reply sender |

### Data Flow
```
Trigger (SMS/WhatsApp/Admin)
  → TriggerManager.triggerBeast()
    → Check safe zone (suppress if inside)
    → Log event to Firestore
    → Get GPS location + system info
    → Send SMS reply + WhatsApp RemoteInput reply
    → Upload location to Firebase
    → Start SirenService (max volume + vibration)
    → Launch AlarmOverlayActivity (PIN lock + kiosk)
    → AccessibilityService blocks power button + power menu
```

```
Power-off attempt (protection ON, no alarm)
  → AccessibilityService detects power menu window
  → EmergencyNotifier.triggerEmergency()
    → Vibrate
    → Send SMS to all emergency contacts
    → Upload location + log event to Firebase
  → GLOBAL_ACTION_LOCK_SCREEN
  → Launch GuardLockActivity
```

---

## Technical Details

### Samsung One UI 6 Specifics
- Power menu class: `com.samsung.android.globalactions.presentation.view.SamsungGlobalActionsDialogBase$ActionsDialog`
- `FLAG_REQUEST_FILTER_KEY_EVENTS` must be set **programmatically** in `onServiceConnected()` (not in XML) for Samsung to accept it.
- `flagRequestTouchExplorationMode` breaks touch interaction — must NOT be included.
- `ACTION_CLOSE_SYSTEM_DIALOGS` broadcast is ignored by Samsung One UI 6 from third-party apps.
- `GLOBAL_ACTION_BACK` does NOT dismiss Samsung power menu — `GLOBAL_ACTION_LOCK_SCREEN` is required.

### Dependencies
- AndroidX, Material Components, ConstraintLayout
- Firebase (Auth, Firestore, Messaging, Config, Analytics)
- Google Play Services (Auth, Location)
- Kotlin Coroutines
- Lottie Animations
- Leaflet.js (OpenStreetMap via WebView)

### Permissions
`INTERNET`, `FOREGROUND_SERVICE`, `RECEIVE_BOOT_COMPLETED`, `SYSTEM_ALERT_WINDOW`,
`SEND_SMS`, `RECEIVE_SMS`, `READ_SMS`, `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`,
`ACCESS_BACKGROUND_LOCATION`, `READ_PHONE_STATE`, `VIBRATE`, `WAKE_LOCK`,
`POST_NOTIFICATIONS`, `READ_CONTACTS`
