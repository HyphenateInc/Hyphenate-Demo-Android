package com.hyphenate.chatuidemo.call;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.hyphenate.chat.EMCallStateChangeListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chatuidemo.Constant;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.util.DateUtils;

/**
 * Created by lzan13 on 2016/10/18.
 *
 * Voice call activity
 */
public class VoiceCallActivity extends CallActivity {

    @BindView(R.id.layout_root) View rootView;
    @BindView(R.id.text_call_state) TextView callStateView;
    @BindView(R.id.text_call_time) TextView callTimeView;
    @BindView(R.id.img_call_avatar) ImageView avatarView;
    @BindView(R.id.text_call_username) TextView usernameView;
    @BindView(R.id.btn_minimize_call_ui) ImageButton exitFullScreenBtn;
    @BindView(R.id.btn_mic_switch) ImageButton micSwitch;
    @BindView(R.id.btn_speaker_switch) ImageButton speakerSwitch;
    @BindView(R.id.btn_record_switch) ImageButton recordSwitch;
    @BindView(R.id.fab_reject_call) FloatingActionButton rejectCallFab;
    @BindView(R.id.fab_end_call) FloatingActionButton endCallFab;
    @BindView(R.id.fab_answer_call) FloatingActionButton answerCallFab;

    private LocalBroadcastManager localBroadcastManager;
    private BroadcastReceiver broadcastReceiver;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.em_activity_voice_call);

        ButterKnife.bind(this);

        initView();

        registerBroadcast();
    }

    /**
     * init call activity ui
     */
    @Override protected void initView() {
        super.initView();
        if (CallManager.getInstance().isInComingCall()) {
            endCallFab.setVisibility(View.GONE);
            answerCallFab.setVisibility(View.VISIBLE);
            rejectCallFab.setVisibility(View.VISIBLE);
            callStateView.setText(R.string.em_call_connected_is_incoming);
        } else {
            endCallFab.setVisibility(View.VISIBLE);
            answerCallFab.setVisibility(View.GONE);
            rejectCallFab.setVisibility(View.GONE);
            callStateView.setText(R.string.em_call_connecting);
        }

        usernameView.setText(CallManager.getInstance().getChatId());

        micSwitch.setActivated(!CallManager.getInstance().isOpenMic());
        speakerSwitch.setActivated(CallManager.getInstance().isOpenSpeaker());
        recordSwitch.setActivated(CallManager.getInstance().isOpenRecord());

        if (CallManager.getInstance().getCallState() == CallManager.CallState.ACCEPTED) {
            endCallFab.setVisibility(View.VISIBLE);
            answerCallFab.setVisibility(View.GONE);
            rejectCallFab.setVisibility(View.GONE);
            callStateView.setText(R.string.em_call_accepted);
            refreshCallTime();
        }
    }

    /**
     * Control layout view click listener
     */
    @OnClick({
            R.id.btn_minimize_call_ui, R.id.btn_mic_switch, R.id.btn_speaker_switch, R.id.btn_record_switch, R.id.fab_reject_call,
            R.id.fab_end_call, R.id.fab_answer_call
    }) void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_minimize_call_ui:
                quitCallInterface();
                break;
            case R.id.btn_mic_switch:
                onMicrophone();
                break;
            case R.id.btn_speaker_switch:
                onSpeaker();
                break;
            case R.id.btn_record_switch:
                recordCall();
                break;
            case R.id.fab_end_call:
                endCall();
                break;
            case R.id.fab_reject_call:
                rejectCall();
                break;
            case R.id.fab_answer_call:
                answerCall();
                break;
        }
    }

    /**
     * answer call
     */
    @Override protected void answerCall() {
        super.answerCall();

        endCallFab.setVisibility(View.VISIBLE);
        rejectCallFab.setVisibility(View.GONE);
        answerCallFab.setVisibility(View.GONE);
    }

    /**
     * Mic toggle
     */
    private void onMicrophone() {
        try {
            if (micSwitch.isActivated()) {
                micSwitch.setActivated(false);
                EMClient.getInstance().callManager().resumeVoiceTransfer();
                CallManager.getInstance().setOpenMic(true);
            } else {
                micSwitch.setActivated(true);
                EMClient.getInstance().callManager().pauseVoiceTransfer();
                CallManager.getInstance().setOpenMic(false);
            }
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    /**
     * Speaker toggle
     */
    private void onSpeaker() {
        if (speakerSwitch.isActivated()) {
            speakerSwitch.setActivated(false);
            CallManager.getInstance().closeSpeaker();
            CallManager.getInstance().setOpenSpeaker(false);
        } else {
            speakerSwitch.setActivated(true);
            CallManager.getInstance().openSpeaker();
            CallManager.getInstance().setOpenSpeaker(true);
        }
    }

    /**
     * Record voice call TODO Unfulfilled
     */
    private void recordCall() {
        Snackbar.make(rootView, "Not yet realized", Snackbar.LENGTH_LONG).show();
        if (recordSwitch.isActivated()) {
            recordSwitch.setActivated(false);
            CallManager.getInstance().setOpenRecord(false);
        } else {
            recordSwitch.setActivated(true);
            CallManager.getInstance().setOpenRecord(true);
        }
    }

    /**
     * Register call broadcast receiver
     */
    private void registerBroadcast() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override public void onReceive(Context context, Intent intent) {
                final boolean isUpdateState = intent.getBooleanExtra("update_state", false);
                final boolean isUpdateTime = intent.getBooleanExtra("update_time", false);
                final EMCallStateChangeListener.CallState callState =
                        (EMCallStateChangeListener.CallState) intent.getExtras().get("call_state");
                final EMCallStateChangeListener.CallError callError =
                        (EMCallStateChangeListener.CallError) intent.getExtras().get("call_error");
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        if (isUpdateState) {
                            refreshCallView(callState, callError);
                        }
                        if (isUpdateTime && CallManager.getInstance().getCallType() == CallManager.CallType.VOICE) {
                            refreshCallTime();
                        }
                    }
                });
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constant.BROADCAST_ACTION_CALL);
        localBroadcastManager = LocalBroadcastManager.getInstance(activity);
        localBroadcastManager.registerReceiver(broadcastReceiver, filter);
    }

    private void unregisterBroadcast() {
        if (localBroadcastManager != null && broadcastReceiver != null) {
            localBroadcastManager.unregisterReceiver(broadcastReceiver);
        }
    }

    /**
     * refresh call ui
     */
    private void refreshCallView(EMCallStateChangeListener.CallState callState, EMCallStateChangeListener.CallError callError) {
        switch (callState) {
            case CONNECTING:
                break;
            case CONNECTED:
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        if (CallManager.getInstance().isInComingCall()) {
                            callStateView.setText(R.string.em_call_connected_is_incoming);
                        } else {
                            callStateView.setText(R.string.em_call_connected);
                        }
                    }
                });
                break;
            case ACCEPTED:
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        callStateView.setText(R.string.em_call_accepted);
                    }
                });
                break;
            case DISCONNECTED:
                onFinish();
                break;
            case NETWORK_DISCONNECTED:
                Toast.makeText(activity, "Remote network disconnected!", Toast.LENGTH_SHORT).show();
                break;
            case NETWORK_NORMAL:
                Toast.makeText(activity, "Remote network connected!", Toast.LENGTH_SHORT).show();
                break;
            case NETWORK_UNSTABLE:
                if (callError == EMCallStateChangeListener.CallError.ERROR_NO_DATA) {
                    Toast.makeText(activity, "No call data!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(activity, "Remote network unstable!", Toast.LENGTH_SHORT).show();
                }
                break;
            case VIDEO_PAUSE:
                Toast.makeText(activity, "Remote pause video", Toast.LENGTH_SHORT).show();
                break;
            case VIDEO_RESUME:
                Toast.makeText(activity, "Remote resume video", Toast.LENGTH_SHORT).show();
                break;
            case VOICE_PAUSE:
                Toast.makeText(activity, "Remote pause voice", Toast.LENGTH_SHORT).show();
                break;
            case VOICE_RESUME:
                Toast.makeText(activity, "Remote resume voice", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    /**
     * refresh call time
     */
    private void refreshCallTime() {
        int time = CallManager.getInstance().getCallTime();
        if (!callTimeView.isShown()) {
            callTimeView.setVisibility(View.VISIBLE);
        }
        callTimeView.setText(DateUtils.toTimeBySecond(time));
    }

    @Override public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= 23) {
                if (!Settings.canDrawOverlays(activity)) {
                    Toast.makeText(activity, "Open the floating window failed~", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(activity, "Open the floating window success!", Toast.LENGTH_LONG).show();
                    minimizeCallUI();
                }
            }
        }
    }

    private void minimizeCallUI() {
        CallManager.getInstance().addFloatWindow();
        // add call notify
        CallManager.getInstance().addCallNotification();
        onFinish();
    }

    /**
     * Exit the call interface
     */
    private void quitCallInterface() {
        if (ackForFloatWindowPermission()) {
            minimizeCallUI();
        } else {
            Toast.makeText(activity, "No floating window permissions", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * When the HOME key is pressed, it is different from pressing BACK and taking the active end
     */
    @Override protected void onUserLeaveHint() {
        //super.onUserLeaveHint();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(activity)) {
                Toast.makeText(activity, "No floating window permissions", Toast.LENGTH_SHORT).show();
                CallManager.getInstance().addCallNotification();
                onFinish();
            } else {
                quitCallInterface();
            }
        } else {
            quitCallInterface();
        }
    }

    @Override public void onBackPressed() {
        //super.onBackPressed();
        quitCallInterface();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        unregisterBroadcast();
    }
}
