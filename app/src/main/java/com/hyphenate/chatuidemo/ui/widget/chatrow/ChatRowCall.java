package com.hyphenate.chatuidemo.ui.widget.chatrow;

import android.content.Context;
import android.content.Intent;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.chatuidemo.call.CallStatus;
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
        return message.direct() == EMMessage.Direct.RECEIVE ? R.layout.em_row_received_call
                : R.layout.em_row_sent_call;
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
        if (CallStatus.getInstance().getCallType() == CallStatus.CALL_TYPE_VOICE) {
            Toast.makeText(context, R.string.em_call_voice_calling, Toast.LENGTH_LONG).show();
            return;
        } else if (CallStatus.getInstance().getCallType() == CallStatus.CALL_TYPE_VIDEO) {
            Toast.makeText(context, R.string.em_call_video_calling, Toast.LENGTH_LONG).show();
            return;
        }
        String toChatUsername = "";
        if (message.direct() == EMMessage.Direct.RECEIVE) {
            toChatUsername = message.getFrom();
        } else {
            toChatUsername = message.getTo();
        }
        Intent intent = new Intent();
        if (message.getBooleanAttribute(EaseConstant.MESSAGE_ATTR_IS_VIDEO_CALL, false)) {
            intent.setClass(context, VideoCallActivity.class);
        } else {
            intent.setClass(context, VoiceCallActivity.class);
        }
        intent.putExtra(EaseConstant.EXTRA_USER_ID, toChatUsername);
        intent.putExtra(EaseConstant.EXTRA_IS_INCOMING_CALL, false);
        context.startActivity(intent);
    }

}
