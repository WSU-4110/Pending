package com.pending.settings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class NotificationPreferencesTest {
    @Test
    public void savedChoicesAreIndependentAndScopedToEachAccount() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        long firstAccount = Long.MIN_VALUE;
        long secondAccount = Long.MIN_VALUE + 1;
        String firstFile = "pending_notification_preferences_" + firstAccount;
        String secondFile = "pending_notification_preferences_" + secondAccount;
        context.deleteSharedPreferences(firstFile);
        context.deleteSharedPreferences(secondFile);

        try {
            NotificationPreferences preferences = new NotificationPreferences(context, firstAccount);
            assertDefaults(preferences);

            preferences.setIconStyle(NotificationPreferences.IconStyle.LARGE);
            preferences.setSoundEnabled(false);
            preferences.setEmailEnabled(true);
            preferences.setNotificationsEnabled(false);

            NotificationPreferences reopened = new NotificationPreferences(context, firstAccount);
            assertFalse(reopened.isNotificationsEnabled());
            assertEquals(NotificationPreferences.IconStyle.LARGE, reopened.getIconStyle());
            assertFalse(reopened.isSoundEnabled());
            assertTrue(reopened.isVibrationEnabled());
            assertTrue(reopened.isEmailEnabled());

            reopened.setVibrationEnabled(false);
            reopened.setSoundEnabled(true);
            assertFalse(preferences.isNotificationsEnabled());
            assertTrue(preferences.isSoundEnabled());
            assertFalse(preferences.isVibrationEnabled());
            assertTrue(preferences.isEmailEnabled());

            assertDefaults(new NotificationPreferences(context, secondAccount));

            reopened.setNotificationsEnabled(true);
            assertTrue(preferences.isNotificationsEnabled());
            assertEquals(NotificationPreferences.IconStyle.LARGE, preferences.getIconStyle());
            assertTrue(preferences.isSoundEnabled());
            assertFalse(preferences.isVibrationEnabled());
            assertTrue(preferences.isEmailEnabled());
        } finally {
            context.deleteSharedPreferences(firstFile);
            context.deleteSharedPreferences(secondFile);
        }
    }

    private static void assertDefaults(NotificationPreferences preferences) {
        assertTrue(preferences.isNotificationsEnabled());
        assertEquals(NotificationPreferences.IconStyle.SMALL, preferences.getIconStyle());
        assertTrue(preferences.isSoundEnabled());
        assertTrue(preferences.isVibrationEnabled());
        assertFalse(preferences.isEmailEnabled());
    }
}
