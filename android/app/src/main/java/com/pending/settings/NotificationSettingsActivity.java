package com.pending.settings;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.pending.R;

public final class NotificationSettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NotificationPreferences preferences = NotificationPreferences.forSignedInUser(this);
        if (preferences == null) {
            Toast.makeText(this, R.string.settings_notifications_sign_in, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_notification_settings);
        View root = findViewById(R.id.notificationSettingsRoot);
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        ViewCompat.requestApplyInsets(root);

        int background = ContextCompat.getColor(this, R.color.notification_settings_background);
        boolean lightBackground = ColorUtils.calculateLuminance(background) > 0.5;
        WindowInsetsControllerCompat bars = WindowCompat.getInsetsController(getWindow(), root);
        bars.setAppearanceLightStatusBars(lightBackground);
        bars.setAppearanceLightNavigationBars(lightBackground);

        findViewById(R.id.notificationSettingsBack).setOnClickListener(view -> finish());
        ViewCompat.setAccessibilityHeading(findViewById(R.id.notificationSettingsTitle), true);
        ViewCompat.setAccessibilityHeading(findViewById(R.id.notificationIconStyleTitle), true);

        RadioGroup iconStyle = findViewById(R.id.notificationIconStyle);
        iconStyle.check(preferences.getIconStyle() == NotificationPreferences.IconStyle.LARGE
                ? R.id.notificationIconLarge : R.id.notificationIconSmall);
        iconStyle.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.notificationIconSmall) {
                preferences.setIconStyle(NotificationPreferences.IconStyle.SMALL);
            } else if (checkedId == R.id.notificationIconLarge) {
                preferences.setIconStyle(NotificationPreferences.IconStyle.LARGE);
            }
        });

        MaterialSwitch sound = findViewById(R.id.notificationSound);
        sound.setChecked(preferences.isSoundEnabled());
        sound.setOnCheckedChangeListener((button, checked) -> preferences.setSoundEnabled(checked));

        MaterialSwitch vibration = findViewById(R.id.notificationVibration);
        vibration.setChecked(preferences.isVibrationEnabled());
        vibration.setOnCheckedChangeListener((button, checked) -> preferences.setVibrationEnabled(checked));

        MaterialSwitch email = findViewById(R.id.notificationEmail);
        email.setChecked(preferences.isEmailEnabled());
        email.setOnCheckedChangeListener((button, checked) -> preferences.setEmailEnabled(checked));
    }
}
