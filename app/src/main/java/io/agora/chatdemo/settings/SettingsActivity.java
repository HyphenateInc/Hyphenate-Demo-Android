package io.agora.chatdemo.settings;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.preference.PreferenceFragmentCompat;

import io.agora.chatdemo.Constant;
import io.agora.chatdemo.R;
import io.agora.chatdemo.ui.BaseInitActivity;

public class SettingsActivity extends BaseInitActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.em_activity_settings);

        Intent intent = getIntent();
        String type = intent.getStringExtra("type");

        if(TextUtils.isEmpty(type)) {
            finish();
            return;
        }
        if(TextUtils.equals(type, Constant.SETTINGS_NOTIFICATION)) {
            setTitle(getResources().getString(R.string.em_pref_notification));
            showFragment(new NotificationSettingsPreference());
        }else if(TextUtils.equals(type, Constant.SETTINGS_CHAT)) {
            setTitle(getResources().getString(R.string.em_pref_chat_call));
            showFragment(new ChatSettingsPreference());
        }else {
            finish();
            return;
        }
    }

    private void showFragment(PreferenceFragmentCompat fragment) {
        if(fragment == null) {
            finish();
            return;
        }
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, fragment)
                .commit();
    }

    private void setTitle(String title) {
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setTitle(title);
        }
    }
}

