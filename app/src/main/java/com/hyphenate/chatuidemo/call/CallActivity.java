package com.hyphenate.chatuidemo.call;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.WindowManager;
import android.widget.Chronometer;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.chatuidemo.DemoHelper;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.chatuidemo.ui.BaseActivity;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.easeui.widget.chatrow.EaseChatRowVoicePlayClickListener;

/**
 * Created by lzan13 on 2016/10/13.
 * Video and Voice Call parent class
 */
public class CallActivity extends BaseActivity {

    protected BaseActivity mActivity;

    // Call time view
    protected Chronometer mChronometer;

    // Call id
    protected String mCallId;
    // Is incoming call
    protected boolean isInComingCall;

    // Call end state, used to save the message after the end of the call tips
    protected int mCallStatus;
    // Call type, used to distinguish between voice and video calls, 0 video, 1 voice
    protected int mCallType;

    // AudioManager and SoundPool
    protected AudioManager mAudioManager;
    protected SoundPool mSoundPool;
    protected int streamID;
    protected int loadId;

    // Vibration
    protected Vibrator mVibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            finish();
            return;
        }
        // eep the screen lit, close the input method, and unlock the device
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        if (EaseChatRowVoicePlayClickListener.currentPlayListener != null && EaseChatRowVoicePlayClickListener.isPlaying) {
            EaseChatRowVoicePlayClickListener.currentPlayListener.stopPlayVoice();
        }
    }

    /**
     * Init layout view
     */
    protected void initView() {
        mActivity = this;

        // Get call id
        mCallId = getIntent().getStringExtra(EaseConstant.EXTRA_USER_ID);
        isInComingCall = getIntent().getBooleanExtra(EaseConstant.EXTRA_IS_INCOMING_CALL, false);
        // Set default call end status
        mCallStatus = CallStatus.CALL_CANCEL;

        // Add call status listener
        DemoHelper.getInstance().addCallStateChangeListener();

        // Vibrator
        mVibrator = (Vibrator) mActivity.getSystemService(Context.VIBRATOR_SERVICE);
        // AudioManager
        mAudioManager = (AudioManager) mActivity.getSystemService(Context.AUDIO_SERVICE);
        // According to the different versions of the system to select different ways to initialize the audio playback tool
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            createSoundPoolWithBuilder();
        } else {
            createSoundPoolWithConstructor();
        }
        if (CallStatus.getInstance().getCallState() == CallStatus.CALL_STATUS_NORMAL) {
            // load sound
            if (isInComingCall) {
                loadId = mSoundPool.load(mActivity, R.raw.sound_call_incoming, 1);
            } else {
                loadId = mSoundPool.load(mActivity, R.raw.sound_calling, 1);
            }
            // Load SoundPool listener
            mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                @Override
                public void onLoadComplete(SoundPool soundPool, int i, int i1) {
                    playCallSound();
                }
            });
        }
    }

    /**
     * Call end save message to local
     */
    protected void saveCallMessage() {
        EMMessage message = null;
        EMTextMessageBody body = null;
        String content = null;
        if (isInComingCall) {
            message = EMMessage.createReceiveMessage(EMMessage.Type.TXT);
            message.setFrom(mCallId);
        } else {
            message = EMMessage.createSendMessage(EMMessage.Type.TXT);
            message.setTo(mCallId);
        }

        switch (mCallStatus) {
            case CallStatus.CALL_ACCEPTED:
                content = mChronometer.getText().toString();
                break;
            case CallStatus.CALL_CANCEL:
                content = mActivity.getString(R.string.em_call_cancel);
                break;
            case CallStatus.CALL_CANCEL_INCOMING_CALL:
                content = mActivity.getString(R.string.em_call_cancel_incoming_call);
                break;
            case CallStatus.CALL_BUSY:
                content = String.format(mActivity.getString(R.string.em_call_busy), mCallId);
                break;
            case CallStatus.CALL_OFFLINE:
                content = String.format(mActivity.getString(R.string.em_call_not_online), mCallId);
                break;
            case CallStatus.CALL_REJECT_INCOMING_CALL:
                content = mActivity.getString(R.string.em_call_reject_incoming_call);
                break;
            case CallStatus.CALL_REJECT:
                content = String.format(mActivity.getString(R.string.em_call_reject), mCallId);
                break;
            case CallStatus.CALL_NO_RESPONSE:
                content = String.format(mActivity.getString(R.string.em_call_no_response), mCallId);
                break;
            case CallStatus.CALL_TRANSPORT:
                content = mActivity.getString(R.string.em_call_connection_fail);
                break;
            case CallStatus.CALL_VERSION_DIFFERENT:
                content = String.format(mActivity.getString(R.string.em_call_not_online), mCallId);
                break;
            default:
                content = mActivity.getString(R.string.em_call_cancel);
                break;
        }
        body = new EMTextMessageBody(content);
        message.addBody(body);
        message.setStatus(EMMessage.Status.SUCCESS);
        if (mCallType == 0) {
            message.setAttribute(EaseConstant.MESSAGE_ATTR_IS_VIDEO_CALL, true);
        } else {
            message.setAttribute(EaseConstant.MESSAGE_ATTR_IS_VOICE_CALL, true);
        }
        message.setUnread(false);
        // Call SDK saveMessage(), save message to local
        EMClient.getInstance().chatManager().saveMessage(message);
    }

    /**
     * Call vibration
     */
    protected void vibrate() {
        if (mVibrator == null) {
            mVibrator = (Vibrator) mActivity.getSystemService(Context.VIBRATOR_SERVICE);
        }
        mVibrator.vibrate(60);
    }

    /**
     * Play a call tone
     */
    protected void playCallSound() {
        if (!mAudioManager.isSpeakerphoneOn()) {
            mAudioManager.setSpeakerphoneOn(true);
        }
        // Set AudioManager MODE to ring the MODE_RINGTONE
        mAudioManager.setMode(AudioManager.MODE_RINGTONE);
        // Play the call tone, return to play the resource id, used to stop later
        if (mSoundPool != null) {
            streamID = mSoundPool.play(loadId,
                    // Resource id, the order in which audio resources are loaded into the SoundPool
                    0.5f,   // Left volume
                    0.5f,   // Right volume
                    1,      // Audio file priority
                    -1,     // Whether the cycle; 0 does not cycle, -1 cycle
                    1);     // Playback ratio; from 0.5-2, generally set to 1, that normal play
        }
    }

    /**
     * Turn off the audio playback, and release the resources
     */
    protected void stopCallSound() {
        if (mSoundPool != null) {
            // Stop SoundPool
            mSoundPool.stop(streamID);
            // release
            mSoundPool.release();
            mSoundPool = null;
        }
    }

    /**
     * When the SDK version of the system is higher than 21, the SoundPool is instantiated in a
     * different way
     * Instantiate SoundPool using {@link SoundPool.Builder}
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void createSoundPoolWithBuilder() {
        AudioAttributes attributes = new AudioAttributes.Builder()
                // Set the audio mode, This select USAGE_NOTIFICATION_RINGTONE
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        mSoundPool =
                new SoundPool.Builder().setAudioAttributes(attributes).setMaxStreams(1).build();
    }

    /**
     * The old version uses the constructor function to instantiate the SoundPool, and the MODE to
     * ring the MODE_RINGTONE
     */
    protected void createSoundPoolWithConstructor() {
        mSoundPool = new SoundPool(1, AudioManager.MODE_RINGTONE, 0);
    }

    /**
     * finish activity
     */
    protected void onFinish() {
        // turn off call sound and release resources
        stopCallSound();
        finish();
    }

    /**
     * Overload Return key
     */
    @Override
    public void onBackPressed() {
        // super.onBackPressed();

    }
}
