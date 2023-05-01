package io.agora.easeui.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import io.agora.chat.ChatRoom;
import io.agora.chat.ChatClient;
import io.agora.chat.Conversation;
import io.agora.chat.Group;
import io.agora.chat.ChatMessage;
import io.agora.chatdemo.Constant;
import io.agora.chatdemo.R;
import io.agora.easeui.model.EaseUser;
import io.agora.easeui.utils.EaseCommonUtils;
import io.agora.easeui.utils.EaseSmileUtils;
import io.agora.easeui.utils.EaseUserUtils;
import io.agora.easeui.widget.EaseListItemClickListener;
import io.agora.util.DateUtils;

import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import java.util.List;

/**
 * Created by wei on 2016/10/9.
 */
public class EaseConversationListAdapter extends RecyclerView.Adapter<EaseConversationListAdapter.ConversationListHolder> {

    private Context mContext;
    private EaseListItemClickListener mOnItemClickListener;
    private MyHandler handler;
    private List<Conversation> conversationList;
    private List<Conversation> conversationFilterList = new ArrayList<>();
    private ConversationFilter conversationFilter;

    public EaseConversationListAdapter(Context context, List<Conversation> conversations) {
        mContext = context;
        handler = new MyHandler();
        conversationList = conversations;
        conversationFilterList.addAll(conversations);
    }

    @Override public ConversationListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //set the view holder
        return new ConversationListHolder(
                LayoutInflater.from(mContext).inflate(R.layout.ease_row_conversation_list, parent, false));
    }

    @Override public void onBindViewHolder(ConversationListHolder holder, final int position) {
        Conversation conversation = conversationList.get(position);
        // get username or group id
        String username = conversation.conversationId();
        if (conversation.conversationId().equals(Constant.CONVERSATION_NAME_APPLY)) {
            holder.mNameView.setText(R.string.em_contacts_apply);
        } else {
            if (conversation.getType() == Conversation.ConversationType.GroupChat) {
                // group message, show group avatar
                holder.mAvatarView.setImageResource(R.drawable.ease_ic_group_default);
                Group group = ChatClient.getInstance().groupManager().getGroup(username);
                holder.mNameView.setText(group != null ? group.getGroupName() : username);
            } else if (conversation.getType() == Conversation.ConversationType.ChatRoom) {
                holder.mAvatarView.setImageResource(R.drawable.ease_ic_group_default);
                ChatRoom room = ChatClient.getInstance().chatroomManager().getChatRoom(username);
                holder.mNameView.setText(room != null && !TextUtils.isEmpty(room.getName()) ? room.getName() : username);
            } else {
                //single chat mConversation
                EaseUserUtils.setUserAvatar(mContext, username, holder.mAvatarView);
                EaseUserUtils.setUserNick(username, holder.mNameView);
            }
        }

        if (conversation.getUnreadMsgCount() > 0) {
            // show unread message count
            holder.mUnreadNumView.setText(String.valueOf(conversation.getUnreadMsgCount()));
            holder.mUnreadNumView.setVisibility(View.VISIBLE);
        } else {
            holder.mUnreadNumView.setVisibility(View.INVISIBLE);
        }

        if (conversation.getAllMsgCount() != 0) {
            // show the content of latest message
            ChatMessage lastMessage = conversation.getLastMessage();
            String content = null;
            //if(cvsListHelper != null){
            //    content = cvsListHelper.onSetItemSecondaryText(lastMessage);
            //}
            holder.mMessageView.setText(
                    EaseSmileUtils.getSmiledText(mContext, EaseCommonUtils.getMessageDigest(lastMessage, mContext)),
                    TextView.BufferType.SPANNABLE);
            if (content != null) {
                holder.mMessageView.setText(content);
            }
            //show the message time
            holder.mTimeView.setText(DateUtils.getTimestampString(new Date(lastMessage.getMsgTime())));
            if (lastMessage.direct() == ChatMessage.Direct.SEND && lastMessage.status() == ChatMessage.Status.FAIL) {
                holder.mMsgStateView.setVisibility(View.VISIBLE);
            } else {
                holder.mMsgStateView.setVisibility(View.INVISIBLE);
            }
        }

        //set item onclick listener
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(v, position);
                }
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override public boolean onLongClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemLongClick(v, position);
                    return true;
                }
                return false;
            }
        });
    }

    @Override public int getItemCount() {
        return conversationList.size();
    }

    public Filter getFilter() {
        if (conversationFilter == null) {
            conversationFilter = new ConversationFilter();
        }
        return conversationFilter;
    }

    class ConversationFilter extends Filter {

        @Override protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            List<Conversation> newConversationList = new ArrayList<>();
            if (!TextUtils.isEmpty(constraint)) {
                for (int i = 0; i < conversationFilterList.size(); i++) {
                    String targetString = "";
                    String conversationId = conversationFilterList.get(i).conversationId();
                    if (conversationFilterList.get(i).isGroup()) {
                        Group group = ChatClient.getInstance().groupManager().getGroup(conversationId);
                        targetString = group.getGroupName();
                    } else {
                        EaseUser user = EaseUserUtils.getUserInfo(conversationId);
                        targetString =
                                TextUtils.isEmpty(user.getEaseNickname()) ? user.getEaseUsername() : user.getEaseNickname();
                    }
                    if (conversationId.startsWith(constraint.toString()) || targetString.startsWith(constraint.toString())) {
                        newConversationList.add(conversationFilterList.get(i));
                    }
                }
            } else {
                newConversationList = conversationFilterList;
            }

            results.count = newConversationList.size();
            results.values = newConversationList;
            return results;
        }

        @Override protected void publishResults(CharSequence constraint, FilterResults results) {
            conversationList.clear();
            if (results.count > 0) {
                conversationList.addAll((List<Conversation>) results.values);
            }
            notifyDataSetChanged();
        }
    }

    public void setOnItemClickListener(EaseListItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    /**
     * 供界面调用的刷新 Adapter 的方法
     */
    public void refreshList() {
        Message msg = handler.obtainMessage(0);
        handler.sendMessage(msg);
    }

    /**
     * 自定义Handler，用来处理消息的刷新等
     */
    protected class MyHandler extends Handler {
        private void refresh() {
            conversationFilterList.clear();
            conversationFilterList.addAll(conversationList);
            notifyDataSetChanged();
        }

        @Override public void handleMessage(Message msg) {
            //super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    refresh();
                    break;
            }
        }
    }

    /**
     * view holder class
     */
    static class ConversationListHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.img_avatar) ImageView mAvatarView;
        @BindView(R.id.txt_name) TextView mNameView;
        @BindView(R.id.img_msg_state) ImageView mMsgStateView;
        @BindView(R.id.txt_message) TextView mMessageView;
        @BindView(R.id.txt_time) TextView mTimeView;
        @BindView(R.id.txt_unread_msg_number) TextView mUnreadNumView;

        public ConversationListHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
