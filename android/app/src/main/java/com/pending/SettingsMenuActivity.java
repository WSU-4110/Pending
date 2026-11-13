package com.pending;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentContainerView;

public class SettingsMenuActivity extends AppCompatActivity {

    public enum e_settingSubmenus{ //Method of allowing specific class to be specified within intent
        none,
        reminderDefaults,
        appearance,
        privData,
        authorizationSecurity,
        deleteAccountData
    }

    @Nullable SettingSubmenuFragment_ currentMenu = null;

    public static Intent newIntent(android.content.Context context, e_settingSubmenus subMenu) {
        Intent intent = new Intent(context, SettingsMenuActivity.class);
        if(subMenu != null){
            intent.putExtra("SETTING_SUBMENU", subMenu);
        }

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_submenu);

        TextView title = findViewById(R.id.subsettingTitle);
        Button applyButton = findViewById(R.id.applySubsetting);
        Button cancelButton = findViewById(R.id.cancelSubsetting); //Maybe make as reset button?
        ImageButton backButton = findViewById(R.id.backButtonSubsetting);
        FragmentContainerView settingBlock = findViewById(R.id.settingsFragmentContainer);

        Intent intent = getIntent();
        if(intent.hasExtra("SETTING_SUBMENU")){
            e_settingSubmenus subMenu = e_settingSubmenus.values()[intent.getIntExtra("SETTING_SUBMENU", 0)];
            Log.d("Pending", "Received intent:" + subMenu);


            switch(subMenu){
                case none: currentMenu =  null;
                    break;
                case reminderDefaults: currentMenu = new SettingRemindersFragment();
                    break;
                case appearance: currentMenu = new SettingAppearanceFragment();
                    break;
                case privData:
                    //break;
                case authorizationSecurity:
                    //break;
                case deleteAccountData: currentMenu =  null;
                    break;
            };

            if(currentMenu != null) {
                title.setText(currentMenu.titleText);
                applyButton.setOnClickListener(v -> currentMenu.ApplySetting());

                if(savedInstanceState == null){
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(settingBlock.getId(), currentMenu)
                            .commit();
                }
            }
        }

        cancelButton.setOnClickListener(v -> finish());
        backButton.setOnClickListener(v -> finish());
    }
}

