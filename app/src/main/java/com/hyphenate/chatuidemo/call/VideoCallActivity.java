package com.hyphenate.chatuidemo.call;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.hyphenate.chat.EMCallStateChangeListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMVideoCallHelper;
import com.hyphenate.chatuidemo.Constant;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.media.EMCallSurfaceView;
import com.hyphenate.util.DateUtils;
import com.superrtc.sdk.VideoView;
import java.io.File;

/**
 * Created by lzan13 on 2017/6/18.
 * Video call activity
 */
public class VideoCallActivity extends CallActivity {

    private EMVideoCallHelper videoCallHelper;
    // SurfaceView state，-1 DISCONNECTED，0 ACCEPT，1 ACCEPT - local and opposite change
    private int surfaceState = -1;

    private EMCallSurfaceView localSurface = null;
    private EMCallSurfaceView oppositeSurface = null;
    private RelativeLayout.LayoutParams localParams = null;
    private RelativeLayout.LayoutParams oppositeParams = null;

    @BindView(R.id.layout_root) View rootView;
    @BindView(R.id.layout_call_control) View controlLayout;
    @BindView(R.id.layout_surface_container) RelativeLayout surfaceLayout;

    @BindView(R.id.btn_minimize_call_ui) ImageButton exitFullScreenBtn;
    @BindView(R.id.text_call_state) TextView callStateView;
    @BindView(R.id.text_call_time) TextView callTimeView;
    @BindView(R.id.btn_mic_switch) ImageButton micSwitch;
    @BindView(R.id.btn_camera_switch) ImageButton cameraSwitch;
    @BindView(R.id.btn_speaker_switch) ImageButton speakerSwitch;
    @BindView(R.id.btn_record_switch) ImageButton recordSwitch;
    @BindView(R.id.btn_screenshot) ImageButton screenshotSwitch;
    @BindView(R.id.btn_change_camera_switch) ImageButton changeCameraSwitch;
    @BindView(R.id.fab_reject_call) FloatingActionButton rejectCallFab;
    @BindView(R.id.fab_end_call) FloatingActionButton endCallFab;
    @BindView(R.id.fab_answer_call) FloatingActionButton answerCallFab;

