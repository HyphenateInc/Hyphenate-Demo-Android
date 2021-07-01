package io.agora.chatdemo.ui.widget.chatrow;

import android.content.Context;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import io.agora.chat.ChatMessage;
import io.agora.chat.TextMessageBody;
import io.agora.chatdemo.R;
import io.agora.easeui.EaseConstant;
import io.agora.easeui.widget.chatrow.EaseChatRow;

/**
 * Create by lzan13 2016/10/20
 * Video and Voice call chatrow
 */
public class ChatRowCall extends EaseChatRow {

    private TextView contentView;
    private ImageView callIcon;

    public ChatRowCall(Context context, ChatMessage message, int position, BaseAdapter adapter) {
        super(context, message, position, adapter);
    }

    @Override protected boolean overrideBaseLayout() {
        return false;
    }

    @Override protected int onGetLayoutId() {
        return message.direct() == ChatMessage.Direct.RECEIVE ? R.layout.em_row_received_call : R.layout.em_row_sent_call;
    }

    @Override protected void onFindViewById() {
        contentView = (TextView) findViewById(R.id.tv_chatcontent);
        callIcon = (ImageView) findViewById(R.id.img_call_icon);
    }

    @Override public void onSetUpView() {
        TextMessageBody txtBody = (TextMessageBody) message.getBody();
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
        if (message.direct() == ChatMessage.Direct.RECEIVE) {
            toChatUsername = message.getFrom();
        } else {
            toChatUsername = message.getTo();
        }
        Toast.makeText(getContext(), "点击了ChatRowCall", Toast.LENGTH_SHORT).show();
    }
}
