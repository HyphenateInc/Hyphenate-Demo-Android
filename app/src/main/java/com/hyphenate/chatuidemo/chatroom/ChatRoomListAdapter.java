package com.hyphenate.chatuidemo.chatroom;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hyphenate.chat.EMChatRoom;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.easeui.widget.EaseListItemClickListener;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by linan on 17/5/24.
 */

public class ChatRoomListAdapter extends RecyclerView.Adapter {

    private Context context;
    private List<EMChatRoom> chatRoomList;
    private EaseListItemClickListener listener;

    ChatRoomListAdapter(Context context, List<EMChatRoom> chatRooms) {
        this.context = context;
        this.chatRoomList = chatRooms;
    }

    public void setItemClickListener(EaseListItemClickListener listener) {
        this.listener = listener;
    }

    @Override public int getItemCount() {
            return chatRoomList == null ? 0 : chatRoomList.size();
    }

    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.em_item_group_list, parent, false));
    }

    @Override public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

        if (holder instanceof ViewHolder) {

            ((ViewHolder) holder).groupItem.setBackgroundResource(0);
            ((ViewHolder) holder).guideArrowView.setImageResource(R.drawable.cell_chevron_right);

            EMChatRoom chatRoom = chatRoomList.get(position);
            ((ViewHolder) holder).nameView.setText(chatRoom.getName());

            if (listener != null) {
                ((ViewHolder) holder).groupItem.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {

                        listener.onItemClick(v, position);
                    }
                });
            }
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.layout_group_list)  RelativeLayout groupItem;
        @BindView(R.id.text_group_name)    TextView nameView;
        @BindView(R.id.img_guide_arrow)    ImageView guideArrowView;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
