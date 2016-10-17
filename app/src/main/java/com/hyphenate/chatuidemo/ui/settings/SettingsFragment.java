package com.hyphenate.chatuidemo.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.OnClick;
import com.hyphenate.EMCallBack;
import com.hyphenate.chatuidemo.DemoHelper;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.chatuidemo.ui.MainActivity;
import com.hyphenate.chatuidemo.ui.chat.call.VideoCallActivity;
import com.hyphenate.chatuidemo.ui.chat.call.VoiceCallActivity;
import com.hyphenate.chatuidemo.ui.sign.SignInActivity;
import com.hyphenate.easeui.EaseConstant;

/**
 * Created by lzan13 on 2016/10/11.
 */
public class SettingsFragment extends Fragment {

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.em_fragment_settings, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    /**
     * Call sign out
     */
    @OnClick(R.id.btn_sign_out) void singOut() {
        DemoHelper.getInstance().signOut(new EMCallBack() {
            @Override public void onSuccess() {
                startActivity(new Intent(getActivity(), SignInActivity.class));
                ((MainActivity) getActivity()).finish();
            }

            @Override public void onError(int i, String s) {

            }

            @Override public void onProgress(int i, String s) {

            }
        });
    }

    /**
     * Test onClick
     */
    @OnClick({ R.id.btn_call_video, R.id.btn_call_voice }) void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_call_video:
                Intent videoIntent = new Intent();
                videoIntent.setClass(getActivity(), VideoCallActivity.class);
                videoIntent.putExtra(EaseConstant.EXTRA_USER_ID, "lz2");
                videoIntent.putExtra(EaseConstant.EXTRA_IS_INCOMING_CALL, false);
                startActivity(videoIntent);
                break;
            case R.id.btn_call_voice:
                Intent voiceIntent = new Intent();
                voiceIntent.setClass(getActivity(), VoiceCallActivity.class);
                voiceIntent.putExtra(EaseConstant.EXTRA_USER_ID, "lz2");
                voiceIntent.putExtra(EaseConstant.EXTRA_IS_INCOMING_CALL, false);
                startActivity(voiceIntent);
                break;
        }
    }
}
