package com.hyphenate.chatuidemo.call;

import com.hyphenate.chat.EMCallStateChangeListener;

/**
 * Created by lzan13 on 2016/10/14.
 * Call event
 */
public class CallEvent {

    // Call error
    private EMCallStateChangeListener.CallError callError;

    // Call state
    private EMCallStateChangeListener.CallState callState;

    public EMCallStateChangeListener.CallError getCallError() {
        return callError;
    }

    public void setCallError(EMCallStateChangeListener.CallError callError) {
        this.callError = callError;
    }

    public EMCallStateChangeListener.CallState getCallState() {
        return callState;
    }

    public void setCallState(EMCallStateChangeListener.CallState callState) {
        this.callState = callState;
    }
}
