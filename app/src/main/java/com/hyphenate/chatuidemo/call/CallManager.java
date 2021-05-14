package com.hyphenate.chatuidemo.call;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.hyphenate.chat.EMCallManager;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.chatuidemo.Constant;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.exceptions.EMNoActiveCallException;
import com.hyphenate.exceptions.EMServiceNotReadyException;
import com.hyphenate.util.DateUtils;
import com.hyphenate.util.EMLog;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by lzan13 on 2017/2/8.
 *
 * Call manager
 */
public class CallManager {

    private Context context;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothHeadset bluetoothHeadset;

    private static CallManager instance;

    private NotificationManager notificationManager;
    private int callNotificationId = 0526;

    private AudioManager audioManager;
    private SoundPool soundPool;
    private int streamID;
    private int loadId;
    private boolean isLoaded = false;

    private CallStateListener callStateListener;

    private boolean isInComingCall = true;
    private boolean isOpenCamera = true;
    private boolean isOpenMic = true;
    private boolean isOpenSpeaker = true;
    private boolean isOpenRecord = false;

    private Timer timer;
    private int callTime = 0;

    private String chatId;
    private CallState callState = CallState.DISCONNECTED;
    private CallType callType = CallType.VIDEO;
    private EndType endType = EndType.CANCEL;

    private LocalBroadcastManager localBroadcastManager;

    private CallManager() {
    }

    public static CallManager getInstance() {
        if (instance == null) {
            instance = new CallManager();
        }
        return instance;
    }

    /**
     * init call options
     */
    public void init(Context context) {
        this.context = context;
        initBluetoothListener();
        initSoundPool();
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        localBroadcastManager = LocalBroadcastManager.getInstance(context);

        /**
         * SDK 3.2.x version after the call-related settings, be sure to start after the start of audio and video features set,
         * otherwise set invalid
         */
        // Set whether or not an offline push notification is sent offline while the call is set，default false
        EMClient.getInstance().callManager().getCallOptions().setIsSendPushIfOffline(true);
        /**
         * Set whether to enable external input video data，default false，If true,
         * needed {@link EMCallManager#inputExternalVideoData(byte[], int, int, int)}
         */
        EMClient.getInstance().callManager().getCallOptions().setEnableExternalVideoData(false);
        // Set the video rotation angle
        //EMClient.getInstance().callManager().getCallOptions().setRotation(90);
        // Set the automatic adjustment resolution，default true
        EMClient.getInstance().callManager().getCallOptions().enableFixedVideoResolution(true);
        /**
         * Set the maximum and minimum bit rates for video calls.
         * >240p: 100k ~ 400kbps
         * >480p: 300k ~ 1Mbps
         * >720p: 900k ~ 2.5Mbps
         * >1080p: 2M  ~ 5Mbps
         */
        EMClient.getInstance().callManager().getCallOptions().setMaxVideoKbps(800);
        EMClient.getInstance().callManager().getCallOptions().setMinVideoKbps(150);
        EMClient.getInstance().callManager().getCallOptions().setVideoResolution(640, 480);
        EMClient.getInstance().callManager().getCallOptions().setMaxVideoFrameRate(30);
        EMClient.getInstance().callManager().getCallOptions().setAudioSampleRate(32000);
        EMClient.getInstance().callManager().getVideoCallHelper().setPreferMovFormatEnable(true);
    }

