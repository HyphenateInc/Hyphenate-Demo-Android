package io.agora.chatdemo.settings;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.Preference;

import io.agora.chat.ChatClient;
import io.agora.chat.ChatOptions;
import io.agora.chatdemo.R;
import io.agora.chatdemo.ui.BasePreferenceFragment;

/**
 * Cretae by lzan13 2016/10/26
 * Setting preference
 */
public class SettingsPreference extends BasePreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener{

    private Preference mAboutVersionPreference;

    private String aboutKey;
    private String autoAcceptKey;


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_settings, rootKey);
        aboutKey = getActivity().getString(R.string.em_pref_key_about);
        autoAcceptKey =
                getActivity().getString(R.string.em_pref_key_accept_group_invite_automatically);

        mAboutVersionPreference = findPreference(aboutKey);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(aboutKey)) {
            checkVersion();
        } else if (key.equals(autoAcceptKey)) {
            setAutoAcceptGroupInvites();
        }
    }

    /**
     * check version
     */
    private void checkVersion() {
        mAboutVersionPreference.setSummary(ChatClient.getInstance().VERSION);
    }

    /**
     * change auto accept group invites
     */
    private void setAutoAcceptGroupInvites() {
        ChatOptions options = ChatClient.getInstance().getOptions();
        options.setAutoAcceptGroupInvitation(!options.isAutoAcceptGroupInvitation());
    }

    @Override public void onResume() {
        super.onResume();
        checkVersion();
        getPreferenceManager().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override public void onPause() {
        getPreferenceManager().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }
}