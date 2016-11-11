package com.hyphenate.chatuidemo.ui.widget;

import android.content.Context;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.chatuidemo.utils.Utils;
import com.hyphenate.easeui.widget.EaseChatExtendMenu;

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
    @BindView(R.id.img_expand) ImageView mExpandView;
    @BindView(R.id.extend_menu) EaseChatExtendMenu mExtendMenu;
    @BindView(R.id.extend_menu_container) FrameLayout mExtendMenuContainer;

    private ChatInputViewEventListener mInputViewEventListener;



    private Handler handler = new Handler();

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

    protected void init(Context context, AttributeSet attrs) {
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
        mEditText.setOnClickListener(new OnClickListener() {
            @Override public void onClick(View v) {
                hideExtendMenuContainer();
            }
        });
    }

    public void init(){
        mExtendMenu.init();
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

    @OnClick(R.id.img_expand) void onToggleMoreClick(){
        toggleMore();
    }


    private void showSendImage() {
        mSendView.setVisibility(View.VISIBLE);
        mExpandView.setVisibility(View.INVISIBLE);
    }

    private void hideSendImage() {
        mSendView.setVisibility(View.INVISIBLE);
        mExpandView.setVisibility(View.VISIBLE);
    }

    public void setViewEventListener(ChatInputViewEventListener eventListener) {
        mInputViewEventListener = eventListener;
    }

    public interface ChatInputViewEventListener {
        void onSendMessage(CharSequence content);
        void onMicClick();
    }


    public EditText getEditText(){
        return mEditText;
    }


    /**
     * show or hide extend menu
     *
     */
    protected void toggleMore() {
        if (mExtendMenuContainer.getVisibility() == View.GONE) {
            Utils.hideKeyboard(mEditText);
            handler.postDelayed(new Runnable() {
                public void run() {
                    mExtendMenuContainer.setVisibility(View.VISIBLE);
                    mExtendMenu.setVisibility(View.VISIBLE);
                    //emojiconMenu.setVisibility(View.GONE);
                }
            }, 50);
        } else {
            //if (emojiconMenu.getVisibility() == View.VISIBLE) {
            //    emojiconMenu.setVisibility(View.GONE);
            //    chatExtendMenu.setVisibility(View.VISIBLE);
            //} else {
            mExtendMenuContainer.setVisibility(View.GONE);
            //}
        }
    }

    /**
     * hide extend menu container
     */
    public void hideExtendMenuContainer() {
        mExtendMenu.setVisibility(View.GONE);
        //emojiconMenu.setVisibility(View.GONE);
        mExtendMenuContainer.setVisibility(View.GONE);
        //chatPrimaryMenu.onExtendMenuContainerHide();
    }

    /**
     * register menu item
     *
     * @param name
     *            item name
     * @param drawableRes
     *            background of item
     * @param itemId
     *             id
     * @param listener
     *            on click event of item
     */
    public void registerExtendMenuItem(String name, int drawableRes, int itemId,
            EaseChatExtendMenu.EaseChatExtendMenuItemClickListener listener) {
        mExtendMenu.registerMenuItem(name, drawableRes, itemId, listener);
    }

    /**
     * register menu item
     *
     * @param nameRes
     *            resource id of item name
     * @param drawableRes
     *            background of item
     * @param itemId
     *             id
     * @param listener
     *            on click event of item
     */
    public void registerExtendMenuItem(int nameRes, int drawableRes, int itemId,
            EaseChatExtendMenu.EaseChatExtendMenuItemClickListener listener) {
        mExtendMenu.registerMenuItem(nameRes, drawableRes, itemId, listener);
    }
}