    /**
     * end call save message
     */
    public void saveCallMessage() {
        EMLog.d("lzan13", "saveCallMessage");
        EMMessage message = null;
        EMTextMessageBody body = null;
        String content = null;
        if (isInComingCall) {
            message = EMMessage.createReceiveMessage(EMMessage.Type.TXT);
            message.setFrom(chatId);
        } else {
            message = EMMessage.createSendMessage(EMMessage.Type.TXT);
            message.setTo(chatId);
        }
        switch (endType) {
            case NORMAL:
                content = String.valueOf(DateUtils.toTimeBySecond(getCallTime()));
                break;
            case CANCEL:
                content = context.getString(R.string.em_call_cancel);
                break;
            case CANCELLED:
                content = context.getString(R.string.em_call_cancel_is_incoming);
                break;
            case BUSY:
                content = context.getString(R.string.em_call_busy);
                break;
            case OFFLINE:
                content = context.getString(R.string.em_call_offline);
                break;
            case REJECT:
                content = context.getString(R.string.em_call_reject_is_incoming);
                break;
            case REJECTED:
                content = context.getString(R.string.em_call_reject);
                break;
            case NORESPONSE:
                content = context.getString(R.string.em_call_no_response);
                break;
            case TRANSPORT:
                content = context.getString(R.string.em_call_connection_fail);
                break;
            case DIFFERENT:
                content = context.getString(R.string.em_call_offline);
                break;
            default:
                content = context.getString(R.string.em_call_cancel);
                break;
        }
        body = new EMTextMessageBody(content);
        message.addBody(body);
        message.setStatus(EMMessage.Status.SUCCESS);
        if (callType == CallType.VIDEO) {
            message.setAttribute(Constant.MESSAGE_ATTR_IS_VIDEO_CALL, true);
        } else {
            message.setAttribute(Constant.MESSAGE_ATTR_IS_VOICE_CALL, true);
        }
        message.setUnread(false);
        EMClient.getInstance().chatManager().saveMessage(message);
    }

    /**
     * call camera data callback
     */
    public void setCallCameraDataProcessor() {
        CameraDataProcessor cameraDataProcessor = new CameraDataProcessor();
        EMClient.getInstance().callManager().setCameraDataProcessor(cameraDataProcessor);
    }

    /**
     * make call
     */
    public void makeCall() {
        try {
            if (callType == CallType.VIDEO) {
                EMClient.getInstance().callManager().makeVideoCall(chatId, "ext data");
            } else {
                EMClient.getInstance().callManager().makeVoiceCall(chatId, "ext data");
            }
            setEndType(EndType.CANCEL);
        } catch (EMServiceNotReadyException e) {
            e.printStackTrace();
        }
    }

    /**
     * reject call
     */
    public void rejectCall() {
        try {
            setEndType(EndType.REJECT);
            unregisterCallStateListener();
            EMClient.getInstance().callManager().rejectCall();
        } catch (EMNoActiveCallException e) {
            e.printStackTrace();
        }
        saveCallMessage();
        reset();
    }

    /**
     * end call
     */
    public void endCall() {
        try {
            unregisterCallStateListener();
            EMClient.getInstance().callManager().endCall();
        } catch (EMNoActiveCallException e) {
            e.printStackTrace();
        }
        saveCallMessage();
        reset();
    }

    /**
     * answer call
     */
    public boolean answerCall() {
        stopCallSound();
        try {
            EMClient.getInstance().callManager().answerCall();
            return true;
        } catch (EMNoActiveCallException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * open speaker
     * Modify the audio playback mode
     * 1、MODE_NORMAL
     * 2、MODE_IN_CALL
     * 3、MODE_IN_COMMUNICATION
     * 4、MODE_RINGTONE
     */
    public void openSpeaker() {
        if (!audioManager.isSpeakerphoneOn()) {
            audioManager.setSpeakerphoneOn(true);
        }
        if (callState == CallManager.CallState.ACCEPTED) {
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        } else {
            audioManager.setMode(AudioManager.MODE_RINGTONE);
        }
        setOpenSpeaker(true);

        disconnectBluetoothAudio();
    }

    /**
     * close speaker
     * see {@link #openSpeaker()}
     */
    public void closeSpeaker() {
        if (audioManager.isSpeakerphoneOn()) {
            audioManager.setSpeakerphoneOn(false);
        }
        if (callState == CallManager.CallState.ACCEPTED) {
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        } else {
            audioManager.setMode(AudioManager.MODE_NORMAL);
        }
        setOpenSpeaker(false);

        connectBluetoothAudio();
    }

    /**
     * init bluetooth listener
     */
    private void initBluetoothListener() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            bluetoothAdapter.getProfileProxy(context, new BluetoothProfile.ServiceListener() {
                @Override public void onServiceConnected(int profile, BluetoothProfile proxy) {
                    bluetoothHeadset = (BluetoothHeadset) proxy;
                }

                @Override public void onServiceDisconnected(int profile) {
                    bluetoothHeadset = null;
                }
            }, BluetoothProfile.HEADSET);
        }
    }

