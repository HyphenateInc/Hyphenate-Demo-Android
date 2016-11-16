package com.hyphenate.chatuidemo.call;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import com.hyphenate.chat.EMCallStateChangeListener;
import com.hyphenate.chatuidemo.DemoConstant;
import com.hyphenate.util.EMLog;
import org.greenrobot.eventbus.EventBus;

/**
 * Created by lzan13 on 2016/10/14.
 */

public class CallStateChangeListener implements EMCallStateChangeListener {

    private final String TAG = this.getClass().getSimpleName();
    private LocalBroadcastManager localBroadcastManager;
    private Context mContext;

    public CallStateChangeListener(Context context) {
        mContext = context;
    }

    @Override public void onCallStateChanged(CallState callState, CallError callError) {
        /**
         * Use EventBus send subscribable events,
         *  {@link VideoCallActivity#onEventBus(CallEvent)}
         *  {@link VoiceCallActivity#onEventBus(CallEvent)}
         */
        CallEvent event = new CallEvent();
        event.setCallState(callState);
        event.setCallError(callError);
        EventBus.getDefault().post(event);

        // send broadcast
        Intent intent = new Intent(DemoConstant.BROADCAST_ACTION_CALL);
        intent.putExtra("callState", callState);
        intent.putExtra("callError", callError);
        localBroadcastManager = LocalBroadcastManager.getInstance(mContext);
        localBroadcastManager.sendBroadcast(intent);

        switch (callState) {
            case CONNECTING:
                // Set call state connecting
                CallStatus.getInstance().setCallState(CallStatus.CALL_STATUS_CONNECTING);
                break;
            case CONNECTED:
                // Set call state connecting
                CallStatus.getInstance().setCallState(CallStatus.CALL_STATUS_CONNECTING);
                break;
            case ACCEPTED:
                // Set call state accepted
                CallStatus.getInstance().setCallState(CallStatus.CALL_STATUS_ACCEPTED);
                break;
            case DISCONNECTED:
                /**
                 * End call, Reset call status, No processing is performed here.
                 *  {@link VideoCallActivity#onEventBus(CallEvent)}
                 *  {@link VoiceCallActivity#onEventBus(CallEvent)}
                 */
                CallStatus.getInstance().reset();
                break;
            case NETWORK_UNSTABLE:
                if (callError == EMCallStateChangeListener.CallError.ERROR_NO_DATA) {
                    EMLog.i(TAG, "No call data " + callError);
                } else {
                    EMLog.i(TAG, "Network unstable " + callError);
                }
                break;
            case NETWORK_NORMAL:
                EMLog.i(TAG, "Network normal");
                break;
            case VIDEO_PAUSE:
                EMLog.i(TAG, "Video streaming pause");
                break;
            case VIDEO_RESUME:
                EMLog.i(TAG, "Video streaming resume");
                break;
            case VOICE_PAUSE:
                EMLog.i(TAG, "Voice transfer pause");
                break;
            case VOICE_RESUME:
                EMLog.i(TAG, "Voice transfer resume");
                break;
            default:
                break;
        }
    }
}
