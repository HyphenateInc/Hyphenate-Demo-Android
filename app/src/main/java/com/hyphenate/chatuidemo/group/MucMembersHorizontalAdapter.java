package com.hyphenate.chatuidemo.group;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.easeui.utils.EaseUserUtils;
import com.hyphenate.easeui.widget.EaseListItemClickListener;
import com.hyphenate.easeui.widget.EaseSwipeLayout;
import com.hyphenate.easeui.widget.RecyclerSwipeView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by linan on 17/3/29.
 */

public class MucMembersHorizontalAdapter extends RecyclerView.Adapter<MucMembersHorizontalAdapter.ViewHolder> {

    private Context context;
    private List<String> membersList;
    private GroupUtils.MucRoleJudge roleJudge;
    private EaseListItemClickListener listener;
    private EaseSwipeLayout.SwipeListener swipeListener;

    EaseSwipeLayout.SwipeAction muteAction, blockAction, deleteAction;

    public MucMembersHorizontalAdapter(Context context, List<String> objects, GroupUtils.MucRoleJudge judge, EaseSwipeLayout.SwipeListener listener) {
        this.context = context;
        this.membersList = objects;
        this.roleJudge = judge;
        this.swipeListener = listener;
    }

    public void setItemClickListener(EaseListItemClickListener listener) {
        this.listener = listener;
    }

    public void setSwipeLayoutActions(EaseSwipeLayout.SwipeAction muteAction, EaseSwipeLayout.SwipeAction blockAction, EaseSwipeLayout.SwipeAction deleteAction) {
        this.muteAction = muteAction;
        this.blockAction = blockAction;
        this.deleteAction = deleteAction;
    }

    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.em_muc_list_item_horizontal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        String username = membersList.get(position);
        EaseUserUtils.setUserNick(username, holder.memberNameView);
        EaseUserUtils.setUserAvatar(context, username, holder.memberAvatarView);
        holder.img_mute.setVisibility(roleJudge.isMuted(username) ? View.VISIBLE : View.GONE);

        boolean isOwner = roleJudge.isOwner(username);
        boolean isAdmin = roleJudge.isAdmin(username);
        if (isOwner) {
            holder.img_role.setVisibility(View.VISIBLE);
            holder.img_role.setImageResource(R.drawable.em_avatar_owner);
        } else if (isAdmin) {
            holder.img_role.setVisibility(View.VISIBLE);
            holder.img_role.setImageResource(R.drawable.em_avatar_admin);
        } else {
            holder.img_role.setVisibility(View.GONE);
        }

        if (listener != null) {
            holder.memberListView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    listener.onItemClick(v, position);
                }
            });
        }

        if (isOwner || username.equals(EMClient.getInstance().getCurrentUser()) ||
                (roleJudge.isAdmin(EMClient.getInstance().getCurrentUser()) && roleJudge.isAdmin(username))) {
            EaseSwipeLayout.SwipeAction[] actions = {};
            holder.swipeLayout.setButtons(actions);
        } else if (roleJudge.isMuted(username)) {
            EaseSwipeLayout.SwipeAction[] actions = {blockAction, deleteAction};
            holder.swipeLayout.setButtons(actions);
        } else {
            EaseSwipeLayout.SwipeAction[] actions = {muteAction, blockAction, deleteAction};
            holder.swipeLayout.setButtons(actions);
        }
        holder.swipeLayout.updateListPosition(position);
    }

    @Override
    public int getItemCount() {
        return membersList.size();
    }

    class ViewHolder extends RecyclerSwipeView.SwipeViewHolder {
        @BindView(R.id.layout_member_list)  RelativeLayout memberListView;
        @BindView(R.id.img_member_avatar)   ImageView memberAvatarView;
        @BindView(R.id.text_member_name)    TextView memberNameView;
        @BindView(R.id.img_mute)            ImageView img_mute;
        @BindView(R.id.img_role)            ImageView img_role;
        @BindView(R.id.swipe_layout)        EaseSwipeLayout swipeLayout;
        public ViewHolder(View itemView) {
            super(itemView, swipeListener);
            ButterKnife.bind(this, itemView);
            swipeLayout.attachListItem(itemView);
        }
    }
}
