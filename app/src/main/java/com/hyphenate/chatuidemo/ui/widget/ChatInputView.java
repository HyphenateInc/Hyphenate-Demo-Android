package com.hyphenate.chatuidemo.ui.widget;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
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
public class ChatInputView extends LinearLayout {
    @BindView(R.id.edt_msg_content) EditText mEditText;
    @BindView(R.id.img_send) ImageView mSendView;
    @BindView(R.id.img_mic) ImageView mMicView;

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

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.em_widget_chat_input, this);
        ButterKnife.bind(this);

        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s.toString())) {
                    hideSendImage();
                } else {
                    showSendImage();
                }
            }
        });
    }

    @OnClick(R.id.img_send) void onSendClick() {
        if (mInputViewEventListener != null) {
            mInputViewEventListener.onSendMessage(mEditText.getText());
        }
        mEditText.setText("");
        hideSendImage();
    }

    @OnClick(R.id.img_mic) void onMicClick() {
        mInputViewEventListener.onMicClick();
    }

    private void showSendImage() {
        mSendView.setVisibility(View.VISIBLE);
        mMicView.setVisibility(View.GONE);
    }

    private void hideSendImage() {
        mSendView.setVisibility(View.GONE);
        mMicView.setVisibility(View.VISIBLE);
    }

    public void setViewEventListener(ChatInputViewEventListener eventListener) {
        mInputViewEventListener = eventListener;
    }

    public interface ChatInputViewEventListener {
        void onSendMessage(CharSequence content);
        void onMicClick();
    }
}
