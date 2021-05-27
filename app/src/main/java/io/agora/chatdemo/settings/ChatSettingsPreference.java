package io.agora.chatdemo.settings;

import android.os.Bundle;

import io.agora.chatdemo.R;
import io.agora.chatdemo.ui.BasePreferenceFragment;

public class ChatSettingsPreference extends BasePreferenceFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_chat, rootKey);
    }
}

