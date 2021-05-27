package io.agora.chatdemo.settings;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.EditTextPreference;

import io.agora.chatdemo.R;
import io.agora.chatdemo.ui.BasePreferenceFragment;

public class NotificationSettingsPreference extends BasePreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener{

    private String displayNameKey;
    private EditTextPreference mDisplayNamePreference;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_notification, rootKey);

        displayNameKey = getActivity().getString(R.string.em_pref_key_notification_display_name);
        mDisplayNamePreference =
                (EditTextPreference) findPreference(displayNameKey);
    }

    /**
     * check display name preference content
     */
    private void checkDisplayName() {
        if (mDisplayNamePreference.getText() != null && mDisplayNamePreference.getText()
                .equals("")) {
            mDisplayNamePreference.setSummary("Input display name");
        } else {
            mDisplayNamePreference.setSummary(mDisplayNamePreference.getText());
        }
    }

    @Override public void onResume() {
        super.onResume();
        checkDisplayName();
        getPreferenceManager().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override public void onPause() {
        getPreferenceManager().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(displayNameKey)) {
            checkDisplayName();
        }
    }
}

