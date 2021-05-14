package com.hyphenate.chatuidemo.call;

import android.content.Intent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.hyphenate.chat.EMCallStateChangeListener;
import com.hyphenate.chatuidemo.Constant;
import com.hyphenate.chatuidemo.DemoApplication;

/**
 * Created by lzan13 on 2016/10/18.
 *
 * Call state change listener
 */

public class CallStateListener implements EMCallStateChangeListener {

    private LocalBroadcastManager localBroadcastManager;

    public CallStateListener() {
        localBroadcastManager = LocalBroadcastManager.getInstance(DemoApplication.getInstance());
    }

    @Override public void onCallStateChanged(CallState callState, CallError callError) {
        Intent intent = new Intent(Constant.BROADCAST_ACTION_CALL);
        intent.putExtra("update_state", true);
        intent.putExtra("call_state", callState);
        intent.putExtra("call_error", callError);
        localBroadcastManager.sendBroadcast(intent);
        switch (callState) {
            case CONNECTING:
                CallManager.getInstance().setCallState(CallManager.CallState.CONNECTING);
                break;
            case CONNECTED:
                CallManager.getInstance().setCallState(CallManager.CallState.CONNECTED);
                break;
            case ACCEPTED:
                CallManager.getInstance().stopCallSound();
                CallManager.getInstance().startCallTime();
                CallManager.getInstance().setEndType(CallManager.EndType.NORMAL);
                CallManager.getInstance().setCallState(CallManager.CallState.ACCEPTED);
                break;
            case DISCONNECTED:
                if (callError == CallError.ERROR_UNAVAILABLE) {
                    CallManager.getInstance().setEndType(CallManager.EndType.OFFLINE);
                } else if (callError == CallError.ERROR_BUSY) {
                    CallManager.getInstance().setEndType(CallManager.EndType.BUSY);
                } else if (callError == CallError.REJECTED) {
                    CallManager.getInstance().setEndType(CallManager.EndType.REJECTED);
                } else if (callError == CallError.ERROR_NORESPONSE) {
                    CallManager.getInstance().setEndType(CallManager.EndType.NORESPONSE);
                } else if (callError == CallError.ERROR_TRANSPORT) {
                    CallManager.getInstance().setEndType(CallManager.EndType.TRANSPORT);
                } else if (callError == CallError.ERROR_LOCAL_SDK_VERSION_OUTDATED) {
                    CallManager.getInstance().setEndType(CallManager.EndType.DIFFERENT);
                } else if (callError == CallError.ERROR_REMOTE_SDK_VERSION_OUTDATED) {
                    CallManager.getInstance().setEndType(CallManager.EndType.DIFFERENT);
                } else if (callError == CallError.ERROR_NO_DATA) {
                } else {
                    if (CallManager.getInstance().getEndType() == CallManager.EndType.CANCEL) {
                        CallManager.getInstance().setEndType(CallManager.EndType.CANCELLED);
                    }
                }
                CallManager.getInstance().saveCallMessage();
                CallManager.getInstance().reset();
                break;
            case NETWORK_DISCONNECTED:
                break;
            case NETWORK_NORMAL:
                break;
            case NETWORK_UNSTABLE:
                if (callError == EMCallStateChangeListener.CallError.ERROR_NO_DATA) {
                } else {
                }
                break;
            case VIDEO_PAUSE:
                break;
            case VIDEO_RESUME:
                break;
            case VOICE_PAUSE:
                break;
            case VOICE_RESUME:
                break;
            default:
                break;
        }
    }
}
