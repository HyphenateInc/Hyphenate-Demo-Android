package io.agora.easeui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ListView;
import io.agora.chat.ChatClient;
import io.agora.chat.Conversation;
import io.agora.chat.ChatMessage;
import io.agora.chatdemo.R;
import io.agora.easeui.adapter.EaseMessageListAdapter;
import io.agora.easeui.model.styles.EaseMessageListItemStyle;
import io.agora.easeui.utils.EaseCommonUtils;
import io.agora.easeui.widget.chatrow.EaseCustomChatRowProvider;
import io.agora.util.DensityUtil;

/**
 * Created by wei on 2016/10/11.
 *
 * A list view to show chat message list
 */

public class EaseMessageListView extends ListView {

    protected static final String TAG = EaseMessageListView.class.getSimpleName();
    protected Context context;
    protected Conversation conversation;
    protected int chatType;
    protected String toChatUsername;
    protected EaseMessageListAdapter messageAdapter;
    protected Drawable myBubbleBg;
    protected Drawable otherBuddleBg;

    protected  EaseMessageListItemStyle itemStyle;

    public EaseMessageListView(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs);
    }

    public EaseMessageListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        parseAttrs(context, attrs);
        init(context);
    }

    public EaseMessageListView(Context context) {
        super(context);
        init(context);
    }


    private void init(Context context){
        this.context = context;
        setTranscriptMode(TRANSCRIPT_MODE_NORMAL);
        setCacheColorHint(getResources().getColor(android.R.color.transparent));
        setSelector(getResources().getDrawable(android.R.color.transparent));
        setDivider(getResources().getDrawable(android.R.color.transparent));
        setDividerHeight(DensityUtil.dip2px(context, 5.0f));
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

        conversation = ChatClient.getInstance().chatManager().getConversation(toChatUsername, EaseCommonUtils
                .getConversationType(chatType), true);
        messageAdapter = new EaseMessageListAdapter(context, toChatUsername, chatType, this);
        messageAdapter.setItemStyle(itemStyle);
        messageAdapter.setCustomChatRowProvider(customChatRowProvider);
        // set message adapter
        setAdapter(messageAdapter);

        refreshSelectLast();
    }

    protected void parseAttrs(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.EaseMessageListView);

        EaseMessageListItemStyle.Builder builder = new EaseMessageListItemStyle.Builder();
        builder.showAvatar(ta.getBoolean(R.styleable.EaseMessageListView_msgListShowUserAvatar, true))
                .showUserNick(ta.getBoolean(R.styleable.EaseMessageListView_msgListShowUserNick, false))
                .myBubbleBg(ta.getDrawable(R.styleable.EaseMessageListView_msgListMyBubbleBackground))
                .otherBuddleBg(ta.getDrawable(R.styleable.EaseMessageListView_msgListMyBubbleBackground));

        itemStyle = builder.build();

        ta.recycle();
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


    public ChatMessage getItem(int position){
        return messageAdapter.getItem(position);
    }

    public void setShowUserNick(boolean showUserNick){
        itemStyle.setShowUserNick(showUserNick);
    }

    public boolean isShowUserNick(){
        return itemStyle.isShowUserNick();
    }

    public interface MessageListItemClicksListener {
        void onResendClick(ChatMessage message);
        /**
         * there is default handling when bubble is clicked, if you want handle it, return true
         * another way is you implement in onBubbleClick() of chat row
         * @param message
         * @return
         */
        boolean onBubbleClick(ChatMessage message);
        void onBubbleLongClick(ChatMessage message);
        void onUserAvatarClick(String username);
        void onUserAvatarLongClick(String username);
    }

    /**
     * set click listener
     * @param listener
     */
    public void setItemClickListener(MessageListItemClicksListener listener){
        if (messageAdapter != null) {
            messageAdapter.setItemClicksListener(listener);
        }
    }

}
