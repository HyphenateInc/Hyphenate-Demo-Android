package io.agora.chatdemo.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.preference.EditTextPreference;

import io.agora.CallBack;
import io.agora.chat.ChatClient;
import io.agora.chat.PushConfigs;
import io.agora.chatdemo.R;
import io.agora.chatdemo.ui.BasePreferenceFragment;
import io.agora.chatdemo.utils.ThreadPoolManager;
import io.agora.exceptions.ChatException;
import io.agora.util.EMLog;

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

        getDisplayNameFromServer();
    }

    private void getDisplayNameFromServer() {
        ThreadPoolManager.getInstance().executeRunnable(()-> {
            PushConfigs configs = null;
            try {
                configs = ChatClient.getInstance().pushManager().getPushConfigsFromServer();
            } catch (ChatException e) {
                e.printStackTrace();
            }
            if(configs == null) {
                return;
            }
            String displayNickname = configs.getDisplayNickname();
            if(!TextUtils.isEmpty(displayNickname)) {
                if(getActivity() != null) {
                    getActivity().runOnUiThread(()-> {
                        if(getActivity() != null && !getActivity().isFinishing()) {
                            mDisplayNamePreference.setSummary(displayNickname);
                        }
                    });
                }
            }
        });

    }

    /**
     * check display name preference content
     */
    private void checkDisplayName() {
        String preName = (String) mDisplayNamePreference.getSummary();
        String displayName = mDisplayNamePreference.getText();
        if (TextUtils.isEmpty(displayName)) {
            mDisplayNamePreference.setSummary("Input display name");
        } else {
            mDisplayNamePreference.setSummary(displayName);
            updatePushDisplayName(displayName, preName);
        }
    }

    private void updatePushDisplayName(String displayName, String preName) {
        ChatClient.getInstance().pushManager().asyncUpdatePushNickname(displayName, new CallBack() {
            @Override
            public void onSuccess() {
                EMLog.d("update", "update success");
            }

            @Override
            public void onError(int code, String error) {
                if(getActivity() != null) {
                    getActivity().runOnUiThread(()-> {
                        if(getActivity() != null && !getActivity().isFinishing()) {
                            mDisplayNamePreference.setSummary(preName);
                            Toast.makeText(getActivity(), "Failed to update display name", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onProgress(int progress, String status) {

            }
        });
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

