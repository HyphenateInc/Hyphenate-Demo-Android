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
import com.hyphenate.chatuidemo.R;
import com.hyphenate.easeui.adapter.SortedListAdapter;
import java.util.Comparator;


/**
 * Created by wei on 2016/10/9.
 */
public class ConversationListAdapter extends SortedListAdapter<EMConversation> {

    private Context mContext;

    public ConversationListAdapter(Context context, Comparator<EMConversation> comparator) {
        super(context, EMConversation.class, comparator);
        mContext = context;
    }

    @Override
    protected ViewHolder<? extends EMConversation> onCreateViewHolder(LayoutInflater inflater,
            ViewGroup parent, int viewType) {
        return new CvsListHolder(LayoutInflater.from(mContext).inflate(R.layout.em_fragment_conversation_list, null));
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
        @BindView(R.id.txt_unread_msg_number) TextView mUnreadNumView;


        public CvsListHolder(View itemView) {
            super(itemView);
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
            }
        }
    }

}
