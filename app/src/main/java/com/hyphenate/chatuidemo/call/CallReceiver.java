package com.hyphenate.chatuidemo.call;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.hyphenate.chat.EMClient;

/**
 * Created by lzan13 on 2016/10/18.
 *
 * Call broadcast receiver
 */
public class CallReceiver extends BroadcastReceiver {

    public CallReceiver() {
    }

    @Override public void onReceive(Context context, Intent intent) {
        if (!EMClient.getInstance().isLoggedInBefore()) {
            return;
        }

        String callFrom = intent.getStringExtra("from");
        String callType = intent.getStringExtra("type");
        String callTo = intent.getStringExtra("to");
        String callExt = EMClient.getInstance().callManager().getCurrentCallSession().getExt();

        Intent callIntent = new Intent();
        if (callType.equals("video")) {
            CallManager.getInstance().setCallType(CallManager.CallType.VIDEO);
            callIntent.setClass(context, VideoCallActivity.class);
        } else if (callType.equals("voice")) {
            CallManager.getInstance().setCallType(CallManager.CallType.VOICE);
            callIntent.setClass(context, VoiceCallActivity.class);
        }
        CallManager.getInstance().setChatId(callFrom);
        CallManager.getInstance().setInComingCall(true);

        callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(callIntent);
    }
}
