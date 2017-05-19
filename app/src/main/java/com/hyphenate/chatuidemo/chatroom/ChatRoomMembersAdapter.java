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
import com.hyphenate.util.EMLog;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by lzan13 on 2017/6/1.
 * Chat room list adapter
 */
public class ChatRoomMembersAdapter extends RecyclerView.Adapter<ChatRoomMembersAdapter.ChatRoomViewHolder> {

    private Context context;
    private ItemClickListener listener;

    private String currentUser;
    // All members (not including blacklist)
    private List<String> allMembers;
    //
    private List<String> adminList;
    // 禁言列表
    private Map<String, Long> muteMap;
    private EMChatRoom chatRoom;

    public ChatRoomMembersAdapter(Context context, String chatRoomId) {
        this.context = context;
        chatRoom = EMClient.getInstance().chatroomManager().getChatRoom(chatRoomId);
        currentUser = EMClient.getInstance().getCurrentUser();
        adminList = chatRoom.getAdminList();
        muteMap = chatRoom.getMuteList();
        EMLog.i("lzan13", "1 --- mute size " + muteMap.size());
        updateMembersData();
    }

    @Override public ChatRoomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.em_item_chatroom_member, parent, false);
        return new ChatRoomViewHolder(view);
    }

    @Override public void onBindViewHolder(ChatRoomViewHolder holder, final int position) {
        final String username = allMembers.get(position);
        if (currentUser.equals(username)) {
            holder.usernameView.setText(username + " (Me)");
        } else {
            holder.usernameView.setText(username);
        }

        if (username.equals(chatRoom.getOwner())) {
            holder.roleView.setText("Owner");
            holder.roleView.setVisibility(View.VISIBLE);
        } else if (adminList.contains(username)) {
            holder.roleView.setText("Admin");
            holder.roleView.setVisibility(View.VISIBLE);
        } else if (muteMap.containsKey(username)) {
            holder.roleView.setText("Muted");
            holder.roleView.setVisibility(View.VISIBLE);
        } else {
            holder.roleView.setText("Member");
            holder.roleView.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (listener != null) {
                    listener.itemClick(username);
                }
            }
        });
    }

    @Override public int getItemCount() {
        return chatRoom.getMemberList().size() + adminList.size() + 1;
    }

    /**
     * update members data
     */
    public void updateMembersData() {
        if (allMembers == null) {
            allMembers = new ArrayList<>();
        }
        // Reacquire admin and mute list
        adminList = chatRoom.getAdminList();
        muteMap = chatRoom.getMuteList();
        EMLog.i("lzan13", "2 --- mute size " + muteMap.size());
        allMembers.clear();
        // Because the members do not include the group owner and the administrator, so it should be added
        allMembers.add(chatRoom.getOwner());
        allMembers.addAll(adminList);
        allMembers.addAll(chatRoom.getMemberList());
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
        TextView roleView;

        public ChatRoomViewHolder(View itemView) {
            super(itemView);
            avatarView = (EaseImageView) itemView.findViewById(R.id.img_avatar);
            usernameView = (TextView) itemView.findViewById(R.id.text_username);
            roleView = (TextView) itemView.findViewById(R.id.text_role);
        }
    }
}
