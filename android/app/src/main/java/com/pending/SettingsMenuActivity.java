package com.pending;

import android.content.Intent;
import android.icu.lang.UCharacter;
import android.os.Bundle;
import android.text.Layout;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.pending.model.PendingItem;

public class SettingsMenuActivity extends AppCompatActivity {

    private abstract class SettingSubmenu { //Abstract for each submenu to extend from
        String titleText;
        Layout settingView;

        boolean ApplySetting(){
            //Check network connectivity
            //Apply settings if true
            return true;
        }
    }

    public enum e_settingSubmenus{ //Method of allowing specific class to be specified within intent
        none,
        reminderDefaults,
        appearance,
        privData,
        authorizationSecurity
    }

    SettingSubmenu currentMenu;

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

        TextView title = findViewById(R.id.subsettingTitle);
        Button applyButton = findViewById(R.id.applySubsetting);
        Button cancelButton = findViewById(R.id.cancelSubSetting); //Maybe make as reset button?
        ImageButton backButton = findViewById(R.id.backButtonSubsetting);
        LinearLayout settingBlock = findViewById(R.id.settingBlockLayout);

        Intent intent = getIntent();
        if(intent.hasExtra("SETTING_SUBMENU")){
            e_settingSubmenus subMenu = e_settingSubmenus.values()[intent.getIntExtra("SETTING_SUBMENU", 0)];
            currentMenu = switch(subMenu){
                case none:
                case reminderDefaults:
                case appearance:
                case privData:
                case authorizationSecurity:
            };

            if(currentMenu != null) {
                title.setText(currentMenu.titleText);
                applyButton.setOnClickListener(v -> currentMenu.ApplySetting());
            }
        }

        cancelButton.setOnClickListener(v -> finish());
        backButton.setOnClickListener(v -> finish());
    }

}