    /**
     * Connect the Bluetooth audio output device, through the Bluetooth output sound
     */
    private void connectBluetoothAudio() {
        try {
            if (bluetoothHeadset != null) {
                bluetoothHeadset.startVoiceRecognition(bluetoothHeadset.getConnectedDevices().get(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Disconnected from the Bluetooth output device
     */
    private void disconnectBluetoothAudio() {
        try {
            if (bluetoothHeadset != null) {
                bluetoothHeadset.stopVoiceRecognition(bluetoothHeadset.getConnectedDevices().get(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * init SoundPool
     */
    private void initSoundPool() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes attributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            soundPool = new SoundPool.Builder().setAudioAttributes(attributes).setMaxStreams(1).build();
        } else {
            soundPool = new SoundPool(1, AudioManager.MODE_RINGTONE, 0);
        }
    }

    /**
     * laod sound res
     */
    private void loadSound() {
        if (isInComingCall) {
            loadId = soundPool.load(context, R.raw.sound_call_incoming, 1);
        } else {
            loadId = soundPool.load(context, R.raw.sound_calling, 1);
        }
    }

    /**
     * Try to play a call sound
     */
    public void attemptPlayCallSound() {
        if (isLoaded) {
            playCallSound();
        } else {
            loadSound();
            // Set the resource to load the monitor, but also because the load resource in a separate process, take time,
            // so wait until the load is completed to play
            soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                @Override public void onLoadComplete(SoundPool soundPool, int i, int i1) {
                    isLoaded = true;
                    playCallSound();
                }
            });
        }
    }

    /**
     * play sound
     */
    private void playCallSound() {
        openSpeaker();
        audioManager.setMode(AudioManager.MODE_RINGTONE);
        if (soundPool != null) {
            streamID = soundPool.play(loadId, // Play resource id
                    0.5f,   // left volume
                    0.5f,   // right volume
                    1,      // Priority, the higher the value, the greater the priority
                    -1,     // Whether the cycle; 0 does not cycle, -1 cycle, N said the number of cycles
                    1);     // Playback rate; from 0.5-2, the general set to 1, that the normal play
        }
    }

    /**
     * stop play sound
     */
    protected void stopCallSound() {
        if (soundPool != null) {
            soundPool.stop(streamID);
            //soundPool.unload(loadId);
            //soundPool.release();
        }
    }

    /**
     * register call state change listener
     * see {@link CallStateListener}
     */
    public void registerCallStateListener() {
        if (callStateListener == null) {
            callStateListener = new CallStateListener();
        }
        EMClient.getInstance().callManager().addCallStateChangeListener(callStateListener);
    }

    /**
     * unregister call state change listener
     */
    private void unregisterCallStateListener() {
        if (callStateListener != null) {
            EMClient.getInstance().callManager().removeCallStateChangeListener(callStateListener);
            callStateListener = null;
        }
    }

    /**
     * add float window
     */
    public void addFloatWindow() {
        FloatWindow.getInstance(context).addFloatWindow();
    }

    /**
     * remove float widnow
     */
    public void removeFloatWindow() {
        FloatWindow.getInstance(context).removeFloatWindow();
    }

    /**
     * Call notify
     */
    public void addCallNotification() {
        if (notificationManager != null) {
            return;
        }
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        builder.setSmallIcon(R.mipmap.em_logo_uidemo);
        builder.setPriority(Notification.PRIORITY_HIGH);
        builder.setAutoCancel(true);
        builder.setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS);

        builder.setContentText("Call continues, click Resume");

        builder.setContentTitle(context.getString(R.string.app_name));
        Intent intent = new Intent();
        if (callType == CallType.VIDEO) {
            intent.setClass(context, VideoCallActivity.class);
        } else {
            intent.setClass(context, VoiceCallActivity.class);
        }
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(pIntent);
        builder.setOngoing(true);

        builder.setWhen(System.currentTimeMillis());

        notificationManager.notify(callNotificationId, builder.build());
    }

    /**
     * cancel call notify
     */
    public void cancelCallNotification() {
        if (notificationManager != null) {
            notificationManager.cancel(callNotificationId);
            notificationManager = null;
        }
    }

    /**
     * start call time
     */
    public void startCallTime() {
        final Intent intent = new Intent(Constant.BROADCAST_ACTION_CALL);
        intent.putExtra("update_time", true);
        localBroadcastManager.sendBroadcast(intent);
        if (timer == null) {
            timer = new Timer();
        }
        timer.purge();
        TimerTask task = new TimerTask() {
            @Override public void run() {
                callTime++;
                localBroadcastManager.sendBroadcast(intent);
            }
        };
        timer.scheduleAtFixedRate(task, 1000, 1000);
    }

    /**
     * stop time
     */
    public void stopCallTime() {
        if (timer != null) {
            timer.purge();
            timer.cancel();
            timer = null;
        }
        callTime = 0;
    }

    /**
     * release
     */
    public void reset() {
        isOpenCamera = true;
        isOpenMic = true;
        isOpenSpeaker = true;
        isOpenRecord = false;
        setCallState(CallState.DISCONNECTED);
        stopCallTime();
        unregisterCallStateListener();
        cancelCallNotification();
        if (soundPool != null) {
            soundPool.stop(streamID);
        }
        if (audioManager != null) {
            audioManager.setSpeakerphoneOn(true);
            audioManager.setMode(AudioManager.MODE_NORMAL);
        }
    }

    public CallState getCallState() {
        return callState;
    }

    public void setCallState(CallState callState) {
        this.callState = callState;
    }

    public CallType getCallType() {
        return callType;
    }

    public void setCallType(CallType callType) {
        this.callType = callType;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public boolean isInComingCall() {
        return isInComingCall;
    }

    public void setInComingCall(boolean isInComingCall) {
        this.isInComingCall = isInComingCall;
    }

    public int getCallTime() {
        return callTime;
    }

    public void setEndType(EndType endType) {
        this.endType = endType;
    }

    public EndType getEndType() {
        return endType;
    }

    public boolean isOpenCamera() {
        return isOpenCamera;
    }

    public void setOpenCamera(boolean openCamera) {
        isOpenCamera = openCamera;
    }

    public boolean isOpenMic() {
        return isOpenMic;
    }

    public void setOpenMic(boolean openMic) {
        isOpenMic = openMic;
    }

    public boolean isOpenSpeaker() {
        return isOpenSpeaker;
    }

    public void setOpenSpeaker(boolean openSpeaker) {
        isOpenSpeaker = openSpeaker;
    }

    public boolean isOpenRecord() {
        return isOpenRecord;
    }

    public void setOpenRecord(boolean openRecord) {
        isOpenRecord = openRecord;
    }

    /**
     * call type enum
     */
    public enum CallType {
        VIDEO,  // video call
        VOICE   // voice call
    }

    /**
     * call state enum
     */
    public enum CallState {
        CONNECTING,     // connecting
        CONNECTED,      // connected，wait accept
        ACCEPTED,       // calling
        DISCONNECTED    // disconnected

    }

    /**
     * call end type
     */
    public enum EndType {
        NORMAL,     // normal stop call
        CANCEL,     // cancel
        CANCELLED,  // opposite cancel
        BUSY,       // opposite buy
        OFFLINE,    // opposite offline
        REJECT,     // reject
        REJECTED,   // opposite rejected
        NORESPONSE, // no response
        TRANSPORT,  // connection failed
        DIFFERENT   // different
    }
}
