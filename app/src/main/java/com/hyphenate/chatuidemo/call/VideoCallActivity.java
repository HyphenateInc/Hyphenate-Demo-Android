package com.hyphenate.chatuidemo.call;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
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

import com.hyphenate.chat.EMCallManager;
import com.hyphenate.chat.EMCallStateChangeListener;
import com.hyphenate.chat.EMCallStateChangeListener.CallError;
import com.hyphenate.chat.EMCallStateChangeListener.CallState;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chatuidemo.Constant;
import com.hyphenate.chatuidemo.DemoHelper;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.exceptions.EMNoActiveCallException;
import com.hyphenate.exceptions.EMServiceNotReadyException;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.media.EMCallSurfaceView;
import com.hyphenate.util.EMLog;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Create by lzan13 2016/10/13
 * Video call activity
 */
public class VideoCallActivity extends CallActivity {

    private final String TAG = VideoCallActivity.class.getSimpleName();

    // Video call helper
    private EMCallManager.EMVideoCallHelper mVideoCallHelper;
    // Video call data processor
    private CameraDataProcessor mCameraDataProcessor;

    // Update video call info
    private boolean mMonitor = true;

    private Timer mTimer;

    // SurfaceView state, 0 local is small, 1 opposite is small
    private int surfaceViewState = 0;

    private LocalBroadcastManager localBroadcastManager;
    private CallBroadcastReceiver broadcastReceiver;

    // Use ButterKnife define view
    @BindView(R.id.layout_call_control) View mControlLayout;
    @BindView(R.id.surface_view_local) EMCallSurfaceView mLocalSurfaceView;
    @BindView(R.id.surface_view_opposite) EMCallSurfaceView mOppositeSurfaceView;

    @BindView(R.id.layout_call_info) View mCallInfoView;
    @BindView(R.id.text_resolution) TextView mResolutionView;
    @BindView(R.id.text_time_latency) TextView mTimeLatencyView;
    @BindView(R.id.text_frame_rate) TextView mFrameRateView;
    @BindView(R.id.text_lost_rate) TextView mLostRateView;
    @BindView(R.id.text_local_bitrate) TextView mLocalBitrateView;
    @BindView(R.id.text_remote_bitrate) TextView mRemoteBitrateView;

