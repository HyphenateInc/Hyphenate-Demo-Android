package com.hyphenate.chatuidemo.call;

/**
 * Created by lzan13 on 2016/10/13.
 * Voice and video call status class, which is a singleton class, because there is only one call at
 * the same time
 */
public class CallStatus {

    // Accepted
    public static final int CALL_ACCEPTED = 0x00;
    // Cancel call
    public static final int CALL_CANCEL = 0x01;
    // Cancel incoming call
    public static final int CALL_CANCEL_INCOMING_CALL = 0x02;
    // Busy
    public static final int CALL_BUSY = 0x03;
    // Offline
    public static final int CALL_OFFLINE = 0x04;
    // Reject call
    public static final int CALL_REJECT = 0x05;
    // Reject incoming call
    public static final int CALL_REJECT_INCOMING_CALL = 0x06;
    // No response
    public static final int CALL_NO_RESPONSE = 0x07;
    // Connection failed
    public static final int CALL_TRANSPORT = 0x08;
    // Version different
    public static final int CALL_VERSION_DIFFERENT = 0x09;

    // Call status
    public static int CALL_STATUS_NORMAL = 0x00;
    public static int CALL_STATUS_CONNECTING = 0x01;
    public static int CALL_STATUS_CONNECTING_INCOMING = 0x02;
    public static int CALL_STATUS_ACCEPTED = 0x03;

    // Call type
    public static int CALL_TYPE_NORMAL = 0x00;
    public static int CALL_TYPE_VIDEO = 0x01;
    public static int CALL_TYPE_VOICE = 0x02;

    // instance
    private static CallStatus instance;

    // Record the call status
    private int callState;
    // Record the call type
    private int callType;

    // is incoming call
    private boolean isInComingCall;
    // Microphone status
    private boolean isMic;
    // Camera status
    private boolean isCamera;
    // Speaker status
    private boolean isSpeaker;

    /**
     * Initialize some default values when instantiating
     */
    private CallStatus() {
        setCallType(CALL_TYPE_NORMAL);
        setCallState(CALL_STATUS_NORMAL);
        setMic(true);
        setCamera(true);
        setSpeaker(true);
    }

    /**
     * Reset call status
     */
    public void reset() {
        setCallType(CALL_TYPE_NORMAL);
        setCallState(CALL_STATUS_NORMAL);
        setMic(true);
        setCamera(true);
        setSpeaker(true);
    }

    /**
     * Gets an instance of a singleton class
     */
    public static CallStatus getInstance() {
        if (instance == null) {
            instance = new CallStatus();
        }
        return instance;
    }

    public boolean isInComing() {
        return isInComingCall;
    }

    public void setInComing(boolean inComing) {
        isInComingCall = inComing;
    }

    public int getCallState() {
        return callState;
    }

    public void setCallState(int callState) {
        this.callState = callState;
    }

    public int getCallType() {
        return callType;
    }

    public void setCallType(int callType) {
        this.callType = callType;
    }

    public boolean isCamera() {
        return isCamera;
    }

    public void setCamera(boolean camera) {
        isCamera = camera;
    }

    public boolean isMic() {
        return isMic;
    }

    public void setMic(boolean mic) {
        isMic = mic;
    }

    public boolean isSpeaker() {
        return isSpeaker;
    }

    public void setSpeaker(boolean speaker) {
        isSpeaker = speaker;
    }
}
