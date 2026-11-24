# Android notification settings

The **Notifications** switch on the main Settings screen controls whether Pending can post push notifications for the signed-in account. Use **Notifications Settings** just below it to adjust icon style, sound, vibration, and email. Changes save immediately. Turning the master switch off preserves the submenu choices.

## Storage

Android stores these values in private `SharedPreferences` named `pending_notification_preferences_<userId>`. `userId` is the backend account ID supplied by `SessionManager`. For older sessions saved without an ID, the app reads the account ID in the server-issued session token to recover the local storage namespace. Signing out clears the session. Signing back in as the same user restores their choices on that installation.

| Key | Type | Default |
| --- | --- | --- |
| `notifications_enabled` | Boolean | `true` |
| `icon_style` | String, `small` or `large` | `small` |
| `sound_enabled` | Boolean | `true` |
| `vibration_enabled` | Boolean | `true` |
| `email_enabled` | Boolean | `false` |

Use `com.pending.settings.NotificationPreferences` to access the store.

```java
NotificationPreferences preferences = NotificationPreferences.forSignedInUser(context);
if (preferences == null || preferences.getUserId() != recipientUserId) {
    return;
}

NotificationPreferences.IconStyle iconStyle = preferences.getIconStyle();
boolean soundEnabled = preferences.isSoundEnabled();
boolean vibrationEnabled = preferences.isVibrationEnabled();
boolean emailEnabled = preferences.isEmailEnabled();
```

Resolve the store when handling an event or syncing a preference and compare its account ID with the event or request owner. A missing session or missing account ID returns `null`. Each resolved instance stays associated with its original account.

## Push integration for Sophia

Resolve the recipient's active account and check `isNotificationsEnabled()` before posting each push notification.

```java
NotificationPreferences preferences = NotificationPreferences.forSignedInUser(context);
if (preferences == null
        || preferences.getUserId() != recipientUserId
        || !preferences.isNotificationsEnabled()) {
    return;
}

// Use the saved icon style, sound, and vibration to post the notification.
```

If dispatch happens on the server and must be suppressed there, sync the master value to the authenticated account before dispatch. The value is currently stored on Android.

Read the icon style, sound, and vibration preferences when posting a notification. Android's status-bar small icon is required for both styles and its size is system-controlled. Use the saved style to choose the size of app-controlled notification artwork, using the large icon slot or expanded content as appropriate. Keep sound and vibration independent when selecting a notification channel on Android 8 and later. Android controls channel behavior once the channel exists, and system permission and channel settings still apply. [Android notification creation](https://developer.android.com/develop/ui/compose/notifications/create-notification) and [channel behavior](https://developer.android.com/develop/ui/compose/notifications/channels) describe these constraints.

## Email integration for Mahdi

Read `isEmailEnabled()` as the user's opt-in. Server-originated email requires syncing this choice to the authenticated backend account and gating delivery on that server-side value. Sync the opt-out when the value switches off, use the account's verified email address for delivery, and initialize new server opt-ins to off. Decide how to reconcile multiple devices when adding sync. This change stores the choice on Android. Push and email delivery and server sync are future integrations.
