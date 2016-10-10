package com.hyphenate.easeui.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.hyphenate.chat.EMChatRoom;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.hyphenate.easeui.utils.EaseUserUtils;
import com.hyphenate.util.DateUtils;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by wei on 2016/10/9.
 */
public class EaseConversationListAdapter extends SortedListAdapter<EMConversation> {

    private Context mContext;

    public EaseConversationListAdapter(Context context, Comparator<EMConversation> comparator) {
        super(context, EMConversation.class, comparator);
        mContext = context;
    }

    @Override
    protected ViewHolder<? extends EMConversation> onCreateViewHolder(LayoutInflater inflater,
            ViewGroup parent, int viewType) {
        return new CvsListHolder(LayoutInflater.from(mContext).inflate(R.layout.em_fragment_conversation_list, null), mContext);
    }

    @Override protected boolean areItemsTheSame(EMConversation item1, EMConversation item2) {
        return false;
    }

    @Override protected boolean areItemContentsTheSame(EMConversation oldItem, EMConversation newItem) {
        return false;
    }


    /**
     * view holder class
     */
    static class CvsListHolder extends SortedListAdapter.ViewHolder<EMConversation>{
        @BindView(R.id.img_avatar) ImageView mAvatarView;
        @BindView(R.id.txt_name) TextView mNameView;
        @BindView(R.id.img_msg_state) ImageView mMsgStateView;
        @BindView(R.id.txt_message) TextView mMessageView;
        @BindView(R.id.txt_time) TextView mTimeView;
        @BindView(R.id.txt_unread_msg_number) TextView mUnreadNumView;

        Context mContext;

        public CvsListHolder(View itemView, Context context) {
            super(itemView);
            mContext = context;
            ButterKnife.bind(this, itemView);
        }

        @Override protected void performBind(EMConversation conversation) {
            // get username or group id
            String username = conversation.getUserName();
            if (conversation.getType() == EMConversation.EMConversationType.GroupChat) {
                // group message, show group avatar
                mAvatarView.setImageResource(R.drawable.ease_ic_group_default);
                EMGroup group = EMClient.getInstance().groupManager().getGroup(username);
                mNameView.setText(group != null ? group.getGroupName() : username);
            } else if (conversation.getType() == EMConversation.EMConversationType.ChatRoom) {
                mAvatarView.setImageResource(R.drawable.ease_ic_group_default);
                EMChatRoom room = EMClient.getInstance().chatroomManager().getChatRoom(username);
                mNameView.setText(room != null && !TextUtils.isEmpty(room.getName()) ? room.getName() : username);
            } else {
                EaseUserUtils.setUserAvatar(mContext, username, mAvatarView);
                EaseUserUtils.setUserNick(username, mNameView);
            }

            if (conversation.getUnreadMsgCount() > 0) {
                // show unread message count
                mUnreadNumView.setText(String.valueOf(conversation.getUnreadMsgCount()));
                mUnreadNumView.setVisibility(View.VISIBLE);
            } else {
                mUnreadNumView.setVisibility(View.INVISIBLE);
            }

            if (conversation.getAllMsgCount() != 0) {
                // show the content of latest message
                EMMessage lastMessage = conversation.getLastMessage();
                String content = null;
                //if(cvsListHelper != null){
                //    content = cvsListHelper.onSetItemSecondaryText(lastMessage);
                //}
                //holder.message.setText(EaseSmileUtils.getSmiledText(getContext(), EaseCommonUtils.getMessageDigest(lastMessage, (this.getContext()))),
                //        TextView.BufferType.SPANNABLE);
                mMessageView.setText(EaseCommonUtils.getMessageDigest(lastMessage, mContext));
                if(content != null){
                    mMessageView.setText(content);
                }
                mTimeView.setText(DateUtils.getTimestampString(new Date(lastMessage.getMsgTime())));
                if (lastMessage.direct() == EMMessage.Direct.SEND && lastMessage.status() == EMMessage.Status.FAIL) {
                    mMsgStateView.setVisibility(View.VISIBLE);
                } else {
                    mMsgStateView.setVisibility(View.GONE);
                }
            }
        }
    }

}
