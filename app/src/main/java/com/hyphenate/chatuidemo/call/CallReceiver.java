package com.hyphenate.chatuidemo.call;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.hyphenate.chat.EMClient;
import com.hyphenate.easeui.EaseConstant;

/**
 * Create by lzan13 2016/10/13
 * Call broadcast receiver,
 */
public class CallReceiver extends BroadcastReceiver {
    private String TYPE_VIDEO = "video";
    private String TYPE_VOICE = "voice";

    public CallReceiver() {
    }

    @Override public void onReceive(Context context, Intent intent) {
        // Check whether the login is successful
        if (!EMClient.getInstance().isLoggedInBefore()) {
            return;
        }

        String callFrom = intent.getStringExtra(EaseConstant.EXTRA_FROM);
        String callType = intent.getStringExtra(EaseConstant.EXTRA_TYPE);
        String callTo = intent.getStringExtra(EaseConstant.EXTRA_TO);

        if (callTo.equals(EMClient.getInstance().getCurrentUser())) {
            Intent callIntent = new Intent();
            // Check call type
            if (callType.equals(TYPE_VIDEO)) {
                callIntent.setClass(context, VideoCallActivity.class);
                CallStatus.getInstance().setCallType(CallStatus.CALL_TYPE_VIDEO);
            } else if (callType.equals(TYPE_VOICE)) {
                callIntent.setClass(context, VoiceCallActivity.class);
                CallStatus.getInstance().setCallType(CallStatus.CALL_TYPE_VOICE);
            }
            // Set activity flag
            callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            callIntent.putExtra(EaseConstant.EXTRA_USER_ID, callFrom);
            callIntent.putExtra(EaseConstant.EXTRA_IS_INCOMING_CALL, true);
            context.startActivity(callIntent);
        }
    }
}
