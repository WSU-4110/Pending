package com.pending;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingAppearanceFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingAppearanceFragment extends SettingSubmenuFragment_ {


    public SettingAppearanceFragment() {
        titleText = "Appearance";
        // Required empty public constructor
    }

    public static SettingAppearanceFragment newInstance() {
        SettingAppearanceFragment fragment = new SettingAppearanceFragment();
        return fragment;
    }

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.preferences_appearance, rootKey);
    }

    @Override
    public boolean ApplySetting() {
        Log.d("Pending", "Applied Appearance Settings");
        return true;
    }

}