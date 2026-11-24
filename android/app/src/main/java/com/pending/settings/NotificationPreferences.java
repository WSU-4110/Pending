package com.pending.settings;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.pending.network.SessionManager;

/** Notification choices stored on this installation for one Pending account. */
public final class NotificationPreferences {
    private static final String FILE_PREFIX = "pending_notification_preferences_";
    private static final String KEY_NOTIFICATIONS = "notifications_enabled";
    private static final String KEY_ICON_STYLE = "icon_style";
    private static final String KEY_SOUND = "sound_enabled";
    private static final String KEY_VIBRATION = "vibration_enabled";
    private static final String KEY_EMAIL = "email_enabled";

    public enum IconStyle {
        SMALL("small"),
        LARGE("large");

        private final String storedValue;

        IconStyle(String storedValue) {
            this.storedValue = storedValue;
        }
    }

    private final long userId;
    private final SharedPreferences preferences;

    NotificationPreferences(Context context, long userId) {
        this.userId = userId;
        preferences = context.getApplicationContext().getSharedPreferences(
                FILE_PREFIX + userId, Context.MODE_PRIVATE);
    }

    /** Opens preferences for the active session, or returns null when there is no signed-in account. */
    @Nullable
    public static NotificationPreferences forSignedInUser(Context context) {
        Long userId = SessionManager.getInstance(context).getSignedInUserId();
        if (userId == null) {
            return null;
        }
        return new NotificationPreferences(context, userId);
    }

    public long getUserId() {
        return userId;
    }

    /** Returns the account's master switch for posting push notifications. */
    public boolean isNotificationsEnabled() {
        return preferences.getBoolean(KEY_NOTIFICATIONS, true);
    }

    public void setNotificationsEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply();
    }

    public IconStyle getIconStyle() {
        String saved = preferences.getString(KEY_ICON_STYLE, IconStyle.SMALL.storedValue);
        return IconStyle.LARGE.storedValue.equals(saved) ? IconStyle.LARGE : IconStyle.SMALL;
    }

    public void setIconStyle(@NonNull IconStyle style) {
        preferences.edit().putString(KEY_ICON_STYLE, style.storedValue).apply();
    }

    public boolean isSoundEnabled() {
        return preferences.getBoolean(KEY_SOUND, true);
    }

    public void setSoundEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_SOUND, enabled).apply();
    }

    public boolean isVibrationEnabled() {
        return preferences.getBoolean(KEY_VIBRATION, true);
    }

    public void setVibrationEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_VIBRATION, enabled).apply();
    }

    public boolean isEmailEnabled() {
        return preferences.getBoolean(KEY_EMAIL, false);
    }

    public void setEmailEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_EMAIL, enabled).apply();
    }
}
