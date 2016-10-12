package com.hyphenate.easeui.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ListView;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.adapter.EaseMessageListAdapter;
import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.hyphenate.easeui.widget.chatrow.EaseCustomChatRowProvider;

/**
 * Created by wei on 2016/10/11.
 *
 * A list view to show chat message list
 */

public class EaseChatMessageListView extends ListView {

    protected static final String TAG = EaseChatMessageListView.class.getSimpleName();
    protected Context context;
    protected EMConversation conversation;
    protected int chatType;
    protected String toChatUsername;
    protected EaseMessageListAdapter messageAdapter;
    protected boolean showUserNick;
    protected boolean showAvatar;
    protected Drawable myBubbleBg;
    protected Drawable otherBuddleBg;

    public EaseChatMessageListView(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs);
    }

    public EaseChatMessageListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        parseStyle(context, attrs);
        init(context);
    }

    public EaseChatMessageListView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context){
        this.context = context;
    }

    /**
     * init widget
     * @param toChatUsername
     * @param chatType
     * @param customChatRowProvider
     */
    public void init(String toChatUsername, int chatType, EaseCustomChatRowProvider customChatRowProvider) {
        this.chatType = chatType;
        this.toChatUsername = toChatUsername;

        conversation = EMClient.getInstance().chatManager().getConversation(toChatUsername, EaseCommonUtils
                .getConversationType(chatType), true);
        messageAdapter = new EaseMessageListAdapter(context, toChatUsername, chatType, this);
        //messageAdapter.setShowAvatar(showAvatar);
        //messageAdapter.setShowUserNick(showUserNick);
        //messageAdapter.setMyBubbleBg(myBubbleBg);
        //messageAdapter.setOtherBuddleBg(otherBuddleBg);
        //messageAdapter.setCustomChatRowProvider(customChatRowProvider);
        // set message adapter
        setAdapter(messageAdapter);

        refreshSelectLast();
    }

    protected void parseStyle(Context context, AttributeSet attrs) {
        //TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.EaseChatMessageList);
        //showAvatar = ta.getBoolean(R.styleable.EaseChatMessageList_msgListShowUserAvatar, true);
        //myBubbleBg = ta.getDrawable(R.styleable.EaseChatMessageList_msgListMyBubbleBackground);
        //otherBuddleBg = ta.getDrawable(R.styleable.EaseChatMessageList_msgListMyBubbleBackground);
        //showUserNick = ta.getBoolean(R.styleable.EaseChatMessageList_msgListShowUserNick, false);
        //ta.recycle();
    }


    /**
     * refresh
     */
    public void refresh(){
        if (messageAdapter != null) {
            messageAdapter.refresh();
        }
    }

    /**
     * refresh and jump to the last
     */
    public void refreshSelectLast(){
        if (messageAdapter != null) {
            messageAdapter.refreshSelectLast();
        }
    }

    /**
     * refresh and jump to the position
     * @param position
     */
    public void refreshSeekTo(int position){
        if (messageAdapter != null) {
            messageAdapter.refreshSeekTo(position);
        }
    }


    public EMMessage getItem(int position){
        return messageAdapter.getItem(position);
    }

    public void setShowUserNick(boolean showUserNick){
        this.showUserNick = showUserNick;
    }

    public boolean isShowUserNick(){
        return showUserNick;
    }

    public interface MessageListItemClickListener{
        void onResendClick(EMMessage message);
        /**
         * there is default handling when bubble is clicked, if you want handle it, return true
         * another way is you implement in onBubbleClick() of chat row
         * @param message
         * @return
         */
        boolean onBubbleClick(EMMessage message);
        void onBubbleLongClick(EMMessage message);
        void onUserAvatarClick(String username);
        void onUserAvatarLongClick(String username);
    }

    /**
     * set click listener
     * @param listener
     */
    public void setItemClickListener(MessageListItemClickListener listener){
        if (messageAdapter != null) {
            messageAdapter.setItemClickListener(listener);
        }
    }

    /**
     * set chat row provider
     * @param rowProvider
     */
    public void setCustomChatRowProvider(EaseCustomChatRowProvider rowProvider){
        //if (messageAdapter != null) {
        //    messageAdapter.setCustomChatRowProvider(rowProvider);
        //}
    }
}
