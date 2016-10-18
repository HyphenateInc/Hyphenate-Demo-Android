package com.hyphenate.chatuidemo.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.hyphenate.chatuidemo.R;

/**
 * Created by wei on 2016/10/14.
 */

/**
 * Input text and send message in a chat
 */
public class ChatInputView extends LinearLayout{
    @BindView(R.id.edt_msg_content) EditText mEditText;

    private ChatInputViewEventListener mInputViewEventListener;


    public ChatInputView(Context context) {
        super(context);
        init(context, null);
    }

    public ChatInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ChatInputView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs);
    }

    private void init(Context context, AttributeSet attrs){
        LayoutInflater.from(context).inflate(R.layout.em_widget_chat_input, this);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btn_send) void onSendClick(){
        if(mInputViewEventListener != null){
            mInputViewEventListener.onSendMessage(mEditText.getText());
        }
        mEditText.setText("");
    }

    public void setViewEventListener(ChatInputViewEventListener eventListener){
        mInputViewEventListener = eventListener;
    }



    public interface ChatInputViewEventListener{
        void onSendMessage(CharSequence content);
    }
}
