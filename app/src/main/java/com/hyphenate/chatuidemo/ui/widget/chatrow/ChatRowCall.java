package com.hyphenate.chatuidemo.ui.widget.chatrow;

import android.content.Context;
import android.content.Intent;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.chatuidemo.call.CallManager;
import com.hyphenate.chatuidemo.call.VideoCallActivity;
import com.hyphenate.chatuidemo.call.VoiceCallActivity;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.easeui.widget.chatrow.EaseChatRow;

/**
 * Create by lzan13 2016/10/20
 * Video and Voice call chatrow
 */
public class ChatRowCall extends EaseChatRow {

    private TextView contentView;
    private ImageView callIcon;

    public ChatRowCall(Context context, EMMessage message, int position, BaseAdapter adapter) {
        super(context, message, position, adapter);
    }

    @Override protected boolean overrideBaseLayout() {
        return false;
    }

    @Override protected int onGetLayoutId() {
        return message.direct() == EMMessage.Direct.RECEIVE ? R.layout.em_row_received_call : R.layout.em_row_sent_call;
    }

    @Override protected void onFindViewById() {
        contentView = (TextView) findViewById(R.id.tv_chatcontent);
        callIcon = (ImageView) findViewById(R.id.img_call_icon);
    }

    @Override public void onSetUpView() {
        EMTextMessageBody txtBody = (EMTextMessageBody) message.getBody();
        //Spannable span = SmileUtils.getSmiledText(context, txtBody.getMessage());
        // set text content
        //contentView.setText(span, BufferType.SPANNABLE);

        contentView.setText(txtBody.getMessage());

        if (message.getBooleanAttribute(EaseConstant.MESSAGE_ATTR_IS_VIDEO_CALL, false)) {
            callIcon.setImageResource(R.drawable.em_ic_videocam_white_24dp);
        } else {
            callIcon.setImageResource(R.drawable.em_ic_call_white_24dp);
        }
    }

    @Override protected void onUpdateView() {
        adapter.notifyDataSetChanged();
    }

    @Override protected void onBubbleClick() {
        String toChatUsername = "";
        if (message.direct() == EMMessage.Direct.RECEIVE) {
            toChatUsername = message.getFrom();
        } else {
            toChatUsername = message.getTo();
        }
        if (CallManager.getInstance().getCallState() == CallManager.CallState.DISCONNECTED) {
            Intent intent = new Intent();
            if (message.getBooleanAttribute(EaseConstant.MESSAGE_ATTR_IS_VIDEO_CALL, false)) {
                intent.setClass(context, VideoCallActivity.class);
                CallManager.getInstance().setCallType(CallManager.CallType.VIDEO);
            } else {
                intent.setClass(context, VoiceCallActivity.class);
                CallManager.getInstance().setCallType(CallManager.CallType.VOICE);
            }
            CallManager.getInstance().setChatId(toChatUsername);
            CallManager.getInstance().setInComingCall(false);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } else {
            Intent intent = new Intent();
            if (CallManager.getInstance().getCallType() == CallManager.CallType.VIDEO) {
                intent.setClass(context, VideoCallActivity.class);
            } else {
                intent.setClass(context, VoiceCallActivity.class);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }
}
