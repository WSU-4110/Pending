package com.pending;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceFragmentCompat;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingRemindersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingRemindersFragment extends SettingSubmenuFragment_ {

    boolean thirtMTimer;
    boolean dayTimer;

    public SettingRemindersFragment() {
        titleText = "Reminder Defaults";
    }

    public static SettingRemindersFragment newInstance() {
        SettingRemindersFragment fragment = new SettingRemindersFragment();
        return fragment;
    }

    @Override
    public boolean ApplySetting() {
        Log.d("Pending", "Applied Reminder Settings");
        return true;
    }

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.preferences_reminders, rootKey);
    }
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//    }
//



//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.xml.preferences_reminders, container, false);
//    }
}