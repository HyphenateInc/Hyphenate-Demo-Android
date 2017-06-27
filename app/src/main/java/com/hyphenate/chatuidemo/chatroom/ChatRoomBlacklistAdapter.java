package com.hyphenate.chatuidemo.chatroom;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.hyphenate.chat.EMChatRoom;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.easeui.widget.EaseImageView;
import java.util.List;

/**
 * Created by lzan13 on 2017/6/1.
 * Chat room list adapter
 */
public class ChatRoomBlacklistAdapter extends RecyclerView.Adapter<ChatRoomBlacklistAdapter.ChatRoomViewHolder> {

    private Context context;
    private List<String> blackList;
    private EMChatRoom chatRoom;
    private String currentUser;
    private ItemClickListener listener;

    public ChatRoomBlacklistAdapter(Context context, String chatRoomId) {
        this.context = context;
        chatRoom = EMClient.getInstance().chatroomManager().getChatRoom(chatRoomId);
        currentUser = EMClient.getInstance().getCurrentUser();
        blackList = chatRoom.getBlackList();
    }

    @Override public ChatRoomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.em_item_chatroom_member, parent, false);
        return new ChatRoomViewHolder(view);
    }

    @Override public void onBindViewHolder(ChatRoomViewHolder holder, int position) {
        final String username = blackList.get(position);
        holder.usernameView.setText(username);
        // set item click listener
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (listener != null) {
                    listener.itemClick(username);
                }
            }
        });
    }

    @Override public int getItemCount() {
        return blackList.size();
    }

    /**
     * update members data
     */
    public void updateMembersData() {
        if (blackList == null) {
            blackList = chatRoom.getBlackList();
        } else {
            blackList.clear();
            blackList.addAll(chatRoom.getBlackList());
        }
    }

    /**
     * refresh members list
     */
    public void refresh() {
        updateMembersData();
        notifyDataSetChanged();
    }

    public void setItemClickListener(ItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * recyclerView callback
     */
    public interface ItemClickListener {
        /**
         * recyclerView item click callback
         *
         * @param username item username
         */
        void itemClick(String username);
    }

    static class ChatRoomViewHolder extends RecyclerView.ViewHolder {
        EaseImageView avatarView;
        TextView usernameView;

        public ChatRoomViewHolder(View itemView) {
            super(itemView);
            avatarView = (EaseImageView) itemView.findViewById(R.id.img_avatar);
            usernameView = (TextView) itemView.findViewById(R.id.text_username);
        }
    }
}