    private LocalBroadcastManager localBroadcastManager;
    private BroadcastReceiver broadcastReceiver;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.em_activity_video_call);

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

        micSwitch.setActivated(!CallManager.getInstance().isOpenMic());
        cameraSwitch.setActivated(!CallManager.getInstance().isOpenCamera());
        speakerSwitch.setActivated(CallManager.getInstance().isOpenSpeaker());
        recordSwitch.setActivated(CallManager.getInstance().isOpenRecord());

        videoCallHelper = EMClient.getInstance().callManager().getVideoCallHelper();

        initCallSurface();
        if (CallManager.getInstance().getCallState() == CallManager.CallState.ACCEPTED) {
            endCallFab.setVisibility(View.VISIBLE);
            answerCallFab.setVisibility(View.GONE);
            rejectCallFab.setVisibility(View.GONE);
            callStateView.setText(R.string.em_call_accepted);
            refreshCallTime();
            onCallSurface();
        }

        try {
            EMClient.getInstance().callManager().setCameraFacing(Camera.CameraInfo.CAMERA_FACING_FRONT);
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
        CallManager.getInstance().setCallCameraDataProcessor();
    }

    /**
     * control layout view click listener
     */
    @OnClick({
            R.id.layout_call_control, R.id.btn_minimize_call_ui, R.id.btn_change_camera_switch, R.id.btn_mic_switch,
            R.id.btn_camera_switch, R.id.btn_speaker_switch, R.id.btn_record_switch, R.id.btn_screenshot, R.id.fab_reject_call,
            R.id.fab_end_call, R.id.fab_answer_call
    }) void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_call_control:
                onControlLayout();
                break;
            case R.id.btn_minimize_call_ui:
                quitCallInterface();
                break;
            case R.id.btn_change_camera_switch:
                changeCamera();
                break;
            case R.id.btn_mic_switch:
                onMicrophone();
                break;
            case R.id.btn_camera_switch:
                onCamera();
                break;
            case R.id.btn_speaker_switch:
                onSpeaker();
                break;
            case R.id.btn_screenshot:
                onScreenShot();
                break;
            case R.id.btn_record_switch:
                onRecordCall();
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
     * control layout show or hide
     */
    private void onControlLayout() {
        if (controlLayout.isShown()) {
            controlLayout.setVisibility(View.GONE);
        } else {
            controlLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * change camera
     */
    private void changeCamera() {
        try {
            if (EMClient.getInstance().callManager().getCameraFacing() == 1) {
                EMClient.getInstance().callManager().switchCamera();
                EMClient.getInstance().callManager().setCameraFacing(0);
                changeCameraSwitch.setImageResource(R.drawable.em_ic_camera_rear_white_24dp);
            } else {
                EMClient.getInstance().callManager().switchCamera();
                EMClient.getInstance().callManager().setCameraFacing(1);
                changeCameraSwitch.setImageResource(R.drawable.em_ic_camera_front_white_24dp);
            }
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    /**
     * mic toggle
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
     * camera toggle
     */
    private void onCamera() {
        try {
            if (cameraSwitch.isActivated()) {
                cameraSwitch.setActivated(false);
                EMClient.getInstance().callManager().resumeVideoTransfer();
                CallManager.getInstance().setOpenCamera(true);
            } else {
                cameraSwitch.setActivated(true);
                EMClient.getInstance().callManager().pauseVideoTransfer();
                CallManager.getInstance().setOpenCamera(false);
            }
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    /**
     * speaker toggle
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
     * record video call
     */
    private void onRecordCall() {
        if (recordSwitch.isActivated()) {
            recordSwitch.setActivated(false);
            //String path = videoCallHelper.stopVideoRecord();
            CallManager.getInstance().setOpenRecord(false);
//            File file = new File(path);
//            if (file.exists()) {
//                Toast.makeText(activity, "Record video success! " + path, Toast.LENGTH_LONG).show();
//            } else {
//                Toast.makeText(activity, "Record video failed~~", Toast.LENGTH_LONG).show();
//            }
        } else {
            recordSwitch.setActivated(true);
//            String dirPath = getExternalFilesDir("").getAbsolutePath() + "/videos";
//            File dir = new File(dirPath);
//            if (!dir.isDirectory()) {
//                dir.mkdirs();
//            }
//            videoCallHelper.startVideoRecord(dirPath);
//            Toast.makeText(activity, "Start record video ~", Toast.LENGTH_LONG).show();
            CallManager.getInstance().setOpenRecord(true);
        }
    }

    /**
     * Screenshot
     */
    private void onScreenShot() {
//        String dirPath = getExternalFilesDir("").getAbsolutePath() + "/videos/";
//        File dir = new File(dirPath);
//        if (!dir.isDirectory()) {
//            dir.mkdirs();
//        }
//        String path = dirPath + "video_" + System.currentTimeMillis() + ".jpg";
//        boolean result = videoCallHelper.takePicture(path);
//        Toast.makeText(activity, "Screenshot success! " + path, Toast.LENGTH_LONG).show();
    }

    @Override protected void answerCall() {
        super.answerCall();
        endCallFab.setVisibility(View.VISIBLE);
        rejectCallFab.setVisibility(View.GONE);
        answerCallFab.setVisibility(View.GONE);
    }

    /**
     * Init call surface view
     */
    private void initCallSurface() {
        oppositeSurface = new EMCallSurfaceView(activity);
        oppositeParams = new RelativeLayout.LayoutParams(0, 0);
        oppositeParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        oppositeParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
        oppositeSurface.setLayoutParams(oppositeParams);
        surfaceLayout.addView(oppositeSurface);

        localSurface = new EMCallSurfaceView(activity);
        localParams = new RelativeLayout.LayoutParams(0, 0);
        localParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        localParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
        localParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        localSurface.setLayoutParams(localParams);
        surfaceLayout.addView(localSurface);

        localSurface.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                onControlLayout();
            }
        });

        localSurface.setZOrderOnTop(false);
        localSurface.setZOrderMediaOverlay(true);

        // Set how the call screen is displayed
        localSurface.setScaleMode(VideoView.EMCallViewScaleMode.EMCallViewScaleModeAspectFill);
        oppositeSurface.setScaleMode(VideoView.EMCallViewScaleMode.EMCallViewScaleModeAspectFill);
        EMClient.getInstance().callManager().setSurfaceView(localSurface, oppositeSurface);
    }

    /**
     * change call surface view
     */
    private void onCallSurface() {
        surfaceState = 0;

        int width = activity.getResources().getDimensionPixelSize(R.dimen.call_local_width);
        int height = activity.getResources().getDimensionPixelSize(R.dimen.call_local_height);
        int rightMargin = activity.getResources().getDimensionPixelSize(R.dimen.call_layout_margin);
        int topMargin = activity.getResources().getDimensionPixelSize(R.dimen.call_local_height);

        localParams = new RelativeLayout.LayoutParams(width, height);
        localParams.width = width;
        localParams.height = height;
        localParams.rightMargin = rightMargin;
        localParams.topMargin = topMargin;
        localParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        localSurface.setLayoutParams(localParams);

        localSurface.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                changeCallSurface();
            }
        });

        oppositeSurface.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                onControlLayout();
            }
        });
    }

    /**
     * change local and opposite view
     */
    private void changeCallSurface() {
        if (surfaceState == 0) {
            surfaceState = 1;
            EMClient.getInstance().callManager().setSurfaceView(oppositeSurface, localSurface);
        } else {
            surfaceState = 0;
            EMClient.getInstance().callManager().setSurfaceView(localSurface, oppositeSurface);
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
                        if (isUpdateTime && CallManager.getInstance().getCallType() == CallManager.CallType.VIDEO) {
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
     * refresh call view
     */
    private void refreshCallView(EMCallStateChangeListener.CallState callState, EMCallStateChangeListener.CallError callError) {
        switch (callState) {
            case CONNECTING:
                break;
            case CONNECTED:
                if (CallManager.getInstance().isInComingCall()) {
                    callStateView.setText(R.string.em_call_connected_is_incoming);
                } else {
                    callStateView.setText(R.string.em_call_connected);
                }
                break;
            case ACCEPTED:
                callStateView.setText(R.string.em_call_accepted);
                onCallSurface();
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
     * Refresh call time
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
        super.onActivityResult(requestCode, resultCode, data);
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

    @Override protected void onFinish() {
        // release surface view
        if (localSurface != null) {
            if (localSurface.getRenderer() != null) {
                localSurface.getRenderer().dispose();
            }
            localSurface.release();
            localSurface = null;
        }
        if (oppositeSurface != null) {
            if (oppositeSurface.getRenderer() != null) {
                oppositeSurface.getRenderer().dispose();
            }
            oppositeSurface.release();
            oppositeSurface = null;
        }
        super.onFinish();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        unregisterBroadcast();
    }
}
