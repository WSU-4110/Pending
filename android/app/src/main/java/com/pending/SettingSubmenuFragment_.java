package com.pending;

import android.os.Bundle;
import android.os.Debug;
import android.text.Layout;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;

import java.io.Console;

public abstract class SettingSubmenuFragment_ extends PreferenceFragmentCompat { //Abstract for each submenu to extend from
    String titleText;
    //Layout settingView;

    public boolean ApplySetting(){
        //Check network connectivity
        //Apply settings if true
        Log.d("Pending", "Applied the Settings");
        return true;
    }

//    @Override
//    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
//
//    }
}
