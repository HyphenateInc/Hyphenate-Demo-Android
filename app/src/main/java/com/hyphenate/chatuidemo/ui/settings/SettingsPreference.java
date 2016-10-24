package com.hyphenate.chatuidemo.ui.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import com.hyphenate.chatuidemo.R;

public class SettingsPreference extends PreferenceFragment {

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_settings);
    }
}
