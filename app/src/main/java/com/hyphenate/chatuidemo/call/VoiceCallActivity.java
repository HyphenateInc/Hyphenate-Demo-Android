package com.hyphenate.chatuidemo.call;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.hyphenate.chat.EMCallStateChangeListener;
import com.hyphenate.chat.EMCallStateChangeListener.CallError;
import com.hyphenate.chat.EMCallStateChangeListener.CallState;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chatuidemo.Constant;
import com.hyphenate.chatuidemo.DemoHelper;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.easeui.widget.EaseImageView;
import com.hyphenate.exceptions.EMNoActiveCallException;
import com.hyphenate.exceptions.EMServiceNotReadyException;
import com.hyphenate.util.EMLog;
import java.util.Timer;
import java.util.TimerTask;
import com.hyphenate.exceptions.HyphenateException;

public class VoiceCallActivity extends CallActivity {

    private final String TAG = VoiceCallActivity.class.getSimpleName();

    private Timer mTimer;

    private LocalBroadcastManager localBroadcastManager;
    private CallBroadcastReceiver broadcastReceiver;

    // Use ButterKnife define view
    @BindView(R.id.img_call_background) ImageView mCallBackgroundView;
    @BindView(R.id.text_call_status) TextView mCallStatusView;
    @BindView(R.id.img_call_avatar) EaseImageView mAvatarView;
    @BindView(R.id.text_call_username) TextView mUsernameView;
    @BindView(R.id.btn_exit_full_screen) ImageButton mExitFullScreenBtn;
    @BindView(R.id.btn_mic_switch) ImageButton mMicSwitch;
    @BindView(R.id.btn_speaker_switch) ImageButton mSpeakerSwitch;
    @BindView(R.id.fab_reject_call) FloatingActionButton mRejectCallFab;
    @BindView(R.id.fab_end_call) FloatingActionButton mEndCallFab;
    @BindView(R.id.fab_answer_call) FloatingActionButton mAnswerCallFab;

