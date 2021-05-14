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
        Toast.makeText(getContext(), "点击了ChatRowCall", Toast.LENGTH_SHORT).show();
    }
}
