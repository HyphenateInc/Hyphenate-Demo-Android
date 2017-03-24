package com.hyphenate.chatuidemo.ui.widget;

import android.content.Context;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hyphenate.chatuidemo.R;
import com.hyphenate.easeui.model.EaseDefaultEmojiconDatas;
import com.hyphenate.easeui.model.EaseEmojicon;
import com.hyphenate.easeui.model.EaseEmojiconGroupEntity;
import com.hyphenate.easeui.utils.EaseSmileUtils;
import com.hyphenate.easeui.utils.Utils;
import com.hyphenate.easeui.widget.EaseChatExtendMenu;
import com.hyphenate.easeui.widget.emojicon.EaseEmojiconMenu;
import com.hyphenate.easeui.widget.emojicon.EaseEmojiconMenuBase;
import com.hyphenate.util.EMLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

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
    @BindView(R.id.img_emojicon) ImageView mEmojiconToggleView;
    @BindView(R.id.extend_menu) EaseChatExtendMenu mExtendMenu;
    @BindView(R.id.emojicon_menu) EaseEmojiconMenu mEmojiconMenu;
    @BindView(R.id.extend_menu_container) FrameLayout mExtendMenuContainer;

    private ChatInputViewEventListener mInputViewEventListener;

    private Context mContext;

    private Handler handler = new Handler();

    private boolean ctrlPress = false;

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

        mContext = context;


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

        mEditText.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                EMLog.d("key", "keyCode:" + keyCode + " action:" + event.getAction());

                // test on Mac virtual machine: Ctrl key map to KEYCODE_UNKNOWN
                if (keyCode == KeyEvent.KEYCODE_UNKNOWN) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        ctrlPress = true;
                    } else if (event.getAction() == KeyEvent.ACTION_UP) {
                        ctrlPress = false;
                    }
                }
                return false;
            }
        });

        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                EMLog.d("key", "keyCode:" + event.getKeyCode() + " action" + event.getAction() + " ctrl:" + ctrlPress);
                if (actionId == EditorInfo.IME_ACTION_SEND ||
                        (event.getKeyCode() == KeyEvent.KEYCODE_ENTER &&
                                event.getAction() == KeyEvent.ACTION_DOWN &&
                                ctrlPress == true)) {
                    onSendClick();
                    return true;
                }
                else{
                    return false;
                }
            }
        });

        mEditText.setOnClickListener(new OnClickListener() {
            @Override public void onClick(View v) {
                hideExtendMenuContainer();
            }
        });
    }

    /**
     *  init view
     * @param emojiconGroupList --will use default if null
     */
    public void init(List<EaseEmojiconGroupEntity> emojiconGroupList){
        if(emojiconGroupList == null){
            emojiconGroupList = new ArrayList<EaseEmojiconGroupEntity>();
            emojiconGroupList.add(new EaseEmojiconGroupEntity(R.drawable.ee_1,  Arrays.asList(
                    EaseDefaultEmojiconDatas.getData())));
        }
        mEmojiconMenu.init(emojiconGroupList);
        mExtendMenu.init();

        mEmojiconMenu.setEmojiconMenuListener(new EaseEmojiconMenuBase.EaseEmojiconMenuListener() {
            @Override public void onExpressionClicked(EaseEmojicon emojicon) {
                if(emojicon.getType() != EaseEmojicon.Type.BIG_EXPRESSION){
                    if(emojicon.getEmojiText() != null){
                        CharSequence str = EaseSmileUtils.getSmiledText(mContext,emojicon.getEmojiText());
                        mEditText.append(str);
                    }
                }else{
                    //on big expression clicked
                }
            }

            @Override public void onDeleteImageClicked() {
                onEmojiconDeleteEvent();
            }
        });
    }

    public void init(){
        init(null);
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

    @OnClick(R.id.img_emojicon) void OnEmojiconClick(){
        if(mEmojiconToggleView.isSelected()){
            mEmojiconToggleView.setSelected(false);
        }else{
            mEmojiconToggleView.setSelected(true);
        }
        toggleEmojicon();
    }

    /**
     * delete emojicon
     */
    public void onEmojiconDeleteEvent(){
        if (!TextUtils.isEmpty(mEditText.getText())) {
            KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
            mEditText.dispatchKeyEvent(event);
        }
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
                    mEmojiconMenu.setVisibility(View.GONE);
                }
            }, 50);
        } else {
            if (mEmojiconMenu.getVisibility() == View.VISIBLE) {
                mEmojiconMenu.setVisibility(View.GONE);
                mExtendMenu.setVisibility(View.VISIBLE);
            } else {
                mExtendMenuContainer.setVisibility(View.GONE);
            }
        }
        mEmojiconToggleView.setSelected(false);
    }

    /**
     * show or hide emojicon
     */
    protected void toggleEmojicon() {
        if (mExtendMenuContainer.getVisibility() == View.GONE) {
            hideKeyboard();
            handler.postDelayed(new Runnable() {
                public void run() {
                    mExtendMenuContainer.setVisibility(View.VISIBLE);
                    mExtendMenu.setVisibility(View.GONE);
                    mEmojiconMenu.setVisibility(View.VISIBLE);
                }
            }, 50);
        } else {
            if (mEmojiconMenu.getVisibility() == View.VISIBLE) {
                mExtendMenuContainer.setVisibility(View.GONE);
                mEmojiconMenu.setVisibility(View.GONE);
            } else {
                mExtendMenu.setVisibility(View.GONE);
                mEmojiconMenu.setVisibility(View.VISIBLE);
            }

        }
    }

    private void hideKeyboard(){
        Utils.hideKeyboard(mEditText);
    }

    /**
     * when back key pressed
     *
     * @return false--extend menu is on, will hide it first
     *         true --extend menu is off
     */
    public boolean onBackPressed() {
        if (mExtendMenuContainer.getVisibility() == View.VISIBLE) {
            hideExtendMenuContainer();
            return false;
        } else {
            return true;
        }

    }

    /**
     * hide extend menu container
     */
    public void hideExtendMenuContainer() {
        mExtendMenu.setVisibility(View.GONE);
        mEmojiconMenu.setVisibility(View.GONE);
        mExtendMenuContainer.setVisibility(View.GONE);
        mEmojiconToggleView.setSelected(false);
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
