package com.hyphenate.chatuidemo.ui.chat.call;

import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
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
import com.hyphenate.chatuidemo.DemoHelper;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.easeui.widget.EaseImageView;
import com.hyphenate.exceptions.EMNoActiveCallException;
import com.hyphenate.exceptions.EMServiceNotReadyException;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class VoiceCallActivity extends CallActivity {

    @BindView(R.id.img_call_backgound) ImageView mCallBackgroundView;
    @BindView(R.id.text_call_status) TextView mCallStatusView;
    @BindView(R.id.img_call_avatar) EaseImageView mAvatarView;
    @BindView(R.id.text_call_username) TextView mUsernameView;
    @BindView(R.id.btn_exit_full_screen) ImageButton mExitFullScreenBtn;
    @BindView(R.id.btn_mic_switch) ImageButton mMicSwitch;
    @BindView(R.id.btn_speaker_switch) ImageButton mSpeakerSwitch;
    @BindView(R.id.fab_reject_call) FloatingActionButton mRejectCallFab;
    @BindView(R.id.fab_end_call) FloatingActionButton mEndCallFab;
    @BindView(R.id.fab_answer_call) FloatingActionButton mAnswerCallFab;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.em_activity_voice_call);

        ButterKnife.bind(this);

        initView();
    }

    /**
     * 重载父类方法,实现一些当前通话的操作，
     */
    @Override protected void initView() {
        super.initView();

        // Set call type
        mCallType = 1;
        // Set switch status
        mMicSwitch.setActivated(CallStatus.getInstance().isMic());
        mSpeakerSwitch.setActivated(CallStatus.getInstance().isSpeaker());

        mUsernameView.setText(mCallId);
        // 判断下当前是否正在进行通话中
        if (CallStatus.getInstance().getCallState() == CallStatus.CALL_STATUS_NORMAL) {
            // 设置通话呼入呼出状态
            CallStatus.getInstance().setInComing(isInComingCall);
            //  设置资源加载监听
            mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                @Override public void onLoadComplete(SoundPool soundPool, int i, int i1) {
                    playCallSound();
                }
            });
            if (isInComingCall) {
                // 收到通话请求，设置通话状态为被呼叫中
                CallStatus.getInstance().setCallState(CallStatus.CALL_STATUS_CONNECTING_INCOMING);
                // 设置通话状态为对方申请通话
                mCallStatusView.setText(R.string.em_call_connected_incoming_call);
                mRejectCallFab.setVisibility(View.VISIBLE);
                mEndCallFab.setVisibility(View.GONE);
                mAnswerCallFab.setVisibility(View.VISIBLE);
            } else {
                // 收到通话请求，设置通话状态为呼叫中
                CallStatus.getInstance().setCallState(CallStatus.CALL_STATUS_CONNECTING);
                // 设置通话状态为正在呼叫中
                mCallStatusView.setText(R.string.em_call_connecting);
                mRejectCallFab.setVisibility(View.GONE);
                mEndCallFab.setVisibility(View.VISIBLE);
                mAnswerCallFab.setVisibility(View.GONE);
                // make call
                makeCall();
            }
        } else if (CallStatus.getInstance().getCallState() == CallStatus.CALL_STATUS_CONNECTING) {
            // 设置通话呼入呼出状态
            isInComingCall = CallStatus.getInstance().isInComing();
            // 设置通话状态为正在呼叫中
            mCallStatusView.setText(R.string.em_call_connecting);
            mRejectCallFab.setVisibility(View.GONE);
            mEndCallFab.setVisibility(View.VISIBLE);
            mAnswerCallFab.setVisibility(View.GONE);
        } else if (CallStatus.getInstance().getCallState()
                == CallStatus.CALL_STATUS_CONNECTING_INCOMING) {
            // 设置通话呼入呼出状态
            isInComingCall = CallStatus.getInstance().isInComing();
            // 设置通话状态为对方申请通话
            mCallStatusView.setText(R.string.em_call_connected_incoming_call);
            mRejectCallFab.setVisibility(View.VISIBLE);
            mEndCallFab.setVisibility(View.GONE);
            mAnswerCallFab.setVisibility(View.VISIBLE);
        } else {
            // 设置通话呼入呼出状态
            isInComingCall = CallStatus.getInstance().isInComing();
            // 再次打开要设置状态为正常通话状态
            mCallStatus = CallStatus.ML_CALL_ACCEPTED;
            mCallStatusView.setText(R.string.em_call_accepted);
            mRejectCallFab.setVisibility(View.GONE);
            mEndCallFab.setVisibility(View.VISIBLE);
            mAnswerCallFab.setVisibility(View.GONE);
        }
    }

    /**
     * 开始呼叫对方
     */
    private void makeCall() {
        try {
            EMClient.getInstance().callManager().makeVoiceCall(mCallId);
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
                exitFullScreen();
                break;
            case R.id.btn_mic_switch:
                // 麦克风开关
                onMicrophone();
                break;
            case R.id.btn_speaker_switch:
                // 扬声器开关
                onSpeaker();
                break;
            case R.id.fab_reject_call:
                // 拒绝接听通话
                rejectCall();
                break;
            case R.id.fab_end_call:
                // 结束通话
                endCall();
                break;
            case R.id.fab_answer_call:
                // 接听通话
                answerCall();
                break;
        }
    }

    /**
     * 退出全屏通话界面
     */
    private void exitFullScreen() {
        // 振动反馈
        vibrate();
        // 让应用回到桌面
        //        mActivity.moveTaskToBack(true);
        mActivity.finish();
    }

    /**
     * 麦克风开关，主要调用环信语音数据传输方法
     */
    private void onMicrophone() {
        // 振动反馈
        vibrate();
        // 根据麦克风开关是否被激活来进行判断麦克风状态，然后进行下一步操作
        if (mMicSwitch.isActivated()) {
            // 暂停语音数据的传输
            EMClient.getInstance().callManager().pauseVoiceTransfer();
            // 设置按钮状态
            mMicSwitch.setActivated(false);
            CallStatus.getInstance().setMic(false);
        } else {
            // 恢复语音数据的传输
            EMClient.getInstance().callManager().resumeVoiceTransfer();
            // 设置按钮状态
            mMicSwitch.setActivated(true);
            CallStatus.getInstance().setMic(true);
        }
    }

    /**
     * 扬声器开关
     */
    private void onSpeaker() {
        // 振动反馈
        vibrate();
        // 根据按钮状态决定打开还是关闭扬声器
        if (mSpeakerSwitch.isActivated()) {
            closeSpeaker();
        } else {
            openSpeaker();
        }
    }

    /**
     * 拒绝通话
     */
    private void rejectCall() {
        // 振动反馈
        vibrate();
        // 结束通话时取消通话状态监听
        DemoHelper.getInstance().removeCallStateChangeListener();
        // 拒绝通话后关闭通知铃音
        stopCallSound();
        try {
            // 调用 SDK 的拒绝通话方法
            EMClient.getInstance().callManager().rejectCall();
        } catch (EMNoActiveCallException e) {
            e.printStackTrace();
            Toast.makeText(mActivity,
                    "Reject call error-" + e.getErrorCode() + "-" + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
        // 通话结束，重置通话状态
        CallStatus.getInstance().reset();
        // 拒绝通话设置通话状态为自己拒绝
        mCallStatus = CallStatus.ML_CALL_REJECT_INCOMING_CALL;
        // 保存一条通话消息
        saveCallMessage();
        // 结束界面
        onFinish();
    }

    /**
     * 结束通话
     */
    private void endCall() {
        // 振动反馈
        vibrate();
        // 结束通话时取消通话状态监听
        DemoHelper.getInstance().removeCallStateChangeListener();
        // 结束通话后关闭通知铃音
        stopCallSound();
        try {
            // 调用 SDK 的结束通话方法
            EMClient.getInstance().callManager().endCall();
        } catch (EMNoActiveCallException e) {
            e.printStackTrace();
            Toast.makeText(mActivity, "End call error-" + e.getErrorCode() + "-" + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
        // 通话结束，重置通话状态
        CallStatus.getInstance().reset();
        // 挂断电话调用保存消息方法
        saveCallMessage();
        // 结束界面
        onFinish();
    }

    /**
     * 接听通话
     */
    private void answerCall() {
        // 振动反馈
        vibrate();
        // 做一些接听时的操作，比如隐藏按钮，打开扬声器等
        mRejectCallFab.setVisibility(View.GONE);
        mAnswerCallFab.setVisibility(View.GONE);
        mEndCallFab.setVisibility(View.VISIBLE);
        // 接听通话后关闭通知铃音
        stopCallSound();
        // 默认接通时打开免提
        openSpeaker();
        // 调用接通通话方法
        try {
            EMClient.getInstance().callManager().answerCall();
            // 设置通话状态为正常结束
            mCallStatus = CallStatus.ML_CALL_ACCEPTED;
            // 设置通话状态为已接通
            CallStatus.getInstance().setCallState(CallStatus.CALL_STATUS_ACCEPTED);
        } catch (EMNoActiveCallException e) {
            e.printStackTrace();
            Toast.makeText(mActivity, "Answer callerror-" + e.getErrorCode() + "-" + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 打开扬声器
     * 主要是通过扬声器的开关以及设置音频播放模式来实现
     * 1、MODE_NORMAL：是正常模式，一般用于外放音频
     * 2、MODE_IN_CALL：
     * 3、MODE_IN_COMMUNICATION：这个和 CALL 都表示通讯模式，不过 CALL 在华为上不好使，故使用 COMMUNICATION
     * 4、MODE_RINGTONE：铃声模式
     */
    private void openSpeaker() {
        // 设置按钮状态
        mSpeakerSwitch.setActivated(true);
        CallStatus.getInstance().setSpeaker(true);
        // 检查是否已经开启扬声器
        if (!mAudioManager.isSpeakerphoneOn()) {
            // 打开扬声器
            mAudioManager.setSpeakerphoneOn(true);
        }
        // 设置声音模式为正常模式
        mAudioManager.setMode(AudioManager.MODE_NORMAL);
    }

    /**
     * 关闭扬声器，即开启听筒播放模式
     * 同上边{@link #openSpeaker()}
     */
    private void closeSpeaker() {
        // 设置按钮状态
        mSpeakerSwitch.setActivated(false);
        CallStatus.getInstance().setSpeaker(false);
        // 检查是否已经开启扬声器
        if (mAudioManager.isSpeakerphoneOn()) {
            // 打开扬声器
            mAudioManager.setSpeakerphoneOn(false);
        }
        // 设置声音模式为通讯模式，即使用听筒播放
        mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
    }

    /**
     * 实现订阅方法，订阅全局监听发来的通话状态事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN) public void onEventBus(CallEvent event) {
        CallError callError = event.getCallError();
        CallState callState = event.getCallState();

        switch (callState) {
            case CONNECTING: // 正在呼叫对方
                mCallStatusView.setText(R.string.em_call_connecting);
                break;
            case CONNECTED: // 正在等待对方接受呼叫申请（对方申请与你进行通话）
                mCallStatusView.setText(R.string.em_call_connected);
                break;
            case ACCEPTED: // 通话已接通
                // 电话接通，停止播放提示音
                mCallStatusView.setText(R.string.em_call_accepted);
                stopCallSound();
                // 通话已接通，设置通话状态为正常状态
                mCallStatus = CallStatus.ML_CALL_ACCEPTED;
                break;
            case DISCONNNECTED: // 通话已中断
                mCallStatusView.setText(R.string.em_call_disconnected);
                if (callError == CallError.ERROR_UNAVAILABLE) {
                    // 设置通话状态为对方不在线
                    mCallStatus = CallStatus.ML_CALL_OFFLINE;
                    mCallStatusView.setText(R.string.em_call_not_online);
                } else if (callError == CallError.ERROR_BUSY) {
                    // 设置通话状态为对方在忙
                    mCallStatus = CallStatus.ML_CALL_BUSY;
                    mCallStatusView.setText(R.string.em_call_busy);
                } else if (callError == CallError.REJECTED) {
                    // 设置通话状态为对方已拒绝
                    mCallStatus = CallStatus.ML_CALL_REJECT;
                    mCallStatusView.setText(R.string.em_call_reject);
                } else if (callError == CallError.ERROR_NORESPONSE) {
                    // 设置通话状态为对方未响应
                    mCallStatus = CallStatus.ML_CALL_NO_RESPONSE;
                    mCallStatusView.setText(R.string.em_call_no_response);
                } else if (callError == CallError.ERROR_TRANSPORT) {
                    // 设置通话状态为建立连接失败
                    mCallStatus = CallStatus.ML_CALL_TRANSPORT;
                    mCallStatusView.setText(R.string.em_call_connection_fail);
                } else if (callError == CallError.ERROR_LOCAL_SDK_VERSION_OUTDATED) {
                    // 设置通话状态为双方协议不同
                    mCallStatus = CallStatus.ML_CALL_VERSION_DIFFERENT;
                    mCallStatusView.setText(R.string.em_call_local_sdk_version_outdated);
                } else if (callError == CallError.ERROR_REMOTE_SDK_VERSION_OUTDATED) {
                    // 设置通话状态为双方协议不同
                    mCallStatus = CallStatus.ML_CALL_VERSION_DIFFERENT;
                    mCallStatusView.setText(R.string.em_call_remote_sdk_version_outdated);
                } else {
                    // 根据当前状态判断是正常结束，还是对方取消通话
                    if (mCallStatus == CallStatus.ML_CALL_CANCEL) {
                        // 设置通话状态
                        mCallStatus = CallStatus.ML_CALL_CANCEL_INCOMING_CALL;
                    }
                    mCallStatusView.setText(R.string.em_call_cancel_incoming_call);
                }
                // 通话结束保存消息
                saveCallMessage();
                // 结束通话时取消通话状态监听
                DemoHelper.getInstance().removeCallStateChangeListener();
                // 结束通话关闭界面
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

    @Override protected void onResume() {
        super.onResume();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
    }
}