    /**
     * Call entrance
     */
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.em_activity_voice_call);

        DemoHelper.getInstance().isVoiceCalling = true;

        // init ButterKnife
        ButterKnife.bind(this);

        initView();

        // register call broadcast receiver
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastReceiver = new CallBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constant.BROADCAST_ACTION_CALL);
        localBroadcastManager.registerReceiver(broadcastReceiver, filter);
    }

    /**
     * Init layout view and call
     */
    @Override protected void initView() {
        super.initView();

        // Set call type
        mCallType = 1;

        mChronometer = (Chronometer) findViewById(R.id.chronometer_call_time);

        // Set switch status
        mMicSwitch.setActivated(CallStatus.getInstance().isMic());
        mSpeakerSwitch.setActivated(CallStatus.getInstance().isSpeaker());

        mUsernameView.setText(mCallId);
        // Check call state
        if (CallStatus.getInstance().getCallState() == CallStatus.CALL_STATUS_NORMAL) {
            // Set call state
            CallStatus.getInstance().setInComing(isInComingCall);

            if (isInComingCall) {
                // Set call state is incoming
                CallStatus.getInstance().setCallState(CallStatus.CALL_STATUS_CONNECTING_INCOMING);
                // Set call state view show content
                mCallStatusView.setText(String.format(mActivity.getResources()
                        .getString(R.string.em_call_connected_incoming_call), mCallId));
                // Set button statue
                mRejectCallFab.setVisibility(View.VISIBLE);
                mEndCallFab.setVisibility(View.GONE);
                mAnswerCallFab.setVisibility(View.VISIBLE);
            } else {
                // Set call state connecting
                CallStatus.getInstance().setCallState(CallStatus.CALL_STATUS_CONNECTING);
                // Set call state view show content
                mCallStatusView.setText(
                        String.format(getString(R.string.em_call_connecting), mCallId));
                // Set button statue
                mRejectCallFab.setVisibility(View.GONE);
                mEndCallFab.setVisibility(View.VISIBLE);
                mAnswerCallFab.setVisibility(View.GONE);
                // make call
                makeCall();
            }
        } else if (CallStatus.getInstance().getCallState() == CallStatus.CALL_STATUS_CONNECTING) {
            isInComingCall = CallStatus.getInstance().isInComing();
            // Set call state view show content
            mCallStatusView.setText(String.format(getString(R.string.em_call_connecting), mCallId));
            // Set button statue
            mRejectCallFab.setVisibility(View.GONE);
            mEndCallFab.setVisibility(View.VISIBLE);
            mAnswerCallFab.setVisibility(View.GONE);
        } else if (CallStatus.getInstance().getCallState()
                == CallStatus.CALL_STATUS_CONNECTING_INCOMING) {
            isInComingCall = CallStatus.getInstance().isInComing();
            // Set call state view show content
            mCallStatusView.setText(
                    String.format(getString(R.string.em_call_connected_incoming_call), mCallId));
            // Set button statue
            mRejectCallFab.setVisibility(View.VISIBLE);
            mEndCallFab.setVisibility(View.GONE);
            mAnswerCallFab.setVisibility(View.VISIBLE);
        } else {
            isInComingCall = CallStatus.getInstance().isInComing();
            // Set call state view show content
            mCallStatus = CallStatus.CALL_ACCEPTED;
            mCallStatusView.setText(R.string.em_call_accepted);
            // Set button statue
            mRejectCallFab.setVisibility(View.GONE);
            mEndCallFab.setVisibility(View.VISIBLE);
            mAnswerCallFab.setVisibility(View.GONE);
        }
    }

    /**
     * Make voice call
     */
    private void makeCall() {
        try {
            EMClient.getInstance().callManager().makeVoiceCall(mCallId);
            // Set call timeout
            mTimer = new Timer();
            TimerTask task = new TimerTask() {
                @Override public void run() {
                    if (CallStatus.getInstance().getCallState() == CallStatus.CALL_CANCEL) {
                        runOnUiThread(new Runnable() {
                            @Override public void run() {
                                EMLog.i(TAG, "call timeout");
                                endCall();
                            }
                        });
                    } else {
                        mTimer.cancel();
                    }
                }
            };
            // set timeout 60s after
            mTimer.schedule(task, 60 * 1000);
        } catch (EMServiceNotReadyException e) {
            e.printStackTrace();
        }
    }

    /**
     * widget onClick
     */
    @OnClick({
            R.id.btn_exit_full_screen, R.id.btn_mic_switch, R.id.btn_speaker_switch,
            R.id.fab_reject_call, R.id.fab_end_call, R.id.fab_answer_call
    }) void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_exit_full_screen:
                // Minimize the view
                exitFullScreen();
                break;
            case R.id.btn_mic_switch:
                // Microphone switch
                onMicrophone();
                break;
            case R.id.btn_speaker_switch:
                // Speaker switch
                onSpeaker();
                break;
            case R.id.fab_reject_call:
                // Reject call
                rejectCall();
                break;
            case R.id.fab_end_call:
                // End call
                endCall();
                break;
            case R.id.fab_answer_call:
                // Answer call
                answerCall();
                break;
        }
    }

    /**
     * Minimize the layout
     */
    private void exitFullScreen() {
        // Vibrate
        vibrate();
        // Back home
        //        mActivity.moveTaskToBack(true);
        //mActivity.finish();
        Toast.makeText(mActivity, "Voice call minimization is not currently supported",
                Toast.LENGTH_LONG).show();
    }

    /**
     * Microphone switch
     */
    private void onMicrophone() {
        // Vibrate
        vibrate();
        if (mMicSwitch.isActivated()) {
            // Pause voice transfer
            try {
                EMClient.getInstance().callManager().pauseVoiceTransfer();
            } catch (HyphenateException e) {
                e.printStackTrace();
            }
            mMicSwitch.setActivated(false);
            CallStatus.getInstance().setMic(false);
        } else {
            // Resume voice transfer
            try {
                EMClient.getInstance().callManager().resumeVoiceTransfer();
            } catch (HyphenateException e) {
                e.printStackTrace();
            }
            mMicSwitch.setActivated(true);
            CallStatus.getInstance().setMic(true);
        }
    }

    /**
     * Speaker switch
     */
    private void onSpeaker() {
        // Vibrate
        vibrate();
        if (mSpeakerSwitch.isActivated()) {
            closeSpeaker();
        } else {
            openSpeaker();
        }
    }

    /**
     * Reject call
     */
    private void rejectCall() {
        // Virbate
        vibrate();
        // Reset call state
        CallStatus.getInstance().reset();
        // Remove call state listener
        DemoHelper.getInstance().removeCallStateChangeListener();
        // Stop call sound
        stopCallSound();
        try {
            // Call rejectCall();
            EMClient.getInstance().callManager().rejectCall();
        } catch (EMNoActiveCallException e) {
            e.printStackTrace();
            Toast.makeText(mActivity,
                    "Reject call error-" + e.getErrorCode() + "-" + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
        // Set call state
        mCallStatus = CallStatus.CALL_REJECT_INCOMING_CALL;
        // Save call message to
        saveCallMessage();
        // Finish activity
        onFinish();
    }

    /**
     * End call
     */
    private void endCall() {
        // Virbate
        vibrate();
        // Reset call state
        CallStatus.getInstance().reset();
        // Remove call state listener
        DemoHelper.getInstance().removeCallStateChangeListener();
        // Stop call sounds
        stopCallSound();
        try {
            // Call endCall();
            EMClient.getInstance().callManager().endCall();
        } catch (EMNoActiveCallException e) {
            e.printStackTrace();
            Toast.makeText(mActivity, "End call error-" + e.getErrorCode() + "-" + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
        // Save call message to local
        saveCallMessage();
        // Finish activity
        onFinish();
    }

    /**
     * Answer call
     */
    private void answerCall() {
        // Vibrate
        vibrate();
        // Set button state
        mRejectCallFab.setVisibility(View.GONE);
        mAnswerCallFab.setVisibility(View.GONE);
        mEndCallFab.setVisibility(View.VISIBLE);
        // Stop call sound
        stopCallSound();
        // Default open speaker
        openSpeaker();
        try {
            // Call answerCall();
            EMClient.getInstance().callManager().answerCall();
            // Set call state
            mCallStatus = CallStatus.CALL_ACCEPTED;
            CallStatus.getInstance().setCallState(CallStatus.CALL_STATUS_ACCEPTED);
        } catch (EMNoActiveCallException e) {
            e.printStackTrace();
            Toast.makeText(mActivity, "Answer callerror-" + e.getErrorCode() + "-" + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Open Speaker
     * Turn on the speaker switch, and set the audio playback mode
     * 1、MODE_NORMAL:   Normal mode, generally used for putting audio
     * 2、MODE_IN_CALL:
     * 3、MODE_IN_COMMUNICATION: This and MODE_IN_CALL communication mode, But Huawei MODE_IN_CALL
     * in
     * the bad, So the use of MODE_IN_COMMUNICATION
     * 4、MODE_RINGTONE: Ringtones mode
     */
    private void openSpeaker() {
        // Set button state
        mSpeakerSwitch.setActivated(true);
        CallStatus.getInstance().setSpeaker(true);
        if (!mAudioManager.isSpeakerphoneOn()) {
            // Open speaker
            mAudioManager.setSpeakerphoneOn(true);
        }
        // Set Audio mode
        mAudioManager.setMode(AudioManager.MODE_NORMAL);
    }

    /**
     * Close speaker
     * more see {@link #openSpeaker()}
     */
    private void closeSpeaker() {
        // Set button state
        mSpeakerSwitch.setActivated(false);
        CallStatus.getInstance().setSpeaker(false);
        if (mAudioManager.isSpeakerphoneOn()) {
            // Close speaker
            mAudioManager.setSpeakerphoneOn(false);
        }
        // Set Audio mode
        mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
    }

    /**
     * call broadcast receiver
     */
    private class CallBroadcastReceiver extends BroadcastReceiver {
        @Override public void onReceive(Context context, Intent intent) {
            // get call status and error
            CallState callState = (CallState) intent.getExtras().get("callState");
            CallError callError = (CallError) intent.getExtras().get("callError");

            switch (callState) {
                case CONNECTING:
                    // Set call state view show content
                    mCallStatusView.setText(
                            String.format(getString(R.string.em_call_connecting), mCallId));
                    break;
                case CONNECTED:
                    // Set call state view show content
                    mCallStatusView.setText(
                            String.format(getString(R.string.em_call_connected), mCallId));
                    break;
                case ACCEPTED:
                    if (mTimer != null) {
                        mTimer.cancel();
                    }
                    stopCallSound();
                    closeSpeaker();
                    // Set call state view show content
                    mCallStatusView.setText(R.string.em_call_accepted);
                    // Set call state
                    mCallStatus = CallStatus.CALL_ACCEPTED;
                    // Start time
                    mChronometer.setBase(SystemClock.elapsedRealtime());
                    mChronometer.start();
                    break;
                case DISCONNECTED:
                    // Stop time
                    mChronometer.stop();
                    // Set call state view show content
                    mCallStatusView.setText(R.string.em_call_disconnected);
                    // Check call error
                    if (callError == CallError.ERROR_UNAVAILABLE) {
                        mCallStatus = CallStatus.CALL_OFFLINE;
                        mCallStatusView.setText(
                                String.format(getString(R.string.em_call_not_online), mCallId));
                    } else if (callError == CallError.ERROR_BUSY) {
                        mCallStatus = CallStatus.CALL_BUSY;
                        mCallStatusView.setText(
                                String.format(getString(R.string.em_call_busy), mCallId));
                    } else if (callError == CallError.REJECTED) {
                        mCallStatus = CallStatus.CALL_REJECT;
                        mCallStatusView.setText(
                                String.format(getString(R.string.em_call_reject), mCallId));
                    } else if (callError == CallError.ERROR_NORESPONSE) {
                        mCallStatus = CallStatus.CALL_NO_RESPONSE;
                        mCallStatusView.setText(
                                String.format(getString(R.string.em_call_no_response), mCallId));
                    } else if (callError == CallError.ERROR_TRANSPORT) {
                        mCallStatus = CallStatus.CALL_TRANSPORT;
                        mCallStatusView.setText(R.string.em_call_connection_fail);
                    } else if (callError == CallError.ERROR_LOCAL_SDK_VERSION_OUTDATED) {
                        mCallStatus = CallStatus.CALL_VERSION_DIFFERENT;
                        mCallStatusView.setText(R.string.em_call_local_sdk_version_outdated);
                    } else if (callError == CallError.ERROR_REMOTE_SDK_VERSION_OUTDATED) {
                        mCallStatus = CallStatus.CALL_VERSION_DIFFERENT;
                        mCallStatusView.setText(R.string.em_call_remote_sdk_version_outdated);
                    } else {
                        if (mCallStatus == CallStatus.CALL_CANCEL) {
                            // Set call state
                            mCallStatus = CallStatus.CALL_CANCEL_INCOMING_CALL;
                        }
                        mCallStatusView.setText(R.string.em_call_cancel_incoming_call);
                    }
                    // Save call message to local
                    saveCallMessage();
                    // Remove call state listener
                    DemoHelper.getInstance().removeCallStateChangeListener();
                    // Finish activity
                    onFinish();
                    break;
                case NETWORK_UNSTABLE:
                    if (callError == EMCallStateChangeListener.CallError.ERROR_NO_DATA) {
                        mCallStatusView.setText(R.string.em_call_no_data);
                    } else {
                        mCallStatusView.setText(R.string.em_call_network_unstable);
                    }
                    break;
                case NETWORK_NORMAL:
                    mCallStatusView.setText(R.string.em_call_network_normal);
                    break;
                case VOICE_PAUSE:
                    mCallStatusView.setText(R.string.em_call_voice_pause);
                    break;
                case VOICE_RESUME:
                    mCallStatusView.setText(R.string.em_call_voice_resume);
                    break;
                default:
                    break;
            }
        }
    }

    private NotificationManager mNotificationManager;
    private int callNotificationId = 0526;

    /**
     * send call notification
     */
    private void sendCallNotification() {
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mActivity);

        builder.setSmallIcon(R.mipmap.em_logo_uidemo);
        builder.setPriority(Notification.PRIORITY_HIGH);
        builder.setAutoCancel(true);
        builder.setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS);

        builder.setContentText("While the video call is in progress, tap Resume");

        builder.setContentTitle(getString(R.string.app_name));
        Intent intent = new Intent(mActivity, VoiceCallActivity.class);
        PendingIntent pIntent =
                PendingIntent.getActivity(mActivity, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(pIntent);
        builder.setOngoing(true);

        builder.setWhen(System.currentTimeMillis());

        mNotificationManager.notify(callNotificationId, builder.build());
    }

    @Override protected void onUserLeaveHint() {
        sendCallNotification();
        super.onUserLeaveHint();
    }

    /**
     * Call end finish activity
     */
    @Override protected void onFinish() {
        // Call end release SurfaceView
        mAudioManager.setMode(AudioManager.MODE_NORMAL);
        if (mNotificationManager != null) {
            mNotificationManager.cancelAll();
        }
        super.onFinish();
    }

    @Override protected void onResume() {
        super.onResume();
        if (mNotificationManager != null) {
            mNotificationManager.cancel(callNotificationId);
        }
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        if (mTimer != null) {
            mTimer.cancel();
        }
        DemoHelper.getInstance().isVoiceCalling = false;
        mAudioManager.setMode(AudioManager.MODE_NORMAL);
        localBroadcastManager.unregisterReceiver(broadcastReceiver);
    }
}