    @BindView(R.id.img_call_background) ImageView mCallBackgroundView;
    @BindView(R.id.text_call_status) TextView mCallStatusView;
    @BindView(R.id.btn_change_camera_switch) ImageButton mChangeCameraSwitch;
    @BindView(R.id.btn_call_info) ImageButton mCallInfoBtn;
    @BindView(R.id.btn_exit_full_screen) ImageButton mExitFullScreenBtn;
    @BindView(R.id.btn_camera_switch) ImageButton mCameraSwitch;
    @BindView(R.id.btn_mic_switch) ImageButton mMicSwitch;
    @BindView(R.id.btn_speaker_switch) ImageButton mSpeakerSwitch;
    @BindView(R.id.fab_reject_call) FloatingActionButton mRejectCallFab;
    @BindView(R.id.fab_end_call) FloatingActionButton mEndCallFab;
    @BindView(R.id.fab_answer_call) FloatingActionButton mAnswerCallFab;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.em_activity_video_call);

        DemoHelper.getInstance().isVideoCalling = true;
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
     * Init layout view
     */
    @Override protected void initView() {
        super.initView();

        // default call type video
        mCallType = 0;

        mChronometer = (Chronometer) findViewById(R.id.chronometer_call_time);

        // Set button state
        mChangeCameraSwitch.setActivated(false);
        mCameraSwitch.setActivated(CallStatus.getInstance().isCamera());
        mMicSwitch.setActivated(CallStatus.getInstance().isMic());
        mSpeakerSwitch.setActivated(CallStatus.getInstance().isSpeaker());

        // SDK call helper
        mVideoCallHelper = EMClient.getInstance().callManager().getVideoCallHelper();
        // Set the local preview image displayed on the top floor, be sure to set as soon as possible, otherwise invalid
        mLocalSurfaceView.setZOrderMediaOverlay(true);
        mLocalSurfaceView.setZOrderOnTop(true);

        try {
            // By default, the front camera is used
            EMClient.getInstance()
                    .callManager()
                    .setCameraFacing(Camera.CameraInfo.CAMERA_FACING_FRONT);
        } catch (HyphenateException e) {
            e.printStackTrace();
        }

        // Set local and opposite SurfaceView
        EMClient.getInstance()
                .callManager()
                .setSurfaceView(mLocalSurfaceView, mOppositeSurfaceView);

        mCameraDataProcessor = new CameraDataProcessor();
        // Set video call data processor
        EMClient.getInstance().callManager().setCameraDataProcessor(mCameraDataProcessor);

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
     * Make call
     */
    private void makeCall() {
        try {
            EMClient.getInstance().callManager().makeVideoCall(mCallId);
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
            R.id.img_call_background, R.id.layout_call_control, R.id.surface_view_local,
            R.id.surface_view_opposite, R.id.btn_exit_full_screen, R.id.btn_change_camera_switch,
            R.id.btn_call_info, R.id.layout_call_info, R.id.btn_mic_switch, R.id.btn_camera_switch,
            R.id.btn_speaker_switch, R.id.fab_reject_call, R.id.fab_end_call, R.id.fab_answer_call
    }) void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_call_control:
            case R.id.img_call_background:
                onControlLayout();
                break;
            case R.id.surface_view_local:
                if (surfaceViewState == 0) {
                    changeSurfaceViewSize();
                } else {
                    onControlLayout();
                }
                break;
            case R.id.surface_view_opposite:
                if (surfaceViewState == 1) {
                    changeSurfaceViewSize();
                } else {
                    onControlLayout();
                }
                break;
            case R.id.btn_exit_full_screen:
                // Minimize the layout
                exitFullScreen();
                break;
            case R.id.btn_change_camera_switch:
                // Change camera
                changeCamera();
                break;
            case R.id.btn_call_info:
                mCallInfoView.setVisibility(View.VISIBLE);
                break;
            case R.id.layout_call_info:
                mCallInfoView.setVisibility(View.GONE);
                break;
            case R.id.btn_mic_switch:
                // Microphone switch
                onMicrophone();
                break;
            case R.id.btn_camera_switch:
                // Camera switch
                onCamera();
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
     * Control layout
     */
    private void onControlLayout() {
        if (mControlLayout.isShown()) {
            mControlLayout.setVisibility(View.GONE);
        } else {
            mControlLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Change surfaceView size
     */
    private void changeSurfaceViewSize() {
        //RelativeLayout.LayoutParams localLayoutParams =
        //        (RelativeLayout.LayoutParams) mLocalSurfaceView.getLayoutParams();
        //RelativeLayout.LayoutParams oppositeLayoutParams =
        //        (RelativeLayout.LayoutParams) mOppositeSurfaceView.getLayoutParams();
        //if (surfaceViewState == 1) {
        //    surfaceViewState = 0;
        //    localLayoutParams.width =
        //            mActivity.getResources().getDimensionPixelSize(R.dimen.call_small_width);
        //    localLayoutParams.height =
        //            mActivity.getResources().getDimensionPixelSize(R.dimen.call_small_height);
        //    localLayoutParams.setMargins(0,
        //            mActivity.getResources().getDimensionPixelOffset(R.dimen.call_small_width), 0,
        //            0);
        //
        //    oppositeLayoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        //    oppositeLayoutParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
        //    oppositeLayoutParams.setMargins(0, 0, 0, 0);
        //
        //    mLocalSurfaceView.setZOrderMediaOverlay(true);
        //    mLocalSurfaceView.setZOrderOnTop(true);
        //    mOppositeSurfaceView.setZOrderMediaOverlay(false);
        //    mOppositeSurfaceView.setZOrderOnTop(false);
        //} else {
        //    surfaceViewState = 1;
        //    localLayoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        //    localLayoutParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
        //    localLayoutParams.setMargins(0, 0, 0, 0);
        //
        //    oppositeLayoutParams.width =
        //            mActivity.getResources().getDimensionPixelSize(R.dimen.call_small_width);
        //    oppositeLayoutParams.height =
        //            mActivity.getResources().getDimensionPixelSize(R.dimen.call_small_height);
        //    oppositeLayoutParams.setMargins(0,
        //            mActivity.getResources().getDimensionPixelOffset(R.dimen.call_small_width), 0,
        //            0);
        //
        //    mLocalSurfaceView.setZOrderMediaOverlay(false);
        //    mLocalSurfaceView.setZOrderOnTop(false);
        //    mOppositeSurfaceView.setZOrderMediaOverlay(true);
        //    mOppositeSurfaceView.setZOrderOnTop(true);
        //}
        //
        //mLocalSurfaceView.setLayoutParams(localLayoutParams);
        //mOppositeSurfaceView.setLayoutParams(oppositeLayoutParams);
        Toast.makeText(mActivity, "Screen Change unrealized", Toast.LENGTH_LONG).show();
    }

    /**
     * Minimize the layout
     * TODO Video call minimization is not currently supported
     */
    private void exitFullScreen() {
        // Vibrate
        vibrate();
        // Back home
        //        mActivity.moveTaskToBack(true);
        //mActivity.finish();
        Toast.makeText(mActivity, "Video call minimization is not currently supported",
                Toast.LENGTH_LONG).show();
    }

    /**
     * Change camera
     */
    private void changeCamera() {
        // Vibrate
        vibrate();
        if (mChangeCameraSwitch.isActivated()) {
            EMClient.getInstance().callManager().switchCamera();
            mChangeCameraSwitch.setActivated(false);
        } else {
            EMClient.getInstance().callManager().switchCamera();
            mChangeCameraSwitch.setActivated(true);
        }
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
     * Camera switch
     */
    private void onCamera() {
        // Vibrate
        vibrate();
        if (mCameraSwitch.isActivated()) {
            // Pause video transfer
            try {
                EMClient.getInstance().callManager().pauseVideoTransfer();
            } catch (HyphenateException e) {
                e.printStackTrace();
            }
            mCameraSwitch.setActivated(false);
            CallStatus.getInstance().setCamera(false);
        } else {
            // Resume video streaming
            try {
                EMClient.getInstance().callManager().resumeVideoTransfer();
            } catch (HyphenateException e) {
                e.printStackTrace();
            }
            mCameraSwitch.setActivated(true);
            CallStatus.getInstance().setCamera(true);
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
     * Set surfaceView
     */
    private void surfaceViewProcessor() {

        // display the caller party's view
        mOppositeSurfaceView.setVisibility(View.VISIBLE);

        //RelativeLayout.LayoutParams lp =
        //        (RelativeLayout.LayoutParams) mLocalSurfaceView.getLayoutParams();
        //lp.width = mActivity.getResources().getDimensionPixelSize(R.dimen.call_small_width);
        //lp.height = mActivity.getResources().getDimensionPixelSize(R.dimen.call_small_height);
        //lp.setMargins(0, mActivity.getResources().getDimensionPixelOffset(R.dimen.call_small_width),
        //        0, 0);
        //mLocalSurfaceView.setLayoutParams(lp);
    }

    /**
     * turn on speaker
     * Turn on the speaker switch and set to audio playback mode
     * 1. MODE_NORMAL: default mode, generally used for putting audio
     * 2. MODE_IN_CALL:
     * 3. MODE_IN_COMMUNICATION:
     * 4. MODE_RINGTONE: Ringtones mode
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
            // get call state and error
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
                    openSpeaker();
                    // Set call state view show content
                    mCallStatusView.setText(R.string.em_call_accepted);
                    // Set call state
                    mCallStatus = CallStatus.CALL_ACCEPTED;
                    // Set SurfaceView processor
                    surfaceViewProcessor();
                    // Start time
                    mChronometer.setBase(SystemClock.elapsedRealtime());
                    mChronometer.start();
                    startMonitor();
                    break;
                case DISCONNECTED:
                    mChronometer.stop();
                    // Set call state view show content
                    mCallStatusView.setText(R.string.em_call_disconnected);
                    // Check call error
                    if (callError == CallError.ERROR_UNAVAILABLE) {
                        mCallStatus = CallStatus.CALL_OFFLINE;
                        mCallStatusView.setText(
                                String.format(getString(R.string.em_call_not_online), mCallId));
                        Toast.makeText(VideoCallActivity.this, mCallStatusView.getText(), Toast.LENGTH_LONG).show();
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
                case VIDEO_PAUSE:
                    Toast.makeText(mActivity, "video pause", Toast.LENGTH_LONG).show();
                    mCallStatusView.setText(R.string.em_call_video_pause);
                    break;
                case VIDEO_RESUME:
                    mCallStatusView.setText(R.string.em_call_video_resume);
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

    /**
     * refresh call info; for debug & testing, you can remove this when release
     */
    private void startMonitor() {
        mMonitor = true;
        new Thread(new Runnable() {
            public void run() {
                while (mMonitor) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            mResolutionView.setText(mVideoCallHelper.getVideoWidth()
                                    + " x "
                                    + mVideoCallHelper.getVideoHeight()
                                    + " px");
                            mTimeLatencyView.setText(mVideoCallHelper.getVideoLatency() + " ms");
                            mFrameRateView.setText(mVideoCallHelper.getVideoFrameRate() + " fps");
                            mLostRateView.setText(mVideoCallHelper.getVideoLostRate() + "%");
                            mLocalBitrateView.setText(mVideoCallHelper.getLocalBitrate() + " KB");
                            mRemoteBitrateView.setText(mVideoCallHelper.getRemoteBitrate() + " KB");
                        }
                    });
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }, "CallMonitor").start();
    }

    /**
     * stop refresh call info
     */
    private void stopMonitor() {
        mMonitor = false;
    }

    /**
     * Call end finish activity
     */
    @Override protected void onFinish() {
        stopMonitor();
        // Call end release SurfaceView
        mLocalSurfaceView = null;
        mOppositeSurfaceView = null;
        mAudioManager.setMode(AudioManager.MODE_NORMAL);
        if (mNotificationManager != null) {
            mNotificationManager.cancelAll();
        }
        super.onFinish();
    }

    /**
     * The activity is not visible
     */
    @Override protected void onUserLeaveHint() {
        if (CallStatus.getInstance().getCallState() == CallStatus.CALL_STATUS_ACCEPTED) {
            // The activity is not visible, Pause video transfer
            try {
                EMClient.getInstance().callManager().pauseVideoTransfer();
            } catch (HyphenateException e) {
                e.printStackTrace();
            }
        }
        sendCallNotification();
        super.onUserLeaveHint();
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
        Intent intent = new Intent(mActivity, VideoCallActivity.class);
        PendingIntent pIntent =
                PendingIntent.getActivity(mActivity, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(pIntent);
        builder.setOngoing(true);

        builder.setWhen(System.currentTimeMillis());

        mNotificationManager.notify(callNotificationId, builder.build());
    }

    /**
     * The activity is resume
     */
    @Override protected void onResume() {
        super.onResume();
        if (CallStatus.getInstance().getCallState() == CallStatus.CALL_STATUS_ACCEPTED) {
            mCallStatusView.setText(R.string.em_call_accepted);
            // The activity is resume, Resume video streaming
            try {
                EMClient.getInstance().callManager().resumeVideoTransfer();
            } catch (HyphenateException e) {
                e.printStackTrace();
            }
        }
        if (mNotificationManager != null) {
            mNotificationManager.cancel(callNotificationId);
        }
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        if (mTimer != null) {
            mTimer.cancel();
        }
        DemoHelper.getInstance().isVideoCalling = false;
        mAudioManager.setMode(AudioManager.MODE_NORMAL);
        localBroadcastManager.unregisterReceiver(broadcastReceiver);
    }
}
