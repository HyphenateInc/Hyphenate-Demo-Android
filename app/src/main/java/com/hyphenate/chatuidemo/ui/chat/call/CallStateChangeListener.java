package com.hyphenate.chatuidemo.ui.chat.call;

import com.hyphenate.chat.EMCallStateChangeListener;
import com.hyphenate.util.EMLog;
import org.greenrobot.eventbus.EventBus;

/**
 * Created by lzan13 on 2016/10/14.
 */

public class CallStateChangeListener implements EMCallStateChangeListener {

    private final String TAG = this.getClass().getSimpleName();

    @Override public void onCallStateChanged(CallState callState, CallError callError) {
        CallEvent event = new CallEvent();
        event.setCallState(callState);
        event.setCallError(callError);
        EventBus.getDefault().post(event);

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
            case DISCONNNECTED:
                // End call, Reset call status
                CallStatus.getInstance().reset();
                if (callError == CallError.ERROR_UNAVAILABLE) {
                    EMLog.i(TAG, "对方不在线" + callError);
                } else if (callError == CallError.ERROR_BUSY) {
                    EMLog.i(TAG, "对方正忙" + callError);
                } else if (callError == CallError.REJECTED) {
                    EMLog.i(TAG, "对方已拒绝" + callError);
                } else if (callError == CallError.ERROR_NORESPONSE) {
                    EMLog.i(TAG, "对方未响应，可能手机不在身边" + callError);
                } else if (callError == CallError.ERROR_TRANSPORT) {
                    EMLog.i(TAG, "连接建立失败" + callError);
                } else if (callError == CallError.ERROR_LOCAL_SDK_VERSION_OUTDATED) {
                    EMLog.i(TAG, "双方通讯协议不同" + callError);
                } else if (callError == CallError.ERROR_REMOTE_SDK_VERSION_OUTDATED) {
                    EMLog.i(TAG, "双方通讯协议不同" + callError);
                } else {
                    EMLog.i(TAG, "通话已结束 error %s" + callError);
                }
                break;
            case NETWORK_UNSTABLE:
                if (callError == EMCallStateChangeListener.CallError.ERROR_NO_DATA) {
                    EMLog.i(TAG, "没有通话数据" + callError);
                } else {
                    EMLog.i(TAG, "网络不稳定" + callError);
                }
                break;
            case NETWORK_NORMAL:
                EMLog.i(TAG, "网络正常");
                break;
            case VIDEO_PAUSE:
                EMLog.i(TAG, "视频传输已暂停");
                break;
            case VIDEO_RESUME:
                EMLog.i(TAG, "视频传输已恢复");
                break;
            case VOICE_PAUSE:
                EMLog.i(TAG, "语音传输已暂停");
                break;
            case VOICE_RESUME:
                EMLog.i(TAG, "语音传输已恢复");
                break;
            default:
                break;
        }
    }
}
